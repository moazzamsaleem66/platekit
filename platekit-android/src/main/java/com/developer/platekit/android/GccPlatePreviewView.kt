package com.developer.platekit.android

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes

/** Compatibility wrapper. All plate visuals now come from VehiclePlateTemplates. */
class GccPlatePreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : VehiclePlateTemplateView(context, attrs, defStyleAttr) {

    fun render(
        @DrawableRes flagRes: Int,
        countryCode: String,
        countryName: String,
        category: String,
        plateNumber: String,
        showCategory: Boolean
    ) {
        render(countryCode = countryCode, region = if (countryCode == "UAE") countryName else "", categoryValue = category, plateNumber = plateNumber)
    }
}
