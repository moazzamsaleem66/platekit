package com.developer.platekit.core

sealed class PlateValidationResult {
    object Valid : PlateValidationResult()
    data class Invalid(val reason: PlateValidationReason) : PlateValidationResult()
}

/** Host apps map each reason to their own localized message (see FlagProvider-style pattern). */
enum class PlateValidationReason {
    NUMBER_INVALID_OR_TOO_LONG,
    CATEGORY_NOT_SELECTED,
    LETTERS_INCOMPLETE,
    REGION_NOT_SELECTED
}

/**
 * Ported as-is from the original `validateGccPlate`. In default-country mode
 * ([selectedCountry] == null) this always returns [PlateValidationResult.Valid] —
 * matching the legacy behavior of not enforcing extra rules on the raw digits.
 *
 * Contract: callers in "alternate country" mode are expected to always have a country
 * selected (the UI defaults to the first enabled country the moment that mode is
 * entered) — this validator does not special-case "alternate mode, no country picked".
 */
object PlateNumberValidator {
    fun validate(
        selectedCountry: PlateCountryDefinition?,
        rawNumber: String,
        maximumLength: Int,
        selectedCategoryCode: String = "",
        selectedLetters: String = "",
        selectedRegion: String = ""
    ): PlateValidationResult {
        val country = selectedCountry ?: return PlateValidationResult.Valid

        if (rawNumber.isBlank() || rawNumber.length > maximumLength || !rawNumber.all(Char::isDigit)) {
            return PlateValidationResult.Invalid(PlateValidationReason.NUMBER_INVALID_OR_TOO_LONG)
        }

        when (country.categoryMode) {
            PlateCategoryInputMode.SINGLE_DROPDOWN -> if (selectedCategoryCode.isBlank()) {
                return PlateValidationResult.Invalid(PlateValidationReason.CATEGORY_NOT_SELECTED)
            }
            PlateCategoryInputMode.TWO_LETTERS -> if (selectedLetters.length != 2) {
                return PlateValidationResult.Invalid(PlateValidationReason.LETTERS_INCOMPLETE)
            }
            PlateCategoryInputMode.THREE_LETTERS -> if (selectedLetters.length != 3) {
                return PlateValidationResult.Invalid(PlateValidationReason.LETTERS_INCOMPLETE)
            }
            PlateCategoryInputMode.NONE -> Unit
        }

        if (country.regionOptions.isNotEmpty() && selectedRegion.isBlank()) {
            return PlateValidationResult.Invalid(PlateValidationReason.REGION_NOT_SELECTED)
        }

        return PlateValidationResult.Valid
    }
}
