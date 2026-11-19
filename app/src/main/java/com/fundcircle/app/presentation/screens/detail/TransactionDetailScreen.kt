package com.fundcircle.app.presentation.screens.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fundcircle.app.R
import com.fundcircle.app.core.common.DateUtils
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.model.VoidMetadata
import com.fundcircle.app.presentation.screens.detail.components.ReverseTransactionDialog
import com.fundcircle.app.presentation.theme.DesignTokens
import com.fundcircle.app.presentation.theme.FundCircleTheme
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TransactionDetailRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is TransactionDetailUiEffect.NavigateBack -> onNavigateBack()
                is TransactionDetailUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
            }
        }
    }

    TransactionDetailScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    uiState: TransactionDetailUiState,
    onAction: (TransactionDetailUiAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tx_detail_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(TransactionDetailUiAction.ClickBack) },
                        modifier = Modifier
                            .size(DesignTokens.MinTouchTarget)
                            .semantics {
                                contentDescription = "Navigate back"
                            }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.tx_detail_back_cd)
                        )
                    }
                },
                actions = {
                    if (uiState.isAdmin) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(end = DesignTokens.Spacing16)
                        ) {
                            Text(
                                text = stringResource(R.string.tx_detail_badge_admin),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = DesignTokens.Spacing16)
                        ) {
                            Text(
                                text = stringResource(R.string.tx_detail_badge_member),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (uiState.deposit == null && uiState.expense == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(DesignTokens.Spacing16),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.errorMessage?.asString()
                        ?: stringResource(R.string.tx_detail_error_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            return@Scaffold
        }

        val scrollState = rememberScrollState()
        val originalAmount = uiState.originalAmount
        val formattedAmount = remember(originalAmount) {
            NumberFormat.getIntegerInstance(Locale.US).format(originalAmount)
        }
        val isVoid = uiState.isVoid
        val voidMetadata = if (uiState.isExpense) {
            uiState.expense?.voidMetadata
        } else {
            uiState.deposit?.voidMetadata
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            // Offline Warning Banner
            if (uiState.isOffline) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = DesignTokens.Spacing16,
                            vertical = DesignTokens.Spacing8
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing8)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.tx_detail_offline_warning),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing16),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing16)
            ) {
                // 1. Hero Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = DesignTokens.Spacing16),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing8)
                    ) {
                        Text(
                            text = if (uiState.isExpense) {
                                stringResource(R.string.tx_detail_type_expense)
                            } else {
                                stringResource(R.string.tx_detail_type_deposit)
                            }.uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val amountPrefix = if (uiState.isExpense) "-" else "+"
                        val amountDisplay = "$amountPrefix ${uiState.currency.symbol} $formattedAmount"

                        Text(
                            text = amountDisplay,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = FontFamily.Default,
                                fontFeatureSettings = "tnum"
                            ),
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                isVoid -> MaterialTheme.colorScheme.onSurfaceVariant
                                uiState.isExpense -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            },
                            textDecoration = if (isVoid) TextDecoration.LineThrough else TextDecoration.None
                        )

                        if (isVoid) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.semantics {
                                    contentDescription = "Status: Reversed"
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.tx_detail_status_reversed),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.semantics {
                                    contentDescription = "Status: Completed and posted"
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.tx_detail_status_completed),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 2. Transaction Specification Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.Spacing16),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing12)
                    ) {
                        Text(
                            text = stringResource(R.string.tx_detail_specs_title),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        DetailRow(
                            label = stringResource(R.string.tx_detail_id_label),
                            value = uiState.txId,
                            isMonospace = true
                        )

                        if (uiState.isExpense) {
                            uiState.expense?.let { exp ->
                                DetailRow(
                                    label = stringResource(R.string.tx_detail_category_label),
                                    value = exp.category.displayName
                                )
                                DetailRow(
                                    label = stringResource(R.string.tx_detail_description_label),
                                    value = exp.description
                                )
                                DetailRow(
                                    label = stringResource(R.string.tx_detail_date_label),
                                    value = DateUtils.formatDisplayDate(exp.expenseDate)
                                )
                                DetailRow(
                                    label = stringResource(R.string.tx_detail_method_label),
                                    value = exp.paymentMethod.displayName
                                )
                                if (exp.paidBy.isNotBlank()) {
                                    DetailRow(
                                        label = stringResource(R.string.tx_detail_paid_by_label),
                                        value = exp.paidBy
                                    )
                                }
                                if (exp.creatorEmail.isNotBlank()) {
                                    DetailRow(
                                        label = stringResource(R.string.tx_detail_recorded_by_label),
                                        value = exp.creatorEmail
                                    )
                                }
                                DetailRow(
                                    label = stringResource(R.string.tx_detail_recorded_at_label),
                                    value = DateUtils.formatDisplayDateTime(exp.expenseDate)
                                )
                            }
                        } else {
                            uiState.deposit?.let { dep ->
                                DetailRow(
                                    label = stringResource(R.string.tx_detail_contributor_label),
                                    value = dep.memberName
                                )
                                if (dep.memberEmail.isNotBlank()) {
                                    DetailRow(
                                        label = stringResource(R.string.tx_detail_contributor_email_label),
                                        value = dep.memberEmail
                                    )
                                }
                                DetailRow(
                                    label = stringResource(R.string.tx_detail_date_label),
                                    value = DateUtils.formatDisplayDate(dep.depositDate)
                                )
                                DetailRow(
                                    label = stringResource(R.string.tx_detail_method_label),
                                    value = dep.paymentMethod.displayName
                                )
                                dep.note?.takeIf { it.isNotBlank() }?.let { note ->
                                    DetailRow(
                                        label = stringResource(R.string.tx_detail_note_label),
                                        value = note
                                    )
                                }
                                DetailRow(
                                    label = stringResource(R.string.tx_detail_recorded_at_label),
                                    value = DateUtils.formatDisplayDateTime(dep.depositDate)
                                )
                            }
                        }
                    }
                }

                // 3. Transparency Audit Disclosure Card (Shield Icon 🛡️)
                AnimatedVisibility(
                    visible = isVoid,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing16),
                            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing12)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing8)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = stringResource(R.string.tx_detail_audit_icon_cd),
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(R.string.tx_detail_audit_title),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Text(
                                text = stringResource(R.string.tx_detail_audit_explanation),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            voidMetadata?.let { meta ->
                                DetailRow(
                                    label = stringResource(R.string.tx_detail_audit_date_label),
                                    value = DateUtils.formatDisplayDateTime(meta.voidedAt)
                                )
                                DetailRow(
                                    label = stringResource(R.string.tx_detail_audit_by_label),
                                    value = meta.voidedBy
                                )
                                DetailRow(
                                    label = stringResource(R.string.tx_detail_audit_reason_label),
                                    value = "\"${meta.reason}\""
                                )
                                DetailRow(
                                    label = stringResource(R.string.tx_detail_audit_impact_label),
                                    value = if (uiState.isExpense) {
                                        stringResource(
                                            R.string.tx_detail_audit_impact_expense_format,
                                            uiState.currency.symbol,
                                            formattedAmount
                                        )
                                    } else {
                                        stringResource(
                                            R.string.tx_detail_audit_impact_deposit_format,
                                            uiState.currency.symbol,
                                            formattedAmount
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // 4. Admin Action Button
                if (uiState.isAdmin && !uiState.isCircleLocked && !isVoid) {
                    Button(
                        onClick = { onAction(TransactionDetailUiAction.OpenReverseDialog) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = DesignTokens.MinTouchTarget)
                    ) {
                        Text(
                            text = stringResource(R.string.tx_detail_btn_reverse),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else if (uiState.isAdmin && uiState.isCircleLocked && !isVoid) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(DesignTokens.Spacing12),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(DesignTokens.Spacing8))
                            Text(
                                text = stringResource(R.string.tx_detail_circle_locked_notice),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showReverseDialog) {
        ReverseTransactionDialog(
            fundId = uiState.fundId,
            txId = uiState.txId,
            isExpense = uiState.isExpense,
            deposit = uiState.deposit,
            expense = uiState.expense,
            currency = uiState.currency,
            isReversing = uiState.isReversing,
            onConfirm = { reason ->
                onAction(TransactionDetailUiAction.ConfirmReversal(reason))
            },
            onDismiss = {
                onAction(TransactionDetailUiAction.DismissReverseDialog)
            }
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isMonospace: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = if (isMonospace) {
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Default,
                    fontFeatureSettings = "tnum"
                )
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.5f)
        )
    }
}

@Preview(name = "Deposit Detail Active - Light")
@Composable
private fun TransactionDetailScreenDepositPreview() {
    FundCircleTheme {
        TransactionDetailScreen(
            uiState = TransactionDetailUiState(
                isLoading = false,
                fundId = "fund-1",
                txId = "TX-DEP-9821",
                isExpense = false,
                deposit = Deposit(
                    id = "TX-DEP-9821",
                    fundId = "fund-1",
                    memberId = "mem-1",
                    memberEmail = "rahim@example.com",
                    memberName = "Rahim Uddin (FC-002)",
                    amount = 100_000L,
                    depositDate = 1731412800000L,
                    paymentMethod = PaymentMethod.BANK_TRANSFER,
                    note = "Monthly contribution tranche 1",
                    isVoid = false
                ),
                currency = Currency.BDT,
                isAdmin = true
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(name = "Expense Detail Active - Dark")
@Composable
private fun TransactionDetailScreenExpensePreview() {
    FundCircleTheme(darkTheme = true) {
        TransactionDetailScreen(
            uiState = TransactionDetailUiState(
                isLoading = false,
                fundId = "fund-1",
                txId = "TX-EXP-1044",
                isExpense = true,
                expense = Expense(
                    id = "TX-EXP-1044",
                    fundId = "fund-1",
                    amount = 15_000L,
                    expenseDate = 1731412800000L,
                    category = ExpenseCategory.OFFICE_AND_ADMINISTRATION,
                    description = "Office Supplies & Paper",
                    paymentMethod = PaymentMethod.MOBILE_BANKING,
                    paidBy = "Apex Stationery",
                    createdBy = "admin-1",
                    creatorEmail = "admin@fundcircle.com",
                    isVoid = false
                ),
                currency = Currency.BDT,
                isAdmin = true
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(name = "Reversed Transaction with Audit Card - Light")
@Composable
private fun TransactionDetailScreenReversedPreview() {
    FundCircleTheme {
        TransactionDetailScreen(
            uiState = TransactionDetailUiState(
                isLoading = false,
                fundId = "fund-1",
                txId = "TX-DEP-9821",
                isExpense = false,
                deposit = Deposit(
                    id = "TX-DEP-9821",
                    fundId = "fund-1",
                    memberId = "mem-1",
                    memberEmail = "rahim@example.com",
                    memberName = "Rahim Uddin (FC-002)",
                    amount = 100_000L,
                    depositDate = 1731412800000L,
                    paymentMethod = PaymentMethod.BANK_TRANSFER,
                    note = "Monthly contribution tranche 1",
                    isVoid = true,
                    voidMetadata = VoidMetadata(
                        reason = "Duplicate bank payment voucher entry.",
                        voidedAt = 1731500000000L,
                        voidedBy = "kawsar@example.com (Admin)"
                    )
                ),
                currency = Currency.BDT,
                isAdmin = true
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
