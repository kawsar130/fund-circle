package com.fundcircle.app.presentation.screens.ledger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Card
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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.fundcircle.app.core.common.DateUtils
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.presentation.theme.DesignTokens
import com.fundcircle.app.presentation.theme.FundCircleTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TransactionsLedgerRoute(
    onNavigateBack: () -> Unit,
    onNavigateToTransactionDetail: (String, String, Boolean) -> Unit,
    onOpenAddTransactionSheet: (String) -> Unit,
    modifier: Modifier = Modifier,
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
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsLedgerScreen(
    state: LedgerUiState,
    onAction: (LedgerUiAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                    navigationIcon = {
                        IconButton(
                            onClick = { onAction(LedgerUiAction.NavigateBack) },
                            modifier = Modifier.defaultMinSize(minWidth = DesignTokens.MinTouchTarget, minHeight = DesignTokens.MinTouchTarget)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.ledger_back_cd)
                            )
                        }
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

                // Segmented Tab Row (M3 PrimaryTabRow)
                PrimaryTabRow(
                    selectedTabIndex = state.selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = state.selectedTab == LedgerTab.DEPOSITS,
                        onClick = { onAction(LedgerUiAction.SelectTab(LedgerTab.DEPOSITS)) },
                        modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                        text = {
                            Text(
                                text = stringResource(R.string.ledger_tab_deposits, state.deposits.size),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (state.selectedTab == LedgerTab.DEPOSITS) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = state.selectedTab == LedgerTab.EXPENSES,
                        onClick = { onAction(LedgerUiAction.SelectTab(LedgerTab.EXPENSES)) },
                        modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                        text = {
                            Text(
                                text = stringResource(R.string.ledger_tab_expenses, state.expenses.size),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (state.selectedTab == LedgerTab.EXPENSES) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }

                // Horizontal Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing8),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            label = { Text(method.displayName) },
                            modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                            colors = FilterChipDefaults.filterChipColors()
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (state.isAdmin) {
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
}

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
    val isDark = isSystemInDarkTheme()
    val formattedAmount = remember(deposit.amount) {
        NumberFormat.getIntegerInstance(Locale.US).format(deposit.amount)
    }
    val formattedDate = remember(deposit.depositDate) {
        DateUtils.formatDisplayDate(deposit.depositDate)
    }
    val subtitleText = "$formattedDate • ${deposit.paymentMethod.displayName}"
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
        shape = RoundedCornerShape(12.dp),
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
                .padding(DesignTokens.Spacing16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Contributor Title & Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deposit.memberName.ifBlank { deposit.memberEmail },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(DesignTokens.Spacing8))

                // Amount and optional Reversed badge
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    val amountColor = if (deposit.isVoid) {
                        if (isDark) DesignTokens.ReversedStrikethroughDark else DesignTokens.ReversedStrikethroughLight
                    } else {
                        if (isDark) DesignTokens.DepositCreditDark else DesignTokens.DepositCreditLight
                    }

                    Text(
                        text = stringResource(
                            R.string.ledger_deposit_amount_format,
                            currency.symbol,
                            formattedAmount
                        ),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (deposit.isVoid) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = amountColor
                    )

                    if (deposit.isVoid) {
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing4))
                        ReversedBadge()
                    }
                }
            }

            // Note (Optional)
            if (!deposit.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
                Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                Text(
                    text = stringResource(R.string.ledger_note_prefix, deposit.note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
    val isDark = isSystemInDarkTheme()
    val formattedAmount = remember(expense.amount) {
        NumberFormat.getIntegerInstance(Locale.US).format(expense.amount)
    }
    val formattedDate = remember(expense.expenseDate) {
        DateUtils.formatDisplayDate(expense.expenseDate)
    }
    val subtitleText = "$formattedDate • ${expense.paymentMethod.displayName} • ${expense.category.displayName}"
    val displayTitle = expense.description.ifBlank { expense.category.displayName }
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
        shape = RoundedCornerShape(12.dp),
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
                .padding(DesignTokens.Spacing16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Description and Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(DesignTokens.Spacing8))

                // Amount and optional Reversed badge
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    val amountColor = if (expense.isVoid) {
                        if (isDark) DesignTokens.ReversedStrikethroughDark else DesignTokens.ReversedStrikethroughLight
                    } else {
                        if (isDark) DesignTokens.ExpenseDebitDark else DesignTokens.ExpenseDebitLight
                    }

                    Text(
                        text = stringResource(
                            R.string.ledger_expense_amount_format,
                            currency.symbol,
                            formattedAmount
                        ),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (expense.isVoid) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = amountColor
                    )

                    if (expense.isVoid) {
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing4))
                        ReversedBadge()
                    }
                }
            }

            // Paid By / Category (Optional)
            if (expense.paidBy.isNotBlank()) {
                Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
                Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                Text(
                    text = stringResource(R.string.ledger_paid_by_prefix, expense.paidBy),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ReversedBadge(modifier: Modifier = Modifier) {
    val a11yDesc = stringResource(R.string.ledger_reversed_badge_cd)
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier.semantics {
            contentDescription = a11yDesc
        }
    ) {
        Text(
            text = stringResource(R.string.ledger_reversed_badge),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
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
