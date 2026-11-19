package com.fundcircle.app.presentation.screens.members.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fundcircle.app.R
import com.fundcircle.app.presentation.theme.DesignTokens
import com.fundcircle.app.presentation.theme.FundCircleTheme

/**
 * Screen 10: Add Stakeholder Modal Bottom Sheet (Admin Flow).
 * Auto-allocates sequential Member ID (FC-00X) and commits atomic circle membership.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStakeholderSheet(
    fundId: String,
    onDismiss: () -> Unit,
    onMemberAdded: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: AddStakeholderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(fundId) {
        viewModel.onAction(AddStakeholderUiAction.Init(fundId))
    }

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is AddStakeholderUiEffect.MemberAdded -> {
                    onMemberAdded?.invoke(effect.memberName, effect.memberId)
                    onDismiss()
                }
                is AddStakeholderUiEffect.ShowSnackbar -> {
                    // Handled in parent host or through callback
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
        AddStakeholderContent(
            state = state,
            onAction = viewModel::onAction,
            onClose = onDismiss
        )
    }
}

@Composable
fun AddStakeholderContent(
    state: AddStakeholderUiState,
    onAction: (AddStakeholderUiAction) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                text = stringResource(R.string.add_member_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(DesignTokens.MinTouchTarget)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.add_member_sheet_close_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing8))

        // Sequential Member ID Allocation Preview Chip
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = DesignTokens.Spacing12, vertical = DesignTokens.Spacing8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                Text(
                    text = stringResource(
                        R.string.add_member_sheet_id_preview_format,
                        state.allocatedMemberId
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing16))

        // Full Name Field
        OutlinedTextField(
            value = state.name,
            onValueChange = { onAction(AddStakeholderUiAction.NameChanged(it)) },
            label = { Text(text = stringResource(R.string.add_member_sheet_name_label)) },
            placeholder = { Text(text = stringResource(R.string.add_member_sheet_name_placeholder)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            isError = state.nameError != null,
            supportingText = state.nameError?.let { { Text(it.asString()) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

        // Mobile Phone Number Field (11 digits)
        OutlinedTextField(
            value = state.phone,
            onValueChange = { onAction(AddStakeholderUiAction.PhoneChanged(it)) },
            label = { Text(text = stringResource(R.string.add_member_sheet_phone_label)) },
            placeholder = { Text(text = stringResource(R.string.add_member_sheet_phone_placeholder)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            isError = state.phoneError != null,
            supportingText = state.phoneError?.let { { Text(it.asString()) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

        // Gmail Address Field
        OutlinedTextField(
            value = state.email,
            onValueChange = { onAction(AddStakeholderUiAction.EmailChanged(it)) },
            label = { Text(text = stringResource(R.string.add_member_sheet_email_label)) },
            placeholder = { Text(text = stringResource(R.string.add_member_sheet_email_placeholder)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onAction(AddStakeholderUiAction.Submit) }
            ),
            isError = state.emailError != null,
            supportingText = state.emailError?.let { { Text(it.asString()) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

        // Explanatory Note
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
            Text(
                text = stringResource(R.string.add_member_sheet_explanation),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // General Error
        state.generalError?.let { error ->
            Spacer(modifier = Modifier.height(DesignTokens.Spacing12))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error.asString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(DesignTokens.Spacing12)
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing24))

        // Submit Action Button (>= 48dp)
        Button(
            onClick = { onAction(AddStakeholderUiAction.Submit) },
            enabled = !state.isSubmitting,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                Text(
                    text = stringResource(R.string.add_member_sheet_submitting),
                    style = MaterialTheme.typography.labelLarge
                )
            } else {
                Text(
                    text = stringResource(R.string.add_member_sheet_submit_btn),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun AddStakeholderPreviewLight() {
    FundCircleTheme(darkTheme = false) {
        Surface {
            AddStakeholderContent(
                state = AddStakeholderUiState(
                    allocatedMemberId = "FC-005",
                    name = "Tariqul Islam",
                    phone = "01712345678",
                    email = "tariqul.invest@gmail.com"
                ),
                onAction = {},
                onClose = {}
            )
        }
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
private fun AddStakeholderPreviewDark() {
    FundCircleTheme(darkTheme = true) {
        Surface {
            AddStakeholderContent(
                state = AddStakeholderUiState(
                    allocatedMemberId = "FC-005"
                ),
                onAction = {},
                onClose = {}
            )
        }
    }
}
