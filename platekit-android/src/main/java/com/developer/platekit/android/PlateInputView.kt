package com.developer.platekit.android

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.developer.platekit.android.databinding.ViewPlateInputBinding
import com.developer.platekit.core.PlateCategoryInputMode
import com.developer.platekit.core.PlateCountryCatalog
import com.developer.platekit.core.PlateCountryDefinition
import com.developer.platekit.core.PlateNumberBuilder
import com.developer.platekit.core.PlateNumberValidator
import com.developer.platekit.core.PlateValidationReason
import com.developer.platekit.core.PlateValidationResult

/** Result of asking a [PlateInputView] for its current, fully-formatted plate number. */
sealed class PlateInputResult {
    data class Valid(val plateNumber: String) : PlateInputResult()
    data class Invalid(val message: String) : PlateInputResult()
}

/**
 * Self-contained "search a vehicle by plate" input: default-country / alternate-country
 * toggle, country + category/letter pickers for the alternate side, a live visual
 * preview, and the raw plate-number field itself.
 *
 * Configure once with a [PlateCountryCatalog] (which country is the default, which
 * others are enabled, what happens to the rest) — everything else is driven from that,
 * so switching the default country for a different deployment/project is a one-line
 * catalog change, not a code change here.
 */
class PlateInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewPlateInputBinding.inflate(LayoutInflater.from(context), this)

    private var catalog: PlateCountryCatalog? = null
    private var isAlternateMode = false
    private var selectedCountry: PlateCountryDefinition? = null
    private var selectedCategoryCode: String = ""
    private var selectedUaeState: String = "Dubai"
    private var vehicleTypeCode: String = ""
    private var vehicleTypeName: String = ""

    private var onCountryChangeListener: ((PlateCountryDefinition?) -> Unit)? = null

    init {
        orientation = VERTICAL
        binding.primaryModeBtn.setOnClickListener { setMode(alternate = false) }
        binding.alternateModeBtn.setOnClickListener { setMode(alternate = true) }
        binding.vehicleNumberTxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                binding.platePreviewNumberTv.text = s?.toString().orEmpty()
                refreshPreview()
            }
        })
        binding.categoryTxt.setOnClickListener { binding.categoryTxt.showDropDown() }
        binding.categoryTxt.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) binding.categoryTxt.showDropDown() }
        binding.categoryTxt.setOnItemClickListener { parent, _, position, _ ->
            selectedCategoryCode = parent.getItemAtPosition(position).toString()
            binding.categoryTxt.setText(selectedCategoryCode, false)
            refreshPreview()
        }
        listOf(binding.letter1Txt, binding.letter2Txt, binding.letter3Txt).forEach {
            it.setOnClickListener { _ -> it.showDropDown() }
            it.setOnItemClickListener { _, _, _, _ -> refreshPreview() }
        }
    }

    /** Must be called once before any other method — everything else assumes a catalog. */
    fun configure(catalog: PlateCountryCatalog) {
        this.catalog = catalog
        val countryAdapter = ArrayAdapter(
            context, android.R.layout.simple_list_item_1, catalog.visibleCountries.map { it.displayName }
        )
        binding.countryTxt.setAdapter(countryAdapter)
        binding.countryTxt.setOnClickListener { binding.countryTxt.showDropDown() }
        binding.countryTxt.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) binding.countryTxt.showDropDown() }
        binding.countryTxt.setOnItemClickListener { _, _, position, _ ->
            val country = catalog.visibleCountries[position]
            selectedCountry = country
            binding.countryTxt.setText(country.displayName, false)
            binding.plateBadgeLettersTv.text = country.code
            binding.plateBadgeWordTv.text = country.displayName
            configureSelectedCountry(country)
            onCountryChangeListener?.invoke(country)
        }
        setupDropdown(binding.uaeStateTxt, com.developer.platekit.core.PlateCountries.uaeStates)
        binding.uaeStateTxt.setOnItemClickListener { parent, _, position, _ ->
            selectedUaeState = parent.getItemAtPosition(position).toString()
            binding.uaeStateTxt.setText(selectedUaeState, false)
            selectedCategoryCode = ""
            binding.categoryTxt.setText("", false)
            setupDropdown(
                binding.categoryTxt,
                if (selectedUaeState.equals("Abu Dhabi", true)) com.developer.platekit.core.PlateCountries.KUWAIT.categoryOptions()
                else com.developer.platekit.core.PlateCountries.singleLetters
            )
            refreshPreview()
        }

        binding.primaryModeTv.text = context.getString(
            com.developer.platekit.android.R.string.platekit_primary_plate_mode_format,
            catalog.defaultCountry.displayName
        )
        val isQatar = catalog.defaultCountry.code == "QAT"
        setMode(alternate = !isQatar)
        onCountryChangeListener?.invoke(if (isQatar) catalog.defaultCountry else selectedCountry)
    }

    fun setOnCountryChangeListener(listener: (PlateCountryDefinition?) -> Unit) {
        this.onCountryChangeListener = listener
    }

    /** Optional: shows a small icon inside each toggle button, using the host app's own
     *  drawable resources (this module never references or ships flag/emblem imagery). */
    fun setModeIcons(@DrawableRes primaryIconRes: Int?, @DrawableRes alternateIconRes: Int?) {
        if (primaryIconRes != null) {
            binding.primaryModeIcon.setImageResource(primaryIconRes)
            binding.primaryModeIcon.visibility = View.VISIBLE
        } else {
            binding.primaryModeIcon.visibility = View.GONE
        }
        if (alternateIconRes != null) {
            binding.alternateModeIcon.setImageResource(alternateIconRes)
            binding.alternateModeIcon.visibility = View.VISIBLE
        } else {
            binding.alternateModeIcon.visibility = View.GONE
        }
    }

    /** Vehicle type feeds the rich visual template (e.g. Qatar's taxi/police/diplomatic
     *  sub-styles) — call whenever the host's vehicle-type selection changes. */
    fun setVehicleType(code: String, name: String) {
        vehicleTypeCode = code
        vehicleTypeName = name
        refreshPreview()
    }

    /** Enables/disables the raw plate-number field (e.g. while a search is in flight). */
    fun setInputEnabled(enabled: Boolean) {
        binding.vehicleNumberTxt.isEnabled = enabled
    }

    fun rawNumber(): String = binding.vehicleNumberTxt.text?.toString().orEmpty()

    /** Forwards a focus-change listener (e.g. hide-keyboard-on-blur) to the internal
     *  raw-number field, since it's no longer directly exposed to the host fragment. */
    fun setRawNumberFocusChangeListener(listener: View.OnFocusChangeListener) {
        binding.vehicleNumberTxt.onFocusChangeListener = listener
    }

    fun reset() {
        binding.vehicleNumberTxt.setText("")
        binding.platePreviewNumberTv.text = ""
        setMode(alternate = false)
    }

    /** Validates (in alternate mode) then builds the final string sent to the search API. */
    fun getFormattedPlateNumber(): PlateInputResult {
        val country = if (isAlternateMode) selectedCountry else null
        val maxLength = currentMaxLength()
        if (isAlternateMode) {
            if (country == null) {
                return PlateInputResult.Invalid(context.getString(com.developer.platekit.android.R.string.platekit_error_select_country))
            }
            val region = if (country.code == "UAE") binding.uaeStateTxt.text?.toString().orEmpty() else ""
            when (
                val result = PlateNumberValidator.validate(
                    selectedCountry = country,
                    rawNumber = rawNumber(),
                    maximumLength = maxLength,
                    selectedCategoryCode = selectedCategoryCode,
                    selectedLetters = selectedLetters(),
                    selectedRegion = region
                )
            ) {
                is PlateValidationResult.Invalid -> return PlateInputResult.Invalid(messageFor(result.reason, maxLength))
                is PlateValidationResult.Valid -> Unit
            }
        }
        val built = PlateNumberBuilder.build(
            selectedCountry = country,
            rawNumber = rawNumber(),
            selectedCategoryCode = selectedCategoryCode,
            selectedLetters = selectedLetters()
        )
        return PlateInputResult.Valid(built)
    }

    private fun messageFor(reason: PlateValidationReason, maximumLength: Int): String = when (reason) {
        PlateValidationReason.NUMBER_INVALID_OR_TOO_LONG ->
            context.getString(com.developer.platekit.android.R.string.platekit_error_number_invalid, maximumLength)
        PlateValidationReason.CATEGORY_NOT_SELECTED ->
            context.getString(com.developer.platekit.android.R.string.platekit_error_category_not_selected)
        PlateValidationReason.LETTERS_INCOMPLETE ->
            if (selectedCountry?.categoryMode == PlateCategoryInputMode.TWO_LETTERS)
                context.getString(com.developer.platekit.android.R.string.platekit_error_letters_two)
            else context.getString(com.developer.platekit.android.R.string.platekit_error_letters_three)
        PlateValidationReason.REGION_NOT_SELECTED ->
            context.getString(com.developer.platekit.android.R.string.platekit_error_region_not_selected)
    }

    private fun setMode(alternate: Boolean) {
        isAlternateMode = alternate
        val catalog = catalog
        if (alternate) {
            binding.alternateFieldsRow.visibility = View.VISIBLE
            binding.platePreviewRow.visibility = View.GONE
            binding.primaryTemplateView.visibility = View.GONE
            binding.alternateTemplateView.visibility = View.VISIBLE

            val defaultAlternate = catalog?.visibleCountries?.firstOrNull()
            selectedCountry = defaultAlternate
            selectedCategoryCode = ""
            binding.countryTxt.setText(defaultAlternate?.displayName.orEmpty(), false)
            binding.categoryTxt.setText("", false)
            binding.plateBadgeLettersTv.text = defaultAlternate?.code.orEmpty()
            binding.plateBadgeWordTv.text = defaultAlternate?.displayName.orEmpty()
            binding.platePreviewCategoryTv.text = ""
            defaultAlternate?.let(::configureSelectedCountry)
            onCountryChangeListener?.invoke(defaultAlternate)

            binding.alternateModeBtn.setBackgroundResource(R.drawable.platekit_bg_toggle_selected)
            binding.alternateModeTv.setTextColor(ContextCompat.getColor(context, R.color.platekit_accent_royal_blue))
            binding.primaryModeBtn.setBackgroundResource(R.drawable.platekit_bg_toggle_unselected)
            binding.primaryModeTv.setTextColor(ContextCompat.getColor(context, R.color.platekit_text_slate_600))
        } else {
            binding.alternateFieldsRow.visibility = View.GONE
            binding.uaeStateTextField.visibility = View.GONE
            binding.lettersRow.visibility = View.GONE
            binding.platePreviewRow.visibility = View.GONE
            binding.primaryTemplateView.visibility = View.VISIBLE
            binding.alternateTemplateView.visibility = View.GONE
            binding.platePreviewCategoryTv.visibility = View.GONE
            selectedCountry = null
            selectedCategoryCode = ""

            // Matches the original fragment exactly: default-mode badge text is always
            // these two fixed strings (not derived from the country code) — a different
            // deployment overrides platekit_badge_letters_default/word_default in its own
            // resources rather than this view deriving text from the country definition.
            val defaultCountry = catalog?.defaultCountry
            if (defaultCountry != null && defaultCountry.code != "QAT") {
                binding.plateBadgeLettersTv.text = defaultCountry.code
                binding.plateBadgeWordTv.text = defaultCountry.displayName
            } else {
                binding.plateBadgeLettersTv.text = context.getString(com.developer.platekit.android.R.string.platekit_badge_letters_default)
                binding.plateBadgeWordTv.text = context.getString(com.developer.platekit.android.R.string.platekit_badge_word_default)
            }
            binding.vehicleNumberTxt.filters = arrayOf(InputFilter.LengthFilter(6))

            binding.primaryModeBtn.setBackgroundResource(R.drawable.platekit_bg_toggle_selected)
            binding.primaryModeTv.setTextColor(ContextCompat.getColor(context, R.color.platekit_accent_royal_blue))
            binding.alternateModeBtn.setBackgroundResource(R.drawable.platekit_bg_toggle_unselected)
            binding.alternateModeTv.setTextColor(ContextCompat.getColor(context, R.color.platekit_text_slate_600))
            onCountryChangeListener?.invoke(catalog?.defaultCountry)
            refreshPreview()
        }
    }

    private fun configureSelectedCountry(country: PlateCountryDefinition) {
        selectedCategoryCode = ""
        binding.categoryTxt.setText("", false)
        binding.letter1Txt.setText("", false)
        binding.letter2Txt.setText("", false)
        binding.letter3Txt.setText("", false)
        binding.categoryContainer.visibility =
            if (country.categoryMode == PlateCategoryInputMode.SINGLE_DROPDOWN) View.VISIBLE else View.GONE
        binding.lettersRow.visibility =
            if (country.categoryMode == PlateCategoryInputMode.TWO_LETTERS || country.categoryMode == PlateCategoryInputMode.THREE_LETTERS) View.VISIBLE else View.GONE
        binding.letter3Txt.visibility =
            if (country.categoryMode == PlateCategoryInputMode.THREE_LETTERS) View.VISIBLE else View.GONE
        binding.uaeStateTextField.visibility = if (country.code == "UAE") View.VISIBLE else View.GONE

        when (country.code) {
            "KWT" -> {
                binding.categoryLabelTv.text = context.getString(com.developer.platekit.android.R.string.platekit_category_code_label_kuwait)
                setupDropdown(binding.categoryTxt, country.categoryOptions())
            }
            "UAE" -> {
                binding.categoryLabelTv.text = context.getString(com.developer.platekit.android.R.string.platekit_category_code_label)
                setupDropdown(
                    binding.categoryTxt,
                    if (selectedUaeState.equals("Abu Dhabi", true)) com.developer.platekit.core.PlateCountries.KUWAIT.categoryOptions()
                    else com.developer.platekit.core.PlateCountries.singleLetters
                )
                binding.uaeStateTxt.setText(selectedUaeState, false)
            }
            "JOR" -> {
                binding.categoryLabelTv.text = context.getString(com.developer.platekit.android.R.string.platekit_category_code_label_jordan)
                setupDropdown(binding.categoryTxt, country.categoryOptions())
            }
            else -> if (country.categoryMode == PlateCategoryInputMode.TWO_LETTERS || country.categoryMode == PlateCategoryInputMode.THREE_LETTERS) {
                configureLetterSelectors(country.categoryOptions(), if (country.categoryMode == PlateCategoryInputMode.THREE_LETTERS) 3 else 2)
            }
        }
        refreshPreview()
    }

    private fun configureLetterSelectors(options: List<String>, count: Int) {
        setupDropdown(binding.letter1Txt, options)
        setupDropdown(binding.letter2Txt, options)
        if (count == 3) setupDropdown(binding.letter3Txt, options)
    }

    private fun selectedLetters(): String {
        val requiredCount = if (selectedCountry?.categoryMode == PlateCategoryInputMode.TWO_LETTERS) 2 else 3
        return listOf(
            binding.letter1Txt.text?.toString().orEmpty(),
            binding.letter2Txt.text?.toString().orEmpty(),
            binding.letter3Txt.text?.toString().orEmpty()
        ).take(requiredCount).filter(String::isNotBlank).joinToString("")
    }

    private fun setupDropdown(view: AutoCompleteTextView, options: List<String>) {
        view.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, options))
        view.setOnClickListener { view.showDropDown() }
        view.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) view.showDropDown() }
    }

    private fun currentMaxLength(): Int =
        if (isAlternateMode) binding.alternateTemplateView.currentNumberMaxLength else binding.primaryTemplateView.currentNumberMaxLength

    private fun refreshPreview() {
        if (isAlternateMode) {
            val country = selectedCountry ?: return
            val category = when (country.categoryMode) {
                PlateCategoryInputMode.SINGLE_DROPDOWN -> selectedCategoryCode
                PlateCategoryInputMode.THREE_LETTERS, PlateCategoryInputMode.TWO_LETTERS -> selectedLetters()
                PlateCategoryInputMode.NONE -> ""
            }
            binding.alternateTemplateView.render(
                countryCode = country.code,
                region = if (country.code == "UAE") selectedUaeState else "",
                vehicleTypeCode = vehicleTypeCode,
                vehicleTypeName = vehicleTypeName,
                categoryValue = category,
                plateNumber = rawNumber()
            )
            clampToMaxLength(binding.alternateTemplateView.currentNumberMaxLength)
        } else {
            val country = catalog?.defaultCountry ?: return
            binding.primaryTemplateView.render(
                countryCode = country.code,
                vehicleTypeCode = vehicleTypeCode,
                vehicleTypeName = vehicleTypeName,
                plateNumber = rawNumber()
            )
            clampToMaxLength(binding.primaryTemplateView.currentNumberMaxLength)
        }
    }

    private fun clampToMaxLength(maxLength: Int) {
        binding.vehicleNumberTxt.filters = arrayOf(InputFilter.LengthFilter(maxLength))
        val current = binding.vehicleNumberTxt.text?.toString().orEmpty()
        if (current.length > maxLength) {
            binding.vehicleNumberTxt.setText(current.take(maxLength))
            binding.vehicleNumberTxt.setSelection(maxLength)
        }
    }
}
