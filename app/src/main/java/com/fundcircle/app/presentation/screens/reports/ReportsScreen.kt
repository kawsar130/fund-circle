package com.fundcircle.app.presentation.screens.reports

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fundcircle.app.R
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.MonthlyFinancialReport
import com.fundcircle.app.domain.model.ReportPeriod
import com.fundcircle.app.domain.model.ReportType
import com.fundcircle.app.presentation.theme.DesignTokens
import com.fundcircle.app.presentation.theme.FundCircleTheme
import java.text.DateFormatSymbols
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ReportsRoute(
    fundId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ReportsUiEffect.ShareCsv -> {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, effect.fileUri)
                        putExtra(Intent.EXTRA_SUBJECT, effect.fileName)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(
                        sendIntent,
                        context.getString(R.string.reports_share_chooser_title)
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooser)
                }
                is ReportsUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error.asString(context))
            viewModel.onAction(ReportsUiAction.DismissError)
        }
    }

    ReportsScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    state: ReportsUiState,
    onAction: (ReportsUiAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.reports_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(DesignTokens.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.reports_back_cd)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (state.isLoading && state.circle == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else {
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding)
                    .padding(horizontal = DesignTokens.Spacing16)
            ) {
                item(key = "spacer_top") {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                }

                // Section 1: Reporting Period Selector
                item(key = "section_period") {
                    PeriodSelectorCard(
                        selectedPeriod = state.selectedPeriod,
                        selectedMonth = state.selectedMonth,
                        selectedYear = state.selectedYear,
                        onSelectPeriod = { onAction(ReportsUiAction.SelectPeriod(it)) },
                        onSelectMonthYear = { month, year ->
                            onAction(ReportsUiAction.SelectMonthYear(month, year))
                        }
                    )
                }

                item(key = "spacer_between_1") {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing16))
                }

                // Section 2: Report Type Selector
                item(key = "section_type") {
                    ReportTypeSelectorCard(
                        selectedReportType = state.selectedReportType,
                        onSelectType = { onAction(ReportsUiAction.SelectReportType(it)) }
                    )
                }

                item(key = "spacer_between_2") {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing16))
                }

                // Section 3: On-Screen Live Preview Card
                item(key = "section_preview") {
                    LivePreviewCard(
                        circle = state.circle,
                        monthlyReport = state.monthlyReport,
                        selectedReportType = state.selectedReportType
                    )
                }

                item(key = "spacer_between_3") {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing24))
                }

                // Section 4: Primary Export Action Button (56dp)
                item(key = "section_export_btn") {
                    Button(
                        onClick = { onAction(ReportsUiAction.ExportCsv) },
                        enabled = !state.isExporting && state.circle != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (state.isExporting) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                            Text(
                                text = stringResource(R.string.reports_exporting),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                            Text(
                                text = stringResource(R.string.reports_btn_export_csv),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item(key = "spacer_bottom") {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing32))
                }
            }
        }
    }
}

/**
 * Period selection card supporting Month/Year vs All Time filtering.
 */
@Composable
fun PeriodSelectorCard(
    selectedPeriod: ReportPeriod,
    selectedMonth: Int,
    selectedYear: Int,
    onSelectPeriod: (ReportPeriod) -> Unit,
    onSelectMonthYear: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(DesignTokens.Spacing16)) {
            Text(
                text = stringResource(R.string.reports_section_period),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

            // Segmented buttons: Month & Year vs All Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing8)
            ) {
                val isMonthYear = selectedPeriod is ReportPeriod.MonthYear
                if (isMonthYear) {
                    Button(
                        onClick = { /* already selected */ },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing4))
                        Text(
                            text = stringResource(R.string.reports_period_month_year),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = { onSelectPeriod(ReportPeriod.MonthYear(selectedMonth, selectedYear)) },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.reports_period_month_year),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                val isAllTime = selectedPeriod is ReportPeriod.AllTime
                if (isAllTime) {
                    Button(
                        onClick = { /* already selected */ },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing4))
                        Text(
                            text = stringResource(R.string.reports_period_all_time),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = { onSelectPeriod(ReportPeriod.AllTime) },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.reports_period_all_time),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Month and Year Selectors if Month & Year is selected
            AnimatedVisibility(
                visible = selectedPeriod is ReportPeriod.MonthYear,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing12))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing8)
                    ) {
                        MonthDropdown(
                            selectedMonth = selectedMonth,
                            onMonthSelected = { onSelectMonthYear(it, selectedYear) },
                            modifier = Modifier.weight(1.5f)
                        )

                        YearDropdown(
                            selectedYear = selectedYear,
                            onYearSelected = { onSelectMonthYear(selectedMonth, it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthDropdown(
    selectedMonth: Int,
    onMonthSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val months = remember { DateFormatSymbols(Locale.US).months.take(12) }
    val currentMonthName = months.getOrNull(selectedMonth - 1) ?: "Month"

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = currentMonthName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            months.forEachIndexed { index, name ->
                val monthIndex = index + 1
                DropdownMenuItem(
                    text = {
                        Text(
                            text = name,
                            fontWeight = if (monthIndex == selectedMonth) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        expanded = false
                        onMonthSelected(monthIndex)
                    }
                )
            }
        }
    }
}

@Composable
fun YearDropdown(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val years = remember { (2024..2030).toList() }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = selectedYear.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = year.toString(),
                            fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        expanded = false
                        onYearSelected(year)
                    }
                )
            }
        }
    }
}

/**
 * Report Type selector radio group.
 */
@Composable
fun ReportTypeSelectorCard(
    selectedReportType: ReportType,
    onSelectType: (ReportType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(DesignTokens.Spacing16)) {
            Text(
                text = stringResource(R.string.reports_section_report_type),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing8))

            val types = listOf(
                ReportType.MONTHLY_SUMMARY to stringResource(R.string.reports_type_monthly_summary),
                ReportType.MEMBER_STATEMENT to stringResource(R.string.reports_type_member_statement),
                ReportType.DEPOSIT_HISTORY to stringResource(R.string.reports_type_deposit_history),
                ReportType.EXPENSE_HISTORY to stringResource(R.string.reports_type_expense_history)
            )

            types.forEach { (type, label) ->
                val isSelected = selectedReportType == type
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                        .clickable(role = Role.RadioButton) { onSelectType(type) }
                        .padding(vertical = DesignTokens.Spacing4),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        modifier = Modifier.size(DesignTokens.MinTouchTarget)
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Live rendered on-screen statement preview card.
 * Invariant 1: Financial amounts are whole 64-bit Long integers formatted with tabular digits (tnum).
 */
@Composable
fun LivePreviewCard(
    circle: FundCircle?,
    monthlyReport: MonthlyFinancialReport?,
    selectedReportType: ReportType = ReportType.MONTHLY_SUMMARY,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(DesignTokens.Spacing16)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.reports_preview_title),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                val typeLabel = when (selectedReportType) {
                    ReportType.MONTHLY_SUMMARY -> stringResource(R.string.reports_type_monthly_summary)
                    ReportType.MEMBER_STATEMENT -> stringResource(R.string.reports_type_member_statement)
                    ReportType.DEPOSIT_HISTORY -> stringResource(R.string.reports_type_deposit_history)
                    ReportType.EXPENSE_HISTORY -> stringResource(R.string.reports_type_expense_history)
                }

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing4))

            Text(
                text = circle?.name.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(
                    R.string.reports_preview_statement_period,
                    monthlyReport?.periodDisplay ?: "All Time"
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val typeDesc = when (selectedReportType) {
                ReportType.MONTHLY_SUMMARY -> stringResource(R.string.reports_preview_desc_monthly_summary)
                ReportType.MEMBER_STATEMENT -> stringResource(R.string.reports_preview_desc_member_statement)
                ReportType.DEPOSIT_HISTORY -> stringResource(R.string.reports_preview_desc_deposit_history)
                ReportType.EXPENSE_HISTORY -> stringResource(R.string.reports_preview_desc_expense_history)
            }
            Text(
                text = typeDesc,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing12))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

            val currency = circle?.currency ?: Currency.BDT
            val report = monthlyReport

            if (report != null) {
                // Opening Balance
                StatementRow(
                    label = stringResource(R.string.reports_preview_opening_balance),
                    amount = report.openingBalance,
                    currency = currency
                )

                Spacer(modifier = Modifier.height(DesignTokens.Spacing8))

                // Total Posted Deposits
                StatementRow(
                    label = stringResource(
                        R.string.reports_preview_posted_deposits,
                        report.periodDisplay
                    ),
                    amount = report.totalDeposits,
                    currency = currency,
                    prefix = "+ ",
                    textColor = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(DesignTokens.Spacing8))

                // Total Posted Expenses
                StatementRow(
                    label = stringResource(
                        R.string.reports_preview_posted_expenses,
                        report.periodDisplay
                    ),
                    amount = report.totalExpenses,
                    currency = currency,
                    prefix = "- ",
                    textColor = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(DesignTokens.Spacing8))

                // Net Change
                val netLabel = if (report.periodDisplay == "All Time") {
                    stringResource(R.string.reports_preview_net_change_all_time)
                } else {
                    stringResource(R.string.reports_preview_net_change)
                }
                val netPrefix = if (report.netChange >= 0) "+ " else "- "
                StatementRow(
                    label = netLabel,
                    amount = kotlin.math.abs(report.netChange),
                    currency = currency,
                    prefix = netPrefix,
                    textColor = if (report.netChange >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    isBold = true
                )

                Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(DesignTokens.Spacing8))

                // Closing Recorded Balance
                StatementRow(
                    label = stringResource(R.string.reports_preview_closing_balance),
                    amount = report.closingBalance,
                    currency = currency,
                    isBold = true,
                    textColor = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

                // Transaction Count footnote
                Text(
                    text = stringResource(R.string.reports_preview_tx_count, report.transactionCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = stringResource(R.string.reports_empty_preview),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = DesignTokens.Spacing16)
                )
            }
        }
    }
}

@Composable
fun StatementRow(
    label: String,
    amount: Long,
    currency: Currency,
    modifier: Modifier = Modifier,
    prefix: String = "",
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    val formattedAmount = NumberFormat.getIntegerInstance(Locale.US).format(amount)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f, fill = false)
        )
        Text(
            text = "$prefix${currency.symbol} $formattedAmount",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFeatureSettings = "tnum"
            ),
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Preview(name = "Reports Screen - Light", showBackground = true)
@Composable
fun ReportsScreenPreview_Month() {
    FundCircleTheme {
        ReportsScreen(
            state = ReportsUiState(
                isLoading = false,
                circle = FundCircle(
                    id = "fund-1",
                    name = "Green Valley Property Pool",
                    currency = Currency.BDT,
                    description = "Syndicate investment fund",
                    adminId = "usr-1",
                    adminEmail = "admin@example.com",
                    memberEmails = listOf("admin@example.com"),
                    memberCount = 5,
                    totalDeposits = 1450000,
                    totalExpenses = 88000,
                    recordedBalance = 1362000,
                    isLocked = false,
                    createdAt = 1700000000000L
                ),
                selectedPeriod = ReportPeriod.MonthYear(11, 2026),
                selectedReportType = ReportType.MONTHLY_SUMMARY,
                monthlyReport = MonthlyFinancialReport(
                    circleName = "Green Valley Property Pool",
                    currency = Currency.BDT,
                    periodDisplay = "November 2026",
                    openingBalance = 1362000,
                    totalDeposits = 100000,
                    totalExpenses = 12000,
                    netChange = 88000,
                    closingBalance = 1450000,
                    transactionCount = 12
                )
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@Preview(name = "Reports Screen - Dark", showBackground = true)
@Composable
fun ReportsScreenPreview_Dark() {
    FundCircleTheme(darkTheme = true) {
        ReportsScreen(
            state = ReportsUiState(
                isLoading = false,
                circle = FundCircle(
                    id = "fund-1",
                    name = "Green Valley Property Pool",
                    currency = Currency.BDT,
                    description = "Syndicate investment fund",
                    adminId = "usr-1",
                    adminEmail = "admin@example.com",
                    memberEmails = listOf("admin@example.com"),
                    memberCount = 5,
                    totalDeposits = 1450000,
                    totalExpenses = 88000,
                    recordedBalance = 1362000,
                    isLocked = false,
                    createdAt = 1700000000000L
                ),
                selectedPeriod = ReportPeriod.AllTime,
                selectedReportType = ReportType.MONTHLY_SUMMARY,
                monthlyReport = MonthlyFinancialReport(
                    circleName = "Green Valley Property Pool",
                    currency = Currency.BDT,
                    periodDisplay = "All Time",
                    openingBalance = 0,
                    totalDeposits = 1450000,
                    totalExpenses = 88000,
                    netChange = 1362000,
                    closingBalance = 1362000,
                    transactionCount = 25
                )
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}
