package com.example.fuelapp.ui.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.fuelapp.MainActivity
import com.example.fuelapp.R
import com.example.fuelapp.model.FuelLog
import com.example.fuelapp.model.Vehicle
import com.example.fuelapp.viewmodel.FuelListViewModel
import com.example.fuelapp.viewmodel.VehicleListViewModel
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*

class AddFuelLogFragment : Fragment() {
    private val tag = "AddFuelLogFragment"
    private lateinit var dateField: EditText
    private lateinit var btnAddFuel: Button
    private lateinit var btnCancelFuel: Button
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var spinnerVehicle: Spinner
    private val fuelViewModel: FuelListViewModel by activityViewModels()
    private val vehicleViewModel: VehicleListViewModel by activityViewModels()
    private var vehicleList: List<Vehicle> = emptyList()
    private var selectedVehicle: String = ""
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "onCreateView")

        (activity as MainActivity).hideBottomNav()

        val view = inflater.inflate(R.layout.fragment_add_fuel_log, container, false)

        dateField = view.findViewById(R.id.etFuelDate)
        dateField.isFocusable = false
        dateField.isClickable = true
        btnAddFuel = view.findViewById(R.id.btnSaveFuel)
        btnCancelFuel = view.findViewById(R.id.btnCancelFuel)
        topAppBar = view.findViewById(R.id.topAppBar)
        spinnerVehicle = view.findViewById(R.id.spinnerVehicle)

        val priceField = view.findViewById<EditText>(R.id.etFuelPrice)
        val gallonsField = view.findViewById<EditText>(R.id.etFuelAmount)
        val totalCostField = view.findViewById<EditText>(R.id.etTotalCost)
        val odometerField = view.findViewById<EditText>(R.id.etOdometer)
        val seekBar = view.findViewById<SeekBar>(R.id.seekBarFillPercent)

        seekBar.progress = 100

        vehicleViewModel.vehicles.observe(viewLifecycleOwner) { list ->
            vehicleList = list
            val names = vehicleList.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerVehicle.adapter = adapter

            if (vehicleList.isNotEmpty()) {
                selectedVehicle = vehicleList[0].id
            }
        }

        spinnerVehicle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (position in vehicleList.indices) {
                    selectedVehicle = vehicleList[position].id
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedVehicle = ""
            }
        }

        val formatter = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
        dateField.setText(formatter.format(calendar.time))

        dateField.setOnClickListener {
            val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    dateField.setText(formatter.format(calendar.time))
                }

                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = calendar.get(Calendar.MINUTE)

                TimePickerDialog(requireContext(), timeListener, currentHour, currentMinute, true).show()
            }

            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), dateListener, currentYear, currentMonth, currentDay).show()
        }

        btnAddFuel.setOnClickListener {

            val dateTimeString = dateField.text.toString()
            val price = priceField.text.toString().toDoubleOrNull() ?: 0.0
            val gallons = gallonsField.text.toString().toDoubleOrNull() ?: 0.0
            val totalCost = totalCostField.text.toString().toDoubleOrNull() ?: 0.0
            val odometer = odometerField.text.toString().toIntOrNull() ?: 0
            val fillPercent = seekBar.progress

            val fuelLog = FuelLog(
                vehicleId = selectedVehicle,
                date = dateTimeString,
                pricePerGallon = price,
                gallons = gallons,
                totalCost = totalCost,
                odometer = odometer,
                fillPercent = fillPercent
            )

            val success = fuelViewModel.addFuelLog(fuelLog)
            if (success) {
                (activity as MainActivity).switchFragment(VehicleListFragment())
            }
        }

        btnCancelFuel.setOnClickListener {
            (activity as MainActivity).switchFragment(VehicleListFragment())
        }

        topAppBar.setNavigationOnClickListener {
            (activity as MainActivity).switchFragment(VehicleListFragment())
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).showBottomNav()
    }
}