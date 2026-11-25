package com.fundcircle.app.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.fundcircle.app.R

/**
 * The 5 top-level navigation tabs within an active circle workspace.
 */
enum class CircleNavTab(
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val contentDescriptionRes: Int
) {
    HOME(
        labelRes = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        contentDescriptionRes = R.string.nav_home_cd
    ),
    MEMBERS(
        labelRes = R.string.nav_members,
        selectedIcon = Icons.Filled.Group,
        unselectedIcon = Icons.Outlined.Group,
        contentDescriptionRes = R.string.nav_members_cd
    ),
    TRANSACTIONS(
        labelRes = R.string.nav_transactions,
        selectedIcon = Icons.AutoMirrored.Filled.ReceiptLong,
        unselectedIcon = Icons.AutoMirrored.Outlined.ReceiptLong,
        contentDescriptionRes = R.string.nav_transactions_cd
    ),
    REPORTS(
        labelRes = R.string.nav_reports,
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
        contentDescriptionRes = R.string.nav_reports_cd
    ),
    SETTINGS(
        labelRes = R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        contentDescriptionRes = R.string.nav_settings_cd
    );

    fun toRoute(fundId: String): ScreenRoute = when (this) {
        HOME -> ScreenRoute.Dashboard(fundId)
        MEMBERS -> ScreenRoute.MembersRoster(fundId)
        TRANSACTIONS -> ScreenRoute.TransactionsLedger(fundId)
        REPORTS -> ScreenRoute.Reports(fundId)
        SETTINGS -> ScreenRoute.Settings(fundId)
    }

    companion object {
        fun fromRoute(route: ScreenRoute?): CircleNavTab? = when (route) {
            is ScreenRoute.Dashboard -> HOME
            is ScreenRoute.MembersRoster -> MEMBERS
            is ScreenRoute.TransactionsLedger -> TRANSACTIONS
            is ScreenRoute.Reports -> REPORTS
            is ScreenRoute.Settings -> SETTINGS
            else -> null
        }
    }
}
