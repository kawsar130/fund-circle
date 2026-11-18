package com.fundcircle.app.domain.model

/**
 * Supported base currencies for Fund Circle.
 * Permanently locked once initialized for a circle.
 * Whole-integer monetary invariant strictly applies across all currencies.
 */
enum class Currency(val symbol: String) {
    BDT(symbol = "৳"),
    USD(symbol = "$"),
    EUR(symbol = "€");

    companion object {
        fun fromString(value: String): Currency = when (value.trim().uppercase()) {
            "BDT" -> BDT
            "USD" -> USD
            "EUR" -> EUR
            else -> throw IllegalArgumentException("Unsupported currency: $value. Supported currencies are BDT, USD, EUR.")
        }

        fun fromStringOrNull(value: String?): Currency? = when (value?.trim()?.uppercase()) {
            "BDT" -> BDT
            "USD" -> USD
            "EUR" -> EUR
            else -> null
        }
    }
}
