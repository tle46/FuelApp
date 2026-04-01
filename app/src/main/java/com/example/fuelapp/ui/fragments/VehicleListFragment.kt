package com.example.fuelapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.fuelapp.model.Vehicle
import com.example.fuelapp.R
import com.example.fuelapp.viewmodel.VehicleListViewModel
import com.google.android.material.textfield.TextInputEditText
import android.content.Intent
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.example.fuelapp.LoginActivity

class VehicleListFragment : Fragment() {

    private val viewModel: VehicleListViewModel by activityViewModels()

    private lateinit var spinnerVehicle: Spinner
    private lateinit var etVehicleName: TextInputEditText
    private lateinit var etYear: TextInputEditText
    private lateinit var etMake: TextInputEditText
    private lateinit var etModel: TextInputEditText
    private lateinit var btnEditVehicle: Button
    private lateinit var btnDeleteVehicle: Button
    private lateinit var btnAddVehicle: Button

    private var vehicleList: List<Vehicle> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_vehicle_list, container, false)

        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        spinnerVehicle = view.findViewById(R.id.spinnerVehicle)
        etVehicleName = view.findViewById(R.id.etVehicleName)
        etYear = view.findViewById(R.id.etYear)
        etMake = view.findViewById(R.id.etMake)
        etModel = view.findViewById(R.id.etModel)
        btnEditVehicle = view.findViewById(R.id.btnEditVehicle)
        btnDeleteVehicle = view.findViewById(R.id.btnDeleteVehicle)
        btnAddVehicle = view.findViewById(R.id.btnAddVehicle)

        // Logout button
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        // Add button
        btnAddVehicle.setOnClickListener {
            (activity as? com.example.fuelapp.MainActivity)?.switchFragment(AddVehicleFragment())
        }

        // Edit button - saves values from edit text to db
        btnEditVehicle.setOnClickListener {
            val selected = viewModel.selectedVehicle.value
            if (selected != null) {
                val updatedVehicle = selected.copy(
                    name = etVehicleName.text.toString(),
                    year = etYear.text.toString().toIntOrNull() ?: selected.year,
                    make = etMake.text.toString(),
                    model = etModel.text.toString()
                )
                val success = viewModel.updateVehicle(updatedVehicle)

                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(if (success) "Success" else "Invalid input")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        // Delete button
        btnDeleteVehicle.setOnClickListener {
            var success = false
            viewModel.selectedVehicle.value?.let { vehicle ->
                success = viewModel.deleteVehicle(vehicle)
            }

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(if (success) "Success" else "Error")
                .setPositiveButton("OK", null)
                .show()
        }

        // Observe vehicles list and update the spinner
        viewModel.vehicles.observe(viewLifecycleOwner) { list ->
            vehicleList = list
            updateSpinner()
        }

        // Observe selected vehicle and update form
        viewModel.selectedVehicle.observe(viewLifecycleOwner) { vehicle ->
            // Vehicle can be null. Case is handled in updateFormUI
            updateFormUI(vehicle)
        }

        spinnerVehicle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                // Spinner is an adapter view
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                // Position in range of vehicle list
                if (position in vehicleList.indices) {
                    viewModel.selectVehicle(vehicleList[position])
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // todo
                // handle the case where there are no vehicles yet
                // it works currently so maybe not needed
            }
        }

        return view
    }

    private fun updateSpinner() {
        // Make vehicleList into Hash map
        val names = vehicleList.map { it.name }

        // Update spinner
        // spinner is a type of adapter
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerVehicle.adapter = adapter

        // Update selected vehicle in spinner
        val selected = viewModel.selectedVehicle.value
        val index = vehicleList.indexOfFirst { it.id == selected?.id }
        if (index != -1) spinnerVehicle.setSelection(index)
    }

    private fun updateFormUI(vehicle: Vehicle?) {
        if (vehicle != null) {
            etVehicleName.setText(vehicle.name)
            etYear.setText(vehicle.year.toString())
            etMake.setText(vehicle.make)
            etModel.setText(vehicle.model)
            btnEditVehicle.isEnabled = true
            btnDeleteVehicle.isEnabled = true
        } else {
            // Defaults when vehicle is null
            etVehicleName.setText("")
            etYear.setText("")
            etMake.setText("")
            etModel.setText("")
            btnEditVehicle.isEnabled = false
            btnDeleteVehicle.isEnabled = false
        }
    }
}