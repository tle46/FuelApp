package com.example.fuelapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.fuelapp.R
import com.google.firebase.firestore.FirebaseFirestore

class AddVehicleFragment : Fragment() {

    private val tag = "addvehiclefragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(tag, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_add_vehicle, container, false)

        val nameField = view.findViewById<EditText>(R.id.etVehicleName)
        val yearField = view.findViewById<EditText>(R.id.etYear)
        val makeField = view.findViewById<EditText>(R.id.etMake)
        val modelField = view.findViewById<EditText>(R.id.etModel)

        val btnAddFuel: Button = view.findViewById(R.id.btnSaveVehicle)
        btnAddFuel.setOnClickListener {

            val name = nameField.text.toString()
            val make = makeField.text.toString()
            val model = modelField.text.toString()
            val year = yearField.text.toString()

            val db = FirebaseFirestore.getInstance()

            val vehicle = hashMapOf(
                "name" to name,
                "make" to make,
                "model" to model,
                "year" to year.toIntOrNull()
            )

            db.collection("vehicles")
                .add(vehicle)
        }

        return view
    }

    override fun onStart() { super.onStart(); Log.d(tag, "onStart") }
    override fun onResume() { super.onResume(); Log.d(tag, "onResume") }
    override fun onPause() { super.onPause(); Log.d(tag, "onPause") }
    override fun onStop() { super.onStop(); Log.d(tag, "onStop") }
    override fun onDestroyView() { super.onDestroyView(); Log.d(tag, "onDestroyView") }
    override fun onDestroy() { super.onDestroy(); Log.d(tag, "onDestroy") }
}