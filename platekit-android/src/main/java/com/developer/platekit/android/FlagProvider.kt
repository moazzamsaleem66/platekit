package com.developer.platekit.android

import androidx.annotation.DrawableRes

/**
 * Lets a host app supply its own flag drawable per country code, without platekit-android
 * ever needing to reference (or ship) the host's actual flag image resources.
 */
fun interface FlagProvider {
    @DrawableRes
    fun flagFor(countryCode: String): Int?
}
