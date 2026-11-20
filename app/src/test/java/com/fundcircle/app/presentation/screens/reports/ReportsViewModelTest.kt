package com.fundcircle.app.presentation.screens.reports

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.fundcircle.app.data.reports.ReportFileManager
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.model.MonthlyFinancialReport
import com.fundcircle.app.domain.model.ReportPeriod
import com.fundcircle.app.domain.model.ReportType
import com.fundcircle.app.domain.usecase.GenerateMonthlyReportUseCase
import com.fundcircle.app.domain.usecase.GenerateReportCsvUseCase
import com.fundcircle.app.domain.usecase.ObserveCircleMembersUseCase
import com.fundcircle.app.domain.usecase.ObserveCircleUseCase
import com.fundcircle.app.domain.usecase.ObserveDepositsUseCase
import com.fundcircle.app.domain.usecase.ObserveExpensesUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {

    private val observeCircleUseCase: ObserveCircleUseCase = mockk(relaxed = true)
    private val observeDepositsUseCase: ObserveDepositsUseCase = mockk(relaxed = true)
    private val observeExpensesUseCase: ObserveExpensesUseCase = mockk(relaxed = true)
    private val observeCircleMembersUseCase: ObserveCircleMembersUseCase = mockk(relaxed = true)
    private val generateMonthlyReportUseCase: GenerateMonthlyReportUseCase = mockk(relaxed = true)
    private val generateReportCsvUseCase: GenerateReportCsvUseCase = mockk(relaxed = true)
    private val reportFileManager: ReportFileManager = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private val testFundId = "circle-123"

    private val sampleCircle = FundCircle(
        id = testFundId,
        name = "Green Valley Syndicate",
        currency = Currency.BDT,
        description = "Pooled capital",
        adminId = "user-1",
        adminEmail = "admin@example.com",
        memberEmails = listOf("admin@example.com"),
        memberCount = 1,
        totalDeposits = 1000000L,
        totalExpenses = 100000L,
        recordedBalance = 900000L,
        isLocked = false,
        createdAt = 1700000000000L
    )

    private val sampleReport = MonthlyFinancialReport(
        circleName = "Green Valley Syndicate",
        currency = Currency.BDT,
        periodDisplay = "November 2026",
        openingBalance = 500000L,
        totalDeposits = 500000L,
        totalExpenses = 100000L,
        netChange = 400000L,
        closingBalance = 900000L,
        transactionCount = 4
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { observeCircleUseCase(testFundId) } returns flowOf(sampleCircle)
        every { observeDepositsUseCase(testFundId) } returns flowOf(emptyList())
        every { observeExpensesUseCase(testFundId) } returns flowOf(emptyList())
        every { observeCircleMembersUseCase(testFundId) } returns flowOf(emptyList())
        every { generateMonthlyReportUseCase(any(), any(), any(), any()) } returns sampleReport
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ReportsViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("fundId" to testFundId))
        return ReportsViewModel(
            savedStateHandle = savedStateHandle,
            observeCircleUseCase = observeCircleUseCase,
            observeDepositsUseCase = observeDepositsUseCase,
            observeExpensesUseCase = observeExpensesUseCase,
            observeCircleMembersUseCase = observeCircleMembersUseCase,
            generateMonthlyReportUseCase = generateMonthlyReportUseCase,
            generateReportCsvUseCase = generateReportCsvUseCase,
            reportFileManager = reportFileManager
        )
    }

    @Test
    fun `initial load populates circle and computes monthly report`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.circle).isEqualTo(sampleCircle)
        assertThat(state.monthlyReport).isEqualTo(sampleReport)
    }

    @Test
    fun `SelectPeriod updates period and triggers live preview recomputation`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val allTimeReport = sampleReport.copy(periodDisplay = "All Time")
        every {
            generateMonthlyReportUseCase(sampleCircle, any(), any(), ReportPeriod.AllTime)
        } returns allTimeReport

        viewModel.onAction(ReportsUiAction.SelectPeriod(ReportPeriod.AllTime))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.selectedPeriod).isEqualTo(ReportPeriod.AllTime)
        assertThat(state.monthlyReport?.periodDisplay).isEqualTo("All Time")
    }

    @Test
    fun `SelectReportType updates selectedReportType in state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReportsUiAction.SelectReportType(ReportType.DEPOSIT_HISTORY))
        assertThat(viewModel.uiState.value.selectedReportType).isEqualTo(ReportType.DEPOSIT_HISTORY)
    }

    @Test
    fun `SelectMonthYear updates month, year and period in state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val decReport = sampleReport.copy(periodDisplay = "December 2026")
        every {
            generateMonthlyReportUseCase(sampleCircle, any(), any(), ReportPeriod.MonthYear(12, 2026))
        } returns decReport

        viewModel.onAction(ReportsUiAction.SelectMonthYear(12, 2026))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.selectedMonth).isEqualTo(12)
        assertThat(state.selectedYear).isEqualTo(2026)
        assertThat(state.selectedPeriod).isEqualTo(ReportPeriod.MonthYear(12, 2026))
        assertThat(state.monthlyReport?.periodDisplay).isEqualTo("December 2026")
    }

    @Test
    fun `ExportCsv generates CSV, caches file and sends ShareCsv effect`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val mockFile = mockk<File>(relaxed = true) {
            every { name } returns "FundCircle_Report.csv"
        }
        val mockUri = mockk<Uri>(relaxed = true)

        every { generateReportCsvUseCase(any(), any(), any(), any(), any(), any()) } returns "CSV_CONTENT"
        every { reportFileManager.saveCsvToCache(any(), any(), any()) } returns mockFile
        every { reportFileManager.getShareUri(mockFile) } returns mockUri

        viewModel.uiEffect.test {
            viewModel.onAction(ReportsUiAction.ExportCsv)
            advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(ReportsUiEffect.ShareCsv::class.java)
            val shareEffect = effect as ReportsUiEffect.ShareCsv
            assertThat(shareEffect.fileUri).isEqualTo(mockUri)
            assertThat(shareEffect.fileName).isEqualTo("FundCircle_Report.csv")
        }
    }
}
