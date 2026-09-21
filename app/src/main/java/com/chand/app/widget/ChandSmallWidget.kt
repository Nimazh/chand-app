package com.chand.app.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.chand.app.data.local.PreferencesManager
import com.chand.app.data.model.PriceItem
import com.chand.app.data.remote.PriceApiService
import com.chand.app.ui.MainActivity
import kotlinx.coroutines.flow.first

class ChandSmallWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val pref = PreferencesManager(context)
        val selectedId = pref.smallWidgetItemFlow.first()
        val allItems = pref.cachedPricesFlow.first()
        val item = allItems.find { it.id.equals(selectedId, ignoreCase = true) } ?: allItems.first()

        val themeId = pref.widgetThemeFlow.first()
        val opacity = pref.widgetOpacityFlow.first()
        val cornerRadius = pref.widgetCornerRadiusFlow.first()
        val widgetTheme = WidgetTheme.fromId(themeId)

        provideContent {
            GlanceTheme {
                SmallWidgetContent(
                    item = item,
                    widgetTheme = widgetTheme,
                    opacity = opacity,
                    cornerRadius = cornerRadius
                )
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun SmallWidgetContent(
        item: PriceItem,
        widgetTheme: WidgetTheme,
        opacity: Int,
        cornerRadius: Int
    ) {
        val size = LocalSize.current
        // Apple Chand Iranian market color rule: drop is green (#34C759), rise is red (#FF3B30)
        val changeColor = if (item.isFavorable) Color(0xFF34C759) else Color(0xFFFF3B30)
        val bgColor = widgetTheme.resolveBackgroundColor(opacity)
        val textColor = widgetTheme.textColor
        val subTextColor = widgetTheme.subTextColor

        // Enforce strict 1:1 square aspect ratio regardless of launcher cell dimension
        val availableWidth = size.width
        val availableHeight = size.height
        val squareSide = if (availableWidth > 30.dp && availableHeight > 30.dp) {
            minOf(availableWidth, availableHeight)
        } else if (availableWidth > 30.dp) {
            availableWidth
        } else if (availableHeight > 30.dp) {
            availableHeight
        } else {
            150.dp
        }

        val isCompact = squareSide < 145.dp
        val cardPadding = if (isCompact) 12.dp else 16.dp
        val iconSize = if (isCompact) 32.dp else 36.dp
        val iconRadius = if (isCompact) 16.dp else 18.dp
        val nameFontSize = if (isCompact) 10.5.sp else 11.5.sp
        val symbolFontSize = if (isCompact) 12.5.sp else 13.5.sp
        val changeFontSize = if (isCompact) 13.5.sp else 15.sp
        val priceFontSize = if (isCompact) 23.sp else 27.sp
        val usdFontSize = if (isCompact) 13.sp else 15.sp

        val effectiveCorner = if (cornerRadius > 20) cornerRadius.dp else 24.dp

        // Outer transparent container filling the launcher cell and centering the square card
        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Apple Chand Squircle Small Widget (Strict 1:1 Square)
            Box(
                modifier = GlanceModifier
                    .width(squareSide)
                    .height(squareSide)
                    .background(bgColor)
                    .cornerRadius(effectiveCorner)
                    .clickable(actionStartActivity<MainActivity>())
                    .padding(cardPadding)
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    // 1. Top Row: Circular Flag Badge (Left) & English Name/Symbol (Right)
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Vertical.Top
                    ) {
                        // Circular flag / asset SVG logo (Matches iOS round flag icon)
                        Image(
                            provider = ImageProvider(item.iconResId),
                            contentDescription = item.effectiveNameEn,
                            modifier = GlanceModifier
                                .width(iconSize)
                                .height(iconSize)
                                .cornerRadius(iconRadius)
                        )

                        Spacer(modifier = GlanceModifier.defaultWeight())

                        // English Name & Symbol on the right
                        Column(
                            horizontalAlignment = Alignment.Horizontal.End
                        ) {
                            Text(
                                text = item.effectiveNameEn,
                                style = TextStyle(
                                    color = ColorProvider(subTextColor),
                                    fontSize = nameFontSize,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                            Spacer(modifier = GlanceModifier.height(1.dp))
                            Text(
                                text = item.symbol,
                                style = TextStyle(
                                    color = ColorProvider(textColor),
                                    fontSize = symbolFontSize,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // 2. Middle Row: Arrow Change Indicator (Matches Photo: ↓5,625)
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Text(
                            text = item.widgetArrowChangeFormatted,
                            style = TextStyle(
                                color = ColorProvider(changeColor),
                                fontSize = changeFontSize,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(3.dp))

                    // 3. Bottom Row: Large Bold Price (Matches Photo: 100,115)
                    Row(
                        verticalAlignment = Alignment.Vertical.Bottom
                    ) {
                        Text(
                            text = item.formattedPrice,
                            style = TextStyle(
                                color = ColorProvider(textColor),
                                fontSize = priceFontSize,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (item.isUsd) {
                            Spacer(modifier = GlanceModifier.width(3.dp))
                            Text(
                                text = "$",
                                style = TextStyle(
                                    color = ColorProvider(subTextColor),
                                    fontSize = usdFontSize,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
