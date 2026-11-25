package com.fundcircle.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fundcircle.app.core.theme.ThemePreferenceManager
import com.fundcircle.app.presentation.navigation.FundCircleNavHost
import com.fundcircle.app.presentation.theme.FundCircleTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferenceManager: ThemePreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val darkThemeOverride by themePreferenceManager.darkThemeOverride.collectAsStateWithLifecycle()
            FundCircleTheme(
                darkTheme = darkThemeOverride ?: isSystemInDarkTheme()
            ) {
                FundCircleNavHost()
            }
        }
    }
}
