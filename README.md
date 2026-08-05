# Platekit

A configurable, reusable Android vehicle plate-number input: country picker, category/letter
rules, live visual preview, and validation — built so a default country and a set of
"other country" options are configuration, not hardcoded logic.

Originally extracted from a Qatar-based fleet app's Customer Identification screen, where
the plate-country picker had grown into ~40% of one fragment's code. Splitting it into a
library made that logic testable on its own, and reusable in any project that needs a
similar "which country's plate is this" input — GCC-region apps in particular, but the
core is not GCC-specific.

## Modules

- **`platekit-core`** — pure Kotlin/JVM, zero Android dependency. Country definitions,
  category rules (single dropdown / two letters / three letters / none), the catalog/builder
  that describes one deployment's configuration, the plate-number builder + validator, and
  the visual-template resolver. Fully unit tested.
- **`platekit-android`** — the `PlateInputView` widget (country/category pickers, live
  preview, plate-number field) plus the supporting `VehiclePlateTemplateView`/
  `GccPlatePreviewView` rendering views. Ships its own colors/drawables/strings so it never
  depends on a host app's resources.
- **`sample`** — a minimal standalone demo Activity.

## Quick start

```kotlin
binding.plateInputView.configure(
    PlateCountryCatalog.builder()
        .defaultCountry(PlateCountries.QATAR)          // primary/simple mode
        .enableCountries(*PlateCountries.gcc.toTypedArray())   // "other country" picker
        .excludeCountries(PlateCountries.JORDAN)        // optional: drop specific ones
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

## Status

Early extraction, not yet published to Maven/JitPack. Package name (`com.developer.platekit`)
and API are still subject to change before a 1.0 release.

## License

Apache License 2.0 — see [LICENSE](LICENSE).
