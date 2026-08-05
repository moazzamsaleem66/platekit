package com.developer.platekit.core

import org.junit.Assert.assertEquals
import org.junit.Test

class PlateNumberValidatorTest {

    @Test
    fun `default country mode is always valid regardless of input`() {
        val result = PlateNumberValidator.validate(
            selectedCountry = null,
            rawNumber = "not-even-digits",
            maximumLength = 6
        )
        assertEquals(PlateValidationResult.Valid, result)
    }

    @Test
    fun `blank number is invalid in alternate country mode`() {
        val result = PlateNumberValidator.validate(
            selectedCountry = PlateCountries.QATAR,
            rawNumber = "",
            maximumLength = 6
        )
        assertEquals(PlateValidationResult.Invalid(PlateValidationReason.NUMBER_INVALID_OR_TOO_LONG), result)
    }

    @Test
    fun `number longer than max length is invalid`() {
        val result = PlateNumberValidator.validate(
            selectedCountry = PlateCountries.QATAR,
            rawNumber = "1234567",
            maximumLength = 6
        )
        assertEquals(PlateValidationResult.Invalid(PlateValidationReason.NUMBER_INVALID_OR_TOO_LONG), result)
    }

    @Test
    fun `non-digit number is invalid`() {
        val result = PlateNumberValidator.validate(
            selectedCountry = PlateCountries.QATAR,
            rawNumber = "12A456",
            maximumLength = 6
        )
        assertEquals(PlateValidationResult.Invalid(PlateValidationReason.NUMBER_INVALID_OR_TOO_LONG), result)
    }

    @Test
    fun `single dropdown country requires a category code`() {
        val result = PlateNumberValidator.validate(
            selectedCountry = PlateCountries.KUWAIT,
            rawNumber = "12345",
            maximumLength = 5,
            selectedCategoryCode = ""
        )
        assertEquals(PlateValidationResult.Invalid(PlateValidationReason.CATEGORY_NOT_SELECTED), result)
    }

    @Test
    fun `three letters country requires exactly three letters`() {
        val incomplete = PlateNumberValidator.validate(
            selectedCountry = PlateCountries.SAUDI_ARABIA,
            rawNumber = "1234",
            maximumLength = 4,
            selectedLetters = "TN"
        )
        assertEquals(PlateValidationResult.Invalid(PlateValidationReason.LETTERS_INCOMPLETE), incomplete)

        val complete = PlateNumberValidator.validate(
            selectedCountry = PlateCountries.SAUDI_ARABIA,
            rawNumber = "1234",
            maximumLength = 4,
            selectedLetters = "TNJ"
        )
        assertEquals(PlateValidationResult.Valid, complete)
    }

    @Test
    fun `two letters country requires exactly two letters`() {
        val result = PlateNumberValidator.validate(
            selectedCountry = PlateCountries.OMAN,
            rawNumber = "1234",
            maximumLength = 4,
            selectedLetters = "A"
        )
        assertEquals(PlateValidationResult.Invalid(PlateValidationReason.LETTERS_INCOMPLETE), result)
    }

    @Test
    fun `UAE requires a region once category is satisfied`() {
        val result = PlateNumberValidator.validate(
            selectedCountry = PlateCountries.UAE,
            rawNumber = "12345",
            maximumLength = 5,
            selectedCategoryCode = "A",
            selectedRegion = ""
        )
        assertEquals(PlateValidationResult.Invalid(PlateValidationReason.REGION_NOT_SELECTED), result)
    }

    @Test
    fun `UAE is valid once category and region are both set`() {
        val result = PlateNumberValidator.validate(
            selectedCountry = PlateCountries.UAE,
            rawNumber = "12345",
            maximumLength = 5,
            selectedCategoryCode = "A",
            selectedRegion = "Dubai"
        )
        assertEquals(PlateValidationResult.Valid, result)
    }

    @Test
    fun `no-category country (Bahrain) only checks the number`() {
        val result = PlateNumberValidator.validate(
            selectedCountry = PlateCountries.BAHRAIN,
            rawNumber = "123456",
            maximumLength = 6
        )
        assertEquals(PlateValidationResult.Valid, result)
    }
}
