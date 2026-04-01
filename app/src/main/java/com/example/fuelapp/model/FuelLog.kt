package com.example.fuelapp.model

data class FuelLog(
    var id: String = "",
    var vehicleId: String = "",
    var userId: String = "",
    var date: String = "",
    var pricePerGallon: Double = 0.0,
    var gallons: Double = 0.0,
    var totalCost: Double = 0.0,
    var odometer: Int = 0,
    var fillPercent: Int = 100
)