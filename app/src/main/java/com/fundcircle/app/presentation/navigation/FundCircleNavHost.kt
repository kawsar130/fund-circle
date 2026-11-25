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
import com.fundcircle.app.presentation.components.FundCircleNavigationBar
import com.fundcircle.app.presentation.screens.auth.AuthScreen
import com.fundcircle.app.presentation.screens.create.CreateCircleRoute
import com.fundcircle.app.presentation.screens.dashboard.DashboardRoute
import com.fundcircle.app.presentation.screens.detail.TransactionDetailRoute
import com.fundcircle.app.presentation.screens.hub.CircleHubRoute
import com.fundcircle.app.presentation.screens.ledger.TransactionsLedgerRoute
import com.fundcircle.app.presentation.screens.members.MembersRosterRoute
import com.fundcircle.app.presentation.screens.members.detail.MemberDetailRoute
import com.fundcircle.app.presentation.screens.profile.ProfileEditRoute
import com.fundcircle.app.presentation.screens.reports.ReportsRoute
import com.fundcircle.app.presentation.screens.settings.SettingsRoute
import com.fundcircle.app.presentation.screens.transaction.RecordTransactionMode
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
                },
                onNavigateToAuth = {
                    navController.navigate(ScreenRoute.Welcome) {
                        popUpTo(0) { inclusive = true }
                    }
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

        composable<ScreenRoute.Dashboard> { backStackEntry ->
            val dashboard = backStackEntry.toRoute<ScreenRoute.Dashboard>()
            var recordingRequest by remember { mutableStateOf<Pair<String, RecordTransactionMode>?>(null) }

            DashboardRoute(
                onNavigateToLedger = { fundId ->
                    navigateToCircleTab(navController, fundId, CircleNavTab.TRANSACTIONS)
                },
                onNavigateToTransactionDetail = { fundId, txId, isExpense ->
                    navController.navigate(
                        ScreenRoute.TransactionDetail(fundId = fundId, txId = txId, isExpense = isExpense)
                    )
                },
                onNavigateToRecordTransaction = { fundId, initialMode ->
                    recordingRequest = fundId to initialMode
                },
                onNavigateToSettings = { fundId ->
                    navigateToCircleTab(navController, fundId, CircleNavTab.SETTINGS)
                },
                onNavigateToMembers = { fundId ->
                    navigateToCircleTab(navController, fundId, CircleNavTab.MEMBERS)
                },
                onNavigateToReports = { fundId ->
                    navigateToCircleTab(navController, fundId, CircleNavTab.REPORTS)
                },
                bottomBar = {
                    FundCircleNavigationBar(
                        selectedTab = CircleNavTab.HOME,
                        onTabSelected = { tab ->
                            navigateToCircleTab(navController, dashboard.fundId, tab)
                        }
                    )
                }
            )

            recordingRequest?.let { (fundId, initialMode) ->
                RecordTransactionSheet(
                    fundId = fundId,
                    initialMode = initialMode,
                    onDismiss = { recordingRequest = null }
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
                },
                bottomBar = {
                    FundCircleNavigationBar(
                        selectedTab = CircleNavTab.TRANSACTIONS,
                        onTabSelected = { tab ->
                            navigateToCircleTab(navController, ledger.fundId, tab)
                        }
                    )
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
            SettingsRoute(
                fundId = settings.fundId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHub = {
                    navController.navigate(ScreenRoute.CircleHub) {
                        popUpTo(ScreenRoute.Dashboard(settings.fundId)) { inclusive = true }
                    }
                },
                onNavigateToProfile = { fundId ->
                    navController.navigate(ScreenRoute.ProfileEdit(fundId = fundId))
                },
                bottomBar = {
                    FundCircleNavigationBar(
                        selectedTab = CircleNavTab.SETTINGS,
                        onTabSelected = { tab ->
                            navigateToCircleTab(navController, settings.fundId, tab)
                        }
                    )
                }
            )
        }

        composable<ScreenRoute.Reports> { backStackEntry ->
            val reports = backStackEntry.toRoute<ScreenRoute.Reports>()
            ReportsRoute(
                fundId = reports.fundId,
                onNavigateBack = { navController.popBackStack() },
                bottomBar = {
                    FundCircleNavigationBar(
                        selectedTab = CircleNavTab.REPORTS,
                        onTabSelected = { tab ->
                            navigateToCircleTab(navController, reports.fundId, tab)
                        }
                    )
                }
            )
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
                },
                bottomBar = {
                    FundCircleNavigationBar(
                        selectedTab = CircleNavTab.MEMBERS,
                        onTabSelected = { tab ->
                            navigateToCircleTab(navController, roster.fundId, tab)
                        }
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
                },
                onNavigateToProfileEdit = { fundId ->
                    navController.navigate(
                        ScreenRoute.ProfileEdit(fundId = fundId)
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

/**
 * Single-top, state-preserving navigation helper between active circle tabs.
 */
private fun navigateToCircleTab(
    navController: NavHostController,
    fundId: String,
    targetTab: CircleNavTab
) {
    val targetRoute: ScreenRoute = targetTab.toRoute(fundId)
    navController.navigate(targetRoute) {
        popUpTo(ScreenRoute.Dashboard(fundId = fundId)) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
