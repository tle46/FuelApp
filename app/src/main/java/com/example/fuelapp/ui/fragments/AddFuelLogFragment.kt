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
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*

class AddFuelLogFragment : Fragment() {
    private val tag = "AddFuelLogFragment"
    private lateinit var dateField: EditText
    private lateinit var btnAddFuel: Button
    private lateinit var btnCancelFuel: Button
    private lateinit var topAppBar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "onCreateView")

        (activity as MainActivity).hideBottomNav()

        val view = inflater.inflate(R.layout.fragment_add_fuel_log, container, false)

        dateField = view.findViewById(R.id.etFuelDate)
        btnAddFuel = view.findViewById(R.id.btnSaveFuel)
        btnCancelFuel = view.findViewById(R.id.btnCancelFuel)
        topAppBar = view.findViewById<MaterialToolbar>(R.id.topAppBar)

        val today = Calendar.getInstance().time
        val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val formattedDate = formatter.format(today)
        dateField.setText(formattedDate)

        btnAddFuel.setOnClickListener {
            Log.d(tag, "Add Fuel button clicked")
            (activity as MainActivity).switchFragment(VehicleListFragment())
        }

        btnCancelFuel.setOnClickListener {
            Log.d(tag, "Cancel Fuel button clicked")
            (activity as MainActivity).switchFragment(VehicleListFragment())
        }

        topAppBar.setNavigationOnClickListener {
            Log.d(tag, "Back button clicked")
            (activity as MainActivity).switchFragment(VehicleListFragment())
        }

        return view
    }

    override fun onStart() { super.onStart(); Log.d(tag, "onStart") }
    override fun onResume() { super.onResume(); Log.d(tag, "onResume") }
    override fun onPause() { super.onPause(); Log.d(tag, "onPause") }
    override fun onStop() { super.onStop(); Log.d(tag, "onStop") }
    override fun onDestroyView() {
        super.onDestroyView(); Log.d(tag, "onDestroyView")
        (activity as MainActivity).showBottomNav()
    }
    override fun onDestroy() { super.onDestroy(); Log.d(tag, "onDestroy") }
}