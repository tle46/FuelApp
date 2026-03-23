package com.example.fuelapp.data

import com.example.fuelapp.model.Vehicle
import com.google.firebase.firestore.FirebaseFirestore

class VehicleRepository {

    private val db = FirebaseFirestore.getInstance()
    private val vehicleCollection = db.collection("vehicles")

    fun getVehicles(onResult: (List<Vehicle>) -> Unit) {
        vehicleCollection.get().addOnSuccessListener { result ->
            val vehicles = result.documents.mapNotNull { doc ->
                val vehicle = doc.toObject(Vehicle::class.java)
                // Vehicle id match doc id
                vehicle?.id = doc.id
                vehicle
            }
            onResult(vehicles)
        }
    }

    fun addVehicle(vehicle: Vehicle) {
        // Firestore generates the vehicle id
        val docRef = vehicleCollection.document()
        vehicle.id = docRef.id
        // Set the document
        docRef.set(vehicle)
    }

    fun updateVehicle(vehicle: Vehicle) {
        vehicleCollection.document(vehicle.id).set(vehicle)
    }

    fun deleteVehicle(vehicle: Vehicle) {
        vehicleCollection.document(vehicle.id).delete()
    }
}