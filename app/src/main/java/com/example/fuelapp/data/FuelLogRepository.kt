package com.example.fuelapp.data

import com.example.fuelapp.model.FuelLog
import com.example.fuelapp.AuthManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FuelLogRepository {

    private val db = FirebaseFirestore.getInstance()
    private val fuelLogCollection = db.collection("fuelLogs")

    fun getFuelLogs(
        onResult: (List<FuelLog>) -> Unit
    ) {
        val userId = AuthManager.getUserId()

        fuelLogCollection
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val logs = result.documents.mapNotNull { doc ->
                    val log = doc.toObject(FuelLog::class.java)
                    log?.id = doc.id
                    log
                }
                onResult(logs)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun getFuelLogsByVehicle(
        vehicleId: String,
        onResult: (List<FuelLog>) -> Unit
    ) {
        val userId = AuthManager.getUserId()

        if (userId == null) {
            onResult(emptyList())
            return
        }

        fuelLogCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("vehicleId", vehicleId)
            .get()
            .addOnSuccessListener { result ->
                val logs = result.documents.mapNotNull { doc ->
                    val log = doc.toObject(FuelLog::class.java)
                    log?.id = doc.id
                    log
                }
                onResult(logs)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun addFuelLog(log: FuelLog) {
        val userId = AuthManager.getUserId() ?: return

        val docRef = fuelLogCollection.document()
        log.id = docRef.id
        log.userId = userId

        docRef.set(log)
    }

    fun updateFuelLog(log: FuelLog) {
        if (log.id.isBlank()) return
        fuelLogCollection.document(log.id).set(log)
    }

    fun deleteFuelLogById(id: String) {
        if (id.isBlank()) return
        fuelLogCollection.document(id).delete()
    }

    fun clearByVehicleId(vehicleId: String, onComplete: () -> Unit) {
        val userId = AuthManager.getUserId() ?: return onComplete()

        fuelLogCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("vehicleId", vehicleId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    onComplete()
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                for (doc in result.documents) {
                    batch.delete(doc.reference)
                }

                batch.commit()
                    .addOnSuccessListener { onComplete() }
                    .addOnFailureListener { onComplete() }
            }
            .addOnFailureListener { onComplete() }
    }

    fun clearByUserId(userId: String) {
        if (userId.isBlank()) return

        fuelLogCollection
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result.documents) {
                    doc.reference.delete()
                }
            }
    }
}