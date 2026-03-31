package com.example.fuelapp.ui.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
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

    private var isUpdating = false
    private val lastEditedFields: LinkedList<EditText> = LinkedList()
    private lateinit var priceField: EditText
    private lateinit var gallonsField: EditText
    private lateinit var totalCostField: EditText
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "onCreateView")

        (activity as MainActivity).hideBottomNav()

        val view = inflater.inflate(R.layout.fragment_add_fuel_log, container, false)

        // Initialize views
        dateField = view.findViewById(R.id.etFuelDate)
        btnAddFuel = view.findViewById(R.id.btnSaveFuel)
        btnCancelFuel = view.findViewById(R.id.btnCancelFuel)
        topAppBar = view.findViewById(R.id.topAppBar)
        spinnerVehicle = view.findViewById(R.id.spinnerVehicle)

        priceField = view.findViewById(R.id.etFuelPrice)
        gallonsField = view.findViewById(R.id.etFuelAmount)
        totalCostField = view.findViewById(R.id.etTotalCost)
        val odometerField = view.findViewById<EditText>(R.id.etOdometer)
        val seekBar = view.findViewById<SeekBar>(R.id.seekBarFillPercent)

        seekBar.progress = 100

        priceField.setHint("0.000")
        gallonsField.setHint("0.000")
        totalCostField.setHint("0.00")
        odometerField.setHint("000 000")

        fun formatDecimal(value: String, decimals: Int): String {
            val digits = value.replace(Regex("\\D"), "")
            if (digits.isEmpty()) return ""
            val padded = digits.padStart(decimals + 1, '0')
            val integerPart = padded.dropLast(decimals)
            val decimalPart = padded.takeLast(decimals)
            return "${integerPart.toInt()}.$decimalPart"
        }

        // Fuel field calculation logic
        fun updateValues() {
            if (isUpdating) return
            isUpdating = true

            val price = priceField.text.toString().toDoubleOrNull()
            val gallons = gallonsField.text.toString().toDoubleOrNull()
            val total = totalCostField.text.toString().toDoubleOrNull()

            if (lastEditedFields.size < 2) {
                isUpdating = false
                return
            }

            val field1 = lastEditedFields[0]
            val field2 = lastEditedFields[1]

            // Determine the third field to calculate
            val allFields = listOf(priceField, gallonsField, totalCostField)
            val missingField = allFields.first { it != field1 && it != field2 }

            // Calculate and fill missing field
            when (missingField) {
                priceField -> {
                    if (gallons != null && total != null && gallons != 0.0) {
                        val result = total / gallons
                        priceField.setText(String.format("%.3f", result))
                        priceField.setSelection(priceField.text.length)
                    }
                }
                gallonsField -> {
                    if (price != null && total != null && price != 0.0) {
                        val result = total / price
                        gallonsField.setText(String.format("%.3f", result))
                        gallonsField.setSelection(gallonsField.text.length)
                    }
                }
                totalCostField -> {
                    if (price != null && gallons != null) {
                        val result = price * gallons
                        totalCostField.setText(String.format("%.2f", result))
                        totalCostField.setSelection(totalCostField.text.length)
                    }
                }
            }

            isUpdating = false
        }

        fun scheduleUpdate() {
            runnable?.let { handler.removeCallbacks(it) }
            runnable = Runnable { updateValues() }
            handler.postDelayed(runnable!!, 50)
        }

        // Watcher to watch price, gallons, totalCost text fields
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                val field = when {
                    priceField.hasFocus() -> priceField
                    gallonsField.hasFocus() -> gallonsField
                    totalCostField.hasFocus() -> totalCostField
                    else -> null
                } ?: return

                isUpdating = true

                val decimals = when (field) {
                    totalCostField -> 2
                    else -> 3
                }

                val formatted = formatDecimal(field.text.toString(), decimals)

                field.setText(formatted)
                field.setSelection(formatted.length)

                isUpdating = false

                // Track the last 2 fields edited
                lastEditedFields.remove(field)
                lastEditedFields.add(field)
                if (lastEditedFields.size > 2) lastEditedFields.removeFirst()

                scheduleUpdate()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        priceField.addTextChangedListener(watcher)
        gallonsField.addTextChangedListener(watcher)
        totalCostField.addTextChangedListener(watcher)

        // Vehicle spinner logic
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

        // Date picker logic
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

        // Save button
        btnAddFuel.setOnClickListener {
            val fuelLog = FuelLog(
                vehicleId = selectedVehicle,
                date = dateField.text.toString(),
                pricePerGallon = priceField.text.toString().toDoubleOrNull() ?: 0.0,
                gallons = gallonsField.text.toString().toDoubleOrNull() ?: 0.0,
                totalCost = totalCostField.text.toString().toDoubleOrNull() ?: 0.0,
                odometer = odometerField.text.toString().replace(" ", "").toIntOrNull() ?: 0,
                fillPercent = seekBar.progress
            )

            val success = fuelViewModel.addFuelLog(fuelLog)
            if (success) {
                (activity as MainActivity).switchFragment(VehicleListFragment())
            }
        }

        // Cancel Button
        btnCancelFuel.setOnClickListener {
            (activity as MainActivity).switchFragment(VehicleListFragment())
        }

        topAppBar.setNavigationOnClickListener {
            (activity as MainActivity).switchFragment(VehicleListFragment())
        }

        odometerField.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                val raw = s.toString().replace(Regex("[^\\d]"), "")
                val formattedInteger = if (raw.length > 3) {
                    "${raw.substring(0, raw.length - 3)} ${raw.takeLast(3)}"
                } else {
                    raw
                }

                odometerField.setText(formattedInteger)
                odometerField.setSelection(formattedInteger.length)

                isEditing = false
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).showBottomNav()
    }
}