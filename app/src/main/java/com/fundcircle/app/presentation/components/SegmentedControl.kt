package com.fundcircle.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fundcircle.app.presentation.theme.DesignTokens
import com.fundcircle.app.presentation.theme.FundCircleTheme
import com.fundcircle.app.presentation.theme.TABULAR_DIGITS

/**
 * Data item representing a selectable tab option in [SegmentedControl].
 */
data class SegmentedItem(
    val title: String,
    val count: Int? = null,
    val contentDescription: String? = null,
    val activeContentColor: Color? = null
)

/**
 * Modern containerized segmented pill control adhering to the Fund Circle design spec.
 * Provides touch target compliance (>= 40dp) and tabular numeric badges for item counts.
 */
@Composable
fun SegmentedControl(
    items: List<SegmentedItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    activeContainerColor: Color = MaterialTheme.colorScheme.surface,
    activeContentColor: Color = MaterialTheme.colorScheme.primary,
    inactiveContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing4),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing4),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) activeContainerColor else Color.Transparent,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "SegmentBackgroundColor"
                )
                val itemActiveContentColor = item.activeContentColor ?: activeContentColor
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) itemActiveContentColor else inactiveContentColor,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "SegmentTextColor"
                )

                val displayText = if (item.count != null) {
                    "${item.title} (${item.count})"
                } else {
                    item.title
                }

                val a11yLabel = item.contentDescription ?: displayText

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = DesignTokens.CompactTouchTarget)
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor)
                        .clickable(
                            role = Role.Tab,
                            onClick = { onItemSelected(index) }
                        )
                        .semantics {
                            role = Role.Tab
                            selected = isSelected
                            contentDescription = a11yLabel
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayText,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFeatureSettings = TABULAR_DIGITS
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Preview(name = "SegmentedControl Light", showBackground = true)
@Composable
private fun SegmentedControlLightPreview() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    FundCircleTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SegmentedControl(
                items = listOf(
                    SegmentedItem(title = "Deposits", count = 14),
                    SegmentedItem(title = "Expenses", count = 6)
                ),
                selectedIndex = selectedIndex,
                onItemSelected = { selectedIndex = it }
            )
            SegmentedControl(
                items = listOf(
                    SegmentedItem(title = "Deposit (+)"),
                    SegmentedItem(title = "Expense (-)")
                ),
                selectedIndex = selectedIndex,
                onItemSelected = { selectedIndex = it }
            )
        }
    }
}

@Preview(name = "SegmentedControl Dark", showBackground = true)
@Composable
private fun SegmentedControlDarkPreview() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    FundCircleTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SegmentedControl(
                items = listOf(
                    SegmentedItem(title = "Deposits", count = 14),
                    SegmentedItem(title = "Expenses", count = 6)
                ),
                selectedIndex = selectedIndex,
                onItemSelected = { selectedIndex = it }
            )
            SegmentedControl(
                items = listOf(
                    SegmentedItem(title = "Deposit (+)"),
                    SegmentedItem(title = "Expense (-)")
                ),
                selectedIndex = selectedIndex,
                onItemSelected = { selectedIndex = it }
            )
        }
    }
}
