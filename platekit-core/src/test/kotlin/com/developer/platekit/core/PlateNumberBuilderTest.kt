package com.developer.platekit.core

import org.junit.Assert.assertEquals
import org.junit.Test

class PlateNumberBuilderTest {

    @Test
    fun `default country mode returns raw digits untouched`() {
        val result = PlateNumberBuilder.build(selectedCountry = null, rawNumber = "  123456 ")
        assertEquals("123456", result)
    }

    @Test
    fun `single dropdown country prefixes the category code`() {
        val result = PlateNumberBuilder.build(
            selectedCountry = PlateCountries.KUWAIT,
            rawNumber = "12345",
            selectedCategoryCode = "7"
        )
        assertEquals("7-12345", result)
    }

    @Test
    fun `three letters country prefixes the letters`() {
        val result = PlateNumberBuilder.build(
            selectedCountry = PlateCountries.SAUDI_ARABIA,
            rawNumber = "1234",
            selectedLetters = "TNJ"
        )
        assertEquals("TNJ-1234", result)
    }

    @Test
    fun `two letters country prefixes the letters`() {
        val result = PlateNumberBuilder.build(
            selectedCountry = PlateCountries.OMAN,
            rawNumber = "1234",
            selectedLetters = "AB"
        )
        assertEquals("AB-1234", result)
    }

    @Test
    fun `no-category alternate country (Qatar picked inside the picker) returns raw digits`() {
        val result = PlateNumberBuilder.build(
            selectedCountry = PlateCountries.QATAR,
            rawNumber = "998877"
        )
        assertEquals("998877", result)
    }

    @Test
    fun `blank category or letters are dropped rather than leaving a dangling dash`() {
        val result = PlateNumberBuilder.build(
            selectedCountry = PlateCountries.KUWAIT,
            rawNumber = "12345",
            selectedCategoryCode = ""
        )
        assertEquals("12345", result)
    }
}
