package com.fundcircle.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fundcircle.app.R

private val GoogleBlue = Color(0xFF4285F4)
private val GoogleRed = Color(0xFFEA4335)
private val GoogleYellow = Color(0xFFFBBC05)
private val GoogleGreen = Color(0xFF34A853)

/**
 * Standard Google multi-color "G" icon for authentication buttons.
 */
@Composable
fun GoogleIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    val iconDesc = stringResource(R.string.auth_google_icon_content_description)

    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = iconDesc }
    ) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f

        // Scaled coordinates based on standard 48x48 Google G path
        val scaleX = w / 48f
        val scaleY = h / 48f

        // Blue segment
        val bluePath = Path().apply {
            moveTo(43.611f * scaleX, 20.083f * scaleY)
            lineTo(42f * scaleX, 20f * scaleY)
            lineTo(24f * scaleX, 20f * scaleY)
            lineTo(24f * scaleX, 28f * scaleY)
            lineTo(35.3f * scaleX, 28f * scaleY)
            cubicTo(
                33.6f * scaleX, 33f * scaleY,
                29.4f * scaleX, 36.5f * scaleY,
                24f * scaleX, 36.5f * scaleY
            )
            lineTo(24f * scaleX, 44.5f * scaleY)
            cubicTo(
                35.3f * scaleX, 44.5f * scaleY,
                44.5f * scaleX, 35.3f * scaleY,
                44.5f * scaleX, 24f * scaleY
            )
            cubicTo(
                44.5f * scaleX, 22.65f * scaleY,
                44.18f * scaleX, 21.34f * scaleY,
                43.611f * scaleX, 20.083f * scaleY
            )
            close()
        }
        drawPath(bluePath, GoogleBlue, style = Fill)

        // Green segment
        val greenPath = Path().apply {
            moveTo(24f * scaleX, 44.5f * scaleY)
            lineTo(24f * scaleX, 36.5f * scaleY)
            cubicTo(
                18.7f * scaleX, 36.5f * scaleY,
                14.2f * scaleX, 33.1f * scaleY,
                12.5f * scaleX, 28.4f * scaleY
            )
            lineTo(5.8f * scaleX, 33.6f * scaleY)
            cubicTo(
                9.3f * scaleX, 40.2f * scaleY,
                16.1f * scaleX, 44.5f * scaleY,
                24f * scaleX, 44.5f * scaleY
            )
            close()
        }
        drawPath(greenPath, GoogleGreen, style = Fill)

        // Yellow segment
        val yellowPath = Path().apply {
            moveTo(12.5f * scaleX, 28.4f * scaleY)
            cubicTo(
                11.9f * scaleX, 26.6f * scaleY,
                11.5f * scaleX, 24.6f * scaleY,
                11.5f * scaleX, 22.5f * scaleY
            )
            cubicTo(
                11.5f * scaleX, 20.4f * scaleY,
                11.9f * scaleX, 18.4f * scaleY,
                12.5f * scaleX, 16.6f * scaleY
            )
            lineTo(5.8f * scaleX, 11.4f * scaleY)
            cubicTo(
                4.2f * scaleX, 14.6f * scaleY,
                3.5f * scaleX, 18.4f * scaleY,
                3.5f * scaleX, 22.5f * scaleY
            )
            cubicTo(
                3.5f * scaleX, 26.6f * scaleY,
                4.2f * scaleX, 30.4f * scaleY,
                5.8f * scaleX, 33.6f * scaleY
            )
            lineTo(12.5f * scaleX, 28.4f * scaleY)
            close()
        }
        drawPath(yellowPath, GoogleYellow, style = Fill)

        // Red segment
        val redPath = Path().apply {
            moveTo(24f * scaleX, 8.5f * scaleY)
            cubicTo(
                28.6f * scaleX, 8.5f * scaleY,
                32.1f * scaleX, 10.3f * scaleY,
                34f * scaleX, 12f * scaleY
            )
            lineTo(39.9f * scaleX, 6.1f * scaleY)
            cubicTo(
                35.8f * scaleX, 2.3f * scaleY,
                30.4f * scaleX, 0.5f * scaleY,
                24f * scaleX, 0.5f * scaleY
            )
            cubicTo(
                16.1f * scaleX, 0.5f * scaleY,
                9.3f * scaleX, 4.8f * scaleY,
                5.8f * scaleX, 11.4f * scaleY
            )
            lineTo(12.5f * scaleX, 16.6f * scaleY)
            cubicTo(
                14.2f * scaleX, 11.9f * scaleY,
                18.7f * scaleX, 8.5f * scaleY,
                24f * scaleX, 8.5f * scaleY
            )
            close()
        }
        drawPath(redPath, GoogleRed, style = Fill)
    }
}
