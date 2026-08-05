package com.example.platesample.dummy

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.developer.platekit.android.PlateInputResult
import com.developer.platekit.core.PlateCountries
import com.developer.platekit.core.PlateCountryCatalog
import com.example.platesample.dummy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val qatarVehicleTypes = listOf(
        "PRIVATE VEHICLE",
        "TAXI",
        "COMMERCIAL",
        "POLICE",
        "ARMY",
        "GOVERNMENT",
        "DIPLOMATIC",
        "PUBLIC TRANSPORT",
        "LIMO"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Vehicle Type Dropdown
        val vehicleTypeAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, qatarVehicleTypes)
        binding.vehicleTypeTxt.setAdapter(vehicleTypeAdapter)
        binding.vehicleTypeTxt.setOnItemClickListener { parent, _, position, _ ->
            val selectedType = parent.getItemAtPosition(position).toString()
            binding.plateInputView.setVehicleType(selectedType, selectedType)
        }

        binding.plateInputView.setOnCountryChangeListener { country ->
            val isQatar = country?.code == "QAT"
            binding.vehicleTypeLabel.visibility = if (isQatar) View.VISIBLE else View.GONE
            binding.vehicleTypeLayout.visibility = if (isQatar) View.VISIBLE else View.GONE
        }

        // Configure the PlateInputView
        binding.plateInputView.configure(
            PlateCountryCatalog.builder()
                .defaultCountry(PlateCountries.KUWAIT)
                .enableCountries(*PlateCountries.gcc.toTypedArray())
                .build()
        )

        binding.checkButton.setOnClickListener {
            val result = binding.plateInputView.getFormattedPlateNumber()
            when (result) {
                is PlateInputResult.Valid -> {
                    binding.resultText.text = "Valid Plate: ${result.plateNumber}"
                    binding.resultText.setTextColor(android.graphics.Color.GREEN)
                }
                is PlateInputResult.Invalid -> {
                    binding.resultText.text = "Error: ${result.message}"
                    binding.resultText.setTextColor(android.graphics.Color.RED)
                }
            }
        }
    }
}
