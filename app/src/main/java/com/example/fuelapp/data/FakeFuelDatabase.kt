package com.example.fuelapp.data

import com.example.fuelapp.model.FuelLog

object FakeFuelDatabase {

    private val fuelLogs = mutableListOf(
        FuelLog("1", "p5J9V5ry5e7dXQDZ7nkT", "03/20/2026 08:15", 3.49, 12.0, 41.88, 120000, 100),
        FuelLog("2", "p5J9V5ry5e7dXQDZ7nkT", "03/22/2026 09:30", 3.59, 10.5, 37.70, 120350, 90),
        FuelLog("3", "p5J9V5ry5e7dXQDZ7nkT", "03/24/2026 07:45", 3.55, 11.2, 39.76, 120700, 100),
        FuelLog("4", "p5J9V5ry5e7dXQDZ7nkT", "03/25/2026 12:10", 3.65, 9.8, 35.77, 121050, 85),
        FuelLog("5", "p5J9V5ry5e7dXQDZ7nkT", "03/26/2026 18:20", 3.69, 10.0, 36.90, 121400, 95),

        FuelLog("6", "F08LZuN2FYQxr8fHOC90", "03/20/2026 10:00", 3.29, 8.5, 27.97, 90000, 80),
        FuelLog("7", "F08LZuN2FYQxr8fHOC90", "03/21/2026 14:25", 3.35, 9.0, 30.15, 90300, 85),
        FuelLog("8", "F08LZuN2FYQxr8fHOC90", "03/23/2026 16:40", 3.39, 8.8, 29.83, 90650, 90),
        FuelLog("9", "F08LZuN2FYQxr8fHOC90", "03/24/2026 11:15", 3.45, 9.3, 32.09, 91000, 100),
        FuelLog("10", "F08LZuN2FYQxr8fHOC90", "03/26/2026 19:05", 3.49, 8.7, 30.36, 91350, 95)
    )

    fun getFuelLogs(): List<FuelLog> {
        return fuelLogs
    }

    fun getFuelLogById(id: String): FuelLog? {
        return fuelLogs.find { it.id == id }
    }

    fun addFuelLog(fuelLog: FuelLog) {
        fuelLogs.add(fuelLog)
    }

    fun updateFuelLog(fuelLog: FuelLog) {
        val index = fuelLogs.indexOfFirst { it.id == fuelLog.id }
        if (index != -1) {
            fuelLogs[index] = fuelLog
        }
    }

    fun deleteFuelLogById(id: String) {
        fuelLogs.removeIf { it.id == id }
    }

    fun clear() {
        fuelLogs.clear()
    }
}