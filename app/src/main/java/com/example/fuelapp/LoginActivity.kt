package com.example.fuelapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)

        val loginBtn = findViewById<Button>(R.id.loginButton)
        val signupBtn = findViewById<Button>(R.id.signupButton)
        val skipBtn = findViewById<Button>(R.id.skipButton)

        //Skip to anonymous login
        skipBtn.setOnClickListener {
            auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //go to dashboard
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Anonymous auth failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        //Login existing user
        loginBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password can not be empty", Toast.LENGTH_LONG).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            startMain()
                        } else {
                            Toast.makeText(this, "Incorrect email and password", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        //Sign up OR link account
        signupBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            val user = auth.currentUser

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password can not be empty", Toast.LENGTH_LONG).show()
            } else if (user != null && user.isAnonymous) {
                //LINK anonymous to real account
                val credential = EmailAuthProvider.getCredential(email, password)

                user.linkWithCredential(credential)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Account created!", Toast.LENGTH_LONG).show()
                            startMain()
                        } else {
                            Toast.makeText(this, "Linking failed", Toast.LENGTH_LONG).show()
                        }
                    }

            } else {
                //normal signup
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            startMain()
                        } else {
                            Toast.makeText(this, "Signup failed. Please enter a valid email address and password.", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }

    private fun startMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
