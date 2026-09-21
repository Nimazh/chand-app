package com.kyant.shapes

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

data class Corners(
    val topLeft: Float,
    val topRight: Float,
    val bottomRight: Float,
    val bottomLeft: Float
)

interface RoundedRectangularShape : Shape {
    fun corners(size: Size, layoutDirection: LayoutDirection, density: Density): Corners
}

fun Capsule(): CornerBasedShape = RoundedCornerShape(percent = 50)
