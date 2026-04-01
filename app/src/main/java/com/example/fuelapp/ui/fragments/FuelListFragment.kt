package com.example.fuelapp.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fuelapp.R
import com.example.fuelapp.model.Vehicle
import com.example.fuelapp.ui.adapter.FuelLogAdapter
import com.example.fuelapp.viewmodel.FuelListViewModel
import com.example.fuelapp.viewmodel.VehicleListViewModel

class FuelListFragment : Fragment(R.layout.fragment_fuel_list) {

    private val fuelViewModel: FuelListViewModel by activityViewModels()
    private val vehicleViewModel: VehicleListViewModel by activityViewModels()

    private lateinit var adapter: FuelLogAdapter
    private var vehicleList: List<Vehicle> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvFuelLogs)
        val btnAdd = view.findViewById<View>(R.id.btnAddFuel)

        // Generate fuel lists as RecyclerView using adapter
        // Initialize adapter with empty lists
        adapter = FuelLogAdapter(
            logs = emptyList(),
            vehicles = vehicleList
        ) { selectedLog ->
            // Select the selected log
            fuelViewModel.selectFuelLog(selectedLog)

            // Navigate to the EditFuelLogFragment
            (activity as? com.example.fuelapp.MainActivity)?.switchFragment(
                EditFuelLogFragment()
            )
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Observe vehicles from VehicleListViewModel
        vehicleViewModel.vehicles.observe(viewLifecycleOwner) { vehicles ->
            vehicleList = vehicles
            adapter.updateVehicles(vehicleList) // update adapter with names
        }

        // Observe fuel logs
        fuelViewModel.fuelLogs.observe(viewLifecycleOwner) { logs ->
            adapter.updateLogs(logs)
        }

        // Add button
        btnAdd.setOnClickListener {
            (activity as? com.example.fuelapp.MainActivity)?.switchFragment(
                AddFuelLogFragment()
            )
        }
    }
}