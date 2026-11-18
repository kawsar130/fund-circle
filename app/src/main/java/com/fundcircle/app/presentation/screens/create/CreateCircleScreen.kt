package com.fundcircle.app.presentation.screens.create

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fundcircle.app.R
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.presentation.theme.DesignTokens
import com.fundcircle.app.presentation.theme.FundCircleTheme

@Composable
fun CreateCircleRoute(
    onNavigateToDashboard: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateCircleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is CreateCircleUiEffect.NavigateToDashboard -> onNavigateToDashboard(effect.fundId)
                is CreateCircleUiEffect.NavigateBack -> onNavigateBack()
                is CreateCircleUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
            }
        }
    }

    CreateCircleScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCircleScreen(
    state: CreateCircleUiState,
    onAction: (CreateCircleUiAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    BackHandler {
        onAction(CreateCircleUiAction.BackClicked)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.create_circle_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(CreateCircleUiAction.BackClicked) },
                        modifier = Modifier.size(DesignTokens.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.create_circle_close_content_description)
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
                .verticalScroll(scrollState)
                .imePadding()
                .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing8)
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
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = DesignTokens.Spacing16)
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

            Text(
                text = stringResource(R.string.create_circle_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing24))

            // Circle Name Input
            OutlinedTextField(
                value = state.name,
                onValueChange = { onAction(CreateCircleUiAction.NameChanged(it)) },
                label = { Text(text = stringResource(R.string.create_circle_name_label)) },
                placeholder = { Text(text = stringResource(R.string.create_circle_name_placeholder)) },
                singleLine = true,
                isError = state.nameError != null,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = state.nameError?.asString(context).orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            text = stringResource(
                                R.string.create_circle_char_count_format,
                                state.name.length,
                                50
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing16))

            // Primary Currency Selector
            Text(
                text = stringResource(R.string.create_circle_currency_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing4))

            Text(
                text = stringResource(R.string.create_circle_currency_helper),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing8))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing8)
            ) {
                Currency.entries.forEach { currency ->
                    val isSelected = state.currency == currency
                    FilterChip(
                        selected = isSelected,
                        onClick = { onAction(CreateCircleUiAction.CurrencySelected(currency)) },
                        label = {
                            Text(
                                text = stringResource(
                                    R.string.create_circle_currency_option,
                                    currency.symbol,
                                    currency.name
                                ),
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing16))

            // Permanent Currency Lock Notice Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(DesignTokens.Spacing16),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing12))
                    Column {
                        Text(
                            text = stringResource(R.string.create_circle_currency_lock_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing4))
                        Text(
                            text = stringResource(R.string.create_circle_currency_lock_warning),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing16))

            // Description Input (Optional)
            OutlinedTextField(
                value = state.description,
                onValueChange = { onAction(CreateCircleUiAction.DescriptionChanged(it)) },
                label = { Text(text = stringResource(R.string.create_circle_description_label)) },
                placeholder = { Text(text = stringResource(R.string.create_circle_description_placeholder)) },
                minLines = 3,
                maxLines = 5,
                isError = state.descriptionError != null,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = state.descriptionError?.asString(context).orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            text = stringResource(
                                R.string.create_circle_char_count_format,
                                state.description.length,
                                250
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing24))

            // Submit Button
            Button(
                onClick = { onAction(CreateCircleUiAction.CreateClicked) },
                enabled = state.isCreateEnabled,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing8))
                    Text(
                        text = stringResource(R.string.create_circle_loading),
                        style = MaterialTheme.typography.labelLarge
                    )
                } else {
                    Text(
                        text = stringResource(R.string.create_circle_btn_submit),
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing16))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateCircleScreenPreview() {
    FundCircleTheme {
        CreateCircleScreen(
            state = CreateCircleUiState(),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateCircleScreenLoadingPreview() {
    FundCircleTheme {
        CreateCircleScreen(
            state = CreateCircleUiState(
                name = "Apex Growth Syndicate",
                currency = Currency.BDT,
                isLoading = true
            ),
            onAction = {}
        )
    }
}
