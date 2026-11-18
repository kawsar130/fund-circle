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

        composable<ScreenRoute.Dashboard> { backStackEntry ->
            val dashboard = backStackEntry.toRoute<ScreenRoute.Dashboard>()
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Dashboard: ${dashboard.fundId} (Slice 3)")
            }
        }
    }
}
