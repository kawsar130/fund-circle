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

    fun formatMonthYear(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp)).uppercase(Locale.getDefault())
    }

    fun isSameDay(
        timestamp1: Long,
        timestamp2: Long,
        timeZone: java.util.TimeZone = java.util.TimeZone.getDefault()
    ): Boolean {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).apply {
            this.timeZone = timeZone
        }
        return sdf.format(Date(timestamp1)) == sdf.format(Date(timestamp2))
    }

    /**
     * Converts a UTC midnight epoch timestamp (from Material 3 DatePicker)
     * into a local timestamp on that same calendar day.
     *
     * If the selected date is today in the local timezone, returns [nowMillis].
     * Otherwise, returns 12:00:00 (midday) in the local timezone for that calendar date.
     */
    fun datePickerUtcToLocalEpoch(
        pickedUtcMidnight: Long,
        nowMillis: Long = System.currentTimeMillis(),
        localTimeZone: java.util.TimeZone = java.util.TimeZone.getDefault()
    ): Long {
        val utcCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = pickedUtcMidnight
        }
        val year = utcCal.get(java.util.Calendar.YEAR)
        val month = utcCal.get(java.util.Calendar.MONTH)
        val day = utcCal.get(java.util.Calendar.DAY_OF_MONTH)

        val localCal = java.util.Calendar.getInstance(localTimeZone).apply {
            timeInMillis = nowMillis
        }
        val isToday = year == localCal.get(java.util.Calendar.YEAR) &&
                month == localCal.get(java.util.Calendar.MONTH) &&
                day == localCal.get(java.util.Calendar.DAY_OF_MONTH)

        return if (isToday) {
            nowMillis
        } else {
            java.util.Calendar.getInstance(localTimeZone).apply {
                clear()
                set(year, month, day, 12, 0, 0)
            }.timeInMillis
        }
    }

    /**
     * Converts a local epoch timestamp into a UTC midnight epoch timestamp for initializing
     * Material 3 DatePickerState.
     */
    fun localEpochToDatePickerUtc(
        localEpochMs: Long,
        localTimeZone: java.util.TimeZone = java.util.TimeZone.getDefault()
    ): Long {
        val localCal = java.util.Calendar.getInstance(localTimeZone).apply {
            timeInMillis = localEpochMs
        }
        val year = localCal.get(java.util.Calendar.YEAR)
        val month = localCal.get(java.util.Calendar.MONTH)
        val day = localCal.get(java.util.Calendar.DAY_OF_MONTH)

        return java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, month, day, 0, 0, 0)
        }.timeInMillis
    }

    /**
     * Determines whether a UTC midnight timestamp in DatePicker is selectable (i.e. <= today locally).
     */
    fun isDatePickerDateSelectable(
        utcMidnight: Long,
        nowMillis: Long = System.currentTimeMillis(),
        localTimeZone: java.util.TimeZone = java.util.TimeZone.getDefault()
    ): Boolean {
        val localCal = java.util.Calendar.getInstance(localTimeZone).apply {
            timeInMillis = nowMillis
        }
        val todayYear = localCal.get(java.util.Calendar.YEAR)
        val todayMonth = localCal.get(java.util.Calendar.MONTH)
        val todayDay = localCal.get(java.util.Calendar.DAY_OF_MONTH)

        val maxUtcCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(todayYear, todayMonth, todayDay, 23, 59, 59)
            set(java.util.Calendar.MILLISECOND, 999)
        }
        return utcMidnight <= maxUtcCal.timeInMillis
    }
}
