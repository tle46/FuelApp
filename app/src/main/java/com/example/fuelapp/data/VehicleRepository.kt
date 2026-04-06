package com.example.fuelapp.data

import com.example.fuelapp.model.Vehicle
import com.google.firebase.firestore.FirebaseFirestore
import com.example.fuelapp.AuthManager
import android.util.Log

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
        val userId = AuthManager.getUserId() ?: return

        val docRef = vehicleCollection.document()
        vehicle.id = docRef.id
        vehicle.userId = userId

        docRef.set(vehicle)
            .addOnSuccessListener {
                Log.d("Firestore", "Vehicle added successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding vehicle", e)
            }
    }

    fun updateVehicle(vehicle: Vehicle) {
        vehicleCollection.document(vehicle.id).set(vehicle)
    }

    fun deleteVehicle(vehicle: Vehicle, onComplete: (Boolean) -> Unit) {
        vehicleCollection.document(vehicle.id)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}