package com.chand.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chand.app.ui.theme.AppleBlue
import com.chand.app.ui.theme.AppleCardBackground
import com.chand.app.ui.theme.AppleCardBorder
import com.chand.app.ui.theme.AppleGreen
import com.chand.app.ui.theme.AppleSegmentBg
import com.chand.app.ui.theme.AppleTextPrimary
import com.chand.app.ui.theme.LocalAppleColors

enum class LiquidGlassButtonStyle {
    PRIMARY,   // Apple dark pill in light mode / Apple blue pill in dark mode
    SECONDARY, // Apple soft gray pill
    SUCCESS,   // Apple green pill for saved status
    ICON       // Apple circular icon button
}

/**
 * Ultra-sleek, clean Apple iOS button with spring press dynamics,
 * haptic click feedback, and dark-mode aware contrast colors.
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: LiquidGlassButtonStyle = LiquidGlassButtonStyle.PRIMARY,
    shape: Shape = RoundedCornerShape(50),
    height: Dp = 48.dp,
    content: @Composable RowScope.() -> Unit
) {
    val view = LocalView.current
    val isDark = LocalAppleColors.current.isDark

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "btn_scale"
    )

    val bgColor = when (style) {
        LiquidGlassButtonStyle.PRIMARY -> if (isDark) AppleBlue else Color(0xFF1C1C1E)
        LiquidGlassButtonStyle.SECONDARY -> AppleSegmentBg
        LiquidGlassButtonStyle.SUCCESS -> AppleGreen
        LiquidGlassButtonStyle.ICON -> AppleCardBackground
    }

    val contentColor = when (style) {
        LiquidGlassButtonStyle.PRIMARY -> Color.White
        LiquidGlassButtonStyle.SECONDARY -> AppleTextPrimary
        LiquidGlassButtonStyle.SUCCESS -> Color.White
        LiquidGlassButtonStyle.ICON -> AppleTextPrimary
    }

    val borderColor = when (style) {
        LiquidGlassButtonStyle.PRIMARY -> Color.Transparent
        LiquidGlassButtonStyle.SECONDARY -> AppleCardBorder
        LiquidGlassButtonStyle.SUCCESS -> Color.Transparent
        LiquidGlassButtonStyle.ICON -> AppleCardBorder
    }

    Box(
        modifier = modifier
            .scale(scale)
            .height(height)
            .clip(shape)
            .background(
                if (enabled) {
                    if (isPressed) bgColor.copy(alpha = 0.88f) else bgColor
                } else {
                    bgColor.copy(alpha = 0.4f)
                }
            )
            .border(width = 0.8.dp, color = borderColor, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    try {
                        view.performHapticFeedback(
                            HapticFeedbackConstants.VIRTUAL_KEY,
                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                        )
                    } catch (_: Throwable) {
                        try {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        } catch (_: Throwable) {}
                    }
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = content
            )
        }
    }
}
