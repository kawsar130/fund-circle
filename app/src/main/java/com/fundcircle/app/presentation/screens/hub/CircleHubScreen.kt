package com.fundcircle.app.presentation.screens.hub

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fundcircle.app.R
import com.fundcircle.app.core.common.AmountFormatter
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.UserSession
import com.fundcircle.app.presentation.components.CircleStatusBadge
import com.fundcircle.app.presentation.components.LiveBadge
import com.fundcircle.app.presentation.components.RoleBadge
import com.fundcircle.app.presentation.theme.DesignTokens
import com.fundcircle.app.presentation.theme.FundCircleTheme
import com.fundcircle.app.presentation.theme.SectionHeaderStyle
import com.fundcircle.app.presentation.theme.TabularNumMedium
import java.util.Locale

@Composable
fun CircleHubRoute(
    onNavigateToDashboard: (String) -> Unit,
    onNavigateToCreateCircle: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToAuth: () -> Unit = {},
    viewModel: CircleHubViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is CircleHubUiEffect.NavigateToDashboard -> onNavigateToDashboard(effect.fundId)
                is CircleHubUiEffect.NavigateToCreateCircle -> onNavigateToCreateCircle()
                is CircleHubUiEffect.NavigateToAuth -> onNavigateToAuth()
                is CircleHubUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
            }
        }
    }

    CircleHubScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleHubScreen(
    state: CircleHubUiState,
    onAction: (CircleHubUiAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.hub_signed_in_label).uppercase(Locale.US),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = state.currentUser?.email ?: stringResource(R.string.hub_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onAction(CircleHubUiAction.SignOutClicked) },
                        modifier = Modifier.size(DesignTokens.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.settings_sign_out_title),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Offline Warning Banner
            AnimatedVisibility(
                visible = state.isOffline,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing8),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                        Text(
                            text = stringResource(R.string.hub_offline_warning),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                state.isEmpty -> {
                    EmptyCirclesView(
                        onCreateCircle = { onAction(CircleHubUiAction.CreateCircleClicked) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = DesignTokens.Spacing16,
                            end = DesignTokens.Spacing16,
                            top = DesignTokens.Spacing16,
                            bottom = DesignTokens.Spacing24
                        ),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing12)
                    ) {
                        // Section: Managed Circles
                        if (state.managedCircles.isNotEmpty()) {
                            item(key = "section_managed") {
                                Text(
                                    text = stringResource(
                                        R.string.hub_section_managed,
                                        state.managedCircles.size
                                    ).uppercase(Locale.US),
                                    style = SectionHeaderStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = DesignTokens.Spacing4)
                                )
                            }

                            items(
                                items = state.managedCircles,
                                key = { it.id }
                            ) { circle ->
                                CircleCard(
                                    circle = circle,
                                    isAdmin = true,
                                    isActive = circle.id == state.activeCircleId,
                                    isOffline = state.isOffline,
                                    onClick = { onAction(CircleHubUiAction.CircleSelected(circle.id)) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }

                        // Section: Joined Circles
                        if (state.joinedCircles.isNotEmpty()) {
                            item(key = "section_joined") {
                                Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                                Text(
                                    text = stringResource(
                                        R.string.hub_section_joined,
                                        state.joinedCircles.size
                                    ).uppercase(Locale.US),
                                    style = SectionHeaderStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = DesignTokens.Spacing4)
                                )
                            }

                            items(
                                items = state.joinedCircles,
                                key = { it.id }
                            ) { circle ->
                                CircleCard(
                                    circle = circle,
                                    isAdmin = false,
                                    isActive = circle.id == state.activeCircleId,
                                    isOffline = state.isOffline,
                                    onClick = { onAction(CircleHubUiAction.CircleSelected(circle.id)) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }

                        // Bottom Action: Dashed Outlined Create Button
                        item(key = "section_create_dashed") {
                            Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
                            OutlinedButton(
                                onClick = { onAction(CircleHubUiAction.CreateCircleClicked) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = DesignTokens.CardShape,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = stringResource(R.string.hub_create_circle_dashed_btn),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(DesignTokens.Spacing16))
                        }
                    }
                }
            }
        }
    }

    if (state.showSignOutConfirm) {
        SignOutConfirmDialog(
            isSigningOut = state.isSigningOut,
            onConfirm = { onAction(CircleHubUiAction.ConfirmSignOut) },
            onDismiss = { onAction(CircleHubUiAction.DismissSignOut) }
        )
    }
}

@Composable
private fun SignOutConfirmDialog(
    isSigningOut: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSigningOut) onDismiss() },
        title = {
            Text(
                text = stringResource(R.string.hub_sign_out_dialog_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.hub_sign_out_dialog_body),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isSigningOut,
                modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
            ) {
                Text(stringResource(R.string.hub_sign_out_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSigningOut,
                modifier = Modifier.defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
            ) {
                Text(stringResource(R.string.hub_sign_out_dialog_cancel))
            }
        }
    )
}

@Composable
fun CircleCard(
    circle: FundCircle,
    isAdmin: Boolean,
    isActive: Boolean,
    isOffline: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedBalance = remember(circle.recordedBalance) {
        AmountFormatter.format(circle.recordedBalance)
    }
    val balanceLabel = stringResource(
        R.string.hub_balance_format,
        circle.currency.symbol,
        formattedBalance
    )
    val cardContentDescription = stringResource(
        R.string.hub_circle_card_content_description,
        circle.name,
        balanceLabel
    )

    val border = if (isActive) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    }

    Card(
        shape = DesignTokens.CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = border,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
            .clip(DesignTokens.CardShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = cardContentDescription }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing16),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing6),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoleBadge(role = if (isAdmin) MemberRole.ADMIN else MemberRole.MEMBER)
                    if (circle.isLocked) {
                        CircleStatusBadge(isLocked = true)
                    }
                    if (isActive) {
                        LiveBadge(isOffline = isOffline)
                    }
                }

                Spacer(modifier = Modifier.height(DesignTokens.Spacing6))

                Text(
                    text = circle.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(DesignTokens.Spacing2))

                val membersCountText = pluralStringResource(
                    R.plurals.hub_members_count,
                    circle.memberCount,
                    circle.memberCount
                )
                Text(
                    text = stringResource(
                        R.string.hub_members_currency_line,
                        membersCountText,
                        circle.currency.name,
                        circle.currency.symbol
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(DesignTokens.Spacing12))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${circle.currency.symbol} $formattedBalance",
                    style = TabularNumMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(
                    text = stringResource(R.string.hub_balance_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyCirclesView(
    onCreateCircle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(DesignTokens.Spacing24),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing16))

            Text(
                text = stringResource(R.string.hub_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing8))

            Text(
                text = stringResource(R.string.hub_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing24))

            Button(
                onClick = onCreateCircle,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                Text(
                    text = stringResource(R.string.hub_btn_create_first),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CircleHubScreenPopulatedPreview() {
    FundCircleTheme {
        CircleHubScreen(
            state = CircleHubUiState(
                isLoading = false,
                managedCircles = listOf(
                    FundCircle(
                        id = "1",
                        name = "Green Valley Property Pool",
                        description = "Pooled commercial real-estate growth fund.",
                        currency = Currency.BDT,
                        adminId = "user1",
                        adminEmail = "kawsar@example.com",
                        memberEmails = listOf("kawsar@example.com"),
                        memberCount = 14,
                        totalDeposits = 1600000L,
                        totalExpenses = 150000L,
                        recordedBalance = 1450000L,
                        isLocked = false,
                        createdAt = 1704067200000L
                    ),
                    FundCircle(
                        id = "2",
                        name = "Angel Syndicate 2026",
                        description = "Tech angel investment pool.",
                        currency = Currency.USD,
                        adminId = "user1",
                        adminEmail = "kawsar@example.com",
                        memberEmails = listOf("kawsar@example.com"),
                        memberCount = 6,
                        totalDeposits = 90000L,
                        totalExpenses = 5000L,
                        recordedBalance = 85000L,
                        isLocked = true,
                        createdAt = 1704067200000L
                    )
                ),
                joinedCircles = listOf(
                    FundCircle(
                        id = "3",
                        name = "Batch '14 Partnership Fund",
                        description = null,
                        currency = Currency.BDT,
                        adminId = "user2",
                        adminEmail = "tanvir@example.com",
                        memberEmails = listOf("kawsar@example.com", "tanvir@example.com"),
                        memberCount = 20,
                        totalDeposits = 500000L,
                        totalExpenses = 20000L,
                        recordedBalance = 480000L,
                        isLocked = false,
                        createdAt = 1704067200000L
                    )
                ),
                activeCircleId = "1",
                totalCircleCount = 3,
                currentUser = UserSession("user1", "kawsar@example.com", "Kawsar", null, "token", "1")
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CircleHubScreenEmptyPreview() {
    FundCircleTheme {
        CircleHubScreen(
            state = CircleHubUiState(
                isLoading = false,
                managedCircles = emptyList(),
                joinedCircles = emptyList(),
                totalCircleCount = 0
            ),
            onAction = {}
        )
    }
}
