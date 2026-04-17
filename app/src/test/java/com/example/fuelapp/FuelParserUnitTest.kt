package com.example.fuelapp

import com.example.fuelapp.util.FuelParser
import org.junit.Assert.*
import org.junit.Test

class FuelParserUnitTest {

    @Test
    fun parsesDecimal() {
        val text = "ldfkier dkflsd f Total: 12.34 fkjdfsdf Gallons: 4.567 kdfsdkjfhkjdf"

        val result = FuelParser.parse(text)

        assertEquals(12.34, result.first)
        assertEquals(4.567, result.second)
    }

    @Test
    fun parsesWithoutDecimals() {
        val text = "fdfdsfgrTotal hrehher1234 hrehrhGallons 4567herhreh"

        val result = FuelParser.parse(text)

        assertEquals(12.34, result.first)
        assertEquals(4.567, result.second)
    }

    @Test
    fun parseNoMatches() {
        val text = "This should not match anything dlsfkajldfkadskfa"

        val result = FuelParser.parse(text)

        assertNull(result.first)
        assertNull(result.second)
    }

    @Test
    fun ignoresSamePriceAndFuel() {
        val text = ";aldskfj;asdlfkj 1234dflkajdsfhlakdsjfldfs 1234"

        val result = FuelParser.parse(text)

        assertNull(result.first)
        assertNull(result.second)
    }

    @Test
    fun picksBestPriority() {
        val text = "1234 44354 Total 99999 12.34 Gallons 4.567 Extra 123 3049 830493 84273"

        val result = FuelParser.parse(text)

        // Best value is 12.34, 4.567
        assertEquals(12.34, result.first)
        assertEquals(4.567, result.second)
    }

    @Test
    fun returnsNullWhenOneValue() {
        val text = "Total 12.34 only"

        val result = FuelParser.parse(text)

        assertNull(result.first)
        assertNull(result.second)
    }
}