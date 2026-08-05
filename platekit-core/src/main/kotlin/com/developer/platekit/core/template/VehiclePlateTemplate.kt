package com.developer.platekit.core.template

// Deliberately no android.graphics.Color import: this module is pure Kotlin/JVM with
// zero Android dependency, so colors are plain ARGB Int constants instead. Values match
// android.graphics.Color.WHITE/BLACK/TRANSPARENT/RED exactly, so rendering is unchanged.
private const val ARGB_WHITE = 0xFFFFFFFF.toInt()
private const val ARGB_BLACK = 0xFF000000.toInt()
private const val ARGB_TRANSPARENT = 0x00000000
private const val ARGB_RED = 0xFFFF0000.toInt()

enum class VehiclePlateLayout { QATAR, QATAR_DIPLOMATIC, QATAR_POLICE, QATAR_ISF, SAUDI, UAE_DUBAI, UAE_ABU_DHABI, UAE_EMIRATE, KUWAIT, KUWAIT_DIPLOMATIC, KUWAIT_SQUARE, KUWAIT_LONG, KUWAIT_SMALL, BAHRAIN, OMAN, GENERIC }

data class VehiclePlateTemplate(
    val layout: VehiclePlateLayout = VehiclePlateLayout.GENERIC,
    val backgroundColor: Int = ARGB_WHITE,
    val textColor: Int = ARGB_BLACK,
    val badgeColor: Int = ARGB_WHITE,
    val badgeTextColor: Int = ARGB_BLACK,
    val badgeTop: String,
    val badgeBottom: String = "",
    val headerText: String = "",
    val headerColor: Int = ARGB_TRANSPARENT,
    val headerTextColor: Int = ARGB_WHITE,
    val footerText: String = "",
    val footerTextColor: Int = textColor,
    val showCategory: Boolean = false,
    val categoryTextColor: Int = textColor,
    val numberMaxLength: Int = 6,
    val widthDp: Int = 238,
    val heightDp: Int = 92
)

/**
 * Resolves a country/vehicle-type combination to a rich visual [VehiclePlateTemplate].
 * Ported as-is from the original implementation — same layouts, same colors, same
 * string-matching rules for Qatar's sub-types — just relocated and Android-free.
 */
object VehiclePlateTemplates {
    private const val MAROON = 0xFF750014.toInt()
    private const val QATAR_PLATE_FACE = 0xFFE7E8E6.toInt()
    private const val ORANGE = 0xFFF28C00.toInt()
    private const val GRAY = 0xFFB8BCC5.toInt()
    private const val DARK_RED = 0xFF8B1E2D.toInt()
    private const val POLICE_BLUE = 0xFF168BC4.toInt()
    private const val ISF_YELLOW = 0xFFE7AA18.toInt()
    private const val ISF_BROWN = 0xFF6C3A12.toInt()
    private const val BAHRAIN_BLUE = 0xFF153B9E.toInt()
    private const val OMAN_YELLOW = 0xFFFFD500.toInt()
    private const val UAE_RED = 0xFFC8102E.toInt()
    private const val KUWAIT_GREEN = 0xFF006633.toInt()
    private const val KUWAIT_BLUE = 0xFF0033AA.toInt()
    private const val KUWAIT_PINK = 0xFFF06292.toInt()
    private const val KUWAIT_PURPLE = 0xFF9C27B0.toInt()
    private const val KUWAIT_ORANGE = 0xFFFF9800.toInt()
    private const val KUWAIT_YELLOW = 0xFFFFD500.toInt()
    private const val KUWAIT_BEIGE = 0xFFC6BC96.toInt()
    private const val KUWAIT_GOV_BLUE = 0xFF5897D0.toInt()

    fun resolve(countryCode: String, region: String = "", vehicleTypeCode: String = "", vehicleTypeName: String = ""): VehiclePlateTemplate {
        return when (countryCode.uppercase()) {
            "QAT" -> qatar(vehicleTypeCode, vehicleTypeName)
            "BHR" -> VehiclePlateTemplate(VehiclePlateLayout.BAHRAIN, textColor = BAHRAIN_BLUE, badgeTextColor = BAHRAIN_BLUE, badgeTop = "BAHRAIN  البحرين", numberMaxLength = 6, widthDp = 230, heightDp = 94)
            "KWT" -> kuwait(vehicleTypeCode, vehicleTypeName)
            "OMN" -> VehiclePlateTemplate(VehiclePlateLayout.OMAN, backgroundColor = OMAN_YELLOW, badgeColor = OMAN_YELLOW, badgeTop = "عُمان", badgeBottom = "OMAN", showCategory = true, numberMaxLength = 4, widthDp = 360, heightDp = 78)
            "SAU", "KSA" -> VehiclePlateTemplate(VehiclePlateLayout.SAUDI, badgeTop = "السعودية", badgeBottom = "KSA", showCategory = true, numberMaxLength = 4, widthDp = 260, heightDp = 150)
            "UAE" -> uae(region)
            else -> VehiclePlateTemplate(VehiclePlateLayout.GENERIC, badgeTop = countryCode.uppercase(), badgeBottom = region, showCategory = true)
        }
    }

    private fun kuwait(code: String, name: String): VehiclePlateTemplate {
        val key = "$code $name".uppercase()
        return when {
            key.contains("PRIVATE (LONG)") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT_LONG, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE, ARGB_BLACK,
                badgeTop = "KUWAIT", headerText = "10",
                showCategory = true, numberMaxLength = 6, widthDp = 400, heightDp = 84
            )
            key.contains("PRIVATE (SMALL)") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT_SMALL, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE, ARGB_BLACK,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "10",
                showCategory = true, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("PRIVATE") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE, ARGB_BLACK,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "10", showCategory = true, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("PUBLIC (LONG)") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT_LONG, KUWAIT_YELLOW, ARGB_BLACK, KUWAIT_YELLOW, ARGB_BLACK,
                badgeTop = "KUWAIT", headerText = "90",
                showCategory = true, numberMaxLength = 6, widthDp = 400, heightDp = 84
            )
            key.contains("PUBLIC (SMALL)") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT_SMALL, KUWAIT_YELLOW, ARGB_BLACK, KUWAIT_YELLOW, ARGB_BLACK,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "90",
                showCategory = true, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("MOTORCYCLE") && !key.contains("POLICE") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT_SQUARE, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE, ARGB_BLACK,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "دولة الكويت", footerText = "دراجة",
                showCategory = true, numberMaxLength = 4, widthDp = 160, heightDp = 160
            )
            key.contains("POLICE MOTORCYCLE") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT_SQUARE, ARGB_BLACK, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "شرطة", footerText = "POLICE",
                showCategory = false, numberMaxLength = 4, widthDp = 160, heightDp = 160
            )
            key.contains("POLICE") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT, ARGB_BLACK, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "POLICE شرطة",
                showCategory = false, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("PUBLIC") && !key.contains("GOODS") && !key.contains("TRANSPORT") && !key.contains("BUS") && !key.contains("TAXI") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT, KUWAIT_YELLOW, ARGB_BLACK, KUWAIT_YELLOW, ARGB_BLACK,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "90",
                showCategory = true, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("GENERAL FIREFIGHTING") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT, ARGB_RED, ARGB_BLACK, ARGB_RED, ARGB_BLACK,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "FIRE - الإطفاء العام",
                showCategory = false, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("PUBLIC TRANSPORTATIONS") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT, KUWAIT_YELLOW, ARGB_BLACK, KUWAIT_YELLOW, ARGB_BLACK,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "91",
                showCategory = true, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("PUBLIC BUSES") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT, KUWAIT_YELLOW, ARGB_BLACK, KUWAIT_YELLOW, ARGB_BLACK,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "92",
                showCategory = true, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("PUBLIC TAXIS") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT, KUWAIT_YELLOW, ARGB_BLACK, KUWAIT_YELLOW, ARGB_BLACK,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "93",
                showCategory = true, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("PUBLIC GOODS EXPORTATION") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT, KUWAIT_YELLOW, ARGB_BLACK, KUWAIT_YELLOW, ARGB_BLACK,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "95",
                showCategory = true, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("NATIONAL GUARD") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT, KUWAIT_GREEN, KUWAIT_YELLOW, KUWAIT_GREEN, KUWAIT_YELLOW,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "الحرس الوطني",
                showCategory = false, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("EMIRI BUREAU") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT, KUWAIT_BLUE, ARGB_WHITE, KUWAIT_BLUE, ARGB_WHITE,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "دولة الكويت - الديوان الأميري",
                showCategory = false, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("EMIRI GUARD") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT, KUWAIT_BLUE, ARGB_WHITE, KUWAIT_BLUE, ARGB_WHITE,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "الحرس الأميري",
                showCategory = false, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("EXPORT") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT, KUWAIT_GREEN, ARGB_BLACK, KUWAIT_GREEN, ARGB_BLACK,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "دولة الكويت - تصدير",
                showCategory = false, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("COMMERCIAL") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT, KUWAIT_PINK, ARGB_BLACK, KUWAIT_PINK, ARGB_BLACK,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "دولة الكويت-تجاري",
                showCategory = false, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("TEMPORARY CUSTOMS") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT, KUWAIT_PURPLE, ARGB_BLACK, KUWAIT_PURPLE, ARGB_BLACK,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "دولة الكويت - جمرك مؤقت",
                showCategory = false, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("CONSTRUCTION") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT, KUWAIT_ORANGE, ARGB_BLACK, KUWAIT_ORANGE, ARGB_BLACK,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "دولة الكويت-إنشاءات",
                showCategory = false, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("GOVERNMENT") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT, KUWAIT_GOV_BLUE, ARGB_BLACK, KUWAIT_GOV_BLUE, ARGB_BLACK,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "دولة الكويت - حكومة",
                showCategory = false, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("ARMY") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT, KUWAIT_BEIGE, ARGB_BLACK, KUWAIT_BEIGE, ARGB_BLACK,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "الجيش الكويتي",
                showCategory = false, numberMaxLength = 5, widthDp = 220, heightDp = 105
            )
            key.contains("DIPLOMATE") || key.contains("CORPS DIPLOMATIQUE") -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT_DIPLOMATIC, ARGB_WHITE, KUWAIT_BLUE, ARGB_WHITE, KUWAIT_BLUE,
                badgeTop = "K", badgeBottom = "KUWAIT", headerText = "هيئة دبلوماسية", footerText = "C.D",
                showCategory = true, numberMaxLength = 6, widthDp = 220, heightDp = 105
            )
            else -> VehiclePlateTemplate(
                VehiclePlateLayout.KUWAIT,
                ARGB_WHITE,
                ARGB_BLACK,
                ARGB_WHITE,
                ARGB_BLACK,
                badgeTop = "K",
                badgeBottom = "KUWAIT",
                showCategory = true,
                numberMaxLength = 5,
                widthDp = 220,
                heightDp = 105
            )
        }
    }

    private fun qatar(code: String, name: String): VehiclePlateTemplate {
        val key = "$code $name".uppercase()
        return when {
            // "Don't change" is the regular private plate. Keep this before every alias
            // because the API code and display name are combined into the lookup key.
            key.contains("DONT CHANGE") || key.contains("DON'T CHANGE") || key.contains("PRIVATE VEHICLE") -> VehiclePlateTemplate(
                VehiclePlateLayout.QATAR,
                QATAR_PLATE_FACE,
                ARGB_BLACK,
                MAROON,
                ARGB_WHITE,
                "QATAR",
                widthDp = 320,
                heightDp = 154
            )
            key.contains("COMMERCIAL") || key.contains(" COM") -> VehiclePlateTemplate(VehiclePlateLayout.QATAR, ORANGE, ARGB_BLACK, ORANGE, ARGB_BLACK, "تجارية", "COMM.")
            key.contains("LIMO") || key.contains("TOURIST") || key.contains("RENT") -> VehiclePlateTemplate(VehiclePlateLayout.QATAR, ARGB_WHITE, ARGB_BLACK, GRAY, ARGB_BLACK, "ليموزين", "LIMO.")
            key.contains("TAXI") || key.contains("TAX") -> VehiclePlateTemplate(VehiclePlateLayout.QATAR, GRAY, ARGB_BLACK, GRAY, ARGB_BLACK, "أجرة", "TAXI")
            key.contains("PUBLIC TRANSPORT") -> VehiclePlateTemplate(VehiclePlateLayout.QATAR, ARGB_WHITE, ARGB_BLACK, DARK_RED, ARGB_WHITE, "نقل عام", "PUB. TRANS.")
            key.contains("PRIVATE TRANSPORT") ->
                VehiclePlateTemplate(VehiclePlateLayout.QATAR, ARGB_WHITE, ARGB_BLACK, DARK_RED, ARGB_WHITE, "نقل خاص", "PRI. TRANS.")
            key.contains("GOVERNMENT") || key.contains("MUNICIPAL") || key.contains("CUSTOMS") || key.contains("CIVIL DEFENCE") || key.contains("IMMIGRATION") || key.contains("WORKSHOP") || key.contains("LOCAL GUARD") || key.contains("MARASIM") || key.contains("CEREMON") || key.contains(" GOV") -> VehiclePlateTemplate(VehiclePlateLayout.QATAR, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE, ARGB_BLACK, "حكومي", "GOV.")
            key.contains("DIPLOMATIC") || key.contains("CONSULAR") || key.contains("INT. ORGANIZATION") || key.contains("POLITICAL BODY") || key.contains(" DIP") -> VehiclePlateTemplate(VehiclePlateLayout.QATAR_DIPLOMATIC, ARGB_WHITE, ARGB_RED, ARGB_WHITE, ARGB_RED, "دبلوماسية", "QATAR", footerText = "CD", footerTextColor = ARGB_RED)
            key.contains("TEMPORARY") || key.contains("TEMP") || key.contains("UNDER TRIAL") || key.contains("TRANSFER") -> VehiclePlateTemplate(VehiclePlateLayout.QATAR, ARGB_WHITE, ARGB_BLACK, ORANGE, ARGB_BLACK, "ادخال\nمؤقت", "TEMP.\nTRANS.")
            key.contains("POLICE") -> VehiclePlateTemplate(VehiclePlateLayout.QATAR_POLICE, POLICE_BLUE, ARGB_WHITE, POLICE_BLUE, ARGB_WHITE, "شرطة", "قطر")
            key.contains("ISF") || key.contains("LKHWAYA") || key.contains("SPECIAL SECURITY") || key.contains("ARMY") || key.contains("ARMED FORCES") -> VehiclePlateTemplate(VehiclePlateLayout.QATAR_ISF, ISF_YELLOW, ISF_BROWN, ISF_YELLOW, ISF_BROWN, "لخويا", "ISF")
            key.contains("LARGE") || key.contains("TRAILER") || key.contains("HEAVY") -> VehiclePlateTemplate(VehiclePlateLayout.QATAR, ARGB_BLACK, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE, "QATAR")
            else -> VehiclePlateTemplate(
                VehiclePlateLayout.QATAR,
                QATAR_PLATE_FACE,
                ARGB_BLACK,
                MAROON,
                ARGB_WHITE,
                "QATAR",
                widthDp = 320,
                heightDp = 154
            )
        }
    }

    private fun uae(region: String): VehiclePlateTemplate {
        val emirate = region.ifBlank { "Dubai" }
        return if (emirate.equals("Abu Dhabi", true)) {
            VehiclePlateTemplate(VehiclePlateLayout.UAE_ABU_DHABI, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE, ARGB_BLACK, "A.D", "U.A.E", headerText = "أبوظبي   الإمارات", headerColor = UAE_RED, headerTextColor = ARGB_WHITE, showCategory = true, numberMaxLength = 5, widthDp = 220, heightDp = 108)
        } else {
            VehiclePlateTemplate(if (emirate.equals("Dubai", true)) VehiclePlateLayout.UAE_DUBAI else VehiclePlateLayout.UAE_EMIRATE, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE, ARGB_BLACK, emirate.uppercase(), "U.A.E", footerText = emirate, showCategory = true, numberMaxLength = 5, widthDp = 220, heightDp = 108)
        }
    }
}
