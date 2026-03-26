package com.example.fuelapp

import com.google.firebase.auth.FirebaseAuth

object AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signInAnonymously(onComplete: (String?) -> Unit) {
        if (auth.currentUser != null) {
            onComplete(auth.currentUser?.uid)
            return
        }

        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(auth.currentUser?.uid)
                } else {
                    onComplete(null)
                }
            }
    }

    fun getUserId(): String? {
        return auth.currentUser?.uid
    }
}