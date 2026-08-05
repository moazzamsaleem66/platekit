package com.developer.platekit.core

/**
 * Builds the single formatted plate-number string that goes to the search/verify API,
 * from the raw digits plus whatever category/letters the user picked.
 *
 * Ported as-is from the original `buildPlateNumberForSearch`: in default-country mode
 * ([selectedCountry] == null) the raw digits are returned untouched; otherwise the
 * category/letters are prefixed, joined by "-".
 */
object PlateNumberBuilder {
    fun build(
        selectedCountry: PlateCountryDefinition?,
        rawNumber: String,
        selectedCategoryCode: String = "",
        selectedLetters: String = ""
    ): String {
        val number = rawNumber.trim()
        val country = selectedCountry ?: return number
        return when (country.categoryMode) {
            PlateCategoryInputMode.SINGLE_DROPDOWN -> listOf(selectedCategoryCode, number)
            PlateCategoryInputMode.TWO_LETTERS, PlateCategoryInputMode.THREE_LETTERS -> listOf(selectedLetters, number)
            PlateCategoryInputMode.NONE -> listOf(number)
        }.filter(String::isNotBlank).joinToString("-")
    }
}
