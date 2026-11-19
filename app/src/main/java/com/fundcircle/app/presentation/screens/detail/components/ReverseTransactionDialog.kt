package com.fundcircle.app.presentation.screens.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/**
 * Screen 14: Reverse Transaction Dialog (dialog_reverse_tx).
 * Immutable transaction reversal protocol with mandatory reason (5-250 characters)
 * and atomic balance reversal.
 */
@Composable
fun ReverseTransactionDialog(
    fundId: String,
    txId: String,
    isExpense: Boolean,
    deposit: Deposit?,
    expense: Expense?,
    currency: Currency,
    isReversing: Boolean,
    onConfirm: (reason: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var reason by rememberSaveable { mutableStateOf("") }
    var hasAttemptedSubmit by rememberSaveable { mutableStateOf(false) }

    val trimmedReason = reason.trim()
    val isReasonValid = trimmedReason.length in 5..250
    val showError = hasAttemptedSubmit && !isReasonValid

    val originalAmount = if (isExpense) expense?.amount ?: 0L else deposit?.amount ?: 0L
    val formattedAmount = remember(originalAmount) {
        NumberFormat.getIntegerInstance(Locale.US).format(originalAmount)
    }

    val typeLabel = if (isExpense) {
        expense?.category?.displayName ?: stringResource(R.string.tx_detail_type_expense)
    } else {
        stringResource(R.string.modal_add_tx_tab_deposit)
    }

    val paymentMethodName = if (isExpense) {
        expense?.paymentMethod?.displayName ?: ""
    } else {
        deposit?.paymentMethod?.displayName ?: ""
    }

    val recordedDate = if (isExpense) {
        expense?.let { DateUtils.formatDisplayDate(it.expenseDate) } ?: ""
    } else {
        deposit?.let { DateUtils.formatDisplayDate(it.depositDate) } ?: ""
    }

    val recordedActor = if (isExpense) {
        expense?.creatorEmail?.takeIf { it.isNotBlank() }
    } else {
        deposit?.memberName?.ifBlank { deposit.memberEmail }?.takeIf { it.isNotBlank() }
    }

    val summaryHeader = stringResource(
        R.string.dialog_reverse_summary_header,
        txId,
        typeLabel,
        paymentMethodName
    )

    AlertDialog(
        onDismissRequest = { if (!isReversing) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = stringResource(R.string.dialog_reverse_tx_icon_cd),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.dialog_reverse_tx_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DesignTokens.Spacing4),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing12)
            ) {
                Text(
                    text = stringResource(R.string.dialog_reverse_tx_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Read-Only Transaction Summary Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(DesignTokens.Spacing12)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing4)) {
                        Text(
                            text = summaryHeader,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.dialog_reverse_amount_label),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(
                                    R.string.dialog_reverse_amount_format,
                                    currency.symbol,
                                    formattedAmount
                                ),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Default,
                                    fontFeatureSettings = "tnum"
                                ),
                                fontWeight = FontWeight.Bold,
                                color = if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = if (recordedActor != null) {
                                stringResource(
                                    R.string.dialog_reverse_recorded_info_with_actor,
                                    recordedDate,
                                    recordedActor
                                )
                            } else {
                                stringResource(R.string.dialog_reverse_recorded_info, recordedDate)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Mandatory Reason Input Field
                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        if (it.length <= 250) {
                            reason = it
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.dialog_reverse_reason_label)) },
                    placeholder = { Text(text = stringResource(R.string.dialog_reverse_reason_placeholder)) },
                    enabled = !isReversing,
                    isError = showError,
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (showError) {
                                    stringResource(R.string.dialog_reverse_reason_error)
                                } else {
                                    stringResource(R.string.dialog_reverse_reason_helper)
                                },
                                color = if (showError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = stringResource(R.string.char_counter_format, reason.length, 250),
                                style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isReversing,
                modifier = Modifier.heightIn(min = DesignTokens.MinTouchTarget)
            ) {
                Text(text = stringResource(R.string.dialog_reverse_btn_keep))
            }
        },
        confirmButton = {
            val confirmCd = stringResource(
                if (isReversing) R.string.dialog_reverse_reversing else R.string.dialog_reverse_btn_confirm
            )
            Button(
                onClick = {
                    hasAttemptedSubmit = true
                    if (isReasonValid && !isReversing) {
                        onConfirm(trimmedReason)
                    }
                },
                enabled = isReasonValid && !isReversing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier
                    .heightIn(min = DesignTokens.MinTouchTarget)
                    .semantics {
                        contentDescription = confirmCd
                    }
            ) {
                if (isReversing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.dialog_reverse_btn_confirm),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        modifier = modifier
    )
}

@Preview(name = "Reverse Deposit Dialog - Light")
@Composable
private fun ReverseTransactionDialogDepositPreview() {
    FundCircleTheme {
        ReverseTransactionDialog(
            fundId = "fund-1",
            txId = "TX-DEP-9821",
            isExpense = false,
            deposit = Deposit(
                id = "TX-DEP-9821",
                fundId = "fund-1",
                memberId = "mem-1",
                memberEmail = "rahim@example.com",
                memberName = "Rahim Uddin",
                amount = 100_000L,
                depositDate = 1731412800000L,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                note = "Tranche 1"
            ),
            expense = null,
            currency = Currency.BDT,
            isReversing = false,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Reverse Expense Dialog - Dark")
@Composable
private fun ReverseTransactionDialogExpensePreview() {
    FundCircleTheme(darkTheme = true) {
        ReverseTransactionDialog(
            fundId = "fund-1",
            txId = "TX-EXP-1044",
            isExpense = true,
            deposit = null,
            expense = Expense(
                id = "TX-EXP-1044",
                fundId = "fund-1",
                amount = 15_000L,
                expenseDate = 1731412800000L,
                category = ExpenseCategory.OFFICE_AND_ADMINISTRATION,
                description = "Office desks & chairs",
                paymentMethod = PaymentMethod.MOBILE_BANKING,
                paidBy = "Vendor Supplies Ltd.",
                createdBy = "admin-1",
                creatorEmail = "admin@fundcircle.com"
            ),
            currency = Currency.BDT,
            isReversing = false,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
