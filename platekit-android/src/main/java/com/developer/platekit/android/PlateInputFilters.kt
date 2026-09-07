package com.developer.platekit.android

import android.text.InputFilter

/**
 * Digits-only + max-length [InputFilter]s for a raw plate-number field -- the exact
 * restriction [PlateInputView] applies to its own number field, verified against every
 * GCC country's real plate format (always numeric).
 *
 * Exposed publicly so a host app building its own custom plate-number `EditText` (instead
 * of using [PlateInputView]) gets identical, future-proof behavior:
 * ```
 * editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
 * editText.filters = PlateInputFilters.digitsOnly(maxLength)
 * ```
 * Any future fix to this filter then reaches that field automatically, with no change
 * needed on the host app's side.
 */
object PlateInputFilters {
    fun digitsOnly(maxLength: Int): Array<InputFilter> =
        arrayOf(InputFilter.LengthFilter(maxLength), DIGITS_ONLY_FILTER)

    /** Rejects any non-digit character as it's typed/pasted, rather than only catching it
     *  later at submit-time validation. */
    private val DIGITS_ONLY_FILTER = InputFilter { source, start, end, _, _, _ ->
        val filtered = source.subSequence(start, end).filter(Char::isDigit)
        if (filtered.length == end - start) null else filtered
    }
}
