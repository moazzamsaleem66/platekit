package com.developer.platekit.core

import com.developer.platekit.core.template.VehiclePlateLayout
import com.developer.platekit.core.template.VehiclePlateTemplates
import org.junit.Test
import org.junit.Assert.assertEquals

class UaeEmirateTemplateTest {
    @Test fun ajmanAndRakUseDedicatedLayout() {
        val ajman = VehiclePlateTemplates.resolve("UAE", "Ajman")
        val rak = VehiclePlateTemplates.resolve("UAE", "Ras Al-Khaimah")
        assertEquals(VehiclePlateLayout.UAE_AJMAN_RAK, ajman.layout)
        assertEquals("عجمان" to "AJMAN", ajman.badgeTop to ajman.badgeBottom)
        assertEquals(VehiclePlateLayout.UAE_AJMAN_RAK, rak.layout)
        assertEquals("رأس الخيمة" to "RAK", rak.badgeTop to rak.badgeBottom)
    }

    @Test fun fujairahAndSharjahShareLayoutWithOwnArabicName() {
        val f = VehiclePlateTemplates.resolve("UAE", "Fujairah")
        val s = VehiclePlateTemplates.resolve("UAE", "Sharjah")
        assertEquals(VehiclePlateLayout.UAE_FUJAIRAH_SHARJAH, f.layout)
        assertEquals(VehiclePlateLayout.UAE_FUJAIRAH_SHARJAH, s.layout)
        assertEquals("الفجيرة", f.badgeTop)
        assertEquals("الشارقة", s.badgeTop)
    }
}
