package com.example.fuelapp.data

import com.example.fuelapp.model.Vehicle
import com.google.firebase.firestore.FirebaseFirestore
import com.example.fuelapp.AuthManager

class VehicleRepository {

    private val db = FirebaseFirestore.getInstance()
    private val vehicleCollection = db.collection("vehicles")

    fun getVehicles(onResult: (List<Vehicle>) -> Unit) {
        val userId = AuthManager.getUserId()

        vehicleCollection
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val vehicles = result.documents.mapNotNull { doc ->
                    val vehicle = doc.toObject(Vehicle::class.java)
                    vehicle?.id = doc.id
                    vehicle
                }
                onResult(vehicles)
            }
    }

    fun addVehicle(vehicle: Vehicle) {
        val docRef = vehicleCollection.document()
        vehicle.id = docRef.id

        vehicle.userId = AuthManager.getUserId() ?: ""

        docRef.set(vehicle)
    }

    fun updateVehicle(vehicle: Vehicle) {
        vehicleCollection.document(vehicle.id).set(vehicle)
    }

    fun deleteVehicle(vehicle: Vehicle) {
        vehicleCollection.document(vehicle.id).delete()
    }
}