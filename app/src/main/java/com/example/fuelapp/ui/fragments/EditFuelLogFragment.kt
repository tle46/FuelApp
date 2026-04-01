package com.example.fuelapp.ui.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
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

class EditFuelLogFragment : Fragment() {

    private lateinit var dateField: EditText
    private lateinit var btnUpdateFuel: Button
    private lateinit var btnDeleteFuel: Button
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var spinnerVehicle: Spinner
    private lateinit var priceField: EditText
    private lateinit var gallonsField: EditText
    private lateinit var totalCostField: EditText
    private lateinit var odometerField: EditText
    private lateinit var seekBar: SeekBar

    private val fuelViewModel: FuelListViewModel by activityViewModels()
    private val vehicleViewModel: VehicleListViewModel by activityViewModels()
    private var vehicleList: List<Vehicle> = emptyList()
    private var selectedVehicle: String = ""
    private val calendar = Calendar.getInstance()

    private lateinit var fuelLog: FuelLog

    private var isUpdating = false
    private val lastEditedFields: LinkedList<EditText> = LinkedList()
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).hideBottomNav()
        val view = inflater.inflate(R.layout.fragment_edit_fuel_log, container, false)

        // Retrieve FuelLog from arguments
        fuelViewModel.selectedFuelLog.observe(viewLifecycleOwner) { log ->
            log?.let {
                fuelLog = it
                populateFields()
            }
        }

        // Initialize views
        dateField = view.findViewById(R.id.etFuelDate)
        btnUpdateFuel = view.findViewById(R.id.btnUpdateFuel)
        btnDeleteFuel = view.findViewById(R.id.btnDeleteFuel)
        topAppBar = view.findViewById(R.id.topAppBar)
        spinnerVehicle = view.findViewById(R.id.spinnerVehicle)
        priceField = view.findViewById(R.id.etFuelPrice)
        gallonsField = view.findViewById(R.id.etFuelAmount)
        totalCostField = view.findViewById(R.id.etTotalCost)
        odometerField = view.findViewById(R.id.etOdometer)
        seekBar = view.findViewById(R.id.seekBarFillPercent)

        setupVehicleSpinner()
        setupTextWatchers()
        setupSeekBar()
        setupDatePicker()
        setupButtons()
        setupOdometerFormatter()

        return view
    }

    private fun setupVehicleSpinner() {
        vehicleViewModel.vehicles.observe(viewLifecycleOwner) { list ->
            vehicleList = list
            val names = vehicleList.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerVehicle.adapter = adapter

            // Pre-select current vehicle
            val pos = vehicleList.indexOfFirst { it.id == fuelLog.vehicleId }
            if (pos >= 0) {
                spinnerVehicle.setSelection(pos)
                selectedVehicle = vehicleList[pos].id
            }
        }

        spinnerVehicle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position in vehicleList.indices) {
                    selectedVehicle = vehicleList[position].id
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedVehicle = ""
            }
        }
    }

    private fun populateFields() {
        dateField.setText(fuelLog.date)
        priceField.setText(fuelLog.pricePerGallon.toString())
        gallonsField.setText(fuelLog.gallons.toString())
        totalCostField.setText(fuelLog.totalCost.toString())
        odometerField.setText(fuelLog.odometer.toString())
        seekBar.progress = fuelLog.fillPercent

        // Initialize lastEditedFields so that auto calculation works right away
        lastEditedFields.clear()
        lastEditedFields.add(priceField)
        lastEditedFields.add(gallonsField)
    }

    private fun setupTextWatchers() {
        fun formatDecimal(value: String, decimals: Int): String {
            val digits = value.replace(Regex("\\D"), "")
            if (digits.isEmpty()) return ""
            val padded = digits.padStart(decimals + 1, '0')
            val integerPart = padded.dropLast(decimals)
            val decimalPart = padded.takeLast(decimals)
            return "${integerPart.toInt()}.$decimalPart"
        }

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
                val decimals = if (field == totalCostField) 2 else 3
                val formatted = formatDecimal(field.text.toString(), decimals)
                field.setText(formatted)
                field.setSelection(formatted.length)

                lastEditedFields.remove(field)
                lastEditedFields.add(field)
                if (lastEditedFields.size > 2) lastEditedFields.removeFirst()

                scheduleUpdate()
                isUpdating = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        priceField.addTextChangedListener(watcher)
        gallonsField.addTextChangedListener(watcher)
        totalCostField.addTextChangedListener(watcher)
    }

    private fun scheduleUpdate() {
        runnable?.let { handler.removeCallbacks(it) }
        runnable = Runnable { updateCalculatedField() }
        handler.postDelayed(runnable!!, 50)
    }

    private fun updateCalculatedField() {
        if (lastEditedFields.size < 2) return

        if (isUpdating) return
        isUpdating = true

        val price = priceField.text.toString().toDoubleOrNull()
        val gallons = gallonsField.text.toString().toDoubleOrNull()
        val total = totalCostField.text.toString().toDoubleOrNull()
        val allFields = listOf(priceField, gallonsField, totalCostField)
        val missingField = allFields.first { it !in lastEditedFields }

        when (missingField) {
            priceField -> if (gallons != null && total != null && gallons != 0.0) {
                val result = total / gallons
                priceField.setText(String.format("%.3f", result))
                priceField.setSelection(priceField.text.length)
            }
            gallonsField -> if (price != null && total != null && price != 0.0) {
                val result = total / price
                gallonsField.setText(String.format("%.3f", result))
                gallonsField.setSelection(gallonsField.text.length)
            }
            totalCostField -> if (price != null && gallons != null) {
                val result = price * gallons
                totalCostField.setText(String.format("%.2f", result))
                totalCostField.setSelection(totalCostField.text.length)
            }
        }

        isUpdating = false
    }

    private fun setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    private fun setupDatePicker() {
        val formatter = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())

        dateField.setOnClickListener {
            val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                calendar.set(year, month, day)
                val timeListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    dateField.setText(formatter.format(calendar.time))
                }
                TimePickerDialog(requireContext(), timeListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            }
            DatePickerDialog(requireContext(), dateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupButtons() {
        btnUpdateFuel.setOnClickListener {
            if (updateFuelLog()) {
                Toast.makeText(requireContext(), "Fuel log updated", Toast.LENGTH_SHORT).show()
                (activity as MainActivity).switchFragment(FuelListFragment())
            }
        }

        btnDeleteFuel.setOnClickListener {
            fuelViewModel.deleteFuelLog(fuelLog.id)
            Toast.makeText(requireContext(), "Fuel log deleted", Toast.LENGTH_SHORT).show()
            (activity as MainActivity).switchFragment(FuelListFragment())
        }

        topAppBar.setNavigationOnClickListener {
            (activity as MainActivity).switchFragment(FuelListFragment())
        }
    }

    private fun updateFuelLog(): Boolean {
        val date = dateField.text.toString()
        val price = priceField.text.toString().toDoubleOrNull()
        val gallons = gallonsField.text.toString().toDoubleOrNull()
        val total = totalCostField.text.toString().toDoubleOrNull()
        val odometer = odometerField.text.toString().replace(" ", "").toIntOrNull()
        val fillPercent = seekBar.progress

        if (date.isBlank() || price == null || gallons == null || total == null || odometer == null) {
            Toast.makeText(requireContext(), "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return false
        }

        val updatedLog = fuelLog.copy(
            date = date,
            vehicleId = selectedVehicle,
            pricePerGallon = price,
            gallons = gallons,
            totalCost = total,
            odometer = odometer,
            fillPercent = fillPercent
        )

        fuelViewModel.updateFuelLog(updatedLog)
        return true
    }

    private fun setupOdometerFormatter() {
        odometerField.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true
                val raw = s.toString().replace(Regex("[^\\d]"), "")
                val formatted = if (raw.length > 3) "${raw.dropLast(3)} ${raw.takeLast(3)}" else raw
                odometerField.setText(formatted)
                odometerField.setSelection(formatted.length)
                isEditing = false
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).showBottomNav()
    }
}