package com.developer.platekit.core

/**
 * What happens to countries that were not explicitly enabled via
 * [PlateCountryCatalog.Builder.enableCountries].
 *
 * - [GENERIC_FALLBACK]: still offered in the picker, rendered with a plain generic
 *   plate template instead of a country-specific one. Keeps every real plate
 *   searchable even if nobody has designed its visual template yet.
 * - [HIDDEN]: not offered at all.
 */
enum class UnlistedCountryPolicy { GENERIC_FALLBACK, HIDDEN }

/**
 * Configuration for one deployment of the plate-input feature: which country is the
 * default/primary one (e.g. Qatar today, Kuwait for a different deployment), which
 * additional countries are enabled in the "other country" picker, and what to do with
 * everything else.
 *
 * Build via [PlateCountryCatalog.builder]; there is deliberately no public constructor
 * so every catalog is guaranteed to have a default country.
 */
class PlateCountryCatalog private constructor(
    val defaultCountry: PlateCountryDefinition,
    val enabledCountries: List<PlateCountryDefinition>,
    val unlistedCountryPolicy: UnlistedCountryPolicy
) {
    /** Countries to actually offer in a picker: the default, plus whatever was enabled. */
    val visibleCountries: List<PlateCountryDefinition> =
        (listOf(defaultCountry) + enabledCountries).distinctBy { it.code.uppercase() }

    fun findByCode(code: String): PlateCountryDefinition? =
        visibleCountries.firstOrNull { it.code.equals(code, ignoreCase = true) }

    class Builder {
        private var defaultCountry: PlateCountryDefinition? = null
        private val enabledCountries = linkedSetOf<PlateCountryDefinition>()
        private val excludedCountryCodes = mutableSetOf<String>()
        private var unlistedCountryPolicy: UnlistedCountryPolicy = UnlistedCountryPolicy.GENERIC_FALLBACK

        /** The primary country for this deployment — e.g. Qatar today, Kuwait tomorrow. */
        fun defaultCountry(country: PlateCountryDefinition) = apply { this.defaultCountry = country }

        /** Additional countries to show in the "other country" picker. */
        fun enableCountries(vararg countries: PlateCountryDefinition) = apply {
            enabledCountries.addAll(countries)
        }

        fun enableCountries(countries: Collection<PlateCountryDefinition>) = apply {
            enabledCountries.addAll(countries)
        }

        /**
         * Removes specific countries from the "other country" picker, even if they were
         * included via a bulk [enableCountries] call (e.g. `enableCountries(*PlateCountries.gcc.toTypedArray())`
         * followed by `.excludeCountries(PlateCountries.EGYPT)` to drop just Egypt from the
         * default GCC set). Exclusion always wins over enabling, regardless of call order.
         *
         * Excluding the [defaultCountry] itself has no effect — the default is always
         * shown (that's a separate, primary mode, not part of this list).
         */
        fun excludeCountries(vararg countries: PlateCountryDefinition) = apply {
            excludedCountryCodes.addAll(countries.map { it.code.uppercase() })
        }

        /** Same as [excludeCountries], for when you only have the country code (e.g. "EGY"). */
        fun excludeCountryCodes(vararg codes: String) = apply {
            excludedCountryCodes.addAll(codes.map { it.uppercase() })
        }

        fun unlistedCountryPolicy(policy: UnlistedCountryPolicy) = apply { this.unlistedCountryPolicy = policy }

        fun build(): PlateCountryCatalog {
            val default = requireNotNull(defaultCountry) {
                "PlateCountryCatalog requires defaultCountry(...) to be set before build()."
            }
            val filteredEnabled = enabledCountries.filterNot { it.code.uppercase() in excludedCountryCodes }
            return PlateCountryCatalog(
                defaultCountry = default,
                enabledCountries = filteredEnabled,
                unlistedCountryPolicy = unlistedCountryPolicy
            )
        }
    }

    companion object {
        fun builder(): Builder = Builder()
    }
}
