package com.example.fuelapp.data

import com.example.fuelapp.model.Vehicle
import com.google.firebase.firestore.FirebaseFirestore

class VehicleRepository {

    private val db = FirebaseFirestore.getInstance()
    private val vehicleCollection = db.collection("vehicles")

    fun getVehicles(onResult: (List<Vehicle>) -> Unit) {
        vehicleCollection.get().addOnSuccessListener { result ->
            val vehicles = result.documents.mapNotNull { doc ->
                doc.toObject(Vehicle::class.java)
            }
            onResult(vehicles)
        }
    }

    fun addVehicle(vehicle: Vehicle) {
        vehicleCollection.add(vehicle)
    }

    fun updateVehicle(vehicle: Vehicle) {
        vehicleCollection.document(vehicle.id.toString()).set(vehicle)
    }

    fun deleteVehicle(vehicle: Vehicle) {
        vehicleCollection.document(vehicle.id.toString()).delete()
    }
}