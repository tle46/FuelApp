package com.example.fuelapp.model

data class FuelLog(
    val id: String = "",
    val vehicleId: String,
    val date: String,
    val pricePerGallon: Double,
    val gallons: Double,
    val totalCost: Double,
    val odometer: Int,
    val fillPercent: Int = 100
)