package com.fundcircle.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fundcircle.app.R

/**
 * Branded 96x96dp circular logo with interlocking rings
 * representing pooled collective capital.
 */
@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val ringColor1 = MaterialTheme.colorScheme.onPrimary
    val ringColor2 = MaterialTheme.colorScheme.primaryContainer
    val logoDesc = stringResource(R.string.auth_logo_content_description)

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
