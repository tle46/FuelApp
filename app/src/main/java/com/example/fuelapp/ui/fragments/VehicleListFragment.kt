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
import com.example.fuelapp.viewmodel.FuelListViewModel
import com.google.android.material.textfield.TextInputEditText
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.example.fuelapp.LoginActivity
import com.example.fuelapp.model.FuelLog
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

        styleLineChart(chartOdometerTime, "Odometer Over Time")
        styleLineChart(chartMPGOdometer, "MPG vs Odometer")

        // SEED todo: REMOVE LATER. THIS IS FOR TESTING AND DEMO ONLY
        fun seedFuelLogs(vehicleId: String, logCount: Int) {
            val calendar = Calendar.getInstance()
            var lastOdometer = 0
            for (i in 1..logCount) {
                val daysBetween = (2..10).random()
                calendar.add(Calendar.DAY_OF_MONTH, daysBetween)
                val mpg = (20..40).random().toDouble()
                val gallons = (8..15).random().toDouble()
                val milesDriven = (mpg * gallons).toInt()
                lastOdometer += milesDriven
                val pricePerGallon = 2 + Math.random() * 4

                val fuelLog = FuelLog(
                    vehicleId = vehicleId,
                    date = calendar.time,
                    pricePerGallon = String.format("%.2f", pricePerGallon).toDouble(),
                    gallons = String.format("%.2f", gallons).toDouble(),
                    totalCost = String.format("%.2f", pricePerGallon * gallons).toDouble(),
                    odometer = lastOdometer,
                    fillPercent = 100
                )
                fuelViewModel.addFuelLog(fuelLog)
            }
        }
        // SEED todo: REMOVE LATER. THIS IS FOR TESTING AND DEMO ONLY
        fun seedData() {
            for (i in 1..3) {
                val vehicle = Vehicle(
                    name = "Vehicle $i",
                    year = 2010 + i,
                    make = "Make",
                    model = "Model"
                )

                val success = viewModel.addVehicle(vehicle)
                if (success) {
                    seedFuelLogs(vehicle.id, 20)
                }
            }
        }
        // SEED todo: REMOVE LATER. THIS IS FOR TESTING AND DEMO ONLY
        val btnSeedData: Button = view.findViewById(R.id.btnSeedData)
        btnSeedData.setOnClickListener {
            seedData()
            Toast.makeText(requireContext(), "Data seeded!", Toast.LENGTH_SHORT).show()
        }

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

        // Recompute stats for whichever vehicle is currently selected, using the
        // in-memory fuel log cache. Safe to call from any observer in any order —
        // it just no-ops if no vehicle is selected yet.
        fun recomputeStats() {
            val vehicle = viewModel.selectedVehicle.value ?: return
            val cachedLogs = fuelViewModel.getFuelLogsForVehicle(vehicle.id)
            if (cachedLogs.isNotEmpty()) {
                viewModel.loadStats(vehicle.id, cachedLogs)
            } else {
                // Fallback: fuel log cache not populated yet (cold-start race)
                viewModel.loadStats(vehicle.id)
            }
        }

        // Observe vehicles list — update the spinner, then recompute stats in case
        // the vehicle list arrived after the fuel log cache (common on cold start)
        viewModel.vehicles.observe(viewLifecycleOwner) { list ->
            vehicleList = list
            updateSpinner()
            recomputeStats()
        }

        // Observe selected vehicle — update form fields and recompute stats
        viewModel.selectedVehicle.observe(viewLifecycleOwner) { vehicle ->
            updateFormUI(vehicle)
            recomputeStats()
        }

        // Observe fuel log cache — fires on initial load AND whenever a log is
        // added/updated/deleted, so the graph always stays current
        fuelViewModel.fuelLogs.observe(viewLifecycleOwner) {
            recomputeStats()
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

        viewModel.stats.observe(viewLifecycleOwner) { stats ->

            txtTotalMPG.text = getString(R.string.mpg_format, stats.avgMpg)
            txtTotalFuelCost.text = getString(R.string.fuel_cost_format, stats.totalCost)
            txtTotalGallonsLogs.text = getString(R.string.gallons_format, stats.totalFuel)
            txtTotalMiles.text = getString(R.string.miles_format, stats.totalMiles)
            txtTotalFuelLogs.text = stats.totalLogs.toString()
            txtLastMPG.text = getString(R.string.mpg_format, stats.lastMpg)

            val odometerEntries = stats.odometerTimeData.map {
                Entry(it.first.toFloat(), it.second)
            }

            val mpgEntries = stats.mpgOdometerData.map {
                Entry(it.first, it.second)
            }

            val dateFormatter = object : ValueFormatter() {
                private val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
                override fun getFormattedValue(value: Float): String {
                    return sdf.format(Date(value.toLong()))
                }
            }

            chartOdometerTime.xAxis.valueFormatter = dateFormatter

            val odometerDataSet = createLineDataSet(
                odometerEntries,
                "Odometer",
                android.graphics.Color.BLUE
            )
            chartOdometerTime.data = LineData(odometerDataSet)
            chartOdometerTime.invalidate()

            val mpgDataSet = createLineDataSet(
                mpgEntries,
                "MPG",
                android.graphics.Color.GREEN
            )
            chartMPGOdometer.data = LineData(mpgDataSet)
            chartMPGOdometer.invalidate()
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

    fun createLineDataSet(entries: List<Entry>, label: String, lineColor: Int): LineDataSet {
        return LineDataSet(entries, label).apply {
            color = lineColor
            setDrawCircles(true)
            circleRadius = 4f
            circleHoleRadius = 2f
            setDrawValues(false)
            lineWidth = 2.5f
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            highLightColor = android.graphics.Color.RED
        }
    }

    private fun styleLineChart(chart: LineChart, description: String) {
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
}