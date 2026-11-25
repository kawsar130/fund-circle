package com.fundcircle.app.presentation.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CloudDone
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fundcircle.app.R
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.presentation.components.CurrencyLockBadge
import com.fundcircle.app.presentation.components.LiveBadge
import com.fundcircle.app.presentation.theme.DesignTokens
import com.fundcircle.app.presentation.theme.FundCircleTheme
import com.fundcircle.app.presentation.theme.LocalFundCircleDarkTheme
import com.fundcircle.app.presentation.theme.SectionHeaderStyle
import java.util.Locale

@Composable
fun SettingsRoute(
    fundId: String,
    onNavigateToHub: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is SettingsUiEffect.NavigateToHub -> onNavigateToHub()
                is SettingsUiEffect.NavigateToProfile -> onNavigateToProfile(effect.fundId)
                is SettingsUiEffect.NavigateToAuth -> onNavigateToAuth()
                is SettingsUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error.asString(context))
            viewModel.onAction(SettingsUiAction.DismissError)
        }
    }

    SettingsScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        bottomBar = bottomBar,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onAction: (SettingsUiAction) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
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
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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
        } else {
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding)
                    .padding(horizontal = DesignTokens.Spacing16)
            ) {
                item(key = "spacer_top") {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                }

                // 1. Workspace Switcher Card
                item(key = "item_switch_circle") {
                    Card(
                        onClick = { onAction(SettingsUiAction.ClickSwitchCircle) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                        shape = DesignTokens.CardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing16),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_switch_circle_card_category),
                                    style = SectionHeaderStyle,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.settings_switch_circle_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.settings_switch_circle_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                item(key = "spacer_switcher_profile") {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing12))
                }

                // 2. Membership & Profile Card
                item(key = "item_my_profile") {
                    val memberIdLabel = state.currentMember?.memberId?.takeIf { it.isNotBlank() } ?: "FC-001"
                    Card(
                        onClick = { onAction(SettingsUiAction.ClickMyProfile) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                        shape = DesignTokens.CardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing16),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_my_profile_title, memberIdLabel),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.settings_my_profile_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "✏️",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                item(key = "spacer_profile_currency") {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                }

                // 3. Base Currency Permanence Card (Invariant 1)
                item(key = "item_base_currency") {
                    val currency = state.circle?.currency ?: Currency.BDT
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                        shape = DesignTokens.CardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing16),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_base_currency_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.settings_base_currency_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            CurrencyLockBadge(currency = currency)
                        }
                    }
                }

                item(key = "spacer_currency_theme") {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                }

                // 4. Color Theme Card
                item(key = "item_dark_theme") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                        shape = DesignTokens.CardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing16),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_dark_theme_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.settings_dark_theme_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            val isDarkActive = state.darkThemeOverride ?: LocalFundCircleDarkTheme.current
                            Switch(
                                checked = isDarkActive,
                                onCheckedChange = { enabled ->
                                    onAction(SettingsUiAction.SetDarkTheme(enabled))
                                }
                            )
                        }
                    }
                }

                item(key = "spacer_theme_cache") {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                }

                // 5. Firestore 100MB Offline Disk Cache Quota Indicator (Invariant 5)
                item(key = "item_offline_cache") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                        shape = DesignTokens.CardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing16),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_offline_cache_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.settings_offline_cache_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            LiveBadge()
                        }
                    }
                }

                item(key = "spacer_cache_governance") {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                }

                // 6. Administration (Danger Zone)
                if (state.isAdmin) {
                    item(key = "item_governance_admin") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = DesignTokens.CardShape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Column(modifier = Modifier.padding(DesignTokens.Spacing16)) {
                                Text(
                                    text = stringResource(R.string.settings_governance_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(DesignTokens.Spacing4))
                                Text(
                                    text = stringResource(R.string.settings_governance_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(DesignTokens.Spacing12))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing8)
                                ) {
                                    OutlinedButton(
                                        onClick = { onAction(SettingsUiAction.OpenLeaveDialog) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.settings_btn_leave_circle),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                    Button(
                                        onClick = { onAction(SettingsUiAction.OpenLockDialog) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        )
                                    ) {
                                        val lockButtonText = if (state.isCircleLocked) {
                                            stringResource(R.string.settings_dialog_unlock_confirm)
                                        } else {
                                            stringResource(R.string.settings_btn_archive_circle)
                                        }
                                        Text(
                                            text = lockButtonText,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                                OutlinedButton(
                                    onClick = { onAction(SettingsUiAction.OpenTransferAdminDialog) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_btn_transfer_leader),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item(key = "item_membership_non_admin") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = DesignTokens.CardShape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(DesignTokens.Spacing16)) {
                                Text(
                                    text = stringResource(R.string.settings_membership_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(DesignTokens.Spacing4))
                                Text(
                                    text = stringResource(R.string.settings_membership_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(DesignTokens.Spacing12))
                                OutlinedButton(
                                    onClick = { onAction(SettingsUiAction.OpenLeaveDialog) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_btn_leave_circle),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "spacer_bottom") {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing32))
                }
            }
        }
    }

    // Dialogs
    if (state.showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { onAction(SettingsUiAction.DismissDialogs) },
            title = {
                Text(
                    text = stringResource(R.string.settings_dialog_leave_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.settings_dialog_leave_body,
                        state.circle?.name.orEmpty()
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { onAction(SettingsUiAction.ConfirmLeaveCircle) },
                    enabled = !state.isActionInProgress,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                ) {
                    Text(stringResource(R.string.settings_dialog_leave_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onAction(SettingsUiAction.DismissDialogs) },
                    modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                ) {
                    Text(stringResource(R.string.settings_dialog_cancel))
                }
            }
        )
    }

    if (state.showTransferAdminDialog) {
        TransferAdminDialog(
            circleName = state.circle?.name.orEmpty(),
            activeMembers = state.activeMembers,
            selectedUserId = state.selectedNewAdminUserId,
            isActionInProgress = state.isActionInProgress,
            onSelectUserId = { onAction(SettingsUiAction.SelectNewAdmin(it)) },
            onConfirm = { onAction(SettingsUiAction.ConfirmTransferAdmin) },
            onDismiss = { onAction(SettingsUiAction.DismissDialogs) }
        )
    }

    if (state.showLockDialog) {
        val lockDialogTitle = if (state.isCircleLocked) {
            stringResource(R.string.settings_dialog_unlock_title)
        } else {
            stringResource(R.string.settings_dialog_lock_title)
        }
        val lockDialogBody = if (state.isCircleLocked) {
            stringResource(R.string.settings_dialog_unlock_body, state.circle?.name.orEmpty())
        } else {
            stringResource(R.string.settings_dialog_lock_body, state.circle?.name.orEmpty())
        }
        val lockDialogConfirm = if (state.isCircleLocked) {
            stringResource(R.string.settings_dialog_unlock_confirm)
        } else {
            stringResource(R.string.settings_dialog_lock_confirm)
        }

        AlertDialog(
            onDismissRequest = { onAction(SettingsUiAction.DismissDialogs) },
            title = {
                Text(
                    text = lockDialogTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = lockDialogBody,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { onAction(SettingsUiAction.ConfirmToggleCircleLock) },
                    enabled = !state.isActionInProgress,
                    modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                ) {
                    Text(lockDialogConfirm)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onAction(SettingsUiAction.DismissDialogs) },
                    modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                ) {
                    Text(stringResource(R.string.settings_dialog_cancel))
                }
            }
        )
    }

    if (state.showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { onAction(SettingsUiAction.DismissDialogs) },
            title = {
                Text(
                    text = stringResource(R.string.settings_dialog_sign_out_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_dialog_sign_out_body),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { onAction(SettingsUiAction.ConfirmSignOut) },
                    enabled = !state.isActionInProgress,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                ) {
                    Text(stringResource(R.string.settings_dialog_sign_out_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onAction(SettingsUiAction.DismissDialogs) },
                    modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                ) {
                    Text(stringResource(R.string.settings_dialog_cancel))
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(Locale.US),
        style = SectionHeaderStyle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(vertical = DesignTokens.Spacing8)
    )
}

@Composable
fun SettingsActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget),
        shape = DesignTokens.CardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isDestructive) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        border = BorderStroke(
            1.dp,
            if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(DesignTokens.Spacing16))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TransferAdminDialog(
    circleName: String,
    activeMembers: List<CircleMember>,
    selectedUserId: String?,
    isActionInProgress: Boolean,
    onSelectUserId: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_dialog_transfer_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(R.string.settings_dialog_transfer_body, circleName),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

                if (activeMembers.isEmpty()) {
                    Text(
                        text = stringResource(R.string.settings_dialog_transfer_no_members),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    activeMembers.forEach { member ->
                        val isSelected = member.userId == selectedUserId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                                .clickable(role = Role.RadioButton) { onSelectUserId(member.userId) }
                                .padding(vertical = DesignTokens.Spacing4),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                                modifier = Modifier.size(DesignTokens.MinTouchTarget)
                            )
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                            Column {
                                Text(
                                    text = "${member.name} (${member.memberId})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = member.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                    Text(
                        text = stringResource(R.string.settings_dialog_transfer_warning),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isActionInProgress && !selectedUserId.isNullOrBlank() && activeMembers.isNotEmpty(),
                modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
            ) {
                Text(stringResource(R.string.settings_dialog_transfer_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
            ) {
                Text(stringResource(R.string.settings_dialog_cancel))
            }
        }
    )
}

@Preview(name = "Settings Screen - Admin Light", showBackground = true)
@Composable
fun SettingsScreenPreview_Admin() {
    FundCircleTheme {
        SettingsScreen(
            state = SettingsUiState(
                isLoading = false,
                circle = FundCircle(
                    id = "fund-1",
                    name = "Green Valley Property Pool",
                    currency = Currency.BDT,
                    description = "Syndicate investment fund",
                    adminId = "usr-1",
                    adminEmail = "admin@example.com",
                    memberEmails = listOf("admin@example.com", "tariqul@example.com"),
                    memberCount = 2,
                    totalDeposits = 1450000,
                    totalExpenses = 88000,
                    recordedBalance = 1362000,
                    isLocked = false,
                    createdAt = 1700000000000L
                ),
                currentUserId = "usr-1",
                isAdmin = true,
                currentMember = CircleMember(
                    userId = "usr-1",
                    memberId = "FC-001",
                    name = "Kawsar Ahmed",
                    email = "admin@example.com",
                    role = MemberRole.ADMIN,
                    status = MemberStatus.ACTIVE,
                    totalContributed = 500000,
                    joinedAt = 1700000000000L,
                    lastActiveAt = 1700000000000L
                ),
                activeMembers = listOf(
                    CircleMember(
                        userId = "usr-2",
                        memberId = "FC-002",
                        name = "Tariqul Islam",
                        email = "tariqul@example.com",
                        role = MemberRole.MEMBER,
                        status = MemberStatus.ACTIVE,
                        totalContributed = 300000,
                        joinedAt = 1700000000000L,
                        lastActiveAt = 1700000000000L
                    )
                )
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@Preview(name = "Settings Screen - Stakeholder Dark", showBackground = true)
@Composable
fun SettingsScreenPreview_Stakeholder_Dark() {
    FundCircleTheme(darkTheme = true) {
        SettingsScreen(
            state = SettingsUiState(
                isLoading = false,
                circle = FundCircle(
                    id = "fund-1",
                    name = "Green Valley Property Pool",
                    currency = Currency.BDT,
                    description = "Syndicate investment fund",
                    adminId = "usr-1",
                    adminEmail = "admin@example.com",
                    memberEmails = listOf("admin@example.com", "tariqul@example.com"),
                    memberCount = 2,
                    totalDeposits = 1450000,
                    totalExpenses = 88000,
                    recordedBalance = 1362000,
                    isLocked = false,
                    createdAt = 1700000000000L
                ),
                currentUserId = "usr-2",
                isAdmin = false,
                currentMember = CircleMember(
                    userId = "usr-2",
                    memberId = "FC-002",
                    name = "Tariqul Islam",
                    email = "tariqul@example.com",
                    role = MemberRole.MEMBER,
                    status = MemberStatus.ACTIVE,
                    totalContributed = 300000,
                    joinedAt = 1700000000000L,
                    lastActiveAt = 1700000000000L
                )
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}
