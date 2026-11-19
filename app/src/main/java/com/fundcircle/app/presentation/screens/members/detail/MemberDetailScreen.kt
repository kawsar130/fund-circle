package com.fundcircle.app.presentation.screens.members.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
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
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.presentation.theme.DesignTokens
import com.fundcircle.app.presentation.theme.FundCircleTheme
import java.text.NumberFormat
import java.util.Locale

/**
 * Screen 09: Member Details & Individual Ledger (CUJ-07).
 * Displays member profile metrics, chronological deposits, and admin danger actions.
 */
@Composable
fun MemberDetailRoute(
    fundId: String,
    userId: String,
    onNavigateBack: () -> Unit,
    onNavigateToTransactionDetail: (String, String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MemberDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(fundId, userId) {
        viewModel.onAction(MemberDetailUiAction.Init(fundId, userId))
    }

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is MemberDetailUiEffect.NavigateToTransactionDetail -> {
                    onNavigateToTransactionDetail(effect.fundId, effect.txId, effect.isExpense)
                }
                is MemberDetailUiEffect.NavigateBack -> {
                    onNavigateBack()
                }
                is MemberDetailUiEffect.ShowSnackbar -> {
                    // Show message in snackbarHost
                }
            }
        }
    }

    MemberDetailScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    state: MemberDetailUiState,
    onAction: (MemberDetailUiAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.member_detail_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(MemberDetailUiAction.NavigateBack) },
                        modifier = Modifier.size(DesignTokens.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.member_detail_back_cd),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (state.isAdmin) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(end = DesignTokens.Spacing16)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.member_detail_badge_admin),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (state.isLoading && state.member == null) {
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
        } else if (state.member == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.member_detail_error_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val member = state.member
            val currency = state.circle?.currency ?: Currency.BDT

            LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + DesignTokens.Spacing32
                ),
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
                                    text = stringResource(R.string.members_roster_offline_warning),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Hero Member Profile Section
                item(key = "hero_profile") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing16),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 64x64dp Avatar Container
                        val initials = remember(member.name) { extractInitials(member.name) }
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

                        // Full Name
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(DesignTokens.Spacing4))

                        // Member ID Badge
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(
                                text = stringResource(R.string.member_detail_id_format, member.memberId),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(DesignTokens.Spacing8))

                        // Status & Joined Date
                        val joinedDate = remember(member.joinedAt) {
                            DateUtils.formatDisplayDate(member.joinedAt)
                        }
                        Text(
                            text = stringResource(
                                R.string.member_detail_status_joined_format,
                                member.status.name,
                                joinedDate
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Contact Details Row
                        if (member.phone.isNotBlank() || member.email.isNotBlank()) {
                            Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (member.phone.isNotBlank()) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = member.phone,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (member.phone.isNotBlank() && member.email.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(DesignTokens.Spacing12))
                                }
                                if (member.email.isNotBlank()) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = member.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Summary Tonal Cards Row
                item(key = "summary_cards") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing8),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing12)
                    ) {
                        // Card 1: Cumulative Deposits
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(DesignTokens.Spacing16)) {
                                Text(
                                    text = stringResource(R.string.member_detail_cumulative_deposits_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(DesignTokens.Spacing4))
                                val formattedDeposits = remember(member.totalContributed) {
                                    NumberFormat.getIntegerInstance(Locale.US).format(member.totalContributed)
                                }
                                Text(
                                    text = "${currency.symbol} $formattedDeposits",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Card 2: Transactions Count
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(DesignTokens.Spacing16)) {
                                Text(
                                    text = stringResource(R.string.member_detail_transactions_count_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(DesignTokens.Spacing4))
                                val count = state.deposits.size
                                Text(
                                    text = if (count == 1) {
                                        stringResource(R.string.member_detail_deposit_count_singular)
                                    } else {
                                        stringResource(R.string.member_detail_deposit_count_plural, count)
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Section Header: Deposit History
                item(key = "deposit_history_header") {
                    Text(
                        text = stringResource(R.string.member_detail_section_deposits),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = DesignTokens.Spacing16,
                                end = DesignTokens.Spacing16,
                                top = DesignTokens.Spacing16,
                                bottom = DesignTokens.Spacing8
                            )
                    )
                }

                // Deposit History Items
                if (state.deposits.isEmpty()) {
                    item(key = "empty_deposits") {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing8)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(DesignTokens.Spacing24),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                                Text(
                                    text = stringResource(R.string.member_detail_empty_deposits_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.member_detail_empty_deposits_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = state.deposits,
                        key = { it.id }
                    ) { deposit ->
                        DepositItemRow(
                            deposit = deposit,
                            currency = currency,
                            onClick = { onAction(MemberDetailUiAction.ClickDeposit(deposit.id)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing4)
                        )
                    }
                }

                // Admin Danger Actions Section
                if (state.isAdmin && !state.isSelf && state.circle?.isLocked == false) {
                    item(key = "admin_danger_actions") {
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing24))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.Spacing16)
                        ) {
                            when (member.status) {
                                MemberStatus.ACTIVE -> {
                                    OutlinedButton(
                                        onClick = {
                                            onAction(MemberDetailUiAction.RequestStatusChange(MemberStatus.INACTIVE))
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.member_detail_btn_deactivate),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                                    TextButton(
                                        onClick = {
                                            onAction(MemberDetailUiAction.RequestStatusChange(MemberStatus.LEFT))
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.member_detail_btn_mark_left),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                MemberStatus.INACTIVE -> {
                                    Button(
                                        onClick = {
                                            onAction(MemberDetailUiAction.RequestStatusChange(MemberStatus.ACTIVE))
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.member_detail_btn_reactivate),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                                    TextButton(
                                        onClick = {
                                            onAction(MemberDetailUiAction.RequestStatusChange(MemberStatus.LEFT))
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.member_detail_btn_mark_left),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                MemberStatus.LEFT, MemberStatus.PENDING -> {
                                    Button(
                                        onClick = {
                                            onAction(MemberDetailUiAction.RequestStatusChange(MemberStatus.ACTIVE))
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.member_detail_btn_reactivate),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Alert Dialog
    if (state.pendingStatusChange != null) {
        val target = state.pendingStatusChange
        val memberName = state.member?.name.orEmpty()
        val (dialogTitle, dialogBody) = when (target) {
            MemberStatus.INACTIVE -> Pair(
                stringResource(R.string.member_detail_dialog_deactivate_title),
                stringResource(R.string.member_detail_dialog_deactivate_body, memberName)
            )
            MemberStatus.ACTIVE -> Pair(
                stringResource(R.string.member_detail_dialog_reactivate_title),
                stringResource(R.string.member_detail_dialog_reactivate_body, memberName)
            )
            MemberStatus.LEFT -> Pair(
                stringResource(R.string.member_detail_dialog_mark_left_title),
                stringResource(R.string.member_detail_dialog_mark_left_body, memberName)
            )
            MemberStatus.PENDING -> Pair(
                stringResource(R.string.member_detail_dialog_reactivate_title),
                stringResource(R.string.member_detail_dialog_reactivate_body, memberName)
            )
        }

        AlertDialog(
            onDismissRequest = { onAction(MemberDetailUiAction.DismissStatusChangeDialog) },
            title = {
                Text(
                    text = dialogTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = dialogBody,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { onAction(MemberDetailUiAction.ConfirmStatusChange) },
                    enabled = !state.isUpdatingStatus,
                    colors = if (target == MemberStatus.INACTIVE || target == MemberStatus.LEFT) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    if (state.isUpdatingStatus) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = stringResource(R.string.member_detail_dialog_confirm_btn))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onAction(MemberDetailUiAction.DismissStatusChangeDialog) }
                ) {
                    Text(text = stringResource(R.string.member_detail_dialog_cancel_btn))
                }
            }
        )
    }
}

@Composable
private fun DepositItemRow(
    deposit: Deposit,
    currency: Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing12)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Date • Payment Method
                val formattedDate = remember(deposit.depositDate) {
                    DateUtils.formatDisplayDate(deposit.depositDate)
                }
                Text(
                    text = stringResource(
                        R.string.member_detail_deposit_date_method_format,
                        formattedDate,
                        deposit.paymentMethod.displayName
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Amount
                val formattedAmount = remember(deposit.amount) {
                    NumberFormat.getIntegerInstance(Locale.US).format(deposit.amount)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (deposit.isVoid) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.tx_detail_status_reversed),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = stringResource(
                            R.string.member_detail_deposit_amount_format,
                            currency.symbol,
                            formattedAmount
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (deposit.isVoid) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                        textDecoration = if (deposit.isVoid) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }

            // Note if present
            deposit.note?.let { note ->
                if (note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.member_detail_deposit_note_format, note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun extractInitials(name: String): String {
    val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "M"
        parts.size == 1 -> parts[0].take(2).uppercase(Locale.US)
        else -> "${parts[0].first()}${parts[1].first()}".uppercase(Locale.US)
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun MemberDetailPreviewLight() {
    FundCircleTheme(darkTheme = false) {
        MemberDetailScreen(
            state = MemberDetailUiState(
                isLoading = false,
                isAdmin = true,
                isSelf = false,
                circle = FundCircle(
                    id = "fund_1",
                    name = "Apex Capital Syndicate",
                    description = null,
                    currency = Currency.BDT,
                    adminId = "usr_admin",
                    adminEmail = "admin@example.com",
                    memberEmails = listOf("admin@example.com"),
                    memberCount = 4,
                    totalDeposits = 600000L,
                    totalExpenses = 50000L,
                    recordedBalance = 550000L,
                    isLocked = false,
                    createdAt = System.currentTimeMillis()
                ),
                member = CircleMember(
                    userId = "usr_2",
                    memberId = "FC-002",
                    email = "rahim@example.com",
                    name = "Rahim Uddin",
                    phone = "01722222222",
                    role = MemberRole.MEMBER,
                    status = MemberStatus.ACTIVE,
                    totalContributed = 250000L,
                    joinedAt = System.currentTimeMillis() - 86400000L * 30,
                    lastActiveAt = System.currentTimeMillis()
                ),
                deposits = listOf(
                    Deposit(
                        id = "dep_1",
                        fundId = "fund_1",
                        memberId = "usr_2",
                        memberEmail = "rahim@example.com",
                        memberName = "Rahim Uddin",
                        amount = 100000L,
                        depositDate = System.currentTimeMillis() - 86400000L * 5,
                        paymentMethod = PaymentMethod.BANK_TRANSFER,
                        note = "Tranche 1",
                        isVoid = false,
                        voidMetadata = null
                    ),
                    Deposit(
                        id = "dep_2",
                        fundId = "fund_1",
                        memberId = "usr_2",
                        memberEmail = "rahim@example.com",
                        memberName = "Rahim Uddin",
                        amount = 150000L,
                        depositDate = System.currentTimeMillis() - 86400000L * 20,
                        paymentMethod = PaymentMethod.BANK_TRANSFER,
                        note = "Initial seed capital",
                        isVoid = false,
                        voidMetadata = null
                    )
                )
            ),
            onAction = {}
        )
    }
}
