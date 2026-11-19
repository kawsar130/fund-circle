package com.fundcircle.app.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.fundcircle.app.presentation.screens.auth.AuthScreen
import com.fundcircle.app.presentation.screens.create.CreateCircleRoute
import com.fundcircle.app.presentation.screens.dashboard.DashboardRoute
import com.fundcircle.app.presentation.screens.detail.TransactionDetailRoute
import com.fundcircle.app.presentation.screens.hub.CircleHubRoute
import com.fundcircle.app.presentation.screens.ledger.TransactionsLedgerRoute
import com.fundcircle.app.presentation.screens.members.MembersRosterRoute
import com.fundcircle.app.presentation.screens.members.detail.MemberDetailRoute
import com.fundcircle.app.presentation.screens.profile.ProfileEditRoute
import com.fundcircle.app.presentation.screens.transaction.RecordTransactionSheet

/**
 * Type-safe navigation host for Fund Circle.
 */
@Composable
fun FundCircleNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = ScreenRoute.Welcome,
        modifier = modifier
    ) {
        composable<ScreenRoute.Welcome> {
            AuthScreen(
                onNavigateToHub = {
                    navController.navigate(ScreenRoute.CircleHub) {
                        popUpTo(ScreenRoute.Welcome) { inclusive = true }
                    }
                },
                onNavigateToDashboard = { fundId ->
                    navController.navigate(ScreenRoute.Dashboard(fundId = fundId)) {
                        popUpTo(ScreenRoute.Welcome) { inclusive = true }
                    }
                }
            )
        }

        composable<ScreenRoute.CircleHub> {
            CircleHubRoute(
                onNavigateToDashboard = { fundId ->
                    navController.navigate(ScreenRoute.Dashboard(fundId = fundId))
                },
                onNavigateToCreateCircle = {
                    navController.navigate(ScreenRoute.CreateCircle)
                }
            )
        }

        composable<ScreenRoute.CreateCircle> {
            CreateCircleRoute(
                onNavigateToDashboard = { fundId ->
                    navController.navigate(ScreenRoute.Dashboard(fundId = fundId)) {
                        popUpTo(ScreenRoute.CircleHub) { inclusive = false }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<ScreenRoute.Dashboard> {
            var recordingFundId by remember { mutableStateOf<String?>(null) }

            DashboardRoute(
                onNavigateToLedger = { fundId ->
                    navController.navigate(ScreenRoute.TransactionsLedger(fundId = fundId))
                },
                onNavigateToTransactionDetail = { fundId, txId, isExpense ->
                    navController.navigate(
                        ScreenRoute.TransactionDetail(fundId = fundId, txId = txId, isExpense = isExpense)
                    )
                },
                onNavigateToRecordTransaction = { fundId ->
                    recordingFundId = fundId
                },
                onNavigateToSettings = { fundId ->
                    navController.navigate(ScreenRoute.Settings(fundId = fundId))
                },
                onNavigateToMembers = { fundId ->
                    navController.navigate(ScreenRoute.MembersRoster(fundId = fundId))
                }
            )

            recordingFundId?.let { fundId ->
                RecordTransactionSheet(
                    fundId = fundId,
                    onDismiss = { recordingFundId = null }
                )
            }
        }

        composable<ScreenRoute.TransactionsLedger> { backStackEntry ->
            val ledger = backStackEntry.toRoute<ScreenRoute.TransactionsLedger>()
            var showRecordSheet by remember { mutableStateOf(false) }

            TransactionsLedgerRoute(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTransactionDetail = { fundId, txId, isExpense ->
                    navController.navigate(
                        ScreenRoute.TransactionDetail(fundId = fundId, txId = txId, isExpense = isExpense)
                    )
                },
                onOpenAddTransactionSheet = { _ ->
                    showRecordSheet = true
                }
            )

            if (showRecordSheet) {
                RecordTransactionSheet(
                    fundId = ledger.fundId,
                    onDismiss = { showRecordSheet = false }
                )
            }
        }

        composable<ScreenRoute.TransactionDetail> {
            TransactionDetailRoute(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<ScreenRoute.Settings> { backStackEntry ->
            val settings = backStackEntry.toRoute<ScreenRoute.Settings>()
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Settings: ${settings.fundId} (Slice 5)")
            }
        }

        composable<ScreenRoute.MembersRoster> { backStackEntry ->
            val roster = backStackEntry.toRoute<ScreenRoute.MembersRoster>()
            MembersRosterRoute(
                fundId = roster.fundId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMemberDetail = { fundId, userId ->
                    navController.navigate(
                        ScreenRoute.MemberDetail(fundId = fundId, memberId = userId)
                    )
                },
                onNavigateToProfileEdit = {
                    navController.navigate(
                        ScreenRoute.ProfileEdit(fundId = roster.fundId)
                    )
                }
            )
        }

        composable<ScreenRoute.MemberDetail> { backStackEntry ->
            val detail = backStackEntry.toRoute<ScreenRoute.MemberDetail>()
            MemberDetailRoute(
                fundId = detail.fundId,
                userId = detail.memberId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTransactionDetail = { fundId, txId, isExpense ->
                    navController.navigate(
                        ScreenRoute.TransactionDetail(fundId = fundId, txId = txId, isExpense = isExpense)
                    )
                }
            )
        }

        composable<ScreenRoute.ProfileEdit> { backStackEntry ->
            val profile = backStackEntry.toRoute<ScreenRoute.ProfileEdit>()
            ProfileEditRoute(
                fundId = profile.fundId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
