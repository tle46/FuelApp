package com.example.fuelapp.model
import java.util.Date

data class FuelLog(
    var id: String = "",
    var vehicleId: String = "",
    var userId: String = "",
    val date: Date = Date(),
    var pricePerGallon: Double = 0.0,
    var gallons: Double = 0.0,
    var totalCost: Double = 0.0,
    var odometer: Int = 0,
    var fillPercent: Int = 100
)