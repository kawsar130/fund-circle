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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Groups
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.UserSession
import com.fundcircle.app.presentation.theme.DesignTokens
import com.fundcircle.app.presentation.theme.FundCircleTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CircleHubRoute(
    onNavigateToDashboard: (String) -> Unit,
    onNavigateToCreateCircle: () -> Unit,
    modifier: Modifier = Modifier,
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
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.hub_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(
                        onClick = { onAction(CircleHubUiAction.RefreshTriggered) },
                        modifier = Modifier.size(DesignTokens.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.hub_refresh_content_description)
                        )
                    }
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing4))
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = stringResource(R.string.hub_counter_badge, state.totalCircleCount),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        border = null,
                        modifier = Modifier
                            .defaultMinSize(minHeight = DesignTokens.CompactTouchTarget)
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                    val avatarDesc = stringResource(R.string.hub_user_avatar_content_description)
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .size(36.dp)
                            .semantics {
                                contentDescription = avatarDesc
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val initial = state.currentUser?.displayName?.takeIf { it.isNotBlank() }?.firstOrNull()?.uppercase()
                                ?: state.currentUser?.email?.takeIf { it.isNotBlank() }?.firstOrNull()?.uppercase()
                                ?: "U"
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing16))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction(CircleHubUiAction.CreateCircleClicked) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.hub_btn_new_circle),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .defaultMinSize(minHeight = DesignTokens.MinTouchTarget, minWidth = DesignTokens.MinTouchTarget)
                    .padding(bottom = DesignTokens.Spacing8)
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
                            bottom = 88.dp // Avoid FAB overlap
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
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.outline,
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
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.outline,
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
                                    onClick = { onAction(CircleHubUiAction.CircleSelected(circle.id)) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CircleCard(
    circle: FundCircle,
    isAdmin: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedBalance = remember(circle.recordedBalance) {
        NumberFormat.getIntegerInstance(Locale.US).format(circle.recordedBalance)
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
        BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = border,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = cardContentDescription }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = circle.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(DesignTokens.Spacing8))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing4),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isActive) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = stringResource(R.string.hub_badge_active),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (circle.isLocked) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = stringResource(R.string.hub_badge_locked),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (isAdmin) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (isAdmin) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    ) {
                        Text(
                            text = if (isAdmin) {
                                stringResource(R.string.hub_badge_admin)
                            } else {
                                stringResource(R.string.hub_badge_member)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (!circle.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(DesignTokens.Spacing4))
                Text(
                    text = circle.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing12))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.hub_balance_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = balanceLabel,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                val membersCountText = if (circle.memberCount == 1) {
                    stringResource(R.string.hub_member_count_single)
                } else {
                    stringResource(R.string.hub_members_count, circle.memberCount)
                }
                Text(
                    text = stringResource(
                        R.string.hub_currency_and_members,
                        circle.currency.name,
                        membersCountText
                    ),
                    style = MaterialTheme.typography.bodyMedium,
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
