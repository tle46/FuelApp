package com.example.fuelapp.data

import com.example.fuelapp.model.Vehicle

object FakeVehicleDatabase {

    private val vehicles = mutableListOf(
        Vehicle("1", "Bob's Truck 1", 2019, "Ford", "F-150"),
        Vehicle("2", "Bob's Honda 2", 2015, "Honda", "Civic"),
        Vehicle("3", "Bob's Eagle 3", 1992, "Eagle", "Talon"),
        Vehicle("4", "Joe's Miata 4", 2004, "Mazda", "MX-5"),
        Vehicle("5", "Joe's Jetta 5", 2025, "Volkswagen", "Jetta"),
        Vehicle("6", "Joe's Lamborghini 6", 2012, "Lamborghini", "Aventador")

    )

    fun getVehicles(): List<Vehicle> {
        return vehicles
    }

    fun addVehicle(vehicle: Vehicle) {
        vehicles.add(vehicle)
    }

    fun updateVehicle(vehicle: Vehicle) {
        val index = vehicles.indexOfFirst { it.id == vehicle.id }
        if (index != -1) {
            vehicles[index] = vehicle
        }
    }

    fun deleteVehicle(vehicle: Vehicle) {
        vehicles.remove(vehicle)
    }
}