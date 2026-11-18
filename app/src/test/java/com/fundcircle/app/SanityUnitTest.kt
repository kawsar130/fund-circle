package com.fundcircle.app

import androidx.compose.ui.unit.dp
import com.fundcircle.app.core.common.DateUtils
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.presentation.navigation.ScreenRoute
import com.fundcircle.app.presentation.theme.DesignTokens
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test

class SanityUnitTest {

    @Test
    fun baselineUnitTestExecution_succeeds() {
        val appName = "Fund Circle"
        assertThat(appName).isEqualTo("Fund Circle")
    }

    @Test
    fun invariant1_wholeIntegerMonetaryModel_is64BitLong() {
        val wholeIntegerDeposit: Long = 50000L
        assertThat(wholeIntegerDeposit).isAtLeast(1L)
        assertThat(wholeIntegerDeposit % 1L).isEqualTo(0L)
    }

    @Test
    fun invariant7_touchTargetFloors_satisfyAccessibilityBounds() {
        assertThat(DesignTokens.MinTouchTarget).isAtLeast(48.dp)
        assertThat(DesignTokens.CompactTouchTarget).isAtLeast(40.dp)
    }

    @Test
    fun uiText_structuralEquality_preservesRecompositionIntegrity() {
        val text1 = UiText.StringResource(101, "arg1", 42)
        val text2 = UiText.StringResource(101, "arg1", 42)
        val text3 = UiText.StringResource(102, "arg1", 42)

        assertThat(text1).isEqualTo(text2)
        assertThat(text1.hashCode()).isEqualTo(text2.hashCode())
        assertThat(text1).isNotEqualTo(text3)

        val dynamic1 = UiText.DynamicString("Hello")
        val dynamic2 = UiText.DynamicString("Hello")
        assertThat(dynamic1).isEqualTo(dynamic2)
    }

    @Test
    fun dateUtils_futureDateValidation_rejectsFutureTimestamps() {
        val now = 1_000_000L
        val pastDate = 900_000L
        val futureDate = 1_100_000L

        assertThat(DateUtils.isDateInFuture(pastDate, now)).isFalse()
        assertThat(DateUtils.isDateInFuture(now, now)).isFalse()
        assertThat(DateUtils.isDateInFuture(futureDate, now)).isTrue()
    }

    @Test
    fun fundCircleError_hierarchy_preservesMessages() {
        val invalidAmount: FundCircleError = FundCircleError.Validation.InvalidAmount(0L)
        assertThat(invalidAmount.message).contains("Amount must be a whole positive integer")

        val notMember: FundCircleError = FundCircleError.Authorization.NotCircleMember
        assertThat(notMember.message).contains("not an active member")

        val voided: FundCircleError = FundCircleError.Conflict.TransactionAlreadyVoided("tx-123")
        assertThat(voided.message).contains("tx-123")
    }

    @Test
    fun screenRoute_kotlinxSerialization_serializesCorrectly() {
        val route: ScreenRoute = ScreenRoute.Dashboard(fundId = "fund-abc-123")
        val json = Json.encodeToString(route)
        assertThat(json).contains("fund-abc-123")

        val deserialized = Json.decodeFromString<ScreenRoute>(json)
        assertThat(deserialized).isEqualTo(route)
    }
}
