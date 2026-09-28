package com.chand.app.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
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
import com.chand.app.data.model.PriceCatalog
import com.chand.app.ui.MainActivity

class ChandSmallWidget : GlanceAppWidget() {

    private val edgeInset = 8.dp

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val pref = PreferencesManager(context)
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val snapshot = pref.readWidgetSnapshot(appWidgetId)
        val selectedId = snapshot.smallItemId
        val allItems = PriceCatalog.withQuotes(snapshot.prices)
        val item = allItems.find { it.id.equals(selectedId, ignoreCase = true) } ?: allItems.firstOrNull()

        val opacity = snapshot.opacity
        val cornerRadius = snapshot.cornerRadius
        val widgetTheme = WidgetTheme.fromId(snapshot.themeId)

        provideContent {
            GlanceTheme {
                if (item == null) EmptySmallWidgetContent(widgetTheme, opacity, cornerRadius)
                else SmallWidgetContent(item, widgetTheme, opacity, cornerRadius)
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
        val availableSide = if (availableWidth > 30.dp && availableHeight > 30.dp) {
            minOf(availableWidth, availableHeight)
        } else if (availableWidth > 30.dp) {
            availableWidth
        } else if (availableHeight > 30.dp) {
            availableHeight
        } else {
            150.dp
        }
        // Leave room for launcher-specific clipping without changing the card's 1:1 aspect ratio.
        val squareSide = (availableSide - edgeInset * 2).coerceAtLeast(1.dp)

        val isCompact = squareSide < 145.dp
        val cardPadding = if (isCompact) 12.dp else 16.dp
        val iconSize = if (isCompact) 32.dp else 36.dp
        val iconRadius = if (isCompact) 16.dp else 18.dp
        val nameFontSize = if (isCompact) 10.5.sp else 11.5.sp
        val symbolFontSize = if (isCompact) 12.5.sp else 13.5.sp
        val changeFontSize = if (isCompact) 13.5.sp else 15.sp
        val priceFontSize = if (isCompact) 23.sp else 27.sp
        val usdFontSize = if (isCompact) 13.sp else 15.sp

        val effectiveCorner = cornerRadius.coerceIn(12, 28).dp
        val cardBackground = ImageProvider(roundedCardBitmap(bgColor, effectiveCorner.value / squareSide.value))

        // Outer transparent container filling the launcher cell and centering the square card
        Box(
            modifier = GlanceModifier.fillMaxSize().padding(edgeInset),
            contentAlignment = Alignment.Center
        ) {
            // Draw the card shape into a bitmap. Some launchers re-clip a Glance
            // rounded background on subsequent RemoteViews updates.
            Box(
                modifier = GlanceModifier
                    .width(squareSide)
                    .height(squareSide)
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                Image(
                    provider = cardBackground,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = GlanceModifier.fillMaxSize()
                )
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(cardPadding)
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
                        if (item.isUsd && item.priceTomans > 0) {
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

    @androidx.compose.runtime.Composable
    private fun EmptySmallWidgetContent(widgetTheme: WidgetTheme, opacity: Int, cornerRadius: Int) {
        val size = LocalSize.current
        val availableSide = minOf(size.width, size.height).takeIf { it > 30.dp } ?: 150.dp
        val squareSide = (availableSide - edgeInset * 2).coerceAtLeast(1.dp)
        val effectiveCorner = cornerRadius.coerceIn(12, 28).dp
        val cardBackground = ImageProvider(
            roundedCardBitmap(widgetTheme.resolveBackgroundColor(opacity), effectiveCorner.value / squareSide.value)
        )
        Box(
            modifier = GlanceModifier.fillMaxSize().padding(edgeInset),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = GlanceModifier.width(squareSide).height(squareSide)
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = cardBackground,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = GlanceModifier.fillMaxSize()
                )
                Text(
                    text = "برای دریافت قیمت\nبرنامه را باز کنید",
                    modifier = GlanceModifier.padding(16.dp),
                    style = TextStyle(color = ColorProvider(widgetTheme.textColor), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                )
            }
        }
    }

    private fun roundedCardBitmap(color: Color, cornerFraction: Float): Bitmap {
        // A modest fixed size avoids large RemoteViews bitmaps on every price refresh.
        val side = 256
        val bitmap = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color.toArgb() }
        val radius = (side * cornerFraction).coerceIn(0f, side / 2f)
        Canvas(bitmap).drawRoundRect(0f, 0f, side.toFloat(), side.toFloat(), radius, radius, paint)
        return bitmap
    }
}
