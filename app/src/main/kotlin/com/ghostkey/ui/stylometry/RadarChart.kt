package com.ghostkey.ui.stylometry

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Full implementation in feature/stylometry
@Composable
fun RadarChart(
    userValues: List<Float>,
    aliasValues: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    userColor: Color = Color(0xFF58A6FF),
    aliasColor: Color = Color(0xFFF59E0B)
) {
    Canvas(modifier = modifier) {
        val axes = labels.size
        if (axes < 3) return@Canvas
        val radius = minOf(size.width, size.height) / 2f * 0.7f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Grid rings
        for (ring in 1..4) {
            val r = radius * ring / 4f
            drawPolygon(center, r, axes, Color(0xFF30363D), fill = false)
        }

        // Axis lines
        for (i in 0 until axes) {
            val angle = i * 2 * PI / axes - PI / 2
            val end = Offset(
                center.x + radius * cos(angle).toFloat(),
                center.y + radius * sin(angle).toFloat()
            )
            drawLine(Color(0xFF30363D), center, end, 1f)
        }

        // User values polygon
        drawRadarPolygon(center, radius, axes, userValues, userColor.copy(alpha = 0.3f), fill = true)
        drawRadarPolygon(center, radius, axes, userValues, userColor, fill = false)

        // Alias values polygon
        drawRadarPolygon(center, radius, axes, aliasValues, aliasColor.copy(alpha = 0.2f), fill = true)
        drawRadarPolygon(center, radius, axes, aliasValues, aliasColor, fill = false)
    }
}

private fun DrawScope.drawPolygon(center: Offset, radius: Float, sides: Int, color: Color, fill: Boolean) {
    val path = Path()
    for (i in 0 until sides) {
        val angle = i * 2 * PI / sides - PI / 2
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    if (fill) drawPath(path, color) else drawPath(path, color, style = Stroke(1f))
}

private fun DrawScope.drawRadarPolygon(
    center: Offset, maxRadius: Float, sides: Int,
    values: List<Float>, color: Color, fill: Boolean
) {
    if (values.size < sides) return
    val path = Path()
    for (i in 0 until sides) {
        val angle = i * 2 * PI / sides - PI / 2
        val r = maxRadius * values[i].coerceIn(0f, 1f)
        val x = center.x + r * cos(angle).toFloat()
        val y = center.y + r * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    if (fill) drawPath(path, color) else drawPath(path, color, style = Stroke(2f))
}
