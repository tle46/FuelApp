package com.example.fuelapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.fuelapp.MainActivity
import com.example.fuelapp.R
import androidx.fragment.app.activityViewModels
import com.example.fuelapp.model.Vehicle
import com.example.fuelapp.viewmodel.VehicleListViewModel
import com.google.android.material.appbar.MaterialToolbar

class AddVehicleFragment : Fragment() {

    private val viewModel: VehicleListViewModel by activityViewModels()
    private val tag = "addvehiclefragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(tag, "onCreateView")

        (activity as MainActivity).hideBottomNav()

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

            val vehicle = Vehicle(
                name = name,
                year = year.toIntOrNull() ?: 0,
                make = make,
                model = model
            )

            val success = viewModel.addVehicle(vehicle)

            if (success) {
                (activity as MainActivity).switchFragment(VehicleListFragment())
            }
        }

        val topAppBar = view.findViewById<MaterialToolbar>(R.id.topAppBar)

        topAppBar.setNavigationOnClickListener {
            Log.d(tag, "Back button clicked")
            (activity as MainActivity).switchFragment(VehicleListFragment())
        }

        val btnCancel = view.findViewById<Button>(R.id.btnCancelVehicle)

        btnCancel.setOnClickListener {
            Log.d(tag, "Cancel vehicle button clicked")
            (activity as MainActivity).switchFragment(VehicleListFragment())
        }

        return view
    }

    override fun onStart() { super.onStart(); Log.d(tag, "onStart") }
    override fun onResume() { super.onResume(); Log.d(tag, "onResume") }
    override fun onPause() { super.onPause(); Log.d(tag, "onPause") }
    override fun onStop() { super.onStop(); Log.d(tag, "onStop") }
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(tag, "onDestroyView")

        (activity as MainActivity).showBottomNav()
    }
    override fun onDestroy() { super.onDestroy(); Log.d(tag, "onDestroy") }
}