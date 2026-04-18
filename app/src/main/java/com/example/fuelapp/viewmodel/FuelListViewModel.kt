package com.example.fuelapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fuelapp.model.FuelLog
import com.example.fuelapp.data.FuelLogRepository

class FuelListViewModel : ViewModel() {

    private val repository = FuelLogRepository()

    private val _fuelLogs = MutableLiveData<List<FuelLog>>()
    val fuelLogs: LiveData<List<FuelLog>> get() = _fuelLogs
    private val _selectedFuelLog = MutableLiveData<FuelLog?>()
    val selectedFuelLog: LiveData<FuelLog?> get() = _selectedFuelLog

    private var currentVehicleId: String? = null

    // Track whether the initial fetch from Firestore has completed.
    // This is per-ViewModel instance (i.e. per user session), so it does not
    // block re-fetches across logins or ViewModel recreations.
    private var hasLoaded = false

    init {
        loadFuelLogs()
    }

    fun addFuelLog(log: FuelLog): Boolean {
        if (
            log.vehicleId.isBlank() ||
            log.pricePerGallon < 0.0 ||
            log.gallons <= 0.0 ||
            log.totalCost < 0.0 ||
            log.odometer < 0
        ) {
            return false
        }

        // Write to Firestore (repository assigns the id)
        repository.addFuelLog(log)

        // Update the local list immediately — no re-fetch needed
        val updated = (_fuelLogs.value ?: emptyList()) + log
        _fuelLogs.value = updated

        return true
    }

    fun updateFuelLog(log: FuelLog) {
        repository.updateFuelLog(log)

        // Replace the matching entry in the local list
        val updated = (_fuelLogs.value ?: emptyList()).map {
            if (it.id == log.id) log else it
        }
        _fuelLogs.value = updated
    }

    fun deleteFuelLog(id: String) {
        repository.deleteFuelLogById(id)

        // Remove the deleted entry from the local list
        val updated = (_fuelLogs.value ?: emptyList()).filter { it.id != id }
        _fuelLogs.value = updated
    }

    fun clearLogsByVehicle(vehicleId: String) {
        repository.clearByVehicleId(vehicleId) {
            // Remove from local list after Firestore batch-delete completes
            val updated = (_fuelLogs.value ?: emptyList()).filter { it.vehicleId != vehicleId }
            _fuelLogs.postValue(updated)
        }
    }

    // Not used yet
    fun clearLogsByUser(userId: String) {
        repository.clearByUserId(userId)
        _fuelLogs.value = emptyList()
    }

    // Overload used by VehicleListViewModel.loadStats — returns logs filtered
    // for a specific vehicle from the already-loaded local list.
    fun getFuelLogsForVehicle(vehicleId: String): List<FuelLog> {
        return (_fuelLogs.value ?: emptyList()).filter { it.vehicleId == vehicleId }
    }

    // Not used yet (kept for API completeness)
    private fun loadFuelLogs(vehicleId: String) {
        currentVehicleId = vehicleId
        repository.getFuelLogsByVehicle(vehicleId) { logs ->
            _fuelLogs.postValue(logs)
        }
    }

    /**
     * Fetches all fuel logs from Firestore. Only performs the network call once
     * per ViewModel lifetime; subsequent calls are no-ops because all mutations
     * keep [_fuelLogs] up-to-date locally.
     */
    fun loadFuelLogs() {
        if (hasLoaded) return
        repository.getFuelLogs { logs ->
            _fuelLogs.postValue(logs)
            hasLoaded = true
        }
    }

    fun selectFuelLog(log: FuelLog) {
        _selectedFuelLog.value = log
    }
}
