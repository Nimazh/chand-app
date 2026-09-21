package com.chand.app.ui.screen

import android.content.Context
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.WindowManager
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
import com.chand.app.data.local.PreferencesManager
import com.chand.app.data.model.PriceItem
import com.chand.app.ui.components.LiquidGlassButton
import com.chand.app.ui.components.LiquidGlassButtonStyle
import com.chand.app.ui.theme.AppleBackground
import com.chand.app.ui.theme.AppleBlue
import com.chand.app.ui.theme.AppleCardBackground
import com.chand.app.ui.theme.AppleCardBorder
import com.chand.app.ui.theme.AppleGreen
import com.chand.app.ui.theme.AppleSegmentBg
import com.chand.app.ui.theme.AppleTextPrimary
import com.chand.app.ui.theme.AppleTextSecondary
import com.chand.app.ui.theme.AppleTextTertiary
import com.chand.app.widget.ChandLargeWidget
import com.chand.app.widget.ChandMediumWidget
import com.chand.app.widget.ChandSmallWidget
import com.chand.app.widget.WidgetTheme
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    val prefManager = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()

    val performHaptic = {
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
    }

    val prices by viewModel.repository.prices.collectAsState()
    val smallItemPref by prefManager.smallWidgetItemFlow.collectAsState(initial = "usd")
    val customApiUrlPref by prefManager.customApiUrlFlow.collectAsState(initial = "")

    val widgetThemePref by prefManager.widgetThemeFlow.collectAsState(initial = PreferencesManager.DEFAULT_WIDGET_THEME)
    val widgetOpacityPref by prefManager.widgetOpacityFlow.collectAsState(initial = PreferencesManager.DEFAULT_WIDGET_OPACITY)
    val widgetCornerRadiusPref by prefManager.widgetCornerRadiusFlow.collectAsState(initial = PreferencesManager.DEFAULT_WIDGET_CORNER_RADIUS)

    var selectedThemeId by remember(widgetThemePref) { mutableStateOf(widgetThemePref) }
    var selectedOpacity by remember(widgetOpacityPref) { mutableStateOf(widgetOpacityPref) }
    var selectedCornerRadius by remember(widgetCornerRadiusPref) { mutableStateOf(widgetCornerRadiusPref) }
    var isStyleSaved by remember { mutableStateOf(false) }

    var apiUrlInput by remember(customApiUrlPref) { mutableStateOf(customApiUrlPref) }
    var isSavedUrl by remember { mutableStateOf(false) }

    val cardShape = RoundedCornerShape(18.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
    ) {
        Scaffold(
            containerColor = AppleBackground,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val backInteraction = remember { MutableInteractionSource() }
                        val isBackPressed by backInteraction.collectIsPressedAsState()
                        val backScale by animateFloatAsState(
                            targetValue = if (isBackPressed) 0.93f else 1f,
                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
                            label = "back_btn_scale"
                        )

                        Box(
                            modifier = Modifier
                                .scale(backScale)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AppleCardBackground)
                                .border(0.8.dp, AppleCardBorder, CircleShape)
                                .clickable(
                                    interactionSource = backInteraction,
                                    indication = null,
                                    onClick = {
                                        performHaptic()
                                        onBack()
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Back",
                                tint = AppleTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "تنظیمات و ابزارک‌ها",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleTextPrimary
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // 1. Widget Guide Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, cardShape, ambientColor = Color(0x06000000), spotColor = Color(0x0A000000))
                        .clip(cardShape)
                        .background(AppleCardBackground)
                        .border(0.8.dp, AppleCardBorder, cardShape)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(AppleSegmentBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Widgets,
                                    contentDescription = "Widgets",
                                    tint = AppleBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "راهنمای افزودن ویجت به صفحه گوشی",
                                color = AppleTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "۱. در صفحه اصلی گوشی انگشت خود را روی فضای خالی نگه دارید.\n" +
                                    "۲. گزینه Widgets (ابزارک‌ها) را انتخاب کنید.\n" +
                                    "۳. برنامه «Chand?!» را انتخاب و به صفحه اصلی اضافه نمایید.\n" +
                                    "۴. قیمت‌ها خودکار در پس‌زمینه با کمترین مصرف باتری همگام می‌شوند.",
                            color = AppleTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. App Appearance (Theme: Light / Dark / System)
                val appThemeMode by viewModel.appThemeMode.collectAsState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, cardShape, ambientColor = Color(0x06000000), spotColor = Color(0x0A000000))
                        .clip(cardShape)
                        .background(AppleCardBackground)
                        .border(0.8.dp, AppleCardBorder, cardShape)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(AppleSegmentBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (appThemeMode) {
                                        "dark" -> Icons.Filled.DarkMode
                                        "light" -> Icons.Filled.LightMode
                                        else -> Icons.Outlined.BrightnessAuto
                                    },
                                    contentDescription = "Theme",
                                    tint = AppleBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "حالت نمایش (تم برنامه)",
                                    color = AppleTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "انتخاب تم روشن، تاریک یا هماهنگ با سیستم",
                                    color = AppleTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Apple-style Segmented Control for Theme Mode
                        val themeOptions = listOf(
                            Triple("light", "روشن", Icons.Filled.LightMode),
                            Triple("dark", "تاریک", Icons.Filled.DarkMode),
                            Triple("system", "سیستم", Icons.Outlined.BrightnessAuto)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppleSegmentBg)
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            themeOptions.forEach { (modeId, title, icon) ->
                                val isSelected = appThemeMode == modeId
                                val textColor by animateColorAsState(
                                    targetValue = if (isSelected) AppleBlue else AppleTextSecondary,
                                    label = "theme_btn_color"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (isSelected) AppleCardBackground else Color.Transparent)
                                        .then(
                                            if (isSelected) {
                                                Modifier
                                                    .shadow(1.dp, RoundedCornerShape(9.dp), ambientColor = Color(0x08000000), spotColor = Color(0x10000000))
                                                    .border(0.6.dp, AppleCardBorder, RoundedCornerShape(9.dp))
                                            } else Modifier
                                        )
                                        .clickable {
                                            performHaptic()
                                            viewModel.setAppThemeMode(modeId)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = title,
                                            tint = textColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = title,
                                            color = textColor,
                                            fontSize = 12.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Widget Style Customizer Card
                val activeWidgetTheme = WidgetTheme.fromId(selectedThemeId)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, cardShape, ambientColor = Color(0x06000000), spotColor = Color(0x0A000000))
                        .clip(cardShape)
                        .background(AppleCardBackground)
                        .border(0.8.dp, AppleCardBorder, cardShape)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎨", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "شخصی‌سازی استایل ویجت‌ها",
                                    color = AppleTextPrimary,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "انتخاب تم، شفافیت و میزان گردی گوشه‌ها برای ویجت‌ها",
                                    color = AppleTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live Widget Preview
                        Text(
                            text = "پیش‌نمایش زنده استایل انتخابی:",
                            color = AppleTextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val previewBg = activeWidgetTheme.resolveBackgroundColor(selectedOpacity)
                        val previewCorner = selectedCornerRadius.dp
                        val sampleItem = prices.find { it.id.equals(smallItemPref, ignoreCase = true) }
                            ?: prices.firstOrNull()
                            ?: PriceItem("btc", "بیت‌کوین", "BTC", com.chand.app.data.model.PriceCategory.CRYPTO, 19320000000L, 1.33, priceUsd = 84300.0)

                        // Apple Chand Live Widget Preview (Matches Symbol - V2 in media_1789907711489.png)
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(155.dp)
                                    .shadow(6.dp, RoundedCornerShape(previewCorner), ambientColor = Color(0x10000000), spotColor = Color(0x1A000000))
                                    .clip(RoundedCornerShape(previewCorner))
                                    .background(previewBg)
                                    .border(
                                        0.8.dp,
                                        AppleCardBorder,
                                        RoundedCornerShape(previewCorner)
                                    )
                                    .padding(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // 1. Top Row: Circular Flag Badge (Left) & Name/Symbol (Right)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Image(
                                            painter = painterResource(id = sampleItem.iconResId),
                                            contentDescription = sampleItem.effectiveNameEn,
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                        )

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = sampleItem.effectiveNameEn,
                                                color = activeWidgetTheme.subTextColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Normal,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = sampleItem.symbol,
                                                color = activeWidgetTheme.textColor,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    // 2. Middle Row: Arrow Change Indicator (Matches Photo: ↓5,625)
                                    Text(
                                        text = sampleItem.widgetArrowChangeFormatted,
                                        color = if (sampleItem.isFavorable) AppleGreen else Color(0xFFFF3B30),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    // 3. Bottom Row: Large Bold Price (Matches Photo: 100,115)
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = sampleItem.formattedPrice,
                                            color = activeWidgetTheme.textColor,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.5).sp
                                        )
                                        if (sampleItem.isUsd) {
                                            Text(
                                                text = " $",
                                                color = activeWidgetTheme.subTextColor,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Theme Selection Chips
                        Text(
                            text = "۱. انتخاب تم ظاهری ویجت:",
                            color = AppleTextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        WidgetTheme.values().forEach { theme ->
                            val isSelected = theme.id == selectedThemeId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) AppleSegmentBg else AppleBackground)
                                    .border(
                                        width = if (isSelected) 1.2.dp else 0.8.dp,
                                        color = if (isSelected) AppleBlue else AppleCardBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        performHaptic()
                                        selectedThemeId = theme.id
                                        isStyleSaved = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(theme.accentColor)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = theme.titleFa,
                                        color = AppleTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = AppleBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Opacity Selection
                        Text(
                            text = "۲. میزان شفافیت پس‌زمینه ویجت:",
                            color = AppleTextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Pair(100, "کامل (۱۰۰٪)"),
                                Pair(80, "ملایم (۸۰٪)"),
                                Pair(55, "شفاف (۵۵٪)")
                            ).forEach { (value, label) ->
                                val isSelected = selectedOpacity == value
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) AppleSegmentBg else AppleBackground)
                                        .border(
                                            width = if (isSelected) 1.2.dp else 0.8.dp,
                                            color = if (isSelected) AppleBlue else AppleCardBorder,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            performHaptic()
                                            selectedOpacity = value
                                            isStyleSaved = false
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) AppleBlue else AppleTextSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Corner Radius Selection
                        Text(
                            text = "۳. میزان گردی گوشه‌ها:",
                            color = AppleTextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Pair(14, "کلاسیک (۱۴)"),
                                Pair(20, "اپل (۲۰)"),
                                Pair(28, "گرد (۲۸)")
                            ).forEach { (value, label) ->
                                val isSelected = selectedCornerRadius == value
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) AppleSegmentBg else AppleBackground)
                                        .border(
                                            width = if (isSelected) 1.2.dp else 0.8.dp,
                                            color = if (isSelected) AppleBlue else AppleCardBorder,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            performHaptic()
                                            selectedCornerRadius = value
                                            isStyleSaved = false
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) AppleBlue else AppleTextSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Save Button (Apple Dynamic Pill Button)
                        LiquidGlassButton(
                            onClick = {
                                scope.launch {
                                    prefManager.setWidgetStyle(selectedThemeId, selectedOpacity, selectedCornerRadius)
                                    ChandSmallWidget().updateAll(context)
                                    ChandMediumWidget().updateAll(context)
                                    ChandLargeWidget().updateAll(context)
                                    isStyleSaved = true
                                }
                            },
                            style = if (isStyleSaved) LiquidGlassButtonStyle.SUCCESS else LiquidGlassButtonStyle.PRIMARY,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth(),
                            height = 46.dp
                        ) {
                            if (isStyleSaved) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = if (isStyleSaved) "استایل روی تمام ویجت‌ها اعمال شد" else "ذخیره و اعمال استایل روی تمام ویجت‌ها",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Choose Item for Small Widget
                Text(
                    text = "انتخاب دارایی برای ویجت تکی (Small)",
                    color = AppleTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, cardShape, ambientColor = Color(0x06000000), spotColor = Color(0x0A000000))
                        .clip(cardShape)
                        .background(AppleCardBackground)
                        .border(0.8.dp, AppleCardBorder, cardShape)
                ) {
                    Column {
                        prices.take(8).forEachIndexed { index, item ->
                            val isSelected = item.id.equals(smallItemPref, ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        performHaptic()
                                        scope.launch {
                                            prefManager.setSmallWidgetItem(item.id)
                                            ChandSmallWidget().updateAll(context)
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                    Text(
                                        text = "${item.effectiveNameEn} (${item.symbol})",
                                        color = if (isSelected) AppleBlue else AppleTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = AppleBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            if (index < prices.take(8).size - 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .height(0.6.dp)
                                        .background(AppleCardBorder)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5. Custom API Endpoint
                Text(
                    text = "تنظیم آدرس اختصاصی API (اختیاری)",
                    color = AppleTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "اگر پروکسی یا سرور اختصاصی خود را دارید، آدرس آن را وارد کنید:",
                    color = AppleTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apiUrlInput,
                    onValueChange = {
                        apiUrlInput = it
                        isSavedUrl = false
                    },
                    placeholder = { Text("https://my-proxy-api.com/rates", fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppleTextPrimary,
                        unfocusedTextColor = AppleTextPrimary,
                        focusedBorderColor = AppleBlue,
                        unfocusedBorderColor = AppleCardBorder,
                        focusedContainerColor = AppleCardBackground,
                        unfocusedContainerColor = AppleCardBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                LiquidGlassButton(
                    onClick = {
                        scope.launch {
                            prefManager.setCustomApiUrl(apiUrlInput.trim())
                            isSavedUrl = true
                        }
                    },
                    style = if (isSavedUrl) LiquidGlassButtonStyle.SUCCESS else LiquidGlassButtonStyle.PRIMARY,
                    shape = RoundedCornerShape(50),
                    height = 46.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSavedUrl) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (isSavedUrl) "تنظیمات با موفقیت ذخیره شد" else "ذخیره تنظیمات API",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}
