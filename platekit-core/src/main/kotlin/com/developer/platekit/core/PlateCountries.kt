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
    /** Arabic equivalents, same order as [omanLetters] (verified against car.uic.bh:
     *  ا ب ح د ر س ط ل ك م و ى). Oman's physical plate shows both scripts stacked per
     *  letter cell, so the category input needs to accept either. */
    private val omanLettersArabic = listOf("ا", "ب", "ح", "د", "ر", "س", "ط", "ل", "ك", "م", "و", "ى")
    /** Public so both the input UI (auto-pairing whichever script was typed) and the
     *  plate preview (drawing the Arabic/Latin pair per cell) share one lookup table. */
    val omanLetterArabicByLatin: Map<String, String> = omanLetters.zip(omanLettersArabic).toMap()
    val omanLetterLatinByArabic: Map<String, String> = omanLettersArabic.zip(omanLetters).toMap()
    /** Explicit "nothing picked" option for Oman's letter dropdowns -- lets the user
     *  deliberately clear a letter instead of having to blindly backspace an
     *  AutoCompleteTextView (which reopens its suggestion list on every edit). */
    const val OMAN_NO_LETTER_OPTION = "None"
    private val egyptLetters = listOf("ا", "ب", "ت", "ث", "ج", "ح", "خ", "د", "ذ", "ر", "ز", "س", "ش", "ص", "ض", "ط", "ظ", "ع", "غ", "ف", "ق", "ك", "ل", "م", "ن", "ه", "و", "ى")
    private val uaeCodes = latinLetters + listOf("AA", "BB", "CC", "DD", "EE", "FF") + (1..55).map(Int::toString)
    /** Bahrain's plate category — a fixed 3-way type, not a letter/number code
     *  (verified against car.uic.bh: PRIVATE / DIPLOMAT / FOR HIRE). */
    private val bahrainCategories = listOf("PRIVATE", "DIPLOMAT", "FOR HIRE")
    /** Qatar's plate letter prefix — Q/T/R (verified against car.uic.bh), distinct from
     *  the vehicle-type-driven visual template in VehiclePlateTemplates. */
    private val qatarCodes = listOf("Q", "T", "R")

    /** UAE emirates — exposed publicly since UAE is also the one country with regionOptions. */
    val uaeStates = listOf("Abu Dhabi", "Dubai", "Sharjah", "Ajman", "Fujairah", "Ras Al-Khaimah", "Umm Al-Quwain")

    // countryId/nameAr below are the client's own country-master IDs (id/NameEN/NameAR),
    // not derived from `code` — kept as a literal lookup here so this stays a one-line
    // diff if the client ever renumbers their master list.
    val QATAR = PlateCountryDefinition(
        code = "QAT", displayName = "Qatar", categoryMode = PlateCategoryInputMode.SINGLE_DROPDOWN,
        categoryOptions = { qatarCodes },
        countryId = 35, nameAr = "قطر"
    )
    val BAHRAIN = PlateCountryDefinition(
        code = "BHR", displayName = "Bahrain", categoryMode = PlateCategoryInputMode.SINGLE_DROPDOWN,
        categoryOptions = { bahrainCategories },
        countryId = 7, nameAr = "البحرين"
    )
    val SAUDI_ARABIA = PlateCountryDefinition(
        code = "SAU", displayName = "Saudi Arabia", categoryMode = PlateCategoryInputMode.THREE_LETTERS,
        categoryOptions = { ksaLetters },
        countryId = 37, nameAr = "المملكة العربية السعودية"
    )
    val KUWAIT = PlateCountryDefinition(
        code = "KWT", displayName = "Kuwait", categoryMode = PlateCategoryInputMode.SINGLE_DROPDOWN,
        categoryOptions = { kuwaitCodes },
        countryId = 23, nameAr = "الكويت"
    )
    val UAE = PlateCountryDefinition(
        code = "UAE", displayName = "United Arab Emirates", categoryMode = PlateCategoryInputMode.SINGLE_DROPDOWN,
        categoryOptions = { uaeCodes }, regionOptions = uaeStates,
        countryId = 49, nameAr = "الإمارات العربية المتحدة"
    )
    val OMAN = PlateCountryDefinition(
        code = "OMN", displayName = "Oman", categoryMode = PlateCategoryInputMode.TWO_LETTERS,
        // Both scripts as separate selectable options (not a combined "B (ب)" string --
        // that would break AutoCompleteTextView's prefix-match filtering for Arabic input) --
        // plus the explicit "None" option so the (optional) second letter can be cleared.
        categoryOptions = { listOf(OMAN_NO_LETTER_OPTION) + omanLetters + omanLettersArabic },
        countryId = 32, nameAr = "عمان"
    )
    val EGYPT = PlateCountryDefinition(
        code = "EGY", displayName = "Egypt", categoryMode = PlateCategoryInputMode.THREE_LETTERS,
        categoryOptions = { egyptLetters },
        countryId = 13, nameAr = "مصر"
    )
    val JORDAN = PlateCountryDefinition(
        code = "JOR", displayName = "Jordan", categoryMode = PlateCategoryInputMode.SINGLE_DROPDOWN,
        categoryOptions = { jordanCodes },
        countryId = 22, nameAr = "الأردن"
    )

    /** All eight countries the app ships with today, in their original display order. */
    val gcc: List<PlateCountryDefinition> =
        listOf(BAHRAIN, SAUDI_ARABIA, KUWAIT, UAE, QATAR, OMAN, EGYPT, JORDAN)
}
