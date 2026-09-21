package com.chand.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chand.app.data.model.PriceCategory
import com.chand.app.data.model.PriceItem
import com.chand.app.ui.theme.AppleBackground
import com.chand.app.ui.theme.AppleBlue
import com.chand.app.ui.theme.AppleCardBackground
import com.chand.app.ui.theme.AppleCardBorder
import com.chand.app.ui.theme.AppleGreen
import com.chand.app.ui.theme.AppleRed
import com.chand.app.ui.theme.AppleSegmentBg
import com.chand.app.ui.theme.AppleTextPrimary
import com.chand.app.ui.theme.AppleTextSecondary
import com.chand.app.ui.theme.AppleTextTertiary
import com.chand.app.ui.theme.AppleYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceDetailModal(
    item: PriceItem?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onToggleFavorite: (PriceItem) -> Unit,
    onSetAsSmallWidget: (PriceItem) -> Unit,
    backdrop: com.kyant.backdrop.Backdrop? = null
) {
    if (item == null) return

    val view = LocalView.current
    val changeColor = if (item.isFavorable) AppleGreen else AppleRed
    var selectedTimeframe by remember { mutableStateOf("1D") }
    val timeframes = listOf("1D", "1W", "1M", "1Y", "5Y", "All")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppleCardBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(38.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(AppleCardBorder)
            )
        }
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                // 1. Top Header Row (Matches Photo 2 right screen): Flag, Name/Symbol (Left) & Close (X) (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = item.iconResId),
                            contentDescription = item.effectiveNameEn,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = item.effectiveNameEn,
                                color = AppleTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.symbol,
                                color = AppleTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                try {
                                    view.performHapticFeedback(
                                        HapticFeedbackConstants.VIRTUAL_KEY,
                                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                                    )
                                } catch (_: Throwable) {
                                    try { view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) } catch (_: Throwable) {}
                                }
                                onToggleFavorite(item)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AppleSegmentBg)
                        ) {
                            Icon(
                                imageVector = if (item.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (item.isFavorite) AppleYellow else AppleTextSecondary,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                try {
                                    view.performHapticFeedback(
                                        HapticFeedbackConstants.VIRTUAL_KEY,
                                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                                    )
                                } catch (_: Throwable) {
                                    try { view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) } catch (_: Throwable) {}
                                }
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AppleSegmentBg)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = AppleTextPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Metrics Block (Matches Photo 2 right screen: Arrow change, Giant price, H/L range)
                Text(
                    text = item.arrowChangeFormatted,
                    color = changeColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = item.formattedPrice,
                        color = AppleTextPrimary,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp
                    )
                    if (item.isUsd) {
                        Text(
                            text = " $",
                            color = AppleTextSecondary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp, start = 3.dp)
                        )
                    } else {
                        Text(
                            text = " تومان",
                            color = AppleTextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(bottom = 5.dp, start = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // High / Low Range (H: 105,890  L: 99,125)
                Text(
                    text = "H: ${item.formattedHigh}   L: ${item.formattedLow}",
                    color = AppleTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )

                if (item.isUsd && item.priceTomans > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "معادل تقریبی: ${String.format(java.util.Locale.US, "%,d", item.priceTomans)} تومان",
                        color = AppleTextTertiary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Apple Smooth Area Chart (Matches Photo 2 right screen)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppleBackground)
                        .border(0.8.dp, AppleCardBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.sparklinePoints.isNotEmpty()) {
                        SparklineChart(
                            points = item.sparklinePoints,
                            isPositive = item.isFavorable,
                            width = 320.dp,
                            height = 110.dp,
                            showGradient = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Timeframe Selector: 1D  1W  1M  1Y  5Y  All (Matches Photo 2 right screen)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppleSegmentBg)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    timeframes.forEach { tf ->
                        val isSelected = tf == selectedTimeframe
                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) AppleTextPrimary else AppleTextSecondary,
                            label = "tf_color"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AppleCardBackground else Color.Transparent)
                                .clickable {
                                    try {
                                        view.performHapticFeedback(
                                            HapticFeedbackConstants.VIRTUAL_KEY,
                                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                                        )
                                    } catch (_: Throwable) {
                                        try { view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) } catch (_: Throwable) {}
                                    }
                                    selectedTimeframe = tf
                                }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tf,
                                color = textColor,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 5. Interactive Currency Calculator Section
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "ماشین‌حساب تبدیل سریع",
                            color = AppleTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        var inputAmount by remember { mutableStateOf("1") }
                        val amount = inputAmount.toDoubleOrNull() ?: 0.0
                        val convertedTomans = (amount * item.priceTomans).toLong()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Amount Input Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AppleSegmentBg)
                                    .border(0.8.dp, AppleCardBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${item.symbol}: ",
                                        color = AppleTextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    BasicTextField(
                                        value = inputAmount,
                                        onValueChange = { inputAmount = it },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = TextStyle(
                                            color = AppleTextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        cursorBrush = SolidColor(AppleBlue),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // Converted Output Box
                            Box(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AppleSegmentBg)
                                    .border(0.8.dp, AppleCardBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column {
                                    if (item.isUsd) {
                                        val convertedUsd = amount * item.effectiveUsdPrice
                                        val formattedUsd = if (convertedUsd >= 1000) String.format(java.util.Locale.US, "%,d", convertedUsd.toLong()) else String.format(java.util.Locale.US, "%,.2f", convertedUsd)
                                        Text("معادل: $formattedUsd دلار", color = AppleBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "تقریبی: ${String.format(java.util.Locale.US, "%,d", convertedTomans)} تومان",
                                            color = AppleGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else {
                                        Text("معادل به تومان", color = AppleTextSecondary, fontSize = 11.sp)
                                        Text(
                                            text = "${String.format(java.util.Locale.US, "%,d", convertedTomans)} تومان",
                                            color = AppleGreen,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 6. Action Button: Set in Home Screen Widget (Apple Black Pill Button)
                LiquidGlassButton(
                    onClick = { onSetAsSmallWidget(item) },
                    style = LiquidGlassButtonStyle.PRIMARY,
                    shape = RoundedCornerShape(50),
                    height = 48.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تنظیم در ویجت صفحه اصلی",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
