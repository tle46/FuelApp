package com.example.fuelapp.util

object FuelParser {

    fun parse(text: String): Pair<Double?, Double?> {
        data class Match(val value: String, val index: Int, val priority: Int)

        val priceRegexes = listOf(
            """(?<!\d)\d+\.\d{2}(?!\d)""".toRegex(), // highest priority
            """(?<!\d)\d{4}(?!\d)""".toRegex(),
            """(?<!\d)\d{5}(?!\d)""".toRegex(),
            """(?<!\d)\d{3}(?!\d)""".toRegex()
        )

        val fuelRegexes = listOf(
            """(?<!\d)\d+\.\d{3}(?!\d)""".toRegex(), // highest priority
            """(?<!\d)\d{5}(?!\d)""".toRegex(),
            """(?<!\d)\d{4}(?!\d)""".toRegex()
        )

        // List all candidates and sort by priority
        val priceCandidates = priceRegexes.flatMapIndexed { priority, regex ->
            regex.findAll(text).map { Match(it.value, it.range.first, priority) }
        }.sortedWith(compareBy<Match> { it.priority })

        val fuelCandidates = fuelRegexes.flatMapIndexed { priority, regex ->
            regex.findAll(text).map { Match(it.value, it.range.first, priority) }
        }.sortedWith(compareBy<Match> { it.priority })

        var rawCost: String? = null
        var rawFuel: String? = null

        // See if any matches where price before fuel
        for (p in priceCandidates) {
            for (f in fuelCandidates) {
                if (p.value != f.value && p.index < f.index) {
                    rawCost = p.value
                    rawFuel = f.value
                    break
                }
            }
            if (rawCost != null) break
        }

        // Pick any distinct pair if ordering failed
        if (rawCost == null) {
            for (p in priceCandidates) {
                for (f in fuelCandidates) {
                    if (p.value != f.value) {
                        rawCost = p.value
                        rawFuel = f.value
                        break
                    }
                }
                if (rawCost != null) break
            }
        }

        if (rawCost == null || rawFuel == null) return Pair(null, null)

        // Normalize decimals
        if (!rawCost.contains(".")) {
            rawCost = rawCost.dropLast(2) + "." + rawCost.takeLast(2)
        }
        if (!rawFuel.contains(".")) {
            rawFuel = rawFuel.dropLast(3) + "." + rawFuel.takeLast(3)
        }

        val cost = rawCost.toDoubleOrNull()
        val fuel = rawFuel.toDoubleOrNull()

        return if (isValid(cost, fuel)) Pair(cost, fuel) else Pair(null, null)
    }
    private fun isValid(price: Double?, fuel: Double?): Boolean {
        return price != null &&
                fuel != null &&
                price in 0.0..999.9 &&
                fuel in 1.0..999.9 &&
                price != fuel
    }
}