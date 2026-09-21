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

class ChandMediumWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val pref = PreferencesManager(context)
        val selectedIds = pref.mediumWidgetItemsFlow.first()
        val allItems = pref.cachedPricesFlow.first()

        val displayItems = selectedIds.mapNotNull { selId ->
            allItems.find { it.id.equals(selId, ignoreCase = true) }
        }.let { list ->
            if (list.isEmpty()) allItems.take(3) else list.take(3)
        }

        val themeId = pref.widgetThemeFlow.first()
        val opacity = pref.widgetOpacityFlow.first()
        val cornerRadius = pref.widgetCornerRadiusFlow.first()
        val widgetTheme = WidgetTheme.fromId(themeId)

        provideContent {
            GlanceTheme {
                MediumWidgetContent(
                    items = displayItems,
                    widgetTheme = widgetTheme,
                    opacity = opacity,
                    cornerRadius = cornerRadius
                )
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun MediumWidgetContent(
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
                .cornerRadius(if (cornerRadius > 20) cornerRadius.dp else 22.dp)
                .clickable(actionStartActivity<MainActivity>())
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                // 3 Rows matching Photo 2 left screen (USD, EMAMI, GRAM)
                items.forEachIndexed { index, item ->
                    WidgetPriceRow(
                        item = item,
                        textColor = textColor,
                        subTextColor = subTextColor
                    )
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

    @androidx.compose.runtime.Composable
    private fun WidgetPriceRow(
        item: PriceItem,
        textColor: Color,
        subTextColor: Color
    ) {
        val changeColor = if (item.isFavorable) Color(0xFF34C759) else Color(0xFFFF3B30)

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // Circular Flag / Coin SVG Logo
            Image(
                provider = ImageProvider(item.iconResId),
                contentDescription = item.effectiveNameEn,
                modifier = GlanceModifier
                    .width(22.dp)
                    .height(22.dp)
                    .cornerRadius(11.dp)
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Symbol
            Text(
                text = item.symbol,
                style = TextStyle(
                    color = ColorProvider(textColor),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.defaultWeight()
            )

            // Price
            Text(
                text = item.formattedPrice,
                style = TextStyle(
                    color = ColorProvider(textColor),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            if (item.isUsd) {
                Spacer(modifier = GlanceModifier.width(2.dp))
                Text(
                    text = "$",
                    style = TextStyle(
                        color = ColorProvider(subTextColor),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Arrow Change
            Text(
                text = item.arrowChangeFormatted,
                style = TextStyle(
                    color = ColorProvider(changeColor),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
