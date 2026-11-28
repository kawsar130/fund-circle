package com.fundcircle.app.presentation.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fundcircle.app.R
import com.fundcircle.app.core.common.AmountFormatter
import com.fundcircle.app.core.common.DateUtils
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.model.TransactionItem
import com.fundcircle.app.presentation.common.asUiText
import com.fundcircle.app.presentation.components.CircleStatusBadge
import com.fundcircle.app.presentation.components.LiveBadge
import com.fundcircle.app.presentation.components.ReversedBadge
import com.fundcircle.app.presentation.screens.transaction.RecordTransactionMode
import com.fundcircle.app.presentation.theme.DesignTokens
import com.fundcircle.app.presentation.theme.FundCircleTheme
import com.fundcircle.app.presentation.theme.SectionHeaderStyle
import com.fundcircle.app.presentation.theme.TABULAR_DIGITS
import com.fundcircle.app.presentation.theme.TabularNumLarge
import com.fundcircle.app.presentation.theme.TabularNumMedium

@Composable
fun DashboardRoute(
    onNavigateToLedger: (String) -> Unit,
    onNavigateToTransactionDetail: (String, String, Boolean) -> Unit,
    onNavigateToRecordTransaction: (String, RecordTransactionMode) -> Unit,
    onNavigateToSettings: (String) -> Unit,
    onNavigateToMembers: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToReports: (String) -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is DashboardUiEffect.NavigateToLedger -> onNavigateToLedger(effect.fundId)
                is DashboardUiEffect.NavigateToTransactionDetail -> {
                    onNavigateToTransactionDetail(effect.fundId, effect.txId, effect.isExpense)
                }
                is DashboardUiEffect.NavigateToRecordTransaction -> onNavigateToRecordTransaction(effect.fundId, effect.initialMode)
                is DashboardUiEffect.NavigateToSettings -> onNavigateToSettings(effect.fundId)
                is DashboardUiEffect.NavigateToMembers -> onNavigateToMembers(effect.fundId)
                is DashboardUiEffect.NavigateToReports -> onNavigateToReports(effect.fundId)
                is DashboardUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            if (state.circle != null) {
                snackbarHostState.showSnackbar(error.asString(context))
                viewModel.onAction(DashboardUiAction.DismissError)
            }
        }
    }

    DashboardScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        bottomBar = bottomBar,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onAction: (DashboardUiAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    bottomBar: @Composable () -> Unit = {}
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = bottomBar,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.dashboard_top_bar_active_circle_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = state.circle?.name.orEmpty(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onAction(DashboardUiAction.OpenNotifications) },
                        modifier = Modifier.defaultMinSize(minWidth = DesignTokens.MinTouchTarget, minHeight = DesignTokens.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(R.string.dashboard_top_bar_notifications_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { onAction(DashboardUiAction.NavigateToSettings) },
                        modifier = Modifier.defaultMinSize(minWidth = DesignTokens.MinTouchTarget, minHeight = DesignTokens.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.dashboard_top_bar_settings_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            // Admin Floating Action Button: Visible only if isAdmin == true and circle is not locked
            if (state.isAdmin && state.circle?.isLocked == false) {
                val fabCd = stringResource(R.string.dashboard_record_fab_cd)
                ExtendedFloatingActionButton(
                    onClick = { onAction(DashboardUiAction.NavigateToRecordTransaction(RecordTransactionMode.DEPOSIT)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.dashboard_btn_record_transaction),
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .defaultMinSize(minHeight = DesignTokens.MinTouchTarget, minWidth = DesignTokens.MinTouchTarget)
                        .padding(bottom = DesignTokens.Spacing8)
                        .semantics {
                            contentDescription = fabCd
                        }
                )
            }
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
        } else if (state.circle == null && state.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(DesignTokens.Spacing24)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing16))
                    Text(
                        text = stringResource(R.string.dashboard_error_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                    Text(
                        text = state.errorMessage.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing16))
                    Button(
                        onClick = { onAction(DashboardUiAction.Refresh) },
                        modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_retry_btn),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding)
            ) {
                // Offline Warning Banner
                item(key = "offline_banner") {
                    AnimatedVisibility(
                        visible = state.isOffline,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
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
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                                Text(
                                    text = stringResource(R.string.viewing_offline_cache),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Locked Circle Notice (Screen 13 Archive / Lock status)
                if (state.circle?.isLocked == true) {
                    item(key = "locked_circle_notice") {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing4)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = DesignTokens.Spacing12, vertical = DesignTokens.Spacing8),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                                Text(
                                    text = stringResource(R.string.dashboard_locked_notice),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                // Hero Balance & Atomic Aggregates Card
                state.circle?.let { circle ->
                    item(key = "hero_balance_card") {
                        HeroBalanceCard(
                            circle = circle,
                            isOffline = state.isOffline,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing12)
                        )
                    }

                    // Quick Action Pill Bar
                    item(key = "quick_actions_bar") {
                        DashboardQuickActionsBar(
                            isAdmin = state.isAdmin,
                            isLocked = circle.isLocked,
                            onDepositClick = { onAction(DashboardUiAction.NavigateToRecordTransaction(RecordTransactionMode.DEPOSIT)) },
                            onExpenseClick = { onAction(DashboardUiAction.NavigateToRecordTransaction(RecordTransactionMode.EXPENSE)) },
                            onReportsClick = { onAction(DashboardUiAction.NavigateToReports) },
                            onMembersClick = { onAction(DashboardUiAction.NavigateToMembers) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing4)
                        )
                    }
                }

                // Recent Transactions Section Header
                item(key = "recent_transactions_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = DesignTokens.Spacing16,
                                end = DesignTokens.Spacing8,
                                top = DesignTokens.Spacing16,
                                bottom = DesignTokens.Spacing8
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_recent_transactions_header),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(
                            onClick = { onAction(DashboardUiAction.NavigateToLedger) },
                            modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                        ) {
                            Text(
                                text = stringResource(R.string.dashboard_btn_view_all),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Empty State or Transaction Items
                if (state.recentTransactions.isEmpty()) {
                    item(key = "empty_transactions") {
                        EmptyTransactionsCard(
                            isAdmin = state.isAdmin,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing8)
                        )
                    }
                } else {
                    items(
                        items = state.recentTransactions,
                        key = { it.id }
                    ) { transaction ->
                        TransactionItemCard(
                            item = transaction,
                            currency = state.circle?.currency ?: Currency.BDT,
                            onClick = {
                                onAction(
                                    DashboardUiAction.NavigateToTransactionDetail(
                                        txId = transaction.id,
                                        isExpense = transaction.isExpense
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing4)
                                .animateItem()
                        )
                    }
                }

                // Bottom spacer for FAB clearance
                item(key = "bottom_fab_spacer") {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        }
    }
}

/**
 * Hero Balance Card displaying O(1) Atomic Aggregates loaded from parent /funds/{fundId} document.
 * Invariant 1: Financial amounts are 64-bit Long integers formatted with tabular digits (tnum).
 * Invariant 2: Values read directly from parent document. Zero client-side ledger scanning.
 */
@Composable
fun HeroBalanceCard(
    circle: FundCircle,
    isOffline: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = DesignTokens.HeroCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing20)
        ) {
            // Header: Label + LIVE status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_hero_balance_label).uppercase(),
                    style = SectionHeaderStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LiveBadge(isOffline = isOffline)
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

            // Main Balance Amount (34sp ExtraBold, Tabular Digits)
            val isNegative = circle.recordedBalance < 0L
            val absBalance = if (isNegative) -circle.recordedBalance else circle.recordedBalance
            val formattedBalance = remember(absBalance) {
                AmountFormatter.format(absBalance)
            }
            val balanceText = if (isNegative) {
                stringResource(
                    R.string.dashboard_negative_balance_format,
                    circle.currency.symbol,
                    formattedBalance
                )
            } else {
                stringResource(
                    R.string.dashboard_balance_format,
                    circle.currency.symbol,
                    formattedBalance
                )
            }
            val balanceColor = if (isNegative) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }
            Text(
                text = balanceText,
                style = TabularNumLarge.copy(fontSize = 34.sp, fontWeight = FontWeight.ExtraBold),
                color = balanceColor
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing16))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

            // 2-Column Atomic Aggregates Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Column 1: Total Deposits (O(1))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.dashboard_total_deposits_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing4))
                    val formattedDeposits = remember(circle.totalDeposits) {
                        AmountFormatter.format(circle.totalDeposits)
                    }
                    Text(
                        text = stringResource(
                            R.string.dashboard_deposit_amount_format,
                            circle.currency.symbol,
                            formattedDeposits
                        ),
                        style = TabularNumMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(DesignTokens.Spacing16))

                // Column 2: Total Expenses (O(1))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.dashboard_total_expenses_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing4))
                    val formattedExpenses = remember(circle.totalExpenses) {
                        AmountFormatter.format(circle.totalExpenses)
                    }
                    Text(
                        text = stringResource(
                            R.string.dashboard_expense_amount_format,
                            circle.currency.symbol,
                            formattedExpenses
                        ),
                        style = TabularNumMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Transaction Item Card for the Home Dashboard feed.
 * Invariant 1: Financial amounts are 64-bit Long integers formatted with tnum.
 * Invariant 4: Reversed transactions (isVoid == true) retain original amount with strikethrough and REVERSED ↩ badge.
 * Invariant 7: Minimum touch target >= 48dp.
 */
@Composable
fun TransactionItemCard(
    item: TransactionItem,
    currency: Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedAmount = remember(item.amount) {
        AmountFormatter.format(item.amount)
    }

    // An expense with no description is titled by its (localized) category.
    val displayTitle = item.title.ifBlank {
        when {
            item.isExpense && item.category != null -> item.category.asUiText().asString()
            item.isExpense -> stringResource(R.string.dashboard_expense_default_title)
            else -> stringResource(R.string.dashboard_deposit_default_title)
        }
    }

    val cd = if (item.isVoid) {
        stringResource(
            R.string.dashboard_tx_card_reversed_cd,
            displayTitle,
            currency.symbol,
            formattedAmount
        )
    } else {
        stringResource(
            R.string.dashboard_tx_card_cd,
            displayTitle,
            currency.symbol,
            formattedAmount
        )
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
            .semantics { contentDescription = cd },
        shape = DesignTokens.CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing12),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing4)
        ) {
            // Row 1: Title on left, Signed Amount (or REVERSED badge) on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (item.isVoid) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(DesignTokens.Spacing8))

                if (item.isVoid) {
                    ReversedBadge()
                } else {
                    val amountText = if (item.isExpense) {
                        stringResource(R.string.dashboard_expense_amount_format, currency.symbol, formattedAmount)
                    } else {
                        stringResource(R.string.dashboard_deposit_amount_format, currency.symbol, formattedAmount)
                    }

                    val amountColor = if (item.isExpense) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }

                    Text(
                        text = amountText,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFeatureSettings = TABULAR_DIGITS,
                            fontWeight = FontWeight.Bold
                        ),
                        color = amountColor
                    )
                }
            }

            // Row 2: Subtitle (Category / Purpose • Payment Method)
            val row2Text = if (item.isVoid) {
                val prefix = if (item.isExpense) "+" else "-"
                stringResource(
                    R.string.tx_reversal_original_format,
                    currency.symbol,
                    formattedAmount,
                    prefix + currency.symbol,
                    formattedAmount
                )
            } else {
                if (item.isExpense && item.category != null && item.title.isNotBlank()) {
                    stringResource(
                        R.string.dashboard_tx_subtitle,
                        item.category.asUiText().asString(),
                        item.paymentMethod.asUiText().asString()
                    )
                } else {
                    item.paymentMethod.asUiText().asString()
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
            val formattedDate = DateUtils.formatDisplayDate(item.timestamp)
            val row3Text = when {
                item.isVoid -> stringResource(
                    R.string.tx_reversal_reason_format,
                    formattedDate,
                    item.voidReason ?: stringResource(R.string.badge_status_reversed)
                )
                item.isPendingSync -> stringResource(R.string.tx_pending_sync_format, formattedDate)
                else -> stringResource(R.string.tx_verified_format, formattedDate)
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

/**
 * Quick Action Pill Bar for instant navigation to Deposit, Expense, Reports, and Members.
 */
@Composable
fun DashboardQuickActionsBar(
    isAdmin: Boolean,
    isLocked: Boolean,
    onDepositClick: () -> Unit,
    onExpenseClick: () -> Unit,
    onReportsClick: () -> Unit,
    onMembersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isAdmin && !isLocked) {
            Surface(
                onClick = onDepositClick,
                shape = DesignTokens.PillShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing8),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing6)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.dashboard_action_deposit),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Surface(
                onClick = onExpenseClick,
                shape = DesignTokens.PillShape,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing8),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing6)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.dashboard_action_expense),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Surface(
            onClick = onReportsClick,
            shape = DesignTokens.PillShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing8),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing6)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.dashboard_action_reports),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Surface(
            onClick = onMembersClick,
            shape = DesignTokens.PillShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing8),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing6)
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.dashboard_action_members),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Clean empty state card instructing user to record their first deposit.
 */
@Composable
fun EmptyTransactionsCard(
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing24),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
            Text(
                text = stringResource(R.string.dashboard_empty_transactions_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(DesignTokens.Spacing4))
            val subtitleRes = if (isAdmin) {
                R.string.dashboard_empty_transactions_subtitle_admin
            } else {
                R.string.dashboard_empty_transactions_subtitle_member
            }
            Text(
                text = stringResource(subtitleRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==============================================================================
// Previews
// ==============================================================================

private val previewCircle = FundCircle(
    id = "circle-preview",
    name = "Green Valley Property Pool",
    description = "Commercial property investment",
    currency = Currency.BDT,
    adminId = "admin-1",
    adminEmail = "admin@example.com",
    memberEmails = listOf("admin@example.com"),
    memberCount = 5,
    totalDeposits = 1_600_000L,
    totalExpenses = 150_000L,
    recordedBalance = 1_450_000L,
    isLocked = false,
    createdAt = 1_700_000_000_000L
)

private val previewTransactions = listOf(
    TransactionItem(
        id = "tx-1",
        fundId = "circle-preview",
        isExpense = false,
        title = "Rahim Uddin",
        amount = 100_000L,
        timestamp = 1_700_000_100_000L,
        paymentMethod = PaymentMethod.BANK_TRANSFER,
        isVoid = false
    ),
    TransactionItem(
        id = "tx-2",
        fundId = "circle-preview",
        isExpense = true,
        title = "Office & Administration",
        amount = 12_000L,
        timestamp = 1_700_000_050_000L,
        paymentMethod = PaymentMethod.CASH,
        isVoid = true,
        category = ExpenseCategory.OFFICE_AND_ADMINISTRATION
    ),
    TransactionItem(
        id = "tx-3",
        fundId = "circle-preview",
        isExpense = false,
        title = "Tanvir Ahmed",
        amount = 50_000L,
        timestamp = 1_700_000_000_000L,
        paymentMethod = PaymentMethod.MOBILE_BANKING,
        isVoid = false
    )
)

@Preview(name = "Dashboard Light Theme", showBackground = true)
@Composable
private fun DashboardScreenLightPreview() {
    FundCircleTheme(darkTheme = false) {
        DashboardScreen(
            state = DashboardUiState(
                isLoading = false,
                circle = previewCircle,
                recentTransactions = previewTransactions,
                isAdmin = true,
                isOffline = false
            ),
            onAction = {}
        )
    }
}

@Preview(name = "Dashboard Dark Theme", showBackground = true)
@Composable
private fun DashboardScreenDarkPreview() {
    FundCircleTheme(darkTheme = true) {
        DashboardScreen(
            state = DashboardUiState(
                isLoading = false,
                circle = previewCircle,
                recentTransactions = previewTransactions,
                isAdmin = true,
                isOffline = false
            ),
            onAction = {}
        )
    }
}

@Preview(name = "Dashboard Offline State", showBackground = true)
@Composable
private fun DashboardScreenOfflinePreview() {
    FundCircleTheme(darkTheme = false) {
        DashboardScreen(
            state = DashboardUiState(
                isLoading = false,
                circle = previewCircle,
                recentTransactions = previewTransactions,
                isAdmin = false,
                isOffline = true
            ),
            onAction = {}
        )
    }
}

@Preview(name = "Dashboard Empty State Admin", showBackground = true)
@Composable
private fun DashboardScreenEmptyPreview() {
    FundCircleTheme(darkTheme = false) {
        DashboardScreen(
            state = DashboardUiState(
                isLoading = false,
                circle = previewCircle,
                recentTransactions = emptyList(),
                isAdmin = true,
                isOffline = false
            ),
            onAction = {}
        )
    }
}

@Preview(name = "Dashboard Empty State Member", showBackground = true)
@Composable
private fun DashboardScreenEmptyMemberPreview() {
    FundCircleTheme(darkTheme = false) {
        DashboardScreen(
            state = DashboardUiState(
                isLoading = false,
                circle = previewCircle,
                recentTransactions = emptyList(),
                isAdmin = false,
                isOffline = false
            ),
            onAction = {}
        )
    }
}

@Preview(name = "Dashboard Locked Circle", showBackground = true)
@Composable
private fun DashboardScreenLockedPreview() {
    FundCircleTheme(darkTheme = false) {
        DashboardScreen(
            state = DashboardUiState(
                isLoading = false,
                circle = previewCircle.copy(isLocked = true),
                recentTransactions = previewTransactions,
                isAdmin = true,
                isOffline = false
            ),
            onAction = {}
        )
    }
}

@Preview(name = "Dashboard Negative Balance", showBackground = true)
@Composable
private fun DashboardScreenNegativeBalancePreview() {
    FundCircleTheme(darkTheme = false) {
        DashboardScreen(
            state = DashboardUiState(
                isLoading = false,
                circle = previewCircle.copy(recordedBalance = -150_000L),
                recentTransactions = previewTransactions,
                isAdmin = true,
                isOffline = false
            ),
            onAction = {}
        )
    }
}
