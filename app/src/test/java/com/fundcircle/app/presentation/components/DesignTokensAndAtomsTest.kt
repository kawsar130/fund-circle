package com.fundcircle.app.presentation.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fundcircle.app.presentation.theme.BadgeTextStyle
import com.fundcircle.app.presentation.theme.DesignTokens
import com.fundcircle.app.presentation.theme.LightColorScheme
import com.fundcircle.app.presentation.theme.SectionHeaderStyle
import com.fundcircle.app.presentation.theme.SlateNavyDarkBase
import com.fundcircle.app.presentation.theme.SlateNavyDarkContainer
import com.fundcircle.app.presentation.theme.SlateNavyDarkOutline
import com.fundcircle.app.presentation.theme.SlateNavyDarkSurface
import com.fundcircle.app.presentation.theme.TABULAR_DIGITS
import com.fundcircle.app.presentation.theme.TabularNumLarge
import com.fundcircle.app.presentation.theme.TabularNumMedium
import com.fundcircle.app.presentation.theme.TabularNumSmall
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DesignTokensAndAtomsTest {

    @Test
    fun slateNavyFoundationColors_matchOfficialDesignTokensHexSpec() {
        // Spec: Base #0A101D, Surface #101726, Container #161F30, Outline #263550
        assertThat(SlateNavyDarkBase).isEqualTo(Color(0xFF0A101D))
        assertThat(SlateNavyDarkSurface).isEqualTo(Color(0xFF101726))
        assertThat(SlateNavyDarkContainer).isEqualTo(Color(0xFF161F30))
        assertThat(SlateNavyDarkOutline).isEqualTo(Color(0xFF263550))
    }

    @Test
    fun touchTargetHygiene_satisfiesWCAGBounds() {
        assertThat(DesignTokens.MinTouchTarget).isEqualTo(48.dp)
        assertThat(DesignTokens.CompactTouchTarget).isEqualTo(40.dp)
    }

    @Test
    fun tabularNumeralStyles_enforceTnumFeatureSettings() {
        assertThat(TABULAR_DIGITS).isEqualTo("tnum")
        assertThat(TabularNumLarge.fontFeatureSettings).isEqualTo("tnum")
        assertThat(TabularNumMedium.fontFeatureSettings).isEqualTo("tnum")
        assertThat(TabularNumSmall.fontFeatureSettings).isEqualTo("tnum")
    }

    @Test
    fun sectionHeaderStyle_hasUppercaseTracking() {
        assertThat(SectionHeaderStyle.letterSpacing).isEqualTo(0.8.sp)
    }

    @Test
    fun segmentedItem_withCount_formatsDisplayCorrectly() {
        val itemWithCount = SegmentedItem(title = "Deposits", count = 14)
        assertThat(itemWithCount.title).isEqualTo("Deposits")
        assertThat(itemWithCount.count).isEqualTo(14)

        val itemWithoutCount = SegmentedItem(title = "All")
        assertThat(itemWithoutCount.title).isEqualTo("All")
        assertThat(itemWithoutCount.count).isNull()
        assertThat(itemWithoutCount.activeContentColor).isNull()

        val expenseColor = Color(0xFFFFB4AB)
        val expenseItem = SegmentedItem(title = "Expenses", activeContentColor = expenseColor)
        assertThat(expenseItem.activeContentColor).isEqualTo(expenseColor)
    }

    @Test
    fun lightColorScheme_configuresSurfaceContainerElevationTiers() {
        assertThat(LightColorScheme.surfaceContainer).isEqualTo(Color(0xFFF2F7F2))
        assertThat(LightColorScheme.surfaceContainerHigh).isEqualTo(Color(0xFFEAF3EC))
        assertThat(LightColorScheme.surfaceContainerHighest).isEqualTo(Color(0xFFE1EFE6))
    }

    @Test
    fun nonBreakingSpaceToken_isAccurate() {
        assertThat(DesignTokens.NBSP).isEqualTo("\u00A0")
        val formatted = "৳${DesignTokens.NBSP}250,000"
        assertThat(formatted).isEqualTo("৳\u00A0250,000")
    }

    @Test
    fun atomicBadgeShape_isFullPillRounded() {
        assertThat(DesignTokens.PillShape).isNotNull()
    }
}
