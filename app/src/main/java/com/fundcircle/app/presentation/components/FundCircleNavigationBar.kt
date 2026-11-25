package com.fundcircle.app.presentation.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fundcircle.app.presentation.navigation.CircleNavTab
import com.fundcircle.app.presentation.theme.FundCircleTheme

/**
 * Persistent 5-tab Material 3 NavigationBar for Fund Circle workspaces.
 * Complies with Slate Navy surface tokens, touch target hygiene, and semantic colors.
 */
@Composable
fun FundCircleNavigationBar(
    selectedTab: CircleNavTab,
    onTabSelected: (CircleNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 2.dp
    ) {
        CircleNavTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = stringResource(tab.contentDescriptionRes)
                    )
                },
                label = {
                    Text(
                        text = stringResource(tab.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Preview(name = "FundCircleNavigationBar Light", showBackground = true)
@Composable
private fun FundCircleNavigationBarLightPreview() {
    var selectedTab by remember { mutableStateOf(CircleNavTab.HOME) }
    FundCircleTheme(darkTheme = false) {
        FundCircleNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }
}

@Preview(name = "FundCircleNavigationBar Dark", showBackground = true)
@Composable
private fun FundCircleNavigationBarDarkPreview() {
    var selectedTab by remember { mutableStateOf(CircleNavTab.HOME) }
    FundCircleTheme(darkTheme = true) {
        FundCircleNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }
}
