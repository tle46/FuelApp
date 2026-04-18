package com.example.fuelapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fuelapp.data.FuelLogRepository
import com.example.fuelapp.data.VehicleRepository
import com.example.fuelapp.model.Vehicle
import com.example.fuelapp.model.VehicleStats

class VehicleListViewModel : ViewModel() {

    private val repository = VehicleRepository()

    // FuelLogRepository is kept only for operations that must reach Firestore
    // directly (clearByVehicleId, clearByUserId). Stats are computed from the
    // shared FuelListViewModel cache — see loadStats(vehicleId, fuelLogs).
    private val fuelRepository = FuelLogRepository()

    private val _vehicles = MutableLiveData<List<Vehicle>>(emptyList())
    val vehicles: LiveData<List<Vehicle>> = _vehicles

    private val _selectedVehicle = MutableLiveData<Vehicle?>()
    val selectedVehicle: LiveData<Vehicle?> = _selectedVehicle

    private val _stats = MutableLiveData<VehicleStats>()
    val stats: LiveData<VehicleStats> = _stats

    init {
        loadVehicles()
    }

    private fun emptyStats() = VehicleStats(
        avgMpg = 0.0,
        totalMiles = 0.0,
        totalFuel = 0.0,
        totalCost = 0.0,
        totalLogs = 0,
        lastMpg = 0.0,
        odometerTimeData = emptyList(),
        mpgOdometerData = emptyList()
    )

    private fun loadVehicles() {
        repository.getVehicles { vehicleList ->
            _vehicles.postValue(vehicleList)

            if (vehicleList.isEmpty()) {
                _selectedVehicle.postValue(null)
                _stats.postValue(emptyStats())
            }
        }
    }

    /**
     * Computes stats from an already-fetched list of [FuelLog]s — no Firestore
     * round-trip required. Call this from the Fragment, passing
     * [FuelListViewModel.getFuelLogsForVehicle] as the source.
     */
    fun loadStats(vehicleId: String, logs: List<com.example.fuelapp.model.FuelLog>) {
        val sortedLogs = logs.filter { it.vehicleId == vehicleId }.sortedBy { it.odometer }

        if (sortedLogs.isEmpty()) {
            _stats.postValue(emptyStats())
            return
        }

        var totalFuel = 0.0
        var totalCost = 0.0
        var totalMiles = 0.0
        var lastMPG = 0.0

        val odometerTimeData = mutableListOf<Pair<Long, Float>>()
        val mpgOdometerData = mutableListOf<Pair<Float, Float>>()

        for (i in sortedLogs.indices) {
            val log = sortedLogs[i]
            val odo = log.odometer.toFloat()
            val gallons = log.gallons.toFloat()
            val cost = log.totalCost.toFloat()
            val timestamp = log.date.time

            odometerTimeData.add(Pair(timestamp, odo))

            if (i > 0) {
                val prev = sortedLogs[i - 1]
                val miles = (log.odometer - prev.odometer).toFloat()

                totalMiles += miles
                totalFuel += gallons
                totalCost += cost

                if (gallons > 0f) {
                    val mpg = miles / gallons
                    mpgOdometerData.add(Pair(odo, mpg))
                }
            }
        }

        if (sortedLogs.size >= 2) {
            val last = sortedLogs.last()
            val prev = sortedLogs[sortedLogs.size - 2]
            lastMPG = if (last.gallons > 0)
                (last.odometer - prev.odometer) / last.gallons
            else 0.0
        }

        val avgMpg = if (totalFuel > 0) totalMiles / totalFuel else 0.0

        _stats.postValue(
            VehicleStats(
                avgMpg = avgMpg,
                totalMiles = totalMiles,
                totalFuel = totalFuel,
                totalCost = totalCost,
                totalLogs = sortedLogs.size,
                lastMpg = lastMPG,
                odometerTimeData = odometerTimeData,
                mpgOdometerData = mpgOdometerData
            )
        )
    }

    /**
     * Legacy overload — still hits Firestore. Kept so nothing breaks if called
     * before FuelListViewModel has finished its initial load. Prefer the
     * two-argument overload whenever possible.
     */
    fun loadStats(vehicleId: String) {
        fuelRepository.getFuelLogsByVehicle(vehicleId) { logs ->
            loadStats(vehicleId, logs)
        }
    }

    fun updateVehicle(updatedVehicle: Vehicle): Boolean {
        if (updatedVehicle.name.isBlank()
            || updatedVehicle.make.isBlank()
            || updatedVehicle.model.isBlank()
        ) return false

        repository.updateVehicle(updatedVehicle)

        // Update local list — no re-fetch
        val updated = (_vehicles.value ?: emptyList()).map {
            if (it.id == updatedVehicle.id) updatedVehicle else it
        }
        _vehicles.value = updated

        return true
    }

    fun deleteVehicle(vehicle: Vehicle, onComplete: (Boolean) -> Unit) {
        repository.deleteVehicle(vehicle) { success ->
            if (success) {
                // Remove from local list — no re-fetch
                val updated = (_vehicles.value ?: emptyList()).filter { it.id != vehicle.id }
                _vehicles.postValue(updated)
            }

            if (_selectedVehicle.value?.id == vehicle.id) {
                _selectedVehicle.postValue(null)
                _stats.postValue(emptyStats())
            }

            onComplete(success)
        }
    }

    fun selectVehicle(vehicle: Vehicle) {
        _selectedVehicle.value = vehicle
    }

    fun setVehicles(vehicleList: List<Vehicle>) {
        _vehicles.value = vehicleList
    }

    fun addVehicle(vehicle: Vehicle): Boolean {
        if (vehicle.name.isBlank()
            || vehicle.make.isBlank()
            || vehicle.model.isBlank()
        ) return false

        repository.addVehicle(vehicle)

        // Append to local list — no re-fetch
        val updated = (_vehicles.value ?: emptyList()) + vehicle
        _vehicles.value = updated

        return true
    }
}
