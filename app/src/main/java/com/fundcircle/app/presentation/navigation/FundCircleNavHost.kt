package com.fundcircle.app.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.fundcircle.app.presentation.screens.hub.CircleHubRoute

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
                    // Slice 4: Record Transaction modal
                },
                onNavigateToSettings = { fundId ->
                    navController.navigate(ScreenRoute.Settings(fundId = fundId))
                },
                onNavigateToMembers = { fundId ->
                    navController.navigate(ScreenRoute.MembersRoster(fundId = fundId))
                }
            )
        }

        composable<ScreenRoute.TransactionsLedger> { backStackEntry ->
            val ledger = backStackEntry.toRoute<ScreenRoute.TransactionsLedger>()
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Transactions Ledger: ${ledger.fundId} (Slice 4)")
            }
        }

        composable<ScreenRoute.TransactionDetail> { backStackEntry ->
            val detail = backStackEntry.toRoute<ScreenRoute.TransactionDetail>()
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Transaction Detail: ${detail.txId} (Slice 4)")
            }
        }

        composable<ScreenRoute.Settings> { backStackEntry ->
            val settings = backStackEntry.toRoute<ScreenRoute.Settings>()
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Settings: ${settings.fundId} (Slice 5)")
            }
        }

        composable<ScreenRoute.MembersRoster> { backStackEntry ->
            val members = backStackEntry.toRoute<ScreenRoute.MembersRoster>()
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Members: ${members.fundId} (Slice 6)")
            }
        }
    }
}
