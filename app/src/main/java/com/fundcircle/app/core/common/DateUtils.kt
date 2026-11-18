package com.fundcircle.app.core.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private const val DISPLAY_DATE_FORMAT = "dd MMM yyyy"
    private const val DISPLAY_TIME_FORMAT = "hh:mm a"
    private const val DISPLAY_DATE_TIME_FORMAT = "dd MMM yyyy, hh:mm a"

    /**
     * Enforces the architectural rule: transactions cannot be recorded with future timestamps.
     */
    fun isDateInFuture(timestamp: Long, currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        return timestamp > currentTimeMillis
    }

    fun formatDisplayDate(timestamp: Long): String {
        val sdf = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDisplayTime(timestamp: Long): String {
        val sdf = SimpleDateFormat(DISPLAY_TIME_FORMAT, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDisplayDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat(DISPLAY_DATE_TIME_FORMAT, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
