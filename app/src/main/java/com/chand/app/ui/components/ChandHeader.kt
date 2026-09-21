package com.chand.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chand.app.ui.theme.AppleBlue
import com.chand.app.ui.theme.AppleCardBackground
import com.chand.app.ui.theme.AppleCardBorder
import com.chand.app.ui.theme.AppleGreen
import com.chand.app.ui.theme.AppleHeaderTitle
import com.chand.app.ui.theme.AppleOrange
import com.chand.app.ui.theme.AppleRed
import com.chand.app.ui.theme.AppleSearchBg
import com.chand.app.ui.theme.AppleTextPrimary
import com.chand.app.ui.theme.AppleTextSecondary
import com.chand.app.ui.theme.AppleTextTertiary
import com.kyant.backdrop.Backdrop
import com.chand.app.data.repository.PriceSyncStatus

/**
 * Apple Chand Header (Matches Photo 1 and Photo 2):
 * - Left: "Chand?!" large title in bold typography
 * - Right: Action icons (Search, Refresh, Settings gear) and subtle timestamp
 */
@Composable
fun ChandHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onToggleSearch: () -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
    lastUpdatedText: String = "",
    syncStatus: PriceSyncStatus = PriceSyncStatus.IDLE,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null
) {
    val rotation = if (isRefreshing) {
        val infiniteTransition = rememberInfiniteTransition(label = "spin")
        val rot by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "refresh_rotation"
        )
        rot
    } else {
        0f
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Top Bar Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: "Chand?!" App Title (Matches Photo 1 & 2)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Chand?!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppleHeaderTitle,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // The indicator reflects the actual cache/network state.
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (syncStatus) {
                                    PriceSyncStatus.SUCCESS -> AppleGreen
                                    PriceSyncStatus.REFRESHING -> AppleBlue
                                    PriceSyncStatus.STALE -> AppleOrange
                                    PriceSyncStatus.ERROR -> AppleRed
                                    PriceSyncStatus.IDLE -> AppleTextSecondary
                                }
                            )
                    )
                }

                // Right: Action Buttons (Search, Refresh, Settings)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppleCircularButton(
                        icon = if (isSearchActive) Icons.Filled.Close else Icons.Filled.Search,
                        contentDescription = "Search",
                        onClick = onToggleSearch
                    )

                    AppleCircularButton(
                        icon = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        onClick = onRefresh,
                        enabled = !isRefreshing,
                        iconModifier = if (isRefreshing) Modifier.rotate(rotation) else Modifier
                    )

                    AppleCircularButton(
                        icon = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        onClick = onOpenSettings
                    )
                }
            }

            val statusText = when (syncStatus) {
                PriceSyncStatus.REFRESHING -> "در حال دریافت قیمت‌ها"
                PriceSyncStatus.STALE -> "نمایش دادهٔ ذخیره‌شده؛ اتصال را بررسی کنید"
                PriceSyncStatus.ERROR -> "دریافت قیمت ناموفق بود"
                else -> lastUpdatedText.takeIf { it.isNotBlank() }?.let { "آخرین دریافت: $it" } ?: "هنوز داده‌ای دریافت نشده"
            }
            if (statusText.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    color = AppleTextSecondary,
                    fontWeight = FontWeight.Normal
                )
            }

            // Expandable Apple iOS Search Bar
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(120))
            ) {
                val searchShape = RoundedCornerShape(12.dp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(searchShape)
                        .background(AppleSearchBg)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = AppleTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search currency, gold, crypto...",
                                    color = AppleTextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = AppleTextPrimary,
                                    fontSize = 14.sp
                                ),
                                cursorBrush = SolidColor(AppleBlue),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppleCircularButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconModifier: Modifier = Modifier
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "apple_btn_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(36.dp)
            .clip(CircleShape)
            .background(AppleCardBackground)
            .border(0.8.dp, AppleCardBorder, CircleShape)
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
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) AppleTextPrimary else AppleTextTertiary,
            modifier = iconModifier.size(18.dp)
        )
    }
}
