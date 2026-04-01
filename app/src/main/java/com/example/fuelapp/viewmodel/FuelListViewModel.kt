package com.example.fuelapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fuelapp.model.FuelLog
import com.example.fuelapp.data.FakeFuelDatabase

class FuelListViewModel : ViewModel() {

    private val _fuelLogs = MutableLiveData<List<FuelLog>>()
    val fuelLogs: LiveData<List<FuelLog>> get() = _fuelLogs

    init {
        loadFuelLogs()
    }

    fun addFuelLog(log: FuelLog): Boolean {

        if (
            log.vehicleId.isBlank() ||
            log.date.isBlank() ||
            log.pricePerGallon <= 0.0 ||
            log.gallons <= 0.0 ||
            log.totalCost <= 0.0 ||
            log.odometer < 0
        ) {
            return false
        }

        FakeFuelDatabase.addFuelLog(log)
        loadFuelLogs()
        return true
    }

    // Not used yet
    fun getFuelLogById(id: String): FuelLog? {
        return FakeFuelDatabase.getFuelLogById(id)
    }

    // Not used yet
    fun updateFuelLog(log: FuelLog) {
        FakeFuelDatabase.updateFuelLog(log)
        loadFuelLogs()
    }

    // Not used yet
    fun deleteFuelLog(id: String) {
        FakeFuelDatabase.deleteFuelLogById(id)
        loadFuelLogs()
    }

    // Not used yet
    fun clearLogs() {
        FakeFuelDatabase.clear()
        loadFuelLogs()
    }

    private fun loadFuelLogs() {
        _fuelLogs.value = FakeFuelDatabase.getFuelLogs()
    }

}