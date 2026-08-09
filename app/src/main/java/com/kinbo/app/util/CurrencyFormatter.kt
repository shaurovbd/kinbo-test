package com.kinbo.app.util

import java.text.DecimalFormat

/**
 * Formats monetary amounts in Bangladeshi Taka (৳).
 */
object CurrencyFormatter {

    private val plain = DecimalFormat("#,##0.##")

    /** e.g. 1234.5 -> "৳1,234.50" */
    fun format(amount: Double): String {
        val s = if (amount == amount.toLong().toDouble()) {
            plain.format(amount.toLong())
        } else {
            DecimalFormat("#,##0.00").format(amount)
        }
        return "৳$s"
    }

    /** Compact form without decimals for large headings, e.g. ৳5,000 */
    fun formatCompact(amount: Double): String = "৳${plain.format(amount.toLong())}"
}
