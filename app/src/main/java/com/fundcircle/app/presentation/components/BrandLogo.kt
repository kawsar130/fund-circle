package com.fundcircle.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fundcircle.app.R

/**
 * Branded logo for Fund Circle.
 * Supports both the 84x84dp rounded square container with bold "FC" monogram (Screen 01)
 * and the circular interlocking rings canvas representation.
 */
@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 84.dp,
    useMonogram: Boolean = true
) {
    val logoDesc = stringResource(R.string.auth_logo_content_description)

    if (useMonogram) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 8.dp,
            modifier = modifier
                .size(size)
                .semantics { contentDescription = logoDesc }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "FC",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = (size.value * 0.44f).sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
        }
    } else {
        val primaryColor = MaterialTheme.colorScheme.primary
        val ringColor1 = MaterialTheme.colorScheme.onPrimary
        val ringColor2 = MaterialTheme.colorScheme.primaryContainer

        Canvas(
            modifier = modifier
                .size(size)
                .semantics { contentDescription = logoDesc }
        ) {
            val radius = this.size.minDimension / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // Background Emerald Circle
            drawCircle(
                color = primaryColor,
                radius = radius,
                center = center
            )

            val ringRadius = radius * 0.38f
            val strokeWidth = radius * 0.12f
            val offsetDistance = radius * 0.22f

            // First Interlocking Ring (Left)
            drawCircle(
                color = ringColor1,
                radius = ringRadius,
                center = Offset(center.x - offsetDistance, center.y),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Second Interlocking Ring (Right)
            drawCircle(
                color = ringColor2,
                radius = ringRadius,
                center = Offset(center.x + offsetDistance, center.y),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}
