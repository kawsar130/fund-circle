package com.fundcircle.app.presentation.screens.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.fundcircle.app.presentation.theme.SectionHeaderStyle
import java.util.Locale

/**
 * Screen 11: Edit My Profile / Self-Service KYC Screen (CUJ-07).
 * Private stakeholder NID and nominee management with strict subcollection isolation.
 */
@Composable
fun ProfileEditRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    fundId: String = "",
    viewModel: ProfileEditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(fundId) {
        if (fundId.isNotBlank()) {
            viewModel.onAction(ProfileEditUiAction.Init(fundId))
        }
    }

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ProfileEditUiEffect.NavigateBack -> {
                    onNavigateBack()
                }
                is ProfileEditUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
            }
        }
    }

    ProfileEditScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    state: ProfileEditUiState,
    onAction: (ProfileEditUiAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_edit_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(ProfileEditUiAction.NavigateBack) },
                        modifier = Modifier.size(DesignTokens.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.profile_edit_back_cd),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onAction(ProfileEditUiAction.Save) },
                        enabled = !state.isSaving && !state.isLoading,
                        modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.profile_edit_save_btn),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing16)
            ) {
                // Circle Context & Privacy Notice Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(DesignTokens.Spacing12)) {
                        val circleName = state.circle?.name ?: "Fund Circle"
                        val memberId = state.member?.memberId ?: "Member"
                        Text(
                            text = stringResource(R.string.profile_edit_circle_info_format, circleName, memberId),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.profile_edit_privacy_notice),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(DesignTokens.Spacing16))

                // Section 1: Personal Information Card
                Card(
                    shape = DesignTokens.CardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(DesignTokens.Spacing16)) {
                        Text(
                            text = stringResource(R.string.profile_edit_section_personal).uppercase(Locale.US),
                            style = SectionHeaderStyle,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

                        // Full Name (Read-Only from Circle Membership)
                        OutlinedTextField(
                            value = state.fullName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(text = stringResource(R.string.profile_edit_name_label)) },
                            supportingText = {
                                Text(text = stringResource(R.string.profile_edit_name_helper))
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

                        // Mobile Phone Number (From Member Record)
                        OutlinedTextField(
                            value = state.member?.phone.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(text = stringResource(R.string.profile_edit_phone_label)) },
                            placeholder = { Text(text = stringResource(R.string.profile_edit_phone_placeholder)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

                        // National Identity Number (NID)
                        OutlinedTextField(
                            value = state.nidNumber,
                            onValueChange = { onAction(ProfileEditUiAction.NidChanged(it)) },
                            label = { Text(text = stringResource(R.string.profile_edit_nid_label)) },
                            placeholder = { Text(text = stringResource(R.string.profile_edit_nid_placeholder)) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            isError = state.nidError != null,
                            supportingText = {
                                if (state.nidError != null) {
                                    Text(state.nidError.asString())
                                } else {
                                    Text(stringResource(R.string.profile_edit_nid_helper))
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Masked NID Indicator if available
                        if (state.maskedNid.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.profile_edit_masked_nid_label, state.maskedNid),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(DesignTokens.Spacing16))

                // Section 2: Beneficiary / Nominee Information Card
                Card(
                    shape = DesignTokens.CardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(DesignTokens.Spacing16)) {
                        Text(
                            text = stringResource(R.string.profile_edit_section_nominee).uppercase(Locale.US),
                            style = SectionHeaderStyle,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

                        // Nominee Full Name
                        OutlinedTextField(
                            value = state.nomineeName,
                            onValueChange = { onAction(ProfileEditUiAction.NomineeNameChanged(it)) },
                            label = { Text(text = stringResource(R.string.profile_edit_nominee_name_label)) },
                            placeholder = { Text(text = stringResource(R.string.profile_edit_nominee_name_placeholder)) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            isError = state.nomineeNameError != null,
                            supportingText = state.nomineeNameError?.let { { Text(it.asString()) } },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

                        // Nominee Relationship Dropdown
                        var relationExpanded by remember { mutableStateOf(false) }
                        val relationOptions = listOf(
                            stringResource(R.string.profile_edit_relation_spouse),
                            stringResource(R.string.profile_edit_relation_parent),
                            stringResource(R.string.profile_edit_relation_sibling),
                            stringResource(R.string.profile_edit_relation_child),
                            stringResource(R.string.profile_edit_relation_partner),
                            stringResource(R.string.profile_edit_relation_other)
                        )

                        ExposedDropdownMenuBox(
                            expanded = relationExpanded,
                            onExpandedChange = { relationExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = state.nomineeRelation,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(text = stringResource(R.string.profile_edit_relation_label)) },
                                placeholder = { Text(text = stringResource(R.string.profile_edit_relation_placeholder)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = relationExpanded) },
                                isError = state.nomineeRelationError != null,
                                supportingText = state.nomineeRelationError?.let { { Text(it.asString()) } },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = relationExpanded,
                                onDismissRequest = { relationExpanded = false }
                            ) {
                                relationOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(text = option) },
                                        onClick = {
                                            onAction(ProfileEditUiAction.NomineeRelationChanged(option))
                                            relationExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

                        // Nominee Contact Phone
                        OutlinedTextField(
                            value = state.nomineePhone,
                            onValueChange = { onAction(ProfileEditUiAction.NomineePhoneChanged(it)) },
                            label = { Text(text = stringResource(R.string.profile_edit_nominee_phone_label)) },
                            placeholder = { Text(text = stringResource(R.string.profile_edit_nominee_phone_placeholder)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { onAction(ProfileEditUiAction.Save) }
                            ),
                            isError = state.nomineePhoneError != null,
                            supportingText = state.nomineePhoneError?.let { { Text(it.asString()) } },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // General Error Banner
                state.generalError?.let { error ->
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing16))
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

                Spacer(modifier = Modifier.height(DesignTokens.Spacing32))

                // Primary Save Action Button (52dp Pill Shape)
                Button(
                    onClick = { onAction(ProfileEditUiAction.Save) },
                    enabled = !state.isSaving,
                    shape = DesignTokens.PillShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                        Text(
                            text = stringResource(R.string.profile_edit_saving),
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                        Text(
                            text = stringResource(R.string.profile_edit_save_btn),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun ProfileEditPreviewLight() {
    FundCircleTheme(darkTheme = false) {
        ProfileEditScreen(
            state = ProfileEditUiState(
                isLoading = false,
                fullName = "Rahim Uddin",
                nidNumber = "19882692015000123",
                maskedNid = "•••• •••• 0123",
                nomineeName = "Nasrin Akter",
                nomineePhone = "01812345678",
                nomineeRelation = "Spouse"
            ),
            onAction = {}
        )
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
private fun ProfileEditPreviewDark() {
    FundCircleTheme(darkTheme = true) {
        ProfileEditScreen(
            state = ProfileEditUiState(
                isLoading = false,
                fullName = "Rahim Uddin",
                maskedNid = "•••• •••• 0123"
            ),
            onAction = {}
        )
    }
}
