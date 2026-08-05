package com.developer.platekit.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.developer.platekit.android.PlateInputResult
import com.developer.platekit.core.PlateCountries
import com.developer.platekit.core.PlateCountryCatalog
import com.developer.platekit.core.UnlistedCountryPolicy
import com.developer.platekit.sample.databinding.ActivityMainBinding

/**
 * Minimal demo of PlateInputView, standalone — no app-specific dependencies beyond
 * this module. Shows the exact configuration used by the reference integration:
 * Qatar as the default country, the rest of the GCC set enabled, unlisted countries
 * fall back to a generic template rather than being hidden.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.plateInputView.configure(
            PlateCountryCatalog.builder()
                .defaultCountry(PlateCountries.QATAR)
                .enableCountries(*PlateCountries.gcc.toTypedArray())
                .unlistedCountryPolicy(UnlistedCountryPolicy.GENERIC_FALLBACK)
                .build()
        )

        binding.searchButton.setOnClickListener {
            when (val result = binding.plateInputView.getFormattedPlateNumber()) {
                is PlateInputResult.Valid -> binding.resultText.text = "Plate number: ${result.plateNumber}"
                is PlateInputResult.Invalid -> binding.resultText.text = "Error: ${result.message}"
            }
        }
    }
}
