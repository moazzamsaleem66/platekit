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

    private val kuwaitVehicleTypes = listOf(
        "PRIVATE",
        "PRIVATE (LONG)",
        "PRIVATE (SMALL)",
        "MOTORCYCLE",
        "PUBLIC",
        "PUBLIC (LONG)",
        "PUBLIC (SMALL)",
        "POLICE",
        "POLICE MOTORCYCLE",
        "GOVERNMENT",
        "DIPLOMATES",
        "ARMY",
        "COMMERCIALS",
        "TEMPORARY CUSTOMS",
        "CONSTRUCTION",
        "NATIONAL GUARD",
        "EMIRI BUREAU",
        "EXPORT",
        "EMIRI GUARD",
        "GENERAL FIREFIGHTING",
        "PUBLIC TRANSPORTATIONS",
        "PUBLIC BUSES",
        "PUBLIC TAXIS",
        "PUBLIC GOODS EXPORTATION"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Vehicle Type Dropdown
        val vehicleTypeAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())
        binding.vehicleTypeTxt.setAdapter(vehicleTypeAdapter)
        binding.vehicleTypeTxt.setOnItemClickListener { parent, _, position, _ ->
            val selectedType = parent.getItemAtPosition(position).toString()
            binding.plateInputView.setVehicleType(selectedType, selectedType)
        }

        binding.plateInputView.setOnCountryChangeListener { country ->
            val isQatar = country?.code == "QAT"
            val isKuwait = country?.code == "KWT"
            
            val showVehicleType = isQatar || isKuwait
            binding.vehicleTypeLabel.visibility = if (showVehicleType) View.VISIBLE else View.GONE
            binding.vehicleTypeLayout.visibility = if (showVehicleType) View.VISIBLE else View.GONE
            
            if (showVehicleType) {
                val types = if (isQatar) qatarVehicleTypes else kuwaitVehicleTypes
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, types)
                binding.vehicleTypeTxt.setAdapter(adapter)
                // Do not clear the text if it's already a valid type for this country
                if (binding.vehicleTypeTxt.text.toString() !in types) {
                    binding.vehicleTypeTxt.setText("", false)
                }
            }
        }

        // Configure the PlateInputView
        binding.plateInputView.configure(
            PlateCountryCatalog.builder()
                .defaultCountry(PlateCountries.KUWAIT)
                .enableCountries(*PlateCountries.gcc.toTypedArray())
                .build()
        )
        
        // Explicitly trigger the listener for the initial default country (Kuwait)
        val initialTypes = kuwaitVehicleTypes
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, initialTypes)
        binding.vehicleTypeTxt.setAdapter(adapter)
        binding.vehicleTypeLabel.visibility = View.VISIBLE
        binding.vehicleTypeLayout.visibility = View.VISIBLE

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
