package com.chand.app.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chand.app.ui.theme.IosGreen
import com.chand.app.ui.theme.IosRed

@Composable
fun SparklineChart(
    points: List<Double>,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
    width: Dp = 68.dp,
    height: Dp = 32.dp,
    showGradient: Boolean = true
) {
    if (points.size < 2) return

    val lineColor = if (isPositive) IosGreen else IosRed
    val gradientColor = if (isPositive) IosGreen.copy(alpha = 0.25f) else IosRed.copy(alpha = 0.25f)

    Spacer(
        modifier = modifier
            .width(width)
            .height(height)
            .drawWithCache {
                val minVal = points.minOrNull() ?: 0.0
                val maxVal = points.maxOrNull() ?: 1.0
                val range = if (maxVal - minVal == 0.0) 1.0 else maxVal - minVal

                val w = size.width
                val h = size.height

                val path = Path()
                val fillPath = Path()

                val stepX = w / (points.size - 1)

                points.forEachIndexed { index, value ->
                    val x = index * stepX
                    val normY = if (maxVal == minVal) 0.5 else (value - minVal) / range
                    val y = (h - (normY * (h - 8f)) - 4f).toFloat()

                    if (index == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, h)
                        fillPath.lineTo(x, y)
                    } else {
                        val prevX = (index - 1) * stepX
                        val prevNormY = if (maxVal == minVal) 0.5 else (points[index - 1] - minVal) / range
                        val prevY = (h - (prevNormY * (h - 8f)) - 4f).toFloat()

                        val cx = (prevX + x) / 2
                        path.cubicTo(cx, prevY, cx, y, x, y)
                        fillPath.cubicTo(cx, prevY, cx, y, x, y)
                    }
                }

                val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                val gradientBrush = if (showGradient) {
                    fillPath.lineTo(w, h)
                    fillPath.close()
                    Brush.verticalGradient(
                        colors = listOf(gradientColor, Color.Transparent),
                        startY = 0f,
                        endY = h
                    )
                } else null

                onDrawBehind {
                    if (gradientBrush != null) {
                        drawPath(
                            path = fillPath,
                            brush = gradientBrush
                        )
                    }
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = stroke
                    )
                }
            }
    )
}

