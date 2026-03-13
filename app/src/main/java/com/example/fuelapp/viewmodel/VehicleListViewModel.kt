package com.example.fuelapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fuelapp.data.FakeVehicleDatabase
import com.example.fuelapp.model.Vehicle

class VehicleListViewModel : ViewModel() {

    private val _vehicles = MutableLiveData<List<Vehicle>>(emptyList())
    val vehicles: LiveData<List<Vehicle>> = _vehicles

    private val _selectedVehicle = MutableLiveData<Vehicle?>()
    val selectedVehicle: LiveData<Vehicle?> = _selectedVehicle

    init {
        // Update vehicles list from db
        loadVehicles()
    }

    private fun loadVehicles() {
        _vehicles.value = FakeVehicleDatabase.getVehicles()
    }

    fun updateVehicle(updatedVehicle: Vehicle): Boolean {
        // Return false if fields are blank
        if (updatedVehicle.name.isBlank()
            || updatedVehicle.make.isBlank()
            || updatedVehicle.model.isBlank())
            return false

        FakeVehicleDatabase.updateVehicle(updatedVehicle)
        loadVehicles()
        return true
    }

    fun deleteVehicle(vehicle: Vehicle): Boolean {
        FakeVehicleDatabase.deleteVehicle(vehicle)
        loadVehicles()

        if (_selectedVehicle.value?.id == vehicle.id) {
            _selectedVehicle.value = null
            // UI can handle selected vehicle being null
        }

        return true
    }

    fun selectVehicle(vehicle: Vehicle) {
        _selectedVehicle.value = vehicle
    }

    fun setVehicles(vehicleList: List<Vehicle>) {
        _vehicles.value = vehicleList
    }
}