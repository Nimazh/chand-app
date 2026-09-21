package com.chand.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chand.app.ui.theme.AppleBackground
import com.kyant.backdrop.Backdrop

/**
 * Apple iOS clean background canvas (#F2F2F7).
 * Liquid glass orbs and blurs have been removed in favor of Apple minimalism.
 */
@Composable
fun LiquidGlassAtmosphere(
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppleBackground)
    )
}
