package com.chand.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chand.app.ui.theme.AppleCardBackground
import com.chand.app.ui.theme.AppleCardBorder
import com.kyant.backdrop.Backdrop

/**
 * Reusable modifier applying Apple-style clean card surface.
 * Replaces liquid glass shaders with crisp Apple HIG squircle card styling.
 */
@Composable
fun Modifier.liquidGlass(
    backdrop: Backdrop? = null,
    shape: Shape = RoundedCornerShape(20.dp),
    tint: Color = AppleCardBackground,
    refractionHeight: Dp = 0.dp,
    refractionAmount: Dp = 0.dp,
    blurRadius: Dp = 0.dp
): Modifier = this
    .shadow(
        elevation = 2.dp,
        shape = shape,
        ambientColor = Color(0x08000000),
        spotColor = Color(0x0C000000)
    )
    .clip(shape)
    .background(AppleCardBackground)
    .border(
        width = 0.8.dp,
        color = AppleCardBorder,
        shape = shape
    )
