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
import com.example.fuelapp.viewmodel.FuelListViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class VehicleListFragment : Fragment() {

    private val viewModel: VehicleListViewModel by activityViewModels()

    private val fuelViewModel: FuelListViewModel by activityViewModels()

    private lateinit var spinnerVehicle: Spinner
    private lateinit var etVehicleName: TextInputEditText
    private lateinit var etYear: TextInputEditText
    private lateinit var etMake: TextInputEditText
    private lateinit var etModel: TextInputEditText
    private lateinit var btnEditVehicle: Button
    private lateinit var btnDeleteVehicle: Button
    private lateinit var btnAddVehicle: Button
    private lateinit var txtTotalMPG: TextView
    private lateinit var txtTotalMiles: TextView
    private lateinit var txtTotalFuelCost: TextView
    private lateinit var txtTotalGallonsLogs: TextView
    private lateinit var txtTotalFuelLogs: TextView
    private lateinit var txtLastMPG: TextView

    private lateinit var chartOdometerTime: LineChart
    private lateinit var chartMPGOdometer: LineChart

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
        txtTotalMPG = view.findViewById(R.id.txtTotalMPG)
        txtTotalMiles = view.findViewById(R.id.txtTotalMiles)
        txtTotalFuelCost = view.findViewById(R.id.txtTotalFuelCost)
        txtTotalGallonsLogs = view.findViewById(R.id.txtTotalGallonsLogs)
        txtTotalFuelLogs = view.findViewById(R.id.txtTotalFuelLogs)
        txtLastMPG = view.findViewById(R.id.txtLastMPG)
        chartOdometerTime = view.findViewById(R.id.chartOdometerTime)
        chartMPGOdometer = view.findViewById(R.id.chartMPGOdometer)

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
            viewModel.selectedVehicle.value?.let { vehicle ->
                viewModel.deleteVehicle(vehicle) { success ->
                    if (success) {
                        fuelViewModel.clearLogsByVehicle(vehicle.id)
                    }
                }
            }
        }

        // Initially hide update form fields
        updateFormUI(null)

        // Observe vehicles list and update the spinner
        viewModel.vehicles.observe(viewLifecycleOwner) { list ->
            vehicleList = list
            updateSpinner()
        }

        // Observe selected vehicle and update form
        viewModel.selectedVehicle.observe(viewLifecycleOwner) { vehicle ->
            updateFormUI(vehicle)

            if (vehicle != null) {
                loadStats(vehicle.id)
            }
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
            etVehicleName.apply {
                setText(vehicle.name)
                visibility = View.VISIBLE
            }
            etYear.apply {
                setText(vehicle.year.toString())
                visibility = View.VISIBLE
            }
            etMake.apply {
                setText(vehicle.make)
                visibility = View.VISIBLE
            }
            etModel.apply {
                setText(vehicle.model)
                visibility = View.VISIBLE
            }
            btnEditVehicle.apply {
                isEnabled = true
                visibility = View.VISIBLE
            }
            btnDeleteVehicle.apply {
                isEnabled = true
                visibility = View.VISIBLE
            }
        } else {
            // Hide fields/buttons when vehicle is null
            etVehicleName.visibility = View.GONE
            etYear.visibility = View.GONE
            etMake.visibility = View.GONE
            etModel.visibility = View.GONE
            btnEditVehicle.visibility = View.GONE
            btnDeleteVehicle.visibility = View.GONE
        }
    }

    private fun loadStats(vehicleId: String) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("fuelLogs")
            .whereEqualTo("vehicleId", vehicleId)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->

                if (documents.isEmpty) return@addOnSuccessListener

                // Sort logs by odometer
                val sortedDocs = documents.sortedBy { it.getDouble("odometer") ?: 0.0 }

                var totalFuel = 0.0
                var totalCost = 0.0
                var totalMiles = 0.0

                // Prepare chart data lists
                val odometerTimeEntries = mutableListOf<Entry>()
                val mpgOdometerEntries = mutableListOf<Entry>()

                val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

                if (sortedDocs.size > 1) {
                    val lastOdo = sortedDocs[sortedDocs.size - 1].getDouble("odometer") ?: 0.0
                    val prevOdo = sortedDocs[sortedDocs.size - 2].getDouble("odometer") ?: 0.0
                    val lastGallons = sortedDocs[sortedDocs.size - 1].getDouble("gallons") ?: 0.0
                    val lastMPG = if (lastGallons > 0) (lastOdo - prevOdo) / lastGallons else 0.0
                    txtLastMPG.text = getString(R.string.mpg_format, lastMPG)
                }

                for ((i, doc) in sortedDocs.withIndex()) {
                    val odo = doc.getDouble("odometer")?.toFloat() ?: 0f
                    val gallons = doc.getDouble("gallons")?.toFloat() ?: 0f
                    val cost = doc.getDouble("totalCost")?.toFloat() ?: 0f
                    val timestamp = doc.getTimestamp("date")?.toDate()?.time?.toFloat() ?: 0f

                    // Odometer vs Time
                    odometerTimeEntries.add(Entry(timestamp, odo))

                    // MPG vs Odometer (skip first log)
                    if (i > 0) {
                        val prevOdo = sortedDocs[i - 1].getDouble("odometer")?.toFloat() ?: 0f
                        val prevGallons = sortedDocs[i].getDouble("gallons")?.toFloat() ?: 0f
                        if (prevGallons > 0f) {
                            val mpg = (odo - prevOdo) / prevGallons
                            mpgOdometerEntries.add(Entry(odo, mpg))
                        }

                        totalMiles += (odo - prevOdo)
                        totalFuel += gallons
                        totalCost += cost
                    }
                }

                // Calculate average MPG
                val avgMpg = if (totalFuel > 0) totalMiles / totalFuel else 0.0

                txtTotalMPG.text = getString(R.string.mpg_format, avgMpg)
                txtTotalFuelCost.text = getString(R.string.fuel_cost_format, totalCost)
                txtTotalGallonsLogs.text = getString(R.string.gallons_format, totalFuel)
                txtTotalMiles.text = getString(R.string.miles_format, totalMiles)
                txtTotalFuelLogs.text = documents.size().toString()

                // --- Odometer vs Time Chart ---
                styleLineChart(chartOdometerTime, "Odometer over Time")
                chartOdometerTime.axisLeft.spaceBottom = 0f
                val odometerDataSet = createLineDataSet(odometerTimeEntries, "Odometer", android.graphics.Color.BLUE)
                chartOdometerTime.data = LineData(odometerDataSet)
                chartOdometerTime.xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return dateFormat.format(Date(value.toLong()))
                    }
                }
                chartOdometerTime.invalidate()

                // --- MPG vs Odometer Chart ---
                styleLineChart(chartMPGOdometer, "MPG vs Odometer")
                val mpgDataSet = createLineDataSet(mpgOdometerEntries, "MPG", android.graphics.Color.parseColor("#4CAF50")) // modern green
                chartMPGOdometer.data = LineData(mpgDataSet)
                chartMPGOdometer.invalidate()
            }
    }

    fun styleLineChart(chart: LineChart, description: String) {
        chart.apply {
            setTouchEnabled(true)
            setPinchZoom(true)
            setScaleEnabled(true)
            setDrawGridBackground(false)
            setBackgroundColor(android.graphics.Color.WHITE)
            legend.isEnabled = true
            this.description.text = description
            this.description.textSize = 12f
            this.description.textColor = android.graphics.Color.DKGRAY
            animateX(800)

            // X-Axis styling
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = android.graphics.Color.DKGRAY
                textSize = 12f
            }

            // Y-Axis styling
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = android.graphics.Color.LTGRAY
                textColor = android.graphics.Color.DKGRAY
                textSize = 12f
            }

            axisRight.isEnabled = false
            chart.axisLeft.apply {
                setLabelCount(4, true)
            }
        }
    }

    fun createLineDataSet(entries: List<Entry>, label: String, lineColor: Int): LineDataSet {
        return LineDataSet(entries, label).apply {
            color = lineColor
            setDrawCircles(true)
            circleRadius = 4f
            circleHoleRadius = 2f
            setDrawValues(false)
            lineWidth = 2.5f
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER  // smooth curves
            highLightColor = android.graphics.Color.RED
        }
    }
}