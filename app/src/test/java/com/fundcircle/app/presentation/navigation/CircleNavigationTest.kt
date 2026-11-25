package com.fundcircle.app.presentation.navigation

import com.fundcircle.app.R
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CircleNavigationTest {

    private val testFundId = "fund-test-123"

    @Test
    fun circleNavTab_definesExactFivePrimaryTabsInOrder() {
        val tabs = CircleNavTab.entries
        assertThat(tabs).hasSize(5)
        assertThat(tabs[0]).isEqualTo(CircleNavTab.HOME)
        assertThat(tabs[1]).isEqualTo(CircleNavTab.MEMBERS)
        assertThat(tabs[2]).isEqualTo(CircleNavTab.TRANSACTIONS)
        assertThat(tabs[3]).isEqualTo(CircleNavTab.REPORTS)
        assertThat(tabs[4]).isEqualTo(CircleNavTab.SETTINGS)
    }

    @Test
    fun circleNavTab_hasValidStringResourcesAndIcons() {
        CircleNavTab.entries.forEach { tab ->
            assertThat(tab.labelRes).isGreaterThan(0)
            assertThat(tab.contentDescriptionRes).isGreaterThan(0)
            assertThat(tab.selectedIcon).isNotNull()
            assertThat(tab.unselectedIcon).isNotNull()
        }
    }

    @Test
    fun circleNavTab_mapsToCorrectScreenRoutes() {
        assertThat(CircleNavTab.HOME.toRoute(testFundId))
            .isEqualTo(ScreenRoute.Dashboard(testFundId))

        assertThat(CircleNavTab.MEMBERS.toRoute(testFundId))
            .isEqualTo(ScreenRoute.MembersRoster(testFundId))

        assertThat(CircleNavTab.TRANSACTIONS.toRoute(testFundId))
            .isEqualTo(ScreenRoute.TransactionsLedger(testFundId))

        assertThat(CircleNavTab.REPORTS.toRoute(testFundId))
            .isEqualTo(ScreenRoute.Reports(testFundId))

        assertThat(CircleNavTab.SETTINGS.toRoute(testFundId))
            .isEqualTo(ScreenRoute.Settings(testFundId))
    }

    @Test
    fun circleNavTab_fromRoute_identifiesPrimaryTabs() {
        assertThat(CircleNavTab.fromRoute(ScreenRoute.Dashboard(testFundId)))
            .isEqualTo(CircleNavTab.HOME)

        assertThat(CircleNavTab.fromRoute(ScreenRoute.MembersRoster(testFundId)))
            .isEqualTo(CircleNavTab.MEMBERS)

        assertThat(CircleNavTab.fromRoute(ScreenRoute.TransactionsLedger(testFundId)))
            .isEqualTo(CircleNavTab.TRANSACTIONS)

        assertThat(CircleNavTab.fromRoute(ScreenRoute.Reports(testFundId)))
            .isEqualTo(CircleNavTab.REPORTS)

        assertThat(CircleNavTab.fromRoute(ScreenRoute.Settings(testFundId)))
            .isEqualTo(CircleNavTab.SETTINGS)
    }

    @Test
    fun circleNavTab_fromRoute_returnsNullForSubScreensAndOnboarding() {
        assertThat(CircleNavTab.fromRoute(ScreenRoute.Welcome)).isNull()
        assertThat(CircleNavTab.fromRoute(ScreenRoute.CircleHub)).isNull()
        assertThat(CircleNavTab.fromRoute(ScreenRoute.CreateCircle)).isNull()
        assertThat(CircleNavTab.fromRoute(ScreenRoute.TransactionDetail(testFundId, "tx-1", false))).isNull()
        assertThat(CircleNavTab.fromRoute(ScreenRoute.MemberDetail(testFundId, "user-1"))).isNull()
        assertThat(CircleNavTab.fromRoute(ScreenRoute.ProfileEdit(testFundId))).isNull()
    }
}
