package com.chand.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chand.app.data.model.PriceCategory
import com.chand.app.data.model.PriceItem
import com.chand.app.ui.theme.AppleCardBackground
import com.chand.app.ui.theme.AppleCardBorder
import com.chand.app.ui.theme.AppleGreen
import com.chand.app.ui.theme.AppleRed
import com.chand.app.ui.theme.AppleTextPrimary
import com.chand.app.ui.theme.AppleTextSecondary
import com.chand.app.ui.theme.AppleYellow
import com.kyant.backdrop.Backdrop

/**
 * Apple Chand Squircle Card (matches Photo 1 and Photo 2):
 * - Clean solid white squircle container
 * - Left: Circular flag or asset badge
 * - Right: English name & Symbol
 * - Middle: Apple arrow change indicator (↓5.66K)
 * - Bottom: Large bold price numbers
 */
@Composable
fun PriceCard(
    item: PriceItem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null
) {
    val view = LocalView.current
    val cardShape = RoundedCornerShape(20.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "card_scale"
    )

    val changeColor = if (item.isFavorable) AppleGreen else AppleRed

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .scale(scale)
                .shadow(
                    elevation = 3.dp,
                    shape = cardShape,
                    ambientColor = Color(0x0A000000),
                    spotColor = Color(0x10000000)
                )
                .clip(cardShape)
                .background(AppleCardBackground)
                .border(0.8.dp, AppleCardBorder, cardShape)
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
                        onClick()
                    }
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Top Row: Circular Flag / Icon (Left) & Name/Symbol (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Circular flag / coin SVG logo
                    Image(
                        painter = painterResource(id = item.iconResId),
                        contentDescription = item.effectiveNameEn,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                    )

                    // Name and Symbol aligned to the right
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    ) {
                        Text(
                            text = item.effectiveNameEn,
                            color = AppleTextSecondary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (item.isFavorite) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Favorite",
                                    tint = AppleYellow,
                                    modifier = Modifier.size(11.dp).padding(end = 2.dp)
                                )
                            }
                            Text(
                                text = item.symbol,
                                color = AppleTextPrimary,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Middle Row: Arrow Change Indicator (e.g. ↓5.66K or ↑2.4%)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.arrowChangeFormatted,
                        color = changeColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 3. Bottom Row: Large Bold Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = item.formattedPrice,
                        color = AppleTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isUsd) {
                        Text(
                            text = " $",
                            color = AppleTextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
