package com.developer.platekit.core

import com.developer.platekit.core.template.VehiclePlateTemplates

/**
 * Resolves the rich visual template for a plate preview, given the region/vehicle-type
 * context collected so far. Kept pluggable per country so a country with no special
 * visual treatment can simply fall back to a generic template.
 */
fun interface PlateTemplateResolver {
    fun resolve(region: String, vehicleTypeCode: String, vehicleTypeName: String): com.developer.platekit.core.template.VehiclePlateTemplate
}

/**
 * Describes one country's plate-entry rules.
 *
 * [flagKey] is a free-form identifier (defaults to [code]) that a host app maps to its
 * own flag drawable via a FlagProvider — this module never references Android resources,
 * which is what keeps it usable outside a single app's resource set.
 */
data class PlateCountryDefinition(
    val code: String,
    val displayName: String,
    val categoryMode: PlateCategoryInputMode,
    val categoryOptions: () -> List<String> = { emptyList() },
    val regionOptions: List<String> = emptyList(),
    val flagKey: String = code,
    val templateResolver: PlateTemplateResolver = PlateTemplateResolver { region, vehicleTypeCode, vehicleTypeName ->
        VehiclePlateTemplates.resolve(code, region, vehicleTypeCode, vehicleTypeName)
    },
    // Client-side numeric country ID + Arabic name, matching the host app's own country
    // master list (id/NameEN/NameAR) so a plate result can be reported straight into that
    // system without a separate code->id lookup on the app side. countryId defaults to -1
    // (unmapped) rather than null so existing call sites/tests that don't pass it don't
    // silently become Int? everywhere.
    val countryId: Int = -1,
    val nameAr: String = ""
)
