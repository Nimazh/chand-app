package com.chand.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chand.app.data.model.PriceCategory
import com.chand.app.ui.theme.AppleCardBackground
import com.chand.app.ui.theme.AppleCardBorder
import com.chand.app.ui.theme.AppleSegmentBg
import com.chand.app.ui.theme.AppleTextPrimary
import com.chand.app.ui.theme.AppleTextSecondary
import com.kyant.backdrop.Backdrop

/**
 * Apple iOS Segmented Control Tab Bar:
 * - Smooth gray background pill
 * - Pure white floating active pill with subtle elevation
 * - Apple HIG typography
 */
@Composable
fun CategoryTabBar(
    selectedCategory: PriceCategory,
    onCategorySelected: (PriceCategory) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null
) {
    val view = LocalView.current
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PriceCategory.values().forEach { category ->
            val isSelected = category == selectedCategory
            val pillShape = RoundedCornerShape(50)

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.96f else 1f,
                animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
                label = "tab_scale"
            )

            val pillTextColor by animateColorAsState(
                targetValue = if (isSelected) AppleTextPrimary else AppleTextSecondary,
                label = "pill_text"
            )

            Box(
                modifier = Modifier
                    .scale(scale)
                    .then(
                        if (isSelected) {
                            Modifier.shadow(
                                elevation = 2.dp,
                                shape = pillShape,
                                ambientColor = Color(0x0A000000),
                                spotColor = Color(0x10000000)
                            )
                        } else Modifier
                    )
                    .clip(pillShape)
                    .background(if (isSelected) AppleCardBackground else AppleSegmentBg)
                    .then(
                        if (isSelected) {
                            Modifier.border(width = 0.8.dp, color = AppleCardBorder, shape = pillShape)
                        } else Modifier
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            try {
                                view.performHapticFeedback(
                                    HapticFeedbackConstants.VIRTUAL_KEY,
                                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                                )
                            } catch (_: Throwable) {
                                try { view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) } catch (_: Throwable) {}
                            }
                            onCategorySelected(category)
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.titleFa,
                    color = pillTextColor,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
