package com.fundcircle.app.presentation.theme

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ColorSchemeTest {

    @Test
    fun darkScheme_lowContainers_useSlateNavyTokens_notStockMaterialGreys() {
        assertThat(DarkColorScheme.surfaceContainerLowest).isEqualTo(BackgroundDark)
        assertThat(DarkColorScheme.surfaceContainerLow).isEqualTo(SlateNavyDarkContainerLow)
    }

    @Test
    fun darkScheme_containerElevations_increaseInLightness() {
        val ramp = listOf(
            DarkColorScheme.surfaceContainerLowest,
            DarkColorScheme.surfaceContainerLow,
            DarkColorScheme.surfaceContainer,
            DarkColorScheme.surfaceContainerHigh,
            DarkColorScheme.surfaceContainerHighest
        ).map { it.red + it.green + it.blue }

        assertThat(ramp).isInStrictOrder()
    }
}
