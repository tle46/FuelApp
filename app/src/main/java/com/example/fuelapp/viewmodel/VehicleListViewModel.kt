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

    private var hasLoaded = false
    private fun loadVehicles() {
        if (hasLoaded) return
        hasLoaded = true

        repository.getVehicles { vehicleList ->
            _vehicles.postValue(vehicleList)
        }
    }

    private var lastVehicleId: String? = null

    fun loadStats(vehicleId: String) {
        if (vehicleId == lastVehicleId) return
        lastVehicleId = vehicleId

        fuelRepository.getFuelLogsByVehicle(vehicleId) { logs ->

            val sortedLogs = logs.sortedBy { it.odometer }

            if (sortedLogs.isEmpty()) {
                _stats.postValue(emptyStats())
                return@getFuelLogsByVehicle
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

            val avgMpg = if (totalFuel > 0)
                totalMiles / totalFuel
            else 0.0

            _stats.postValue(
                VehicleStats(
                    avgMpg,
                    totalMiles,
                    totalFuel,
                    totalCost,
                    sortedLogs.size,
                    lastMPG,
                    odometerTimeData,
                    mpgOdometerData
                )
            )
        }
    }

    fun addVehicle(vehicle: Vehicle): Boolean {
        if (vehicle.name.isBlank()
            || vehicle.make.isBlank()
            || vehicle.model.isBlank()
        ) return false

        repository.addVehicle(vehicle)

        val updated = _vehicles.value.orEmpty().toMutableList()
        updated.add(vehicle)
        _vehicles.value = updated

        return true
    }

    fun updateVehicle(updatedVehicle: Vehicle): Boolean {
        if (updatedVehicle.name.isBlank()
            || updatedVehicle.make.isBlank()
            || updatedVehicle.model.isBlank()
        ) return false

        repository.updateVehicle(updatedVehicle)

        val updatedList = _vehicles.value.orEmpty().map {
            if (it.id == updatedVehicle.id) updatedVehicle else it
        }

        _vehicles.value = updatedList
        return true
    }

    fun deleteVehicle(vehicle: Vehicle, onComplete: (Boolean) -> Unit) {
        repository.deleteVehicle(vehicle) { success ->

            if (success) {
                val updatedList = _vehicles.value.orEmpty().filter {
                    it.id != vehicle.id
                }
                _vehicles.value = updatedList
            }

            onComplete(success)
        }
    }

    fun selectVehicle(vehicle: Vehicle) {
        _selectedVehicle.value = vehicle
    }
}