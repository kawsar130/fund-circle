package com.fundcircle.app.presentation.screens.auth

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.data.datasource.remote.GoogleAuthClient
import com.fundcircle.app.data.datasource.remote.GoogleSignInResult
import com.fundcircle.app.presentation.components.BrandLogo
import com.fundcircle.app.presentation.components.GoogleIcon
import com.fundcircle.app.presentation.theme.FundCircleTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Screen 01: Login Screen (Branded One-Tap Hero) as specified in:
 * - docs/Fund_Circle_Wireframes_and_UI_Spec_v1.0.md (Screen 01: auth_welcome)
 * - docs/Fund_Circle_API_Specification_v1.0.md (§ 2.1 Credential Manager Request Contract)
 */
@Composable
fun AuthScreen(
    onNavigateToHub: () -> Unit,
    onNavigateToDashboard: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    googleAuthClient: GoogleAuthClient? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val authClient = remember(googleAuthClient, context) {
        googleAuthClient ?: GoogleAuthClient(CredentialManager.create(context))
    }

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is AuthUiEffect.LaunchGoogleSignIn -> {
                    coroutineScope.launch {
                        when (val result = authClient.getGoogleIdToken(context)) {
                            is GoogleSignInResult.Success -> {
                                viewModel.onAction(AuthUiAction.GoogleTokenReceived(result.idToken))
                            }
                            is GoogleSignInResult.Cancelled -> {
                                viewModel.onAction(AuthUiAction.GoogleSignInCancelled)
                            }
                            is GoogleSignInResult.Failure -> {
                                viewModel.onAction(
                                    AuthUiAction.GoogleSignInFailed(
                                        UiText.DynamicString(result.message)
                                    )
                                )
                            }
                        }
                    }
                }
                is AuthUiEffect.NavigateToHub -> {
                    onNavigateToHub()
                }
                is AuthUiEffect.NavigateToDashboard -> {
                    onNavigateToDashboard(effect.fundId)
                }
                is AuthUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message.asString(context)
                    )
                }
            }
        }
    }

    AuthScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onSignInClicked = { viewModel.onAction(AuthUiAction.SignInClicked) },
        modifier = modifier
    )
}

@Composable
fun AuthScreenContent(
    uiState: AuthUiState,
    snackbarHostState: SnackbarHostState,
    onSignInClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Offline Banner (Animated)
            AnimatedVisibility(
                visible = uiState.isOffline,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.auth_offline_cache_warning),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Center Brand Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BrandLogo(
                    modifier = Modifier.padding(bottom = 20.dp),
                    size = 96.dp
                )

                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.auth_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Feature Highlights Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureHighlightItem(text = stringResource(R.string.auth_feature_1))
                    FeatureHighlightItem(text = stringResource(R.string.auth_feature_2))
                    FeatureHighlightItem(text = stringResource(R.string.auth_feature_3))
                    FeatureHighlightItem(text = stringResource(R.string.auth_feature_4))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Actions Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onSignInClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleIcon(size = 24.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.auth_btn_continue_google),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.auth_terms_notice),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FeatureHighlightItem(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun AuthScreenPreviewLight() {
    FundCircleTheme(darkTheme = false) {
        AuthScreenContent(
            uiState = AuthUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onSignInClicked = {}
        )
    }
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AuthScreenPreviewDark() {
    FundCircleTheme(darkTheme = true) {
        AuthScreenContent(
            uiState = AuthUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onSignInClicked = {}
        )
    }
}

@Preview(name = "Loading State", showBackground = true)
@Composable
private fun AuthScreenPreviewLoading() {
    FundCircleTheme(darkTheme = false) {
        AuthScreenContent(
            uiState = AuthUiState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            onSignInClicked = {}
        )
    }
}
