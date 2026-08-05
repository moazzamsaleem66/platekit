package com.developer.platekit.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlateCountryCatalogTest {

    @Test
    fun `enabled countries plus default are all visible by default`() {
        val catalog = PlateCountryCatalog.builder()
            .defaultCountry(PlateCountries.QATAR)
            .enableCountries(*PlateCountries.gcc.toTypedArray())
            .build()

        assertEquals(PlateCountries.gcc.size, catalog.visibleCountries.size)
        assertTrue(catalog.visibleCountries.any { it.code == "EGY" })
    }

    @Test
    fun `excludeCountries drops a specific country even after a bulk enable`() {
        val catalog = PlateCountryCatalog.builder()
            .defaultCountry(PlateCountries.QATAR)
            .enableCountries(*PlateCountries.gcc.toTypedArray())
            .excludeCountries(PlateCountries.EGYPT, PlateCountries.JORDAN)
            .build()

        assertFalse(catalog.visibleCountries.any { it.code == "EGY" })
        assertFalse(catalog.visibleCountries.any { it.code == "JOR" })
        assertTrue(catalog.visibleCountries.any { it.code == "BHR" })
        assertEquals(PlateCountries.gcc.size - 2, catalog.visibleCountries.size)
    }

    @Test
    fun `excludeCountryCodes works the same as excludeCountries`() {
        val catalog = PlateCountryCatalog.builder()
            .defaultCountry(PlateCountries.QATAR)
            .enableCountries(*PlateCountries.gcc.toTypedArray())
            .excludeCountryCodes("egy") // case-insensitive
            .build()

        assertFalse(catalog.visibleCountries.any { it.code == "EGY" })
    }

    @Test
    fun `excluding the default country does not remove it from visibleCountries`() {
        // The default is always shown as the primary/simple mode — exclusion only
        // applies to the alternate "other country" list built from enableCountries.
        val catalog = PlateCountryCatalog.builder()
            .defaultCountry(PlateCountries.QATAR)
            .enableCountries(*PlateCountries.gcc.toTypedArray())
            .excludeCountries(PlateCountries.QATAR)
            .build()

        assertEquals(PlateCountries.QATAR, catalog.defaultCountry)
        assertTrue(catalog.visibleCountries.any { it.code == "QAT" })
    }

    @Test
    fun `findByCode is case-insensitive and respects exclusion`() {
        val catalog = PlateCountryCatalog.builder()
            .defaultCountry(PlateCountries.QATAR)
            .enableCountries(*PlateCountries.gcc.toTypedArray())
            .excludeCountries(PlateCountries.EGYPT)
            .build()

        assertEquals(PlateCountries.KUWAIT, catalog.findByCode("kwt"))
        assertEquals(null, catalog.findByCode("EGY"))
    }
}
