package com.example.fuelapp.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fuelapp.R
import com.example.fuelapp.model.FuelLog
import com.example.fuelapp.model.Vehicle
import java.text.SimpleDateFormat
import java.util.*

class FuelLogAdapter(
    private var logs: List<FuelLog>,
    private var vehicles: List<Vehicle>,
    private val onClick: (FuelLog) -> Unit
) : RecyclerView.Adapter<FuelLogAdapter.FuelLogViewHolder>() {

    private var vehicleNameMap: Map<String, String> = vehicles.associate { it.id to it.name }

    // Object that holds views for each card in the list
    class FuelLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvVehicleName: TextView = itemView.findViewById(R.id.tvVehicleName)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvMiles: TextView = itemView.findViewById(R.id.tvMiles)
        val tvGallons: TextView = itemView.findViewById(R.id.tvGallons)
        val tvCost: TextView = itemView.findViewById(R.id.tvCost)
        val tvMpg: TextView = itemView.findViewById(R.id.tvMpg)
    }

    // Create new View Holder
    // This is called when the RecyclerView needs a new ViewHolder to display an item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FuelLogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fuel_log, parent, false)
        return FuelLogViewHolder(view)
    }

    // Fills ViewHolder with data
    override fun onBindViewHolder(holder: FuelLogViewHolder, position: Int) {
        val log = logs[position]

        holder.tvVehicleName.text = vehicleNameMap[log.vehicleId]

        holder.tvDate.text = try {
            val inputFormat = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            inputFormat.parse(log.date)?.let { date -> outputFormat.format(date) } ?: "N/A"
        } catch (e: Exception) {
            Log.d("FuelLogAdapter", e.message ?: "Error parsing date")
            "N/A"
        }

        val mpg = getMPG(log)

        val previousLog = findPreviousLogForVehicle(log)
        val miles = previousLog?.let { log.odometer - it.odometer } ?: 0
        holder.tvMiles.text = miles.toString()

        holder.tvGallons.text = "%.1f".format(log.gallons)
        holder.tvCost.text = "$%.2f".format(log.totalCost)
        holder.tvMpg.text = "%.1f".format(mpg)

        holder.itemView.setOnClickListener { onClick(log) }
    }

    override fun getItemCount(): Int = logs.size

    fun updateLogs(newLogs: List<FuelLog>) {
        val inputFormat = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
        // Sort fuel log list by time descending
        logs = newLogs.sortedByDescending { log ->
            try {
                inputFormat.parse(log.date)
            } catch (e: Exception) {
                Date(0) // In case date can not be parsed
            }
        }
        notifyDataSetChanged()
    }

    fun updateVehicles(newVehicles: List<Vehicle>) {
        vehicles = newVehicles
        vehicleNameMap = vehicles.associate { it.id to it.name }
        notifyDataSetChanged()
    }

    private fun getMPG(log: FuelLog):  Double{
        val previousLog = findPreviousLogForVehicle(log)
        val miles = previousLog?.let { log.odometer - it.odometer } ?: 0
        val mpg = if (miles > 0 && log.gallons > 0) miles.toDouble() / log.gallons else 0.0
        return mpg
    }

    // Gets previous fuel log for the same vehicle where ODOMETER MUST BE LOWER
    private fun findPreviousLogForVehicle(log: FuelLog): FuelLog? {
        return logs
            .filter { it.vehicleId == log.vehicleId && it.odometer < log.odometer }
            .maxByOrNull { it.odometer }
    }
}