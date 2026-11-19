package com.fundcircle.app.presentation.screens.members

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.GroupOff
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.fundcircle.app.presentation.screens.members.components.AddStakeholderSheet
import com.fundcircle.app.presentation.theme.DesignTokens
import com.fundcircle.app.presentation.theme.FundCircleTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Screen 08: Stakeholders Roster Screen (CUJ-07).
 * Searchable member directory with cumulative contributions and status badges.
 */
@Composable
fun MembersRosterRoute(
    fundId: String,
    onNavigateBack: () -> Unit,
    onNavigateToMemberDetail: (String, String) -> Unit,
    onNavigateToProfileEdit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MembersRosterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(fundId) {
        viewModel.onAction(MembersRosterUiAction.Init(fundId))
    }

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is MembersRosterUiEffect.NavigateToMemberDetail -> {
                    onNavigateToMemberDetail(effect.fundId, effect.userId)
                }
                is MembersRosterUiEffect.NavigateToProfileEdit -> {
                    onNavigateToProfileEdit()
                }
                is MembersRosterUiEffect.NavigateBack -> {
                    onNavigateBack()
                }
                is MembersRosterUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error.asString(context))
            viewModel.onAction(MembersRosterUiAction.DismissError)
        }
    }

    MembersRosterScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )

    if (state.showAddMemberSheet) {
        AddStakeholderSheet(
            fundId = state.fundId,
            onDismiss = { viewModel.onAction(MembersRosterUiAction.DismissAddMemberSheet) },
            onMemberAdded = { name, memberId ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.add_member_sheet_success, name, memberId)
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersRosterScreen(
    state: MembersRosterUiState,
    onAction: (MembersRosterUiAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = state.isSearchActive || state.searchQuery.isNotBlank()) {
        onAction(MembersRosterUiAction.SearchQueryChanged(""))
        onAction(MembersRosterUiAction.SetSearchActive(false))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            R.string.members_roster_title,
                            state.members.size
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(MembersRosterUiAction.NavigateBack) },
                        modifier = Modifier.size(DesignTokens.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.members_roster_back_cd),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Search toggle icon
                    IconButton(
                        onClick = {
                            onAction(MembersRosterUiAction.SetSearchActive(!state.isSearchActive))
                        },
                        modifier = Modifier.size(DesignTokens.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.members_roster_search_cd),
                            tint = if (state.isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Profile Edit Shortcut
                    IconButton(
                        onClick = { onAction(MembersRosterUiAction.NavigateToProfileEdit) },
                        modifier = Modifier.size(DesignTokens.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = stringResource(R.string.members_roster_profile_action_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            // Admin FAB: Add Member (Visible only if isAdmin && !circle.isLocked)
            if (state.isAdmin && state.circle?.isLocked == false) {
                val fabCd = stringResource(R.string.members_roster_fab_add_member_cd)
                ExtendedFloatingActionButton(
                    onClick = { onAction(MembersRosterUiAction.OpenAddMemberSheet) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.members_roster_fab_add_member),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
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
        if (state.isLoading && state.members.isEmpty()) {
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
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 88.dp
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

                // Search Bar Section
                item(key = "search_section") {
                    AnimatedVisibility(
                        visible = state.isSearchActive || state.searchQuery.isNotBlank(),
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing8)
                        ) {
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = { onAction(MembersRosterUiAction.SearchQueryChanged(it)) },
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.members_roster_search_placeholder),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = {
                                    if (state.searchQuery.isNotBlank()) {
                                        IconButton(
                                            onClick = { onAction(MembersRosterUiAction.SearchQueryChanged("")) },
                                            modifier = Modifier.size(DesignTokens.MinTouchTarget)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = stringResource(R.string.members_roster_clear_search_cd),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Empty States or List
                if (state.members.isEmpty()) {
                    item(key = "empty_roster") {
                        EmptyMembersCard(
                            isSearch = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing24)
                        )
                    }
                } else if (state.filteredMembers.isEmpty()) {
                    item(key = "empty_search") {
                        EmptyMembersCard(
                            isSearch = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing24)
                        )
                    }
                } else {
                    items(
                        items = state.filteredMembers,
                        key = { it.userId }
                    ) { member ->
                        MemberCardItem(
                            member = member,
                            depositCount = state.depositCounts[member.userId] ?: 0,
                            currency = state.circle?.currency ?: Currency.BDT,
                            onClick = { onAction(MembersRosterUiAction.ClickMember(member.userId)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing4)
                                .animateItem()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Member Item Card.
 * Minimum touch target >= 48dp.
 */
@Composable
fun MemberCardItem(
    member: CircleMember,
    depositCount: Int,
    currency: Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusCd = when (member.status) {
        MemberStatus.ACTIVE -> stringResource(R.string.members_roster_badge_active)
        MemberStatus.INACTIVE -> stringResource(R.string.members_roster_badge_inactive)
        MemberStatus.LEFT -> stringResource(R.string.members_roster_badge_left)
        MemberStatus.PENDING -> stringResource(R.string.members_roster_badge_pending)
    }

    val itemCd = stringResource(
        R.string.members_roster_member_item_cd,
        member.name,
        member.memberId,
        statusCd
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = itemCd
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Initials (40x40dp Circular Container)
            val initials = remember(member.name) {
                extractInitials(member.name)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.width(DesignTokens.Spacing12))

            // Main Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${member.name} (${member.memberId})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(DesignTokens.Spacing8))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Admin Role Badge
                        if (member.role == MemberRole.ADMIN) {
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ) {
                                Text(
                                    text = stringResource(R.string.members_roster_badge_admin),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        // Status Badge
                        val (containerColor, contentColor, labelRes) = when (member.status) {
                            MemberStatus.ACTIVE -> Triple(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.onPrimaryContainer,
                                R.string.members_roster_badge_active
                            )
                            MemberStatus.INACTIVE -> Triple(
                                MaterialTheme.colorScheme.tertiaryContainer,
                                MaterialTheme.colorScheme.onTertiaryContainer,
                                R.string.members_roster_badge_inactive
                            )
                            MemberStatus.LEFT -> Triple(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.onSurfaceVariant,
                                R.string.members_roster_badge_left
                            )
                            MemberStatus.PENDING -> Triple(
                                MaterialTheme.colorScheme.tertiaryContainer,
                                MaterialTheme.colorScheme.onTertiaryContainer,
                                R.string.members_roster_badge_inactive
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = containerColor,
                            contentColor = contentColor
                        ) {
                            Text(
                                text = stringResource(labelRes),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Cumulative Contribution & Contact Info
                val formattedAmount = remember(member.totalContributed) {
                    NumberFormat.getIntegerInstance(Locale.US).format(member.totalContributed)
                }
                val countText = if (depositCount == 1) {
                    stringResource(R.string.members_roster_deposits_count_singular)
                } else {
                    stringResource(R.string.members_roster_deposits_count_plural, depositCount)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.members_roster_deposits_format,
                            currency.symbol,
                            formattedAmount
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(DesignTokens.Spacing8))

                    Text(
                        text = countText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (member.phone.isNotBlank()) {
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                        Text(
                            text = "• ${member.phone}",
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
}

@Composable
fun EmptyMembersCard(
    isSearch: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSearch) Icons.Outlined.SearchOff else Icons.Outlined.GroupOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(DesignTokens.Spacing16))
            Text(
                text = stringResource(
                    if (isSearch) R.string.members_roster_empty_title else R.string.members_roster_empty_roster_title
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(DesignTokens.Spacing8))
            Text(
                text = stringResource(
                    if (isSearch) R.string.members_roster_empty_subtitle else R.string.members_roster_empty_roster_subtitle
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
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
private fun MembersRosterPreviewLight() {
    FundCircleTheme(darkTheme = false) {
        MembersRosterScreen(
            state = MembersRosterUiState(
                isLoading = false,
                isAdmin = true,
                circle = FundCircle(
                    id = "fund_1",
                    name = "Apex Capital Syndicate",
                    description = null,
                    currency = Currency.BDT,
                    adminId = "usr_1",
                    adminEmail = "admin@example.com",
                    memberEmails = listOf("admin@example.com"),
                    memberCount = 4,
                    totalDeposits = 600000L,
                    totalExpenses = 50000L,
                    recordedBalance = 550000L,
                    isLocked = false,
                    createdAt = System.currentTimeMillis()
                ),
                members = listOf(
                    CircleMember(
                        userId = "usr_1",
                        memberId = "FC-001",
                        email = "kawsar@example.com",
                        name = "Kawsar Ahmed",
                        phone = "01711111111",
                        role = MemberRole.ADMIN,
                        status = MemberStatus.ACTIVE,
                        totalContributed = 300000L,
                        joinedAt = System.currentTimeMillis(),
                        lastActiveAt = System.currentTimeMillis()
                    ),
                    CircleMember(
                        userId = "usr_2",
                        memberId = "FC-002",
                        email = "rahim@example.com",
                        name = "Rahim Uddin",
                        phone = "01722222222",
                        role = MemberRole.MEMBER,
                        status = MemberStatus.ACTIVE,
                        totalContributed = 250000L,
                        joinedAt = System.currentTimeMillis(),
                        lastActiveAt = System.currentTimeMillis()
                    ),
                    CircleMember(
                        userId = "usr_3",
                        memberId = "FC-003",
                        email = "tanvir@example.com",
                        name = "Tanvir Ahmed",
                        phone = "01733333333",
                        role = MemberRole.MEMBER,
                        status = MemberStatus.INACTIVE,
                        totalContributed = 100000L,
                        joinedAt = System.currentTimeMillis(),
                        lastActiveAt = System.currentTimeMillis()
                    ),
                    CircleMember(
                        userId = "usr_4",
                        memberId = "FC-004",
                        email = "sajjad@example.com",
                        name = "Sajjad Hossain",
                        phone = "01744444444",
                        role = MemberRole.MEMBER,
                        status = MemberStatus.LEFT,
                        totalContributed = 50000L,
                        joinedAt = System.currentTimeMillis(),
                        lastActiveAt = System.currentTimeMillis()
                    )
                ),
                filteredMembers = listOf(
                    CircleMember(
                        userId = "usr_1",
                        memberId = "FC-001",
                        email = "kawsar@example.com",
                        name = "Kawsar Ahmed",
                        phone = "01711111111",
                        role = MemberRole.ADMIN,
                        status = MemberStatus.ACTIVE,
                        totalContributed = 300000L,
                        joinedAt = System.currentTimeMillis(),
                        lastActiveAt = System.currentTimeMillis()
                    ),
                    CircleMember(
                        userId = "usr_2",
                        memberId = "FC-002",
                        email = "rahim@example.com",
                        name = "Rahim Uddin",
                        phone = "01722222222",
                        role = MemberRole.MEMBER,
                        status = MemberStatus.ACTIVE,
                        totalContributed = 250000L,
                        joinedAt = System.currentTimeMillis(),
                        lastActiveAt = System.currentTimeMillis()
                    )
                ),
                depositCounts = mapOf("usr_1" to 3, "usr_2" to 2, "usr_3" to 1, "usr_4" to 1)
            ),
            onAction = {}
        )
    }
}
