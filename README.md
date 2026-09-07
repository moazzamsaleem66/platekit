# Platekit

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.8.10-7F52FF.svg?logo=kotlin)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/platform-Android-3DDC84.svg?logo=android)](https://developer.android.com)
[![minSdk](https://img.shields.io/badge/minSdk-28-orange.svg)](https://developer.android.com)

A configurable, reusable Android vehicle plate-number input: country picker, category/letter
rules, live visual preview, and validation — built so a default country and a set of
"other country" options are configuration, not hardcoded logic.

Originally extracted from a Qatar-based fleet app's Customer Identification screen, where
the plate-country picker had grown into ~40% of one fragment's code. Splitting it into a
library made that logic testable on its own, and reusable in any project that needs a
similar "which country's plate is this" input — GCC-region apps in particular, but the
core is not GCC-specific.

## Supported Plates

The library provides high-fidelity, dimension-accurate renderings for vehicle plates across the GCC region.

![Animated Plate Gallery](art/preview.svg)

### 🇰🇼 Kuwait Styles (Full Collection)
- **Standard & Long:** Standard (33x15), European Long (52x11), and Small formats.
- **Transport & Public:** Public (90), Public Transport (91), Buses (92), Taxis (93), Goods Exportation (95).
- **Security & Specialty:** Police, National Guard, Firefighting, Army, Construction, Export.
- **Diplomatic & Emiri:** Emiri Bureau, Emiri Guard, Diplomatic (with unique red band).
- **Motorcycles:** Square formats for both Private and Police bikes.

### 🇶🇦 Qatar Styles
- **Comprehensive:** Private, Taxi, Police, ISF, Diplomatic, Government.
- **Dynamic Rendering:** National maroon strip with serrated edge and sub-style specific badges.

### 🇸🇦 Saudi Arabia, 🇧🇭 Bahrain, 🇴🇲 Oman, 🇦🇪 UAE
- **Saudi Arabia:** Accurate Palm & Crossed Swords with bilingual 2x2 grid.
- **Bahrain:** High-detail serrated national flag.
- **Oman:** Wide format with national seal.
- **UAE:** Region-specific emirate layouts (Dubai, Abu Dhabi, and more).

## Features

- **Configurable, not hardcoded.** Default country, enabled "other countries," and excluded
  countries are all set via a builder — no fragment/activity code branches on country.
- **Country-agnostic core.** `PlateCountryDefinition` is an open data class, not a closed
  enum, so any country's plate rules can be added without touching library internals.
- **Zero host-resource coupling.** `platekit-android` ships its own colors/drawables/strings;
  it never reaches into a consuming app's resources, so it drops into any project as-is.
- **Live visual preview.** Country-specific plate templates (including Qatar's
  taxi/police/diplomatic/government sub-styles) render as the user types.
- **Dynamic UI hooks.** Use `setOnCountryChangeListener` to adapt your host app's UI (like
  showing/hiding extra vehicle-type options) as the user switches between countries.
- **Fully unit-tested logic.** `platekit-core` has no Android dependency, so the
  build/validate rules are tested as plain JVM code, no emulator required.

## Modules

- **`platekit-core`** — pure Kotlin/JVM, zero Android dependency. Country definitions,
  category rules (single dropdown / two letters / three letters / none), the catalog/builder
  that describes one deployment's configuration, the plate-number builder + validator, and
  the visual-template resolver. Fully unit tested.
- **`platekit-android`** — the `PlateInputView` widget (country/category pickers, live
  preview, plate-number field) plus the supporting `VehiclePlateTemplateView`/
  `GccPlatePreviewView` rendering views. Ships its own colors/drawables/strings so it never
  depends on a host app's resources.
- **`sample`** — a minimal standalone demo Activity (Qatar default).
- **`dummy-sample`** — a second demo app showing how to default to Kuwait and handle dynamic
  UI changes (`com.example.platesample.dummy`).

## Quick start

```kotlin
binding.plateInputView.configure(
    PlateCountryCatalog.builder()
        .defaultCountry(PlateCountries.QATAR)          // primary/simple mode
        .enableCountries(*PlateCountries.gcc.toTypedArray())   // "other country" picker
        .excludeCountries(PlateCountries.JORDAN, PlateCountries.EGYPT) // optional: exclude multiple
        .unlistedCountryPolicy(UnlistedCountryPolicy.GENERIC_FALLBACK)
        .build()
)

binding.searchButton.setOnClickListener {
    when (val result = binding.plateInputView.getFormattedPlateNumber()) {
        is PlateInputResult.Valid -> submit(result.plateNumber)
        is PlateInputResult.Invalid -> showError(result.message)
    }
}
```

Switching the default country for a different deployment (e.g. Qatar → Kuwait) is a
one-line change to `defaultCountry(...)` — no code elsewhere needs to change.

## Built-in countries

Bahrain, Saudi Arabia, Kuwait, UAE, Qatar, Oman, Egypt, Jordan (`PlateCountries.gcc`).
`PlateCountryDefinition` is an open data class, not a closed enum — add your own for any
country not covered here.

## API Reference

### Custom UI (headless usage)

`platekit-android`'s views are optional convenience on top of `platekit-core`, which has
**zero Android dependency**. If you want your own fields in your own layout with your own
colors, skip `PlateInputView` entirely and call `platekit-core` directly — it's the exact
same code the widget itself calls, so your custom UI stays just as accurate:

```kotlin
val country = PlateCountries.OMAN
val template = country.templateResolver.resolve(region = "", vehicleTypeCode = "", vehicleTypeName = "")
// country.categoryMode, country.categoryOptions(), template.numberMaxLength drive your own fields

// Oman's letter field may hold a Latin letter, an Arabic letter, or the "None" sentinel --
// always normalize before validating/building. Same function PlateInputView uses internally,
// so a future fix to it reaches your custom UI automatically.
val letters = if (country.code == "OMN") PlateCountries.canonicalOmanLetter(rawLetterFieldText) else rawLetterFieldText

// Your own plate-number EditText should reuse the library's digits-only restriction too --
// every GCC plate number is digits-only, verified per country.
numberEditText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
numberEditText.filters = PlateInputFilters.digitsOnly(template.numberMaxLength)

val result = PlateNumberValidator.validate(
    selectedCountry = country, rawNumber = "1234", maximumLength = template.numberMaxLength, selectedLetters = letters
)
if (result is PlateValidationResult.Valid) {
    val formatted = PlateNumberBuilder.build(country, rawNumber = "1234", selectedLetters = letters)
}
```

### `platekit-core` (module `com.developer.platekit.core`)

| Class / object | Members |
|---|---|
| `PlateCountries` | `gcc: List<PlateCountryDefinition>` — Bahrain, Saudi Arabia, Kuwait, UAE, Qatar, Oman, Egypt, Jordan. Also exposes the built-in constants directly: `QATAR`, `BAHRAIN`, `SAUDI_ARABIA`, `KUWAIT`, `UAE`, `OMAN`, `EGYPT`, `JORDAN`. `uaeStates: List<String>` (7 emirates). `singleLetters: List<String>` (A-Z). `omanLetterArabicByLatin` / `omanLetterLatinByArabic: Map<String,String>` for Oman's dual-script category letters. `OMAN_NO_LETTER_OPTION: String` — the "clear this letter" sentinel offered in Oman's category dropdown. `canonicalOmanLetter(value: String): String` — normalizes a raw Oman letter field (Latin, Arabic, the "None" sentinel, or blank) to the canonical Latin code; the one shared place this conversion lives, call it before validating/building if you're not using `PlateInputView`. |
| `PlateCountryDefinition` | Data class describing one country's rules: `code`, `displayName`, `categoryMode: PlateCategoryInputMode`, `categoryOptions(): List<String>`, `regionOptions: List<String>` (UAE only), `flagKey`, `templateResolver: PlateTemplateResolver`, `countryId`, `nameAr`. Not a closed enum — construct your own for a country outside the built-in set. |
| `PlateCategoryInputMode` | Enum: `NONE` (digits only, e.g. Qatar's legacy no-category mode), `SINGLE_DROPDOWN` (one value from `categoryOptions()`, e.g. Kuwait 1-99, Jordan 100-999 + PM/GV/CD/PR/SN, UAE letter/AA-FF/1-55, Qatar Q/T/R, Bahrain PRIVATE/DIPLOMAT/FOR HIRE), `TWO_LETTERS` (Oman — 2nd letter optional), `THREE_LETTERS` (Saudi Arabia, Egypt — all 3 required). |
| `PlateTemplateResolver` | `fun interface`: `resolve(region, vehicleTypeCode, vehicleTypeName): VehiclePlateTemplate`. Every `PlateCountryDefinition` carries one by default via `VehiclePlateTemplates.resolve`. |
| `PlateCountryCatalog` | Configuration for one deployment. Build via `PlateCountryCatalog.builder()`: `.defaultCountry(country)` (required), `.enableCountries(vararg/Collection)`, `.excludeCountries(vararg)` / `.excludeCountryCodes(vararg)` (always wins over enabling), `.unlistedCountryPolicy(UnlistedCountryPolicy)`, `.build()`. Instance: `defaultCountry`, `enabledCountries`, `visibleCountries` (default + enabled, deduped), `findByCode(code): PlateCountryDefinition?`. |
| `UnlistedCountryPolicy` | Enum: `GENERIC_FALLBACK` (still searchable, generic plate visual) or `HIDDEN` (not offered), for any country not explicitly enabled. |
| `PlateNumberValidator` | `validate(selectedCountry, rawNumber, maximumLength, selectedCategoryCode = "", selectedLetters = "", selectedRegion = ""): PlateValidationResult`. `selectedCountry == null` always returns `Valid` (legacy default-country pass-through). Otherwise: digits-only + within `maximumLength`, category/letters required per `categoryMode` (Oman needs only the first letter), region required if `regionOptions` is non-empty. |
| `PlateValidationResult` / `PlateValidationReason` | `Valid` or `Invalid(reason)`. Reasons: `NUMBER_INVALID_OR_TOO_LONG`, `CATEGORY_NOT_SELECTED`, `LETTERS_INCOMPLETE`, `REGION_NOT_SELECTED` — map each to your own localized message. |
| `PlateNumberBuilder` | `build(selectedCountry, rawNumber, selectedCategoryCode = "", selectedLetters = ""): String` — the final string for a search/verify API. `selectedCountry == null` returns the raw digits untouched; otherwise category/letters are prefixed, joined by `-`. |
| `VehiclePlateTemplates` | `resolve(countryCode, region = "", vehicleTypeCode = "", vehicleTypeName = ""): VehiclePlateTemplate` — the country/vehicle-type-specific colors, badge text, and `numberMaxLength` (includes Qatar's private/taxi/police/diplomatic/government sub-styles and Kuwait's 16 sub-templates, matched by vehicle-type string). |
| `VehiclePlateTemplate` | Data class: `layout: VehiclePlateLayout`, `backgroundColor`/`textColor`/`badgeColor`/`badgeTextColor` (ARGB Int), `badgeTop`/`badgeBottom`/`headerText`/`footerText` (String), `headerColor`/`headerTextColor`/`footerTextColor`, `showCategory: Boolean`, `categoryTextColor`, `numberMaxLength: Int` (the real per-country/plate-type digit cap — this is the source of truth, not a UI constant), `widthDp`/`heightDp`. |
| `VehiclePlateLayout` | Enum of visual layouts the renderer understands: `QATAR`, `QATAR_DIPLOMATIC`, `QATAR_POLICE`, `QATAR_ISF`, `SAUDI`, `UAE_DUBAI`, `UAE_ABU_DHABI`, `UAE_EMIRATE`, `KUWAIT`, `KUWAIT_LONG`, `KUWAIT_SMALL`, `KUWAIT_SQUARE`, `KUWAIT_DIPLOMATIC`, `BAHRAIN`, `OMAN`, `GENERIC`. |

### `platekit-android` (module `com.developer.platekit.android`)

| Class | Members |
|---|---|
| `PlateInputView` | `configure(catalog: PlateCountryCatalog)` — call once before anything else. `setOnCountryChangeListener(listener: (PlateCountryDefinition?) -> Unit)` — fires whenever the selected country changes (either mode). `setModeIcons(primaryIconRes, alternateIconRes)`, `setVehicleType(code, name)` (drives which visual sub-template renders), `setInputEnabled(enabled)`, `rawNumber(): String`, `setRawNumberFocusChangeListener(listener)`, `reset()`, `getFormattedPlateNumber(): PlateInputResult` — validates (alternate mode only) then builds the final string. |
| `PlateInputResult` | `Valid(plateNumber, countryCode?, countryName?, countryId?, countryNameAr?)` (last four populated only in alternate/GCC mode) or `Invalid(message)` (already localized via the view's string resources). |
| `VehiclePlateTemplateView` (open) | The actual plate-drawing custom `View` — usable completely standalone, anywhere in your own layout. `render(countryCode, region = "", vehicleTypeCode = "", vehicleTypeName = "", categoryValue = "", plateNumber = "")` or `render(template: VehiclePlateTemplate, categoryValue, plateNumber)` for a pre-resolved template. `currentNumberMaxLength: Int` (read-only) reflects the last-rendered template's digit cap. |
| `GccPlatePreviewView` | Compatibility subclass of `VehiclePlateTemplateView` with an older `render(flagRes, countryCode, countryName, category, plateNumber, showCategory)` signature; delegates straight through. |
| `FlagProvider` | `fun interface`: `flagFor(countryCode): Int?` — supply your own flag drawable per country without this library ever shipping or referencing your resources. |
| `PlateInputFilters` | `digitsOnly(maxLength: Int): Array<InputFilter>` — the same digits-only + max-length `InputFilter`s `PlateInputView` applies to its own number field. Set on your own custom plate-number `EditText` (with `inputType = InputType.TYPE_CLASS_NUMBER`) to get identical, future-proof behavior without reimplementing the filter. |

## Build Requirements

- **JDK 1.8+** (Configured with `jvmTarget = "1.8"` for maximum compatibility across environments).
- **Android SDK 34** (Compile SDK).

## Status

Early extraction, not yet published to Maven/JitPack. Package name (`com.developer.platekit`)
and API are still subject to change before a 1.0 release.

## Contributing

Issues and pull requests are welcome — especially new `PlateCountryDefinition`s for
countries outside the current GCC set. No formal process yet; open an issue to discuss
anything non-trivial before sending a PR.

## License

Apache License 2.0 — see [LICENSE](LICENSE).
