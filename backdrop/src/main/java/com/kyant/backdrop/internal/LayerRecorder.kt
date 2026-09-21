package com.kyant.backdrop.internal

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntSize

internal fun DrawScope.recordLayer(
    layer: GraphicsLayer,
    node: DelegatableNode? = null,
    size: IntSize = this.size.toIntSize(),
    block: DrawScope.() -> Unit
) {
    val density = node?.requireDensity()
    layer.record(size) {
        val prevDensity = drawContext.density
        if (density != null) {
            drawContext.density = density
        }
        try {
            this.block()
        } finally {
            drawContext.density = prevDensity
        }
    }
}
