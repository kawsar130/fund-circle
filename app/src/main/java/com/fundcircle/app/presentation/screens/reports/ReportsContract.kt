package com.fundcircle.app.presentation.screens.reports

import android.net.Uri
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.MonthlyFinancialReport
import com.fundcircle.app.domain.model.ReportPeriod
import com.fundcircle.app.domain.model.ReportType
import com.fundcircle.app.presentation.common.UiAction
import com.fundcircle.app.presentation.common.UiEffect
import com.fundcircle.app.presentation.common.UiState

/**
 * MVI Contract for Screen 12: Financial Reports Engine & Live Preview (CUJ-08).
 * Single Source of Truth for state, intents, and one-shot effects.
 */
data class ReportsUiState(
    val isLoading: Boolean = true,
    val fundId: String = "",
    val circle: FundCircle? = null,
    val members: List<CircleMember> = emptyList(),
    val deposits: List<Deposit> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val selectedPeriod: ReportPeriod = ReportPeriod.AllTime,
    val selectedMonth: Int = 11, // 1..12
    val selectedYear: Int = 2026,
    val selectedReportType: ReportType = ReportType.MONTHLY_SUMMARY,
    val monthlyReport: MonthlyFinancialReport? = null,
    val isExporting: Boolean = false,
    val errorMessage: UiText? = null
) : UiState

sealed interface ReportsUiAction : UiAction {
    data class SelectPeriod(val period: ReportPeriod) : ReportsUiAction
    data class SelectMonthYear(val month: Int, val year: Int) : ReportsUiAction
    data class SelectReportType(val type: ReportType) : ReportsUiAction
    data object ExportCsv : ReportsUiAction
    data object DismissError : ReportsUiAction
}

sealed interface ReportsUiEffect : UiEffect {
    data class ShareCsv(val fileUri: Uri, val fileName: String) : ReportsUiEffect
    data class ShowSnackbar(val message: UiText) : ReportsUiEffect
}
