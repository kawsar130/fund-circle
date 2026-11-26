package com.fundcircle.app.presentation.screens.ledger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Card
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fundcircle.app.R
import com.fundcircle.app.core.common.AmountFormatter
import com.fundcircle.app.core.common.DateUtils
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.presentation.common.asUiText
import com.fundcircle.app.presentation.components.ReversedBadge
import com.fundcircle.app.presentation.components.SegmentedControl
import com.fundcircle.app.presentation.components.SegmentedItem
import com.fundcircle.app.presentation.theme.DesignTokens
import com.fundcircle.app.presentation.theme.FundCircleTheme
import com.fundcircle.app.presentation.theme.TABULAR_DIGITS
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun TransactionsLedgerRoute(
    onNavigateToTransactionDetail: (String, String, Boolean) -> Unit,
    onOpenAddTransactionSheet: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    viewModel: LedgerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is LedgerUiEffect.NavigateToDetail -> {
                    onNavigateToTransactionDetail(effect.fundId, effect.txId, effect.isExpense)
                }
                is LedgerUiEffect.OpenAddTransactionSheet -> {
                    onOpenAddTransactionSheet(effect.fundId)
                }
                is LedgerUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
                is LedgerUiEffect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error.asString(context))
            viewModel.onAction(LedgerUiAction.DismissError)
        }
    }

    TransactionsLedgerScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        bottomBar = bottomBar,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsLedgerScreen(
    state: LedgerUiState,
    onAction: (LedgerUiAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    bottomBar: @Composable () -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var showDateRangePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = bottomBar,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.ledger_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { onAction(LedgerUiAction.ToggleSearch) },
                            modifier = Modifier.defaultMinSize(minWidth = DesignTokens.MinTouchTarget, minHeight = DesignTokens.MinTouchTarget)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.ledger_search_cd)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                // Search Bar Expandable
                AnimatedVisibility(
                    visible = state.isSearchActive,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing8)
                    ) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { onAction(LedgerUiAction.UpdateSearchQuery(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(text = stringResource(R.string.ledger_search_placeholder)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                if (state.searchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = { onAction(LedgerUiAction.UpdateSearchQuery("")) },
                                        modifier = Modifier.size(DesignTokens.MinTouchTarget)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = stringResource(R.string.ledger_clear_search_cd)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Invariant 5: Offline Cache Warning Banner
                AnimatedVisibility(
                    visible = state.isOffline,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing8),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CloudOff,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                            Text(
                                text = stringResource(R.string.ledger_offline_warning),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Circle Locked Notice Banner
                AnimatedVisibility(
                    visible = state.isLocked,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing8),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                            Text(
                                text = stringResource(R.string.dashboard_locked_notice),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Segmented Tab Row (Containerized SegmentedControl)
                SegmentedControl(
                    items = listOf(
                        SegmentedItem(
                            title = stringResource(R.string.ledger_tab_deposits_title),
                            count = state.deposits.size
                        ),
                        SegmentedItem(
                            title = stringResource(R.string.ledger_tab_expenses_title),
                            count = state.expenses.size,
                            activeContentColor = MaterialTheme.colorScheme.error
                        )
                    ),
                    selectedIndex = state.selectedTab.ordinal,
                    onItemSelected = { index ->
                        val tab = LedgerTab.entries[index]
                        onAction(LedgerUiAction.SelectTab(tab))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing8)
                )

                // Horizontal Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing8),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.selectedTab == LedgerTab.DEPOSITS) {
                        // Deposits View: Date Range + Payment Method Filters
                        DateRangeFilterChip(
                            range = state.filterDateRange,
                            onClick = { showDateRangePicker = true }
                        )
                        FilterChip(
                            selected = state.filterPaymentMethod == null,
                            onClick = { onAction(LedgerUiAction.FilterByPaymentMethod(null)) },
                            label = { Text(stringResource(R.string.ledger_filter_all_methods)) },
                            modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                            colors = FilterChipDefaults.filterChipColors()
                        )
                        PaymentMethod.entries.forEach { method ->
                            FilterChip(
                                selected = state.filterPaymentMethod == method,
                                onClick = { onAction(LedgerUiAction.FilterByPaymentMethod(method)) },
                                label = { Text(method.asUiText().asString()) },
                                modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                                colors = FilterChipDefaults.filterChipColors()
                            )
                        }
                    } else {
                        // Expenses View: Date Range + Category Chips + Payment Method Filters
                        DateRangeFilterChip(
                            range = state.filterDateRange,
                            onClick = { showDateRangePicker = true }
                        )
                        FilterChip(
                            selected = state.filterCategory == null,
                            onClick = { onAction(LedgerUiAction.FilterByCategory(null)) },
                            label = { Text(stringResource(R.string.ledger_filter_all_categories)) },
                            modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                            colors = FilterChipDefaults.filterChipColors()
                        )
                        ExpenseCategory.entries.forEach { category ->
                            FilterChip(
                                selected = state.filterCategory == category,
                                onClick = { onAction(LedgerUiAction.FilterByCategory(category)) },
                                label = { Text(category.asUiText().asString()) },
                                modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                                colors = FilterChipDefaults.filterChipColors()
                            )
                        }
                        FilterChip(
                            selected = state.filterPaymentMethod == null,
                            onClick = { onAction(LedgerUiAction.FilterByPaymentMethod(null)) },
                            label = { Text(stringResource(R.string.ledger_filter_all_methods)) },
                            modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                            colors = FilterChipDefaults.filterChipColors()
                        )
                        PaymentMethod.entries.forEach { method ->
                            FilterChip(
                                selected = state.filterPaymentMethod == method,
                                onClick = { onAction(LedgerUiAction.FilterByPaymentMethod(method)) },
                                label = { Text(method.asUiText().asString()) },
                                modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                                colors = FilterChipDefaults.filterChipColors()
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (state.isAdmin && !state.isLocked) {
                val fabCd = stringResource(R.string.ledger_fab_record_cd)
                ExtendedFloatingActionButton(
                    onClick = { onAction(LedgerUiAction.ClickAddTransaction) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    },
                    text = { Text(text = stringResource(R.string.ledger_fab_record)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                        .semantics {
                            contentDescription = fabCd
                        }
                )
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
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
            when (state.selectedTab) {
                LedgerTab.DEPOSITS -> {
                    if (state.deposits.isEmpty()) {
                        EmptyLedgerState(
                            title = stringResource(R.string.ledger_empty_deposits_title),
                            subtitle = stringResource(R.string.ledger_empty_deposits_subtitle),
                            innerPadding = innerPadding
                        )
                    } else {
                        DepositsChronologicalList(
                            deposits = state.deposits,
                            currency = state.currency,
                            onDepositClick = { txId -> onAction(LedgerUiAction.ClickTransaction(txId, isExpense = false)) },
                            innerPadding = innerPadding
                        )
                    }
                }
                LedgerTab.EXPENSES -> {
                    if (state.expenses.isEmpty()) {
                        EmptyLedgerState(
                            title = stringResource(R.string.ledger_empty_expenses_title),
                            subtitle = stringResource(R.string.ledger_empty_expenses_subtitle),
                            innerPadding = innerPadding
                        )
                    } else {
                        ExpensesChronologicalList(
                            expenses = state.expenses,
                            currency = state.currency,
                            onExpenseClick = { txId -> onAction(LedgerUiAction.ClickTransaction(txId, isExpense = true)) },
                            innerPadding = innerPadding
                        )
                    }
                }
            }
        }
    }

    if (showDateRangePicker) {
        LedgerDateRangePickerDialog(
            initialRange = state.filterDateRange,
            onApply = { range ->
                showDateRangePicker = false
                onAction(LedgerUiAction.FilterByDateRange(range))
            },
            onDismiss = { showDateRangePicker = false }
        )
    }
}

/** Shows "All Dates" with no filter, or the picked range ("3 Nov – 20 Nov 2026") once one is set. */
@Composable
private fun DateRangeFilterChip(
    range: LedgerDateRange?,
    onClick: () -> Unit
) {
    val label = if (range == null) {
        stringResource(R.string.ledger_filter_all_dates)
    } else {
        stringResource(
            R.string.ledger_filter_date_range,
            formatPickerDay(range.startUtcDay),
            formatPickerDay(range.endUtcDay)
        )
    }
    FilterChip(
        selected = range != null,
        onClick = onClick,
        label = { Text(label) },
        modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
        colors = FilterChipDefaults.filterChipColors()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LedgerDateRangePickerDialog(
    initialRange: LedgerDateRange?,
    onApply: (LedgerDateRange?) -> Unit,
    onDismiss: () -> Unit
) {
    val pickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialRange?.startUtcDay,
        initialSelectedEndDateMillis = initialRange?.endUtcDay,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                DateUtils.isDatePickerDateSelectable(utcTimeMillis)
        }
    )
    val start = pickerState.selectedStartDateMillis
    // A single tapped day counts as a one-day range.
    val end = pickerState.selectedEndDateMillis ?: start

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { if (start != null && end != null) onApply(LedgerDateRange(start, end)) },
                enabled = start != null,
                modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
            ) {
                Text(stringResource(R.string.ledger_date_range_apply))
            }
        },
        dismissButton = {
            Row {
                if (initialRange != null) {
                    TextButton(
                        onClick = { onApply(null) },
                        modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                    ) {
                        Text(stringResource(R.string.ledger_date_range_clear))
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                ) {
                    Text(stringResource(R.string.date_picker_cancel))
                }
            }
        }
    ) {
        DateRangePicker(
            state = pickerState,
            title = {
                Text(
                    text = stringResource(R.string.ledger_date_range_title),
                    modifier = Modifier.padding(
                        start = DesignTokens.Spacing24,
                        end = DesignTokens.Spacing12,
                        top = DesignTokens.Spacing16
                    )
                )
            },
            showModeToggle = false,
            modifier = Modifier.weight(1f)
        )
    }
}

/** Formats a DateRangePicker value (UTC midnight) as its calendar day, e.g. "3 Nov 2026". */
private fun formatPickerDay(utcDay: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(utcDay))

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DepositsChronologicalList(
    deposits: List<Deposit>,
    currency: Currency,
    onDepositClick: (String) -> Unit,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    // Group deposits by Month & Year for sticky headers
    val groupedDeposits = remember(deposits) {
        deposits.groupBy { DateUtils.formatMonthYear(it.depositDate) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .consumeWindowInsets(innerPadding),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + DesignTokens.Spacing8,
            bottom = innerPadding.calculateBottomPadding() + 80.dp, // Space for FAB
            start = DesignTokens.Spacing16,
            end = DesignTokens.Spacing16
        ),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing8)
    ) {
        groupedDeposits.forEach { (monthYear, monthDeposits) ->
            stickyHeader(key = "deposit_header_$monthYear") {
                StickyDateHeader(monthYear = monthYear)
            }

            items(monthDeposits, key = { it.id }) { deposit ->
                DepositItemCard(
                    deposit = deposit,
                    currency = currency,
                    onClick = { onDepositClick(deposit.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExpensesChronologicalList(
    expenses: List<Expense>,
    currency: Currency,
    onExpenseClick: (String) -> Unit,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    // Group expenses by Month & Year for sticky headers
    val groupedExpenses = remember(expenses) {
        expenses.groupBy { DateUtils.formatMonthYear(it.expenseDate) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .consumeWindowInsets(innerPadding),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + DesignTokens.Spacing8,
            bottom = innerPadding.calculateBottomPadding() + 80.dp, // Space for FAB
            start = DesignTokens.Spacing16,
            end = DesignTokens.Spacing16
        ),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing8)
    ) {
        groupedExpenses.forEach { (monthYear, monthExpenses) ->
            stickyHeader(key = "expense_header_$monthYear") {
                StickyDateHeader(monthYear = monthYear)
            }

            items(monthExpenses, key = { it.id }) { expense ->
                ExpenseItemCard(
                    expense = expense,
                    currency = currency,
                    onClick = { onExpenseClick(expense.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
private fun StickyDateHeader(
    monthYear: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = DesignTokens.Spacing4)
    ) {
        Text(
            text = monthYear,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = DesignTokens.Spacing8)
        )
    }
}

@Composable
private fun DepositItemCard(
    deposit: Deposit,
    currency: Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedAmount = remember(deposit.amount) {
        AmountFormatter.format(deposit.amount)
    }
    val formattedDate = remember(deposit.depositDate) {
        DateUtils.formatDisplayDate(deposit.depositDate)
    }
    val a11yDesc = if (deposit.isVoid) {
        stringResource(R.string.dashboard_tx_card_reversed_cd, deposit.memberName, currency.symbol, formattedAmount)
    } else {
        stringResource(R.string.dashboard_tx_card_cd, deposit.memberName, currency.symbol, formattedAmount)
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = DesignTokens.CardShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
            .semantics {
                contentDescription = a11yDesc
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing12),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing4)
        ) {
            // Row 1: Contributor Title on left, Amount or REVERSED badge on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = deposit.memberName.ifBlank { deposit.memberEmail },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (deposit.isVoid) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(DesignTokens.Spacing8))

                if (deposit.isVoid) {
                    ReversedBadge()
                } else {
                    val amountColor = MaterialTheme.colorScheme.primary
                    Text(
                        text = stringResource(
                            R.string.ledger_deposit_amount_format,
                            currency.symbol,
                            formattedAmount
                        ),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFeatureSettings = TABULAR_DIGITS,
                            fontWeight = FontWeight.Bold
                        ),
                        color = amountColor
                    )
                }
            }

            // Row 2: Subtitle / Purpose & Payment Method (or Original & Reversal if void)
            val row2Text = if (deposit.isVoid) {
                stringResource(
                    R.string.tx_reversal_original_format,
                    currency.symbol,
                    formattedAmount,
                    "-" + currency.symbol,
                    formattedAmount
                )
            } else {
                if (!deposit.note.isNullOrBlank()) {
                    stringResource(R.string.dashboard_tx_subtitle, deposit.note.orEmpty(), deposit.paymentMethod.asUiText().asString())
                } else {
                    deposit.paymentMethod.asUiText().asString()
                }
            }
            Text(
                text = row2Text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Row 3: Timestamp & Status
            val row3Text = if (deposit.isVoid) {
                val reason = deposit.voidMetadata?.reason?.takeIf { it.isNotBlank() } ?: stringResource(R.string.badge_status_reversed)
                stringResource(R.string.tx_reversal_reason_format, formattedDate, reason)
            } else {
                stringResource(R.string.tx_verified_format, formattedDate)
            }
            Text(
                text = row3Text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExpenseItemCard(
    expense: Expense,
    currency: Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedAmount = remember(expense.amount) {
        AmountFormatter.format(expense.amount)
    }
    val formattedDate = remember(expense.expenseDate) {
        DateUtils.formatDisplayDate(expense.expenseDate)
    }
    val displayTitle = expense.description.ifBlank { expense.category.asUiText().asString() }
    val a11yDesc = if (expense.isVoid) {
        stringResource(R.string.dashboard_tx_card_reversed_cd, displayTitle, currency.symbol, formattedAmount)
    } else {
        stringResource(R.string.dashboard_tx_card_cd, displayTitle, currency.symbol, formattedAmount)
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = DesignTokens.CardShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
            .semantics {
                contentDescription = a11yDesc
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing12),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing4)
        ) {
            // Row 1: Title on left, Amount or REVERSED badge on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (expense.isVoid) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(DesignTokens.Spacing8))

                if (expense.isVoid) {
                    ReversedBadge()
                } else {
                    val amountColor = MaterialTheme.colorScheme.error
                    Text(
                        text = stringResource(
                            R.string.ledger_expense_amount_format,
                            currency.symbol,
                            formattedAmount
                        ),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFeatureSettings = TABULAR_DIGITS,
                            fontWeight = FontWeight.Bold
                        ),
                        color = amountColor
                    )
                }
            }

            // Row 2: Subtitle (Category • Payment Method)
            val row2Text = if (expense.isVoid) {
                stringResource(
                    R.string.tx_reversal_original_format,
                    currency.symbol,
                    formattedAmount,
                    "+" + currency.symbol,
                    formattedAmount
                )
            } else {
                stringResource(
                    R.string.dashboard_tx_subtitle,
                    expense.category.asUiText().asString(),
                    expense.paymentMethod.asUiText().asString()
                )
            }
            Text(
                text = row2Text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Row 3: Timestamp & Status / Paid to/by
            val row3Text = if (expense.isVoid) {
                val reason = expense.voidMetadata?.reason?.takeIf { it.isNotBlank() } ?: stringResource(R.string.badge_status_reversed)
                stringResource(R.string.tx_reversal_reason_format, formattedDate, reason)
            } else if (expense.paidBy.isNotBlank()) {
                stringResource(R.string.ledger_paid_by_prefix, expense.paidBy) + " • " + formattedDate
            } else {
                stringResource(R.string.tx_verified_format, formattedDate)
            }
            Text(
                text = row3Text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyLedgerState(
    title: String,
    subtitle: String,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(DesignTokens.Spacing24),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(DesignTokens.Spacing16))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = DesignTokens.Spacing16)
            )
        }
    }
}

@Preview(name = "Ledger Screen - Deposits Tab", showBackground = true)
@Composable
private fun TransactionsLedgerScreenDepositsPreview() {
    FundCircleTheme {
        TransactionsLedgerScreen(
            state = LedgerUiState(
                isLoading = false,
                isAdmin = true,
                selectedTab = LedgerTab.DEPOSITS,
                deposits = listOf(
                    Deposit(
                        id = "dep-1",
                        fundId = "fund-1",
                        memberId = "mem-1",
                        memberEmail = "rahim@example.com",
                        memberName = "Rahim Uddin (FC-002)",
                        amount = 100_000L,
                        depositDate = System.currentTimeMillis(),
                        paymentMethod = PaymentMethod.BANK_TRANSFER,
                        note = "Monthly contribution tranche 1",
                        isVoid = false
                    ),
                    Deposit(
                        id = "dep-2",
                        fundId = "fund-1",
                        memberId = "mem-2",
                        memberEmail = "sajjad@example.com",
                        memberName = "Sajjad Hossain (FC-004)",
                        amount = 50_000L,
                        depositDate = System.currentTimeMillis() - 86400000L,
                        paymentMethod = PaymentMethod.CASH,
                        note = null,
                        isVoid = true
                    )
                )
            ),
            onAction = {}
        )
    }
}

@Preview(name = "Ledger Screen - Empty State", showBackground = true)
@Composable
private fun TransactionsLedgerScreenEmptyPreview() {
    FundCircleTheme {
        TransactionsLedgerScreen(
            state = LedgerUiState(
                isLoading = false,
                isAdmin = true,
                selectedTab = LedgerTab.EXPENSES,
                expenses = emptyList()
            ),
            onAction = {}
        )
    }
}
