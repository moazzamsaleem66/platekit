package com.developer.platekit.core

/**
 * How a country's "category" portion of a plate is entered, on top of the raw digits.
 *
 * - [NONE]: no category at all — just digits (e.g. Qatar).
 * - [SINGLE_DROPDOWN]: one value picked from [PlateCountryDefinition.categoryOptions]
 *   (e.g. Kuwait's 1-99 code, Jordan's 100-999 code, UAE's letter/number code).
 * - [TWO_LETTERS] / [THREE_LETTERS]: that many letters picked from
 *   [PlateCountryDefinition.categoryOptions] (e.g. Oman, Saudi Arabia, Egypt).
 */
enum class PlateCategoryInputMode { NONE, SINGLE_DROPDOWN, TWO_LETTERS, THREE_LETTERS }
