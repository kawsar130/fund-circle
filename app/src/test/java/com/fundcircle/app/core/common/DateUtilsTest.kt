package com.fundcircle.app.core.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DateUtilsTest {

    @Test
    fun isDateInFuture_returnsTrueWhenTimestampIsGreaterThanCurrentTime() {
        val now = 1_700_000_000_000L
        assertThat(DateUtils.isDateInFuture(now + 1_000L, now)).isTrue()
        assertThat(DateUtils.isDateInFuture(now, now)).isFalse()
        assertThat(DateUtils.isDateInFuture(now - 1_000L, now)).isFalse()
    }

    @Test
    fun isSameDay_returnsTrueForSameCalendarDayInDefaultTimeZone() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.NOVEMBER, 19, 8, 30, 0)
        val morning = cal.timeInMillis

        cal.set(2026, Calendar.NOVEMBER, 19, 21, 45, 0)
        val night = cal.timeInMillis

        cal.set(2026, Calendar.NOVEMBER, 20, 1, 0, 0)
        val nextDay = cal.timeInMillis

        assertThat(DateUtils.isSameDay(morning, night)).isTrue()
        assertThat(DateUtils.isSameDay(morning, nextDay)).isFalse()
    }

    @Test
    fun isSameDay_respectsExplicitTimeZone() {
        val utc = TimeZone.getTimeZone("UTC")
        val cal = Calendar.getInstance(utc)
        cal.set(2026, Calendar.NOVEMBER, 19, 8, 30, 0)
        val morningUtc = cal.timeInMillis

        cal.set(2026, Calendar.NOVEMBER, 19, 21, 45, 0)
        val nightUtc = cal.timeInMillis

        assertThat(DateUtils.isSameDay(morningUtc, nightUtc, utc)).isTrue()
    }

    @Test
    fun datePickerUtcToLocalEpoch_returnsCurrentTimeWhenSelectedDateIsToday() {
        val now = 1_763_550_000_000L
        val pickedUtcMidnight = DateUtils.localEpochToDatePickerUtc(now)
        val result = DateUtils.datePickerUtcToLocalEpoch(pickedUtcMidnight, now)

        assertThat(result).isEqualTo(now)
    }

    @Test
    fun datePickerUtcToLocalEpoch_returnsMiddayForPastDates() {
        val now = 1_763_550_000_000L
        val localCal = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_MONTH, -5)
        }
        val pastEpochMs = localCal.timeInMillis
        val pickedUtcMidnight = DateUtils.localEpochToDatePickerUtc(pastEpochMs)

        val result = DateUtils.datePickerUtcToLocalEpoch(pickedUtcMidnight, now)

        val resultCal = Calendar.getInstance().apply { timeInMillis = result }
        assertThat(resultCal.get(Calendar.HOUR_OF_DAY)).isEqualTo(12)
        assertThat(resultCal.get(Calendar.MINUTE)).isEqualTo(0)
        assertThat(resultCal.get(Calendar.DAY_OF_MONTH)).isEqualTo(localCal.get(Calendar.DAY_OF_MONTH))
        assertThat(resultCal.get(Calendar.MONTH)).isEqualTo(localCal.get(Calendar.MONTH))
        assertThat(resultCal.get(Calendar.YEAR)).isEqualTo(localCal.get(Calendar.YEAR))
    }

    @Test
    fun localEpochToDatePickerUtc_producesUtcMidnight() {
        val now = 1_763_550_000_000L
        val utcMidnight = DateUtils.localEpochToDatePickerUtc(now)

        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = utcMidnight
        }
        assertThat(utcCal.get(Calendar.HOUR_OF_DAY)).isEqualTo(0)
        assertThat(utcCal.get(Calendar.MINUTE)).isEqualTo(0)
        assertThat(utcCal.get(Calendar.SECOND)).isEqualTo(0)
        assertThat(utcCal.get(Calendar.MILLISECOND)).isEqualTo(0)
    }

    @Test
    fun isDatePickerDateSelectable_allowsTodayAndPastDates_rejectsFutureDates() {
        val now = 1_763_550_000_000L
        val todayUtc = DateUtils.localEpochToDatePickerUtc(now)

        val pastUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = todayUtc
            add(Calendar.DAY_OF_MONTH, -1)
        }.timeInMillis

        val futureUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = todayUtc
            add(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis

        assertThat(DateUtils.isDatePickerDateSelectable(todayUtc, now)).isTrue()
        assertThat(DateUtils.isDatePickerDateSelectable(pastUtc, now)).isTrue()
        assertThat(DateUtils.isDatePickerDateSelectable(futureUtc, now)).isFalse()
    }

    @Test
    fun formatDisplayDate_formatsTimestampCorrectly() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.NOVEMBER, 12, 10, 0, 0)
        val formatted = DateUtils.formatDisplayDate(cal.timeInMillis)

        assertThat(formatted).contains("12")
        assertThat(formatted).contains("Nov")
        assertThat(formatted).contains("2026")
    }

    @Test
    fun formatMonthYear_formatsMonthAndYearInUppercase() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.NOVEMBER, 1, 0, 0, 0)
        val formatted = DateUtils.formatMonthYear(cal.timeInMillis)

        assertThat(formatted).contains("2026")
        assertThat(formatted).isEqualTo(formatted.uppercase())
    }
}
