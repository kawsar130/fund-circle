package com.fundcircle.app.presentation.screens.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fundcircle.app.R
import com.fundcircle.app.core.common.DateUtils
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.presentation.theme.DesignTokens
import com.fundcircle.app.presentation.theme.FundCircleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordTransactionSheet(
    fundId: String,
    onDismiss: () -> Unit,
    onTransactionSaved: () -> Unit = {},
    onShowSnackbar: ((com.fundcircle.app.core.common.UiText) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: RecordTransactionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is RecordTransactionUiEffect.TransactionSaved -> {
                    onTransactionSaved()
                    onDismiss()
                }
                is RecordTransactionUiEffect.DismissSheet -> {
                    onDismiss()
                }
                is RecordTransactionUiEffect.ShowSnackbar -> {
                    onShowSnackbar?.invoke(effect.message)
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        RecordTransactionContent(
            state = state,
            onAction = viewModel::onAction,
            onClose = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordTransactionContent(
    state: RecordTransactionUiState,
    onAction: (RecordTransactionUiAction) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignTokens.Spacing16)
            .padding(bottom = DesignTokens.Spacing24)
    ) {
        // Modal Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.modal_add_tx_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier.defaultMinSize(minWidth = DesignTokens.MinTouchTarget, minHeight = DesignTokens.MinTouchTarget)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.modal_add_tx_close_cd)
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

        // Mode Switcher (Segmented Tab Row)
        PrimaryTabRow(
            selectedTabIndex = state.mode.ordinal,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = state.mode == RecordTransactionMode.DEPOSIT,
                onClick = { onAction(RecordTransactionUiAction.SelectMode(RecordTransactionMode.DEPOSIT)) },
                modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                text = {
                    Text(
                        text = stringResource(R.string.modal_add_tx_tab_deposit),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (state.mode == RecordTransactionMode.DEPOSIT) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
            Tab(
                selected = state.mode == RecordTransactionMode.EXPENSE,
                onClick = { onAction(RecordTransactionUiAction.SelectMode(RecordTransactionMode.EXPENSE)) },
                modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                text = {
                    Text(
                        text = stringResource(R.string.modal_add_tx_tab_expense),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (state.mode == RecordTransactionMode.EXPENSE) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing16))

        // Error Banner (if any)
        AnimatedVisibility(visible = state.generalError != null) {
            state.generalError?.let { err ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = DesignTokens.Spacing12)
                ) {
                    Row(
                        modifier = Modifier.padding(DesignTokens.Spacing12),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                        Text(
                            text = err.asString(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onAction(RecordTransactionUiAction.DismissError) },
                            modifier = Modifier.size(DesignTokens.CompactTouchTarget)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        when (state.mode) {
            RecordTransactionMode.DEPOSIT -> {
                DepositForm(
                    state = state,
                    onAction = onAction,
                    onOpenDatePicker = { showDatePicker = true }
                )
            }
            RecordTransactionMode.EXPENSE -> {
                ExpenseForm(
                    state = state,
                    onAction = onAction,
                    onOpenDatePicker = { showDatePicker = true }
                )
            }
        }
    }

    // M3 DatePickerDialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateUtils.localEpochToDatePickerUtc(state.dateEpochMs),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return DateUtils.isDatePickerDateSelectable(utcTimeMillis)
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { pickedUtc ->
                            val effectiveEpochMs = DateUtils.datePickerUtcToLocalEpoch(pickedUtc)
                            onAction(RecordTransactionUiAction.UpdateDate(effectiveEpochMs))
                        }
                        showDatePicker = false
                    },
                    modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                ) {
                    Text(stringResource(R.string.date_picker_ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                ) {
                    Text(stringResource(R.string.date_picker_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DepositForm(
    state: RecordTransactionUiState,
    onAction: (RecordTransactionUiAction) -> Unit,
    onOpenDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    var memberDropdownExpanded by remember { mutableStateOf(false) }
    var methodDropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // 1. Stakeholder Selector
        ExposedDropdownMenuBox(
            expanded = memberDropdownExpanded,
            onExpandedChange = { memberDropdownExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            val selectedText = state.selectedMember?.let { "${it.name} (${it.memberId})" }
                ?: stringResource(R.string.modal_add_tx_select_stakeholder_placeholder)

            OutlinedTextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.modal_add_tx_stakeholder_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberDropdownExpanded) },
                isError = state.memberError != null,
                supportingText = state.memberError?.let { { Text(it.asString()) } },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = memberDropdownExpanded,
                onDismissRequest = { memberDropdownExpanded = false }
            ) {
                if (state.members.isEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.modal_add_tx_select_stakeholder_placeholder),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = { memberDropdownExpanded = false },
                        enabled = false,
                        modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                    )
                } else {
                    state.members.forEach { member ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = member.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${member.memberId} • ${member.email}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onAction(RecordTransactionUiAction.SelectMember(member))
                                memberDropdownExpanded = false
                            },
                            modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

        // 2. Deposit Amount (Whole-Integer Long >= 1)
        OutlinedTextField(
            value = state.amountInput,
            onValueChange = { onAction(RecordTransactionUiAction.UpdateAmount(it)) },
            label = {
                Text(
                    stringResource(
                        R.string.modal_add_tx_deposit_amount_label,
                        "${state.currency.symbol} ${state.currency.name}"
                    )
                )
            },
            prefix = {
                Text(
                    text = "${state.currency.symbol} ",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            supportingText = {
                val err = state.amountError
                if (err != null) {
                    Text(text = err.asString(), color = MaterialTheme.colorScheme.error)
                } else {
                    Text(text = stringResource(R.string.modal_add_tx_amount_helper))
                }
            },
            isError = state.amountError != null,
            textStyle = MaterialTheme.typography.titleLarge.copy(
                fontFeatureSettings = "tnum"
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

        // 3. Deposit Date (<= Today)
        OutlinedTextField(
            value = DateUtils.formatDisplayDate(state.dateEpochMs),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.modal_add_tx_deposit_date_label)) },
            trailingIcon = {
                IconButton(
                    onClick = onOpenDatePicker,
                    modifier = Modifier.size(DesignTokens.MinTouchTarget)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = stringResource(R.string.modal_add_tx_select_date_cd)
                    )
                }
            },
            supportingText = state.dateError?.let { { Text(it.asString(), color = MaterialTheme.colorScheme.error) } }
                ?: { Text(stringResource(R.string.modal_add_tx_date_helper)) },
            isError = state.dateError != null,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenDatePicker() }
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

        // 4. Payment Method
        ExposedDropdownMenuBox(
            expanded = methodDropdownExpanded,
            onExpandedChange = { methodDropdownExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.paymentMethod.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.modal_add_tx_payment_method_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodDropdownExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = methodDropdownExpanded,
                onDismissRequest = { methodDropdownExpanded = false }
            ) {
                PaymentMethod.entries.forEach { method ->
                    DropdownMenuItem(
                        text = { Text(method.displayName) },
                        onClick = {
                            onAction(RecordTransactionUiAction.SelectPaymentMethod(method))
                            methodDropdownExpanded = false
                        },
                        modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

        // 5. Note (Optional)
        OutlinedTextField(
            value = state.note,
            onValueChange = { onAction(RecordTransactionUiAction.UpdateNote(it)) },
            label = { Text(stringResource(R.string.modal_add_tx_note_label)) },
            placeholder = { Text(stringResource(R.string.modal_add_tx_note_placeholder)) },
            supportingText = {
                Text(stringResource(R.string.char_counter_format, state.note.length, 250))
            },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing16))

        // 6. Action Button
        Button(
            onClick = { onAction(RecordTransactionUiAction.Submit) },
            enabled = !state.isSubmitting,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = DesignTokens.MinTouchTarget)
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                Text(text = stringResource(R.string.modal_add_tx_posting))
            } else {
                Text(
                    text = stringResource(R.string.modal_add_tx_btn_post_deposit),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseForm(
    state: RecordTransactionUiState,
    onAction: (RecordTransactionUiAction) -> Unit,
    onOpenDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var methodDropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // 1. Expense Date (<= Today)
        OutlinedTextField(
            value = DateUtils.formatDisplayDate(state.dateEpochMs),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.modal_add_tx_expense_date_label)) },
            trailingIcon = {
                IconButton(
                    onClick = onOpenDatePicker,
                    modifier = Modifier.size(DesignTokens.MinTouchTarget)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = stringResource(R.string.modal_add_tx_select_date_cd)
                    )
                }
            },
            supportingText = state.dateError?.let { { Text(it.asString(), color = MaterialTheme.colorScheme.error) } }
                ?: { Text(stringResource(R.string.modal_add_tx_date_helper)) },
            isError = state.dateError != null,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenDatePicker() }
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

        // 2. Expense Amount (Whole integer >= 1)
        OutlinedTextField(
            value = state.amountInput,
            onValueChange = { onAction(RecordTransactionUiAction.UpdateAmount(it)) },
            label = {
                Text(
                    stringResource(
                        R.string.modal_add_tx_expense_amount_label,
                        "${state.currency.symbol} ${state.currency.name}"
                    )
                )
            },
            prefix = {
                Text(
                    text = "${state.currency.symbol} ",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            supportingText = {
                val err = state.amountError
                if (err != null) {
                    Text(text = err.asString(), color = MaterialTheme.colorScheme.error)
                } else {
                    Text(text = stringResource(R.string.modal_add_tx_amount_helper))
                }
            },
            isError = state.amountError != null,
            textStyle = MaterialTheme.typography.titleLarge.copy(
                fontFeatureSettings = "tnum"
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

        // 3. Expense Category (8 Predefined Categories)
        ExposedDropdownMenuBox(
            expanded = categoryDropdownExpanded,
            onExpandedChange = { categoryDropdownExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.category.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.modal_add_tx_category_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = categoryDropdownExpanded,
                onDismissRequest = { categoryDropdownExpanded = false }
            ) {
                ExpenseCategory.entries.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.displayName) },
                        onClick = {
                            onAction(RecordTransactionUiAction.SelectCategory(category))
                            categoryDropdownExpanded = false
                        },
                        modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

        // 4. Description or Purpose (3..250 chars)
        OutlinedTextField(
            value = state.description,
            onValueChange = { onAction(RecordTransactionUiAction.UpdateDescription(it)) },
            label = { Text(stringResource(R.string.modal_add_tx_description_label)) },
            placeholder = { Text(stringResource(R.string.modal_add_tx_description_placeholder)) },
            supportingText = {
                val err = state.descriptionError
                if (err != null) {
                    Text(text = err.asString(), color = MaterialTheme.colorScheme.error)
                } else {
                    Text(stringResource(R.string.char_counter_format, state.description.length, 250))
                }
            },
            isError = state.descriptionError != null,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

        // 5. Payment Method
        ExposedDropdownMenuBox(
            expanded = methodDropdownExpanded,
            onExpandedChange = { methodDropdownExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.paymentMethod.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.modal_add_tx_payment_method_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodDropdownExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = methodDropdownExpanded,
                onDismissRequest = { methodDropdownExpanded = false }
            ) {
                PaymentMethod.entries.forEach { method ->
                    DropdownMenuItem(
                        text = { Text(method.displayName) },
                        onClick = {
                            onAction(RecordTransactionUiAction.SelectPaymentMethod(method))
                            methodDropdownExpanded = false
                        },
                        modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

        // 6. Paid By / Vendor (Optional)
        OutlinedTextField(
            value = state.paidBy,
            onValueChange = { onAction(RecordTransactionUiAction.UpdatePaidBy(it)) },
            label = { Text(stringResource(R.string.modal_add_tx_paid_by_label)) },
            placeholder = { Text(stringResource(R.string.modal_add_tx_paid_by_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing16))

        // 7. Action Button
        Button(
            onClick = { onAction(RecordTransactionUiAction.Submit) },
            enabled = !state.isSubmitting,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = DesignTokens.MinTouchTarget)
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                Text(text = stringResource(R.string.modal_add_tx_posting))
            } else {
                Text(
                    text = stringResource(R.string.modal_add_tx_btn_post_expense),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(name = "Add Transaction - Deposit Mode", showBackground = true)
@Composable
private fun RecordTransactionDepositPreview() {
    FundCircleTheme {
        Surface {
            RecordTransactionContent(
                state = RecordTransactionUiState(
                    mode = RecordTransactionMode.DEPOSIT,
                    amountInput = "50000",
                    currency = Currency.BDT,
                    members = listOf(
                        CircleMember(
                            userId = "user-1",
                            memberId = "FC-001",
                            email = "rahim@example.com",
                            name = "Rahim Uddin",
                            role = MemberRole.MEMBER,
                            status = MemberStatus.ACTIVE,
                            totalContributed = 200_000L,
                            joinedAt = 0L,
                            lastActiveAt = 0L
                        )
                    ),
                    selectedMember = CircleMember(
                        userId = "user-1",
                        memberId = "FC-001",
                        email = "rahim@example.com",
                        name = "Rahim Uddin",
                        role = MemberRole.MEMBER,
                        status = MemberStatus.ACTIVE,
                        totalContributed = 200_000L,
                        joinedAt = 0L,
                        lastActiveAt = 0L
                    )
                ),
                onAction = {},
                onClose = {}
            )
        }
    }
}
