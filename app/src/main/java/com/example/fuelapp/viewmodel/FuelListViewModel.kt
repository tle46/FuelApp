package com.example.fuelapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fuelapp.data.FakeFuelDatabase
import com.example.fuelapp.model.FuelLog
import com.example.fuelapp.data.FuelLogRepository

class FuelListViewModel : ViewModel() {

    private val repository = FuelLogRepository()

    private val _fuelLogs = MutableLiveData<List<FuelLog>>()
    val fuelLogs: LiveData<List<FuelLog>> get() = _fuelLogs

    private var currentVehicleId: String? = null

    init {
        loadFuelLogs()
    }

    fun addFuelLog(log: FuelLog): Boolean {

        if (
            log.vehicleId.isBlank() ||
            log.date.isBlank() ||
            log.pricePerGallon < 0.0 ||
            log.gallons <= 0.0 ||
            log.totalCost < 0.0 ||
            log.odometer < 0
        ) {
            return false
        }

        repository.addFuelLog(log)
        loadFuelLogs()
        return true
    }

    // Not used yet
    fun updateFuelLog(log: FuelLog) {
        repository.updateFuelLog(log)
        loadFuelLogs()
    }

    // Not used yet
    fun deleteFuelLog(id: String) {
        //repository.deleteFuelLogById(id)
        loadFuelLogs()
    }

    // Not used yet
    fun clearLogsByVehicle(vehicleId: String) {
        //repository.clearByVehicleId(vehicleId)
        loadFuelLogs()
    }

    fun clearLogsByUser(vehicleId: String) {
        //repository.clearByUserId(vehicleId)
        loadFuelLogs()
    }

    private fun loadFuelLogs(vehicleId: String) {
        currentVehicleId = vehicleId

        repository.getFuelLogsByVehicle(vehicleId) { logs ->
            _fuelLogs.postValue(logs)
        }
    }

    private fun loadFuelLogs() {
        repository.getFuelLogs() { logs ->
            _fuelLogs.postValue(logs)
        }
    }

}