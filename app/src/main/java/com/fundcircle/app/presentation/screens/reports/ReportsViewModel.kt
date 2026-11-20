package com.fundcircle.app.presentation.screens.reports

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.data.reports.ReportFileManager
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.ReportPeriod
import com.fundcircle.app.domain.model.ReportType
import com.fundcircle.app.domain.usecase.GenerateMonthlyReportUseCase
import com.fundcircle.app.domain.usecase.GenerateReportCsvUseCase
import com.fundcircle.app.domain.usecase.ObserveCircleMembersUseCase
import com.fundcircle.app.domain.usecase.ObserveCircleUseCase
import com.fundcircle.app.domain.usecase.ObserveDepositsUseCase
import com.fundcircle.app.domain.usecase.ObserveExpensesUseCase
import com.fundcircle.app.presentation.common.BaseViewModel
import com.fundcircle.app.presentation.navigation.ScreenRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeCircleUseCase: ObserveCircleUseCase,
    private val observeDepositsUseCase: ObserveDepositsUseCase,
    private val observeExpensesUseCase: ObserveExpensesUseCase,
    private val observeCircleMembersUseCase: ObserveCircleMembersUseCase,
    private val generateMonthlyReportUseCase: GenerateMonthlyReportUseCase,
    private val generateReportCsvUseCase: GenerateReportCsvUseCase,
    private val reportFileManager: ReportFileManager
) : BaseViewModel<ReportsUiState, ReportsUiAction, ReportsUiEffect>(
    run {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentYear = cal.get(Calendar.YEAR)
        ReportsUiState(
            selectedMonth = currentMonth,
            selectedYear = currentYear,
            selectedPeriod = ReportPeriod.MonthYear(currentMonth, currentYear)
        )
    }
) {

    val fundId: String = savedStateHandle.get<String>(KEY_FUND_ID)
        ?: runCatching { savedStateHandle.toRoute<ScreenRoute.Reports>().fundId }.getOrNull()
        .orEmpty()

    init {
        setState { copy(fundId = this@ReportsViewModel.fundId) }
        loadData()
    }

    private fun loadData() {
        if (fundId.isBlank()) {
            setState { copy(isLoading = false, errorMessage = UiText.StringResource(R.string.circle_error_not_found)) }
            return
        }

        viewModelScope.launch {
            combine(
                observeCircleUseCase(fundId),
                observeDepositsUseCase(fundId),
                observeExpensesUseCase(fundId),
                observeCircleMembersUseCase(fundId)
            ) { circle, deposits, expenses, members ->
                Quadruple(circle, deposits, expenses, members)
            }.catch { error ->
                setState {
                    copy(
                        isLoading = false,
                        errorMessage = UiText.DynamicString(error.localizedMessage ?: "Failed to load report data")
                    )
                }
            }.collect { (circle, deposits, expenses, members) ->
                updateReportsState(circle, deposits, expenses, members)
            }
        }
    }

    private fun updateReportsState(
        circle: FundCircle,
        deposits: List<Deposit>,
        expenses: List<Expense>,
        members: List<CircleMember>
    ) {
        val currentPeriod = uiState.value.selectedPeriod
        val monthlyReport = generateMonthlyReportUseCase(
            circle = circle,
            deposits = deposits,
            expenses = expenses,
            period = currentPeriod
        )

        setState {
            copy(
                isLoading = false,
                circle = circle,
                deposits = deposits,
                expenses = expenses,
                members = members,
                monthlyReport = monthlyReport
            )
        }
    }

    override fun onAction(action: ReportsUiAction) {
        when (action) {
            is ReportsUiAction.SelectPeriod -> {
                val circle = uiState.value.circle
                val monthlyReport = circle?.let {
                    generateMonthlyReportUseCase(
                        circle = it,
                        deposits = uiState.value.deposits,
                        expenses = uiState.value.expenses,
                        period = action.period
                    )
                }
                setState {
                    copy(
                        selectedPeriod = action.period,
                        monthlyReport = monthlyReport
                    )
                }
            }
            is ReportsUiAction.SelectMonthYear -> {
                val newPeriod = ReportPeriod.MonthYear(action.month, action.year)
                val circle = uiState.value.circle
                val monthlyReport = circle?.let {
                    generateMonthlyReportUseCase(
                        circle = it,
                        deposits = uiState.value.deposits,
                        expenses = uiState.value.expenses,
                        period = newPeriod
                    )
                }
                setState {
                    copy(
                        selectedMonth = action.month,
                        selectedYear = action.year,
                        selectedPeriod = newPeriod,
                        monthlyReport = monthlyReport
                    )
                }
            }
            is ReportsUiAction.SelectReportType -> {
                setState { copy(selectedReportType = action.type) }
            }
            is ReportsUiAction.ExportCsv -> {
                exportReportCsv()
            }
            is ReportsUiAction.DismissError -> {
                setState { copy(errorMessage = null) }
            }
        }
    }

    private fun exportReportCsv() {
        val state = uiState.value
        val circle = state.circle ?: return

        setState { copy(isExporting = true) }
        viewModelScope.launch {
            try {
                val csvContent = generateReportCsvUseCase(
                    reportType = state.selectedReportType,
                    circle = circle,
                    monthlyReport = state.monthlyReport,
                    members = state.members,
                    deposits = state.deposits,
                    expenses = state.expenses
                )

                val file = reportFileManager.saveCsvToCache(
                    circleName = circle.name,
                    reportType = state.selectedReportType,
                    csvContent = csvContent
                )
                val uri = reportFileManager.getShareUri(file)

                sendEffect(ReportsUiEffect.ShareCsv(fileUri = uri, fileName = file.name))
                setState { copy(isExporting = false) }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                setState {
                    copy(
                        isExporting = false,
                        errorMessage = UiText.StringResource(R.string.reports_error_export)
                    )
                }
            }
        }
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    companion object {
        const val KEY_FUND_ID = "fundId"
    }
}
