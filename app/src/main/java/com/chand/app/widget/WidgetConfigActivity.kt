package com.chand.app.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
import com.chand.app.data.local.PreferencesManager
import com.chand.app.data.remote.PriceApiService
import com.chand.app.ui.theme.AppleBackground
import com.chand.app.ui.theme.AppleBlue
import com.chand.app.ui.theme.AppleCardBackground
import com.chand.app.ui.theme.AppleCardBorder
import com.chand.app.ui.theme.AppleTextPrimary
import com.chand.app.ui.theme.AppleTextSecondary
import com.chand.app.ui.theme.ChandTheme
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)

        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val items = PriceApiService.getPreviewItems()
        val prefManager = PreferencesManager(this)

        setContent {
            ChandTheme {
                val scope = rememberCoroutineScope()
                var selectedItemId by remember { mutableStateOf("usd") }
                var selectedThemeId by remember { mutableStateOf(PreferencesManager.DEFAULT_WIDGET_THEME) }

                val selectedItem = items.find { it.id.equals(selectedItemId, ignoreCase = true) } ?: items.first()
                val activeTheme = WidgetTheme.fromId(selectedThemeId)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppleBackground)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Symbol - V2",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Focus on a single item",
                        fontSize = 13.sp,
                        color = AppleTextSecondary
                    )

                    Text(
                        text = "پیش‌نمایش استایل است؛ قیمت واقعی پس از دریافت آنلاین نمایش داده می‌شود.",
                        fontSize = 11.sp,
                        color = AppleTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Apple Squircle Widget Card Preview (Exact match to media_1789907711489.png)
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x10000000), spotColor = Color(0x1A000000))
                            .clip(RoundedCornerShape(24.dp))
                            .background(activeTheme.baseColor)
                            .border(0.8.dp, AppleCardBorder, RoundedCornerShape(24.dp))
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Row: Circular Flag Badge (Left) & Name/Symbol (Right)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Image(
                                    painter = painterResource(id = selectedItem.iconResId),
                                    contentDescription = selectedItem.effectiveNameEn,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                )

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = selectedItem.effectiveNameEn,
                                        color = activeTheme.subTextColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Normal,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = selectedItem.symbol,
                                        color = activeTheme.textColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Middle Row: Arrow Change Indicator (Matches Photo: ↓5,625)
                            Text(
                                text = selectedItem.widgetArrowChangeFormatted,
                                color = if (selectedItem.isFavorable) Color(0xFF34C759) else Color(0xFFFF3B30),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Bottom Row: Large Bold Price (Matches Photo: 100,115)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = selectedItem.formattedPrice,
                                    color = activeTheme.textColor,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                )
                                if (selectedItem.isUsd) {
                                    Text(
                                        text = " $",
                                        color = activeTheme.subTextColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Theme selector chips
                    Text(
                        text = "تم ظاهری ویجت:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WidgetTheme.values().take(3).forEach { theme ->
                            val isSelected = theme.id == selectedThemeId
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) AppleCardBackground else Color(0xFFE5E5EA))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.8.dp,
                                        color = if (isSelected) AppleBlue else AppleCardBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedThemeId = theme.id }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = theme.titleFa.substringBefore(" ("),
                                    color = if (isSelected) AppleBlue else AppleTextSecondary,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "انتخاب دارایی:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items) { item ->
                            val isSelected = item.id == selectedItemId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AppleCardBackground)
                                    .border(0.8.dp, if (isSelected) AppleBlue else AppleCardBorder, RoundedCornerShape(12.dp))
                                    .clickable { selectedItemId = item.id }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = item.iconResId),
                                        contentDescription = item.effectiveNameEn,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "${item.effectiveNameEn} (${item.symbol})",
                                            color = if (isSelected) AppleBlue else AppleTextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.formattedPriceWithUnit,
                                            color = AppleTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = AppleBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                prefManager.setSmallWidgetItem(selectedItemId, appWidgetId)
                                prefManager.setWidgetTheme(selectedThemeId)
                                ChandSmallWidget().updateAll(this@WidgetConfigActivity)

                                val resultValue = Intent().apply {
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                }
                                setResult(Activity.RESULT_OK, resultValue)
                                finish()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue, contentColor = Color.White),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "+ Add Widget  (افزودن ویجت)",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
