package com.example.fuelapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.fuelapp.MainActivity
import com.example.fuelapp.R

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

        val btnAddFuel: Button = view.findViewById(R.id.btnSaveVehicle)
        btnAddFuel.setOnClickListener {
            Log.d(tag, "add fuel button clicked")
            (activity as MainActivity).switchFragment(VehicleListFragment())
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