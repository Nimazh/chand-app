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
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
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
import com.chand.app.ui.MainActivity
import kotlinx.coroutines.flow.first

class ChandLargeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val pref = PreferencesManager(context)
        val allItems = pref.cachedPricesFlow.first()
        val favs = pref.favoritesFlow.first()

        val items = allItems.filter { it.id in favs }.ifEmpty { allItems }.take(6)

        val themeId = pref.widgetThemeFlow.first()
        val opacity = pref.widgetOpacityFlow.first()
        val cornerRadius = pref.widgetCornerRadiusFlow.first()
        val widgetTheme = WidgetTheme.fromId(themeId)

        provideContent {
            GlanceTheme {
                LargeWidgetContent(
                    items = items,
                    widgetTheme = widgetTheme,
                    opacity = opacity,
                    cornerRadius = cornerRadius
                )
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun LargeWidgetContent(
        items: List<PriceItem>,
        widgetTheme: WidgetTheme,
        opacity: Int,
        cornerRadius: Int
    ) {
        val bgColor = widgetTheme.resolveBackgroundColor(opacity)
        val textColor = widgetTheme.textColor
        val subTextColor = widgetTheme.subTextColor
        val dividerColor = if (widgetTheme == WidgetTheme.APPLE_WHITE) Color(0xFFE5E5EA) else Color(0x33FFFFFF)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(bgColor)
                .cornerRadius(cornerRadius.coerceIn(12, 28).dp)
                .clickable(actionStartActivity<MainActivity>())
                .padding(14.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                // Header: Chand?! & Refresh
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = "چند؟",
                        style = TextStyle(
                            color = ColorProvider(textColor),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = "نرخ بازار",
                        style = TextStyle(
                            color = ColorProvider(subTextColor),
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = "↻",
                        modifier = GlanceModifier
                            .clickable(actionRunCallback<WidgetRefreshAction>())
                            .padding(4.dp),
                        style = TextStyle(
                            color = ColorProvider(subTextColor),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(10.dp))

                if (items.isEmpty()) {
                    Text(
                        text = "برای دریافت قیمت، برنامه را باز کنید",
                        style = TextStyle(color = ColorProvider(subTextColor), fontSize = 13.sp)
                    )
                }

                items.forEachIndexed { index, item ->
                    val changeColor = if (item.isFavorable) Color(0xFF34C759) else Color(0xFFFF3B30)

                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Image(
                            provider = ImageProvider(item.iconResId),
                            contentDescription = item.effectiveNameEn,
                            modifier = GlanceModifier
                                .width(22.dp)
                                .height(22.dp)
                                .cornerRadius(11.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Text(
                            text = item.symbol,
                            style = TextStyle(
                                color = ColorProvider(textColor),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = GlanceModifier.defaultWeight()
                        )

                        Text(
                            text = item.formattedPrice,
                            style = TextStyle(
                                color = ColorProvider(textColor),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (item.isUsd) {
                            Spacer(modifier = GlanceModifier.width(2.dp))
                            Text(
                                text = "$",
                                style = TextStyle(
                                    color = ColorProvider(subTextColor),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(8.dp))

                        Text(
                            text = item.arrowChangeFormatted,
                            style = TextStyle(
                                color = ColorProvider(changeColor),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    if (index < items.size - 1) {
                        Spacer(modifier = GlanceModifier.height(5.dp))
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(0.8.dp)
                                .background(dividerColor)
                        ) {}
                        Spacer(modifier = GlanceModifier.height(5.dp))
                    }
                }
            }
        }
    }
}
