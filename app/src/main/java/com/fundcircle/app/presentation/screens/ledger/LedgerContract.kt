package com.fundcircle.app.presentation.screens.ledger

import com.fundcircle.app.core.common.DateUtils
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.presentation.common.UiAction
import com.fundcircle.app.presentation.common.UiEffect
import com.fundcircle.app.presentation.common.UiState
import java.util.TimeZone

enum class LedgerTab {
    DEPOSITS,
    EXPENSES
}

/**
 * An inclusive calendar-day range picked in the M3 DateRangePicker.
 *
 * Both bounds are the picker's own values: UTC midnight of the chosen calendar day. A transaction
 * matches when its timestamp falls on one of those calendar days in the device's time zone, so a
 * deposit made at 23:30 local time is never pushed into the next or previous day.
 */
data class LedgerDateRange(
    val startUtcDay: Long,
    val endUtcDay: Long
) {
    init {
        require(startUtcDay <= endUtcDay) { "startUtcDay must not be after endUtcDay" }
    }

    /** True when the user picked one day, so the chip shows that date once instead of "X – X". */
    val isSingleDay: Boolean
        get() = startUtcDay == endUtcDay

    fun contains(timestamp: Long, timeZone: TimeZone = TimeZone.getDefault()): Boolean {
        val day = DateUtils.localEpochToDatePickerUtc(timestamp, timeZone)
        return day in startUtcDay..endUtcDay
    }
}

data class LedgerUiState(
    val isLoading: Boolean = true,
    val selectedTab: LedgerTab = LedgerTab.DEPOSITS,
    val deposits: List<Deposit> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val filterPaymentMethod: PaymentMethod? = null,
    val filterCategory: ExpenseCategory? = null,
    val filterDateRange: LedgerDateRange? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isOffline: Boolean = false,
    val isAdmin: Boolean = false,
    val isLocked: Boolean = false,
    val currency: Currency = Currency.BDT,
    val errorMessage: UiText? = null
) : UiState

sealed interface LedgerUiAction : UiAction {
    data class SelectTab(val tab: LedgerTab) : LedgerUiAction
    data class UpdateSearchQuery(val query: String) : LedgerUiAction
    data class FilterByPaymentMethod(val method: PaymentMethod?) : LedgerUiAction
    data class FilterByCategory(val category: ExpenseCategory?) : LedgerUiAction
    /** `null` clears the date filter (All Dates). */
    data class FilterByDateRange(val range: LedgerDateRange?) : LedgerUiAction
    data class ClickTransaction(val txId: String, val isExpense: Boolean) : LedgerUiAction
    data object ClickAddTransaction : LedgerUiAction
    data object ToggleSearch : LedgerUiAction
    data object DismissError : LedgerUiAction
    data object NavigateBack : LedgerUiAction
}

sealed interface LedgerUiEffect : UiEffect {
    data class NavigateToDetail(val fundId: String, val txId: String, val isExpense: Boolean) : LedgerUiEffect
    data class OpenAddTransactionSheet(val fundId: String) : LedgerUiEffect
    data class ShowSnackbar(val message: UiText) : LedgerUiEffect
    data object NavigateBack : LedgerUiEffect
}
