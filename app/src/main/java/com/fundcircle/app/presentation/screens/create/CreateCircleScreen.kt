package com.fundcircle.app.presentation.screens.create

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var currencyDropdownExpanded by remember { mutableStateOf(false) }

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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.create_circle_close_content_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.Spacing16, vertical = DesignTokens.Spacing12)
                ) {
                    Button(
                        onClick = { onAction(CreateCircleUiAction.CreateClicked) },
                        enabled = state.isCreateEnabled,
                        shape = DesignTokens.CardShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
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
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
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
                leadingIcon = { Text("🏛️", fontSize = 18.sp) },
                shape = DesignTokens.InputShape,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing16))

            // Base Currency Dropdown (Permanent Lock)
            ExposedDropdownMenuBox(
                expanded = currencyDropdownExpanded,
                onExpandedChange = { currencyDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.currency.displayName(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = stringResource(R.string.create_circle_currency_label)) },
                    leadingIcon = { Text("💱", fontSize = 18.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyDropdownExpanded) },
                    shape = DesignTokens.InputShape,
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = DesignTokens.MinTouchTarget)
                )

                ExposedDropdownMenu(
                    expanded = currencyDropdownExpanded,
                    onDismissRequest = { currencyDropdownExpanded = false }
                ) {
                    Currency.entries.forEach { currency ->
                        DropdownMenuItem(
                            text = { Text(currency.displayName()) },
                            onClick = {
                                onAction(CreateCircleUiAction.CurrencySelected(currency))
                                currencyDropdownExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing4))

            Text(
                text = stringResource(R.string.create_circle_currency_lock_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing16))

            // Description Input (Optional)
            OutlinedTextField(
                value = state.description,
                onValueChange = { onAction(CreateCircleUiAction.DescriptionChanged(it)) },
                label = { Text(text = stringResource(R.string.create_circle_description_label)) },
                placeholder = { Text(text = stringResource(R.string.create_circle_description_placeholder)) },
                leadingIcon = { Text("📝", fontSize = 18.sp) },
                shape = DesignTokens.InputShape,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing24))
        }
    }
}

@Composable
private fun Currency.displayName(): String = when (this) {
    Currency.BDT -> stringResource(R.string.currency_option_bdt)
    Currency.USD -> stringResource(R.string.currency_option_usd)
    Currency.EUR -> stringResource(R.string.currency_option_eur)
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
