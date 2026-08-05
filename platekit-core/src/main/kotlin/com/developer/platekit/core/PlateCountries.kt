package com.developer.platekit.core

/**
 * Built-in GCC-region country definitions, ported as-is from the original
 * SearchVehicleFragment implementation (same codes, same category rules, same
 * option lists) so behavior is unchanged after the extraction.
 */
object PlateCountries {
    private val kuwaitCodes = (1..99).map(Int::toString)
    private val jordanCodes = (100..999).map(Int::toString)
    /** Plain A-Z, exposed publicly: also used directly for the UAE category dropdown
     *  outside Abu Dhabi (mirrors the original fragment's conditional logic). */
    val singleLetters = ('A'..'Z').map(Char::toString)
    private val latinLetters = singleLetters
    private val ksaLetters = listOf("A", "B", "J", "D", "R", "S", "X", "T", "E", "G", "K", "L", "Z", "N", "H", "U", "V")
    private val omanLetters = listOf("A", "B", "H", "D", "R", "S", "T", "L", "K", "M", "W", "Y")
    private val egyptLetters = listOf("ا", "ب", "ت", "ث", "ج", "ح", "خ", "د", "ر", "ز", "س", "ش", "ص", "ض", "ط", "ظ", "ع", "غ", "ف", "ق", "ك", "ل", "م", "ن", "ه", "و", "ي")
    private val uaeCodes = latinLetters + listOf("AA", "BB", "CC", "DD", "EE", "FF") + (1..55).map(Int::toString)

    /** UAE emirates — exposed publicly since UAE is also the one country with regionOptions. */
    val uaeStates = listOf("Abu Dhabi", "Dubai", "Sharjah", "Ajman", "Fujairah", "Ras Al-Khaimah", "Umm Al-Quwain")

    val QATAR = PlateCountryDefinition(
        code = "QAT", displayName = "Qatar", categoryMode = PlateCategoryInputMode.NONE
    )
    val BAHRAIN = PlateCountryDefinition(
        code = "BHR", displayName = "Bahrain", categoryMode = PlateCategoryInputMode.NONE
    )
    val SAUDI_ARABIA = PlateCountryDefinition(
        code = "SAU", displayName = "Saudi Arabia", categoryMode = PlateCategoryInputMode.THREE_LETTERS,
        categoryOptions = { ksaLetters }
    )
    val KUWAIT = PlateCountryDefinition(
        code = "KWT", displayName = "Kuwait", categoryMode = PlateCategoryInputMode.SINGLE_DROPDOWN,
        categoryOptions = { kuwaitCodes }
    )
    val UAE = PlateCountryDefinition(
        code = "UAE", displayName = "United Arab Emirates", categoryMode = PlateCategoryInputMode.SINGLE_DROPDOWN,
        categoryOptions = { uaeCodes }, regionOptions = uaeStates
    )
    val OMAN = PlateCountryDefinition(
        code = "OMN", displayName = "Oman", categoryMode = PlateCategoryInputMode.TWO_LETTERS,
        categoryOptions = { omanLetters }
    )
    val EGYPT = PlateCountryDefinition(
        code = "EGY", displayName = "Egypt", categoryMode = PlateCategoryInputMode.THREE_LETTERS,
        categoryOptions = { egyptLetters }
    )
    val JORDAN = PlateCountryDefinition(
        code = "JOR", displayName = "Jordan", categoryMode = PlateCategoryInputMode.SINGLE_DROPDOWN,
        categoryOptions = { jordanCodes }
    )

    /** All eight countries the app ships with today, in their original display order. */
    val gcc: List<PlateCountryDefinition> =
        listOf(BAHRAIN, SAUDI_ARABIA, KUWAIT, UAE, QATAR, OMAN, EGYPT, JORDAN)
}
