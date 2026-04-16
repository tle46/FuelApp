package com.example.fuelapp.model

data class VehicleStats(
    val avgMpg: Double,
    val totalMiles: Double,
    val totalFuel: Double,
    val totalCost: Double,
    val totalLogs: Int,
    val lastMpg: Double,

    val odometerTimeData: List<Pair<Long, Float>>,
    val mpgOdometerData: List<Pair<Float, Float>>
)