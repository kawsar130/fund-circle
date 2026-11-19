package com.fundcircle.app.presentation.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fundcircle.app.R
import com.fundcircle.app.core.common.DateUtils
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.model.TransactionItem
import com.fundcircle.app.presentation.theme.DesignTokens
import com.fundcircle.app.presentation.theme.FundCircleTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardRoute(
    onNavigateToLedger: (String) -> Unit,
    onNavigateToTransactionDetail: (String, String, Boolean) -> Unit,
    onNavigateToRecordTransaction: (String) -> Unit,
    onNavigateToSettings: (String) -> Unit,
    onNavigateToMembers: (String) -> Unit,
    modifier: Modifier = Modifier,
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
                is DashboardUiEffect.NavigateToRecordTransaction -> onNavigateToRecordTransaction(effect.fundId)
                is DashboardUiEffect.NavigateToSettings -> onNavigateToSettings(effect.fundId)
                is DashboardUiEffect.NavigateToMembers -> onNavigateToMembers(effect.fundId)
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
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onAction: (DashboardUiAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                    // Circle Status Badge (ACTIVE or LOCKED)
                    if (state.circle != null) {
                        val isLocked = state.circle.isLocked
                        val badgeLabel = if (isLocked) {
                            stringResource(R.string.dashboard_badge_locked)
                        } else {
                            stringResource(R.string.dashboard_badge_active)
                        }
                        val badgeCd = if (isLocked) {
                            stringResource(R.string.dashboard_status_badge_locked_cd)
                        } else {
                            stringResource(R.string.dashboard_status_badge_active_cd)
                        }
                        val badgeContainerColor = if (isLocked) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                        val badgeTextColor = if (isLocked) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = badgeContainerColor,
                            contentColor = badgeTextColor,
                            modifier = Modifier.semantics {
                                contentDescription = badgeCd
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = badgeLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing4))
                    }

                    // Notification Action
                    IconButton(
                        onClick = { onAction(DashboardUiAction.OpenNotifications) },
                        modifier = Modifier.size(DesignTokens.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(R.string.dashboard_top_bar_notifications_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Stakeholders Roster Action
                    IconButton(
                        onClick = { onAction(DashboardUiAction.NavigateToMembers) },
                        modifier = Modifier.size(DesignTokens.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = stringResource(R.string.members_roster_search_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Settings Action (Clean Top Bar Rule: Settings tab with circle switch situated here)
                    IconButton(
                        onClick = { onAction(DashboardUiAction.NavigateToSettings) },
                        modifier = Modifier.size(DesignTokens.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.dashboard_top_bar_settings_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing4))
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
                    onClick = { onAction(DashboardUiAction.NavigateToRecordTransaction) },
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing12)
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing16)
        ) {
            // Header: Label + LIVE status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_hero_balance_label),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_badge_live),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = DesignTokens.Spacing8, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing8))

            // Main Balance Amount (32sp Bold, Tabular Digits)
            val isNegative = circle.recordedBalance < 0L
            val absBalance = if (isNegative) -circle.recordedBalance else circle.recordedBalance
            val formattedBalance = remember(absBalance) {
                NumberFormat.getIntegerInstance(Locale.US).format(absBalance)
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
                MaterialTheme.colorScheme.onPrimaryContainer
            }
            Text(
                text = balanceText,
                style = MaterialTheme.typography.headlineLarge,
                color = balanceColor
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing16))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing4))
                    val formattedDeposits = remember(circle.totalDeposits) {
                        NumberFormat.getIntegerInstance(Locale.US).format(circle.totalDeposits)
                    }
                    Text(
                        text = stringResource(
                            R.string.dashboard_deposit_amount_format,
                            circle.currency.symbol,
                            formattedDeposits
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(DesignTokens.Spacing16))

                // Column 2: Total Expenses (O(1))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.dashboard_total_expenses_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing4))
                    val formattedExpenses = remember(circle.totalExpenses) {
                        NumberFormat.getIntegerInstance(Locale.US).format(circle.totalExpenses)
                    }
                    Text(
                        text = stringResource(
                            R.string.dashboard_expense_amount_format,
                            circle.currency.symbol,
                            formattedExpenses
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
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
        NumberFormat.getIntegerInstance(Locale.US).format(item.amount)
    }

    val displayTitle = item.title.ifBlank {
        if (item.isExpense) {
            stringResource(R.string.dashboard_expense_default_title)
        } else {
            stringResource(R.string.dashboard_deposit_default_title)
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicator Icon (+ green for Deposit, - red for Expense, muted outline if Voided)
            val indicatorBg = if (item.isVoid) {
                MaterialTheme.colorScheme.surfaceContainerHighest
            } else if (item.isExpense) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
            val indicatorTint = if (item.isVoid) {
                MaterialTheme.colorScheme.outline
            } else if (item.isExpense) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            }

            Surface(
                shape = CircleShape,
                color = indicatorBg,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (item.isExpense) Icons.Default.Remove else Icons.Default.Add,
                        contentDescription = null,
                        tint = indicatorTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(DesignTokens.Spacing12))

            // Details: Title + Subtitle (Date • Payment Method localized)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val subtitleText = if (item.isExpense && item.category != null && item.title != item.category.displayName) {
                    stringResource(
                        R.string.dashboard_tx_subtitle_with_category,
                        DateUtils.formatDisplayDate(item.timestamp),
                        item.category.asUiText().asString(),
                        item.paymentMethod.asUiText().asString()
                    )
                } else {
                    stringResource(
                        R.string.dashboard_tx_subtitle,
                        DateUtils.formatDisplayDate(item.timestamp),
                        item.paymentMethod.asUiText().asString()
                    )
                }
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(DesignTokens.Spacing12))

            // Amount Column (tnum, strikethrough if voided, REVERSED ↩ badge)
            Column(
                horizontalAlignment = Alignment.End
            ) {
                val amountText = if (item.isExpense) {
                    stringResource(R.string.dashboard_expense_amount_format, currency.symbol, formattedAmount)
                } else {
                    stringResource(R.string.dashboard_deposit_amount_format, currency.symbol, formattedAmount)
                }

                val amountColor = if (item.isVoid) {
                    MaterialTheme.colorScheme.outline
                } else if (item.isExpense) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }

                Text(
                    text = amountText,
                    style = MaterialTheme.typography.labelLarge,
                    color = amountColor,
                    textDecoration = if (item.isVoid) TextDecoration.LineThrough else TextDecoration.None
                )

                // Invariant 4: If voided, show REVERSED badge
                if (item.isVoid) {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing4))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_badge_reversed),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clean empty state card instructing user to record their first deposit.
 */
@Composable
fun EmptyTransactionsCard(
    isAdmin: Boolean = false,
    modifier: Modifier = Modifier
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
        subtitle = "12 Nov 2026 • Bank Transfer",
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
        subtitle = "10 Nov 2026 • Stationery",
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
        subtitle = "08 Nov 2026 • Mobile Banking",
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
