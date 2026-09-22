package com.chand.app.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chand.app.data.local.PreferencesManager
import com.chand.app.data.model.PriceItem
import com.chand.app.data.model.PriceCategory
import com.chand.app.data.remote.PriceApiService
import com.chand.app.ui.theme.AppleBackground
import com.chand.app.ui.theme.AppleBlue
import com.chand.app.ui.theme.AppleCardBackground
import com.chand.app.ui.theme.AppleCardBorder
import com.chand.app.ui.theme.AppleTextPrimary
import com.chand.app.ui.theme.AppleTextSecondary
import com.chand.app.ui.theme.ChandTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val preferences = PreferencesManager(this)
        setContent {
            ChandTheme {
                WidgetConfigurationScreen(
                    kind = resolveWidgetKind(),
                    appWidgetId = appWidgetId,
                    preferences = preferences,
                    onSaved = ::finishWithSuccess
                )
            }
        }
    }

    private fun resolveWidgetKind(): WidgetKind {
        val providerClassName = AppWidgetManager.getInstance(this)
            .getAppWidgetInfo(appWidgetId)
            ?.provider
            ?.className
            .orEmpty()
        return when {
            providerClassName.endsWith("ChandMediumWidgetReceiver") -> WidgetKind.MEDIUM
            providerClassName.endsWith("ChandLargeWidgetReceiver") -> WidgetKind.LARGE
            else -> WidgetKind.SMALL
        }
    }

    private fun finishWithSuccess() {
        setResult(
            Activity.RESULT_OK,
            android.content.Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        )
        finish()
    }
}

private enum class WidgetKind(val title: String, val maxItems: Int, val helper: String) {
    SMALL("ویجت تکی", 1, "یک دارایی را برای این ویجت انتخاب کنید."),
    MEDIUM("ویجت متوسط", 4, "تا ۴ دارایی انتخاب کنید؛ ترتیب انتخاب، ترتیب نمایش است."),
    LARGE("ویجت بزرگ", 6, "تا ۶ دارایی انتخاب کنید؛ ترتیب انتخاب، ترتیب نمایش است.")
}

@androidx.compose.runtime.Composable
private fun WidgetConfigurationScreen(
    kind: WidgetKind,
    appWidgetId: Int,
    preferences: PreferencesManager,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cachedItems by preferences.cachedPricesFlow.collectAsState(initial = emptyList())
    val availableItems = cachedItems.ifEmpty { PriceApiService.getPreviewItems() }
    val savedIdsFlow: Flow<List<String>> = remember(kind, appWidgetId) {
        when (kind) {
            WidgetKind.SMALL -> preferences.smallWidgetItemFlow(appWidgetId).map { listOf(it) }
            WidgetKind.MEDIUM -> preferences.mediumWidgetItemsFlow(appWidgetId)
            WidgetKind.LARGE -> preferences.largeWidgetItemsFlow(appWidgetId)
        }
    }
    val storedIds by savedIdsFlow.collectAsState(initial = emptyList())
    val widgetThemeId by preferences.widgetThemeFlow.collectAsState(initial = PreferencesManager.DEFAULT_WIDGET_THEME)
    val fallbackIds = remember(kind, availableItems) {
        when (kind) {
            WidgetKind.SMALL -> listOf(availableItems.firstOrNull()?.id ?: "usd")
            WidgetKind.MEDIUM -> PreferencesManager.DEFAULT_MEDIUM_ITEMS
            WidgetKind.LARGE -> availableItems.filter { it.id in PreferencesManager.DEFAULT_FAVORITES }
                .map { it.id }
                .take(kind.maxItems)
        }
    }
    var selectedIds by remember(kind, storedIds, fallbackIds) {
        mutableStateOf(storedIds.filter { storedId -> availableItems.any { it.id == storedId } }.ifEmpty { fallbackIds })
    }
    var selectedThemeId by remember(widgetThemeId) { mutableStateOf(widgetThemeId) }
    var searchQuery by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf<PriceCategory?>(null) }
    val normalizedQuery = remember(searchQuery) { searchQuery.normalizeForSearch() }
    val filteredItems = remember(availableItems, normalizedQuery, categoryFilter) {
        availableItems.filter { item ->
            (categoryFilter == null || item.category == categoryFilter) &&
                (normalizedQuery.isBlank() || listOf(item.effectiveNameEn, item.nameFa, item.symbol)
                    .any { it.normalizeForSearch().contains(normalizedQuery) })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(kind.title, color = AppleTextPrimary, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(kind.helper, color = AppleTextSecondary, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(14.dp))

        SelectionPreview(
            items = selectedIds.mapNotNull { selectedId -> availableItems.find { it.id == selectedId } },
            title = "${selectedIds.size} از ${kind.maxItems} دارایی انتخاب شده"
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("دارایی‌های نمایش‌داده‌شده", color = AppleTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("جست‌وجوی ارز، کریپتو یا طلا") },
            placeholder = { Text("مثلاً بیت‌کوین، دلار یا طلای ۱۸") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppleBlue,
                focusedLabelColor = AppleBlue,
                unfocusedBorderColor = AppleCardBorder
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CategoryFilter.entries.forEach { filter ->
                val selected = categoryFilter == filter.category
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) AppleCardBackground else Color(0xFFE5E5EA))
                        .border(1.dp, if (selected) AppleBlue else AppleCardBorder, RoundedCornerShape(10.dp))
                        .clickable { categoryFilter = filter.category }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(filter.title, color = if (selected) AppleBlue else AppleTextSecondary, fontSize = 11.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (filteredItems.isEmpty()) {
            Text("دارایی مطابق جست‌وجو پیدا نشد.", color = AppleTextSecondary, fontSize = 13.sp)
        }
        filteredItems.forEach { item ->
            val selectedIndex = selectedIds.indexOf(item.id)
            val isSelected = selectedIndex >= 0
            val canToggle = !isSelected || selectedIds.size > 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppleCardBackground)
                    .border(0.8.dp, if (isSelected) AppleBlue else AppleCardBorder, RoundedCornerShape(12.dp))
                    .then(
                        if (canToggle) Modifier.clickable {
                            selectedIds = when {
                                kind == WidgetKind.SMALL -> listOf(item.id)
                                isSelected -> selectedIds.filterNot { it == item.id }
                                selectedIds.size < kind.maxItems -> selectedIds + item.id
                                else -> selectedIds
                            }
                        } else Modifier
                    )
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(item.iconResId),
                        contentDescription = item.effectiveNameEn,
                        modifier = Modifier.size(26.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("${item.effectiveNameEn} (${item.symbol})", color = if (isSelected) AppleBlue else AppleTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(item.formattedPriceWithUnit, color = AppleTextSecondary, fontSize = 12.sp)
                    }
                }
                if (isSelected) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (kind != WidgetKind.SMALL) {
                            Text("${selectedIndex + 1}", color = AppleBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { selectedIds = selectedIds.move(selectedIndex, selectedIndex - 1) },
                                enabled = selectedIndex > 0,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "انتقال به بالا", tint = AppleBlue)
                            }
                            IconButton(
                                onClick = { selectedIds = selectedIds.move(selectedIndex, selectedIndex + 1) },
                                enabled = selectedIndex < selectedIds.lastIndex,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "انتقال به پایین", tint = AppleBlue)
                            }
                        }
                        Icon(Icons.Filled.Check, contentDescription = "انتخاب شده", tint = AppleBlue, modifier = Modifier.size(19.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("تم ظاهری", color = AppleTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text("این انتخاب فعلاً روی همهٔ ویجت‌ها اعمال می‌شود.", color = AppleTextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WidgetTheme.values().take(3).forEach { theme ->
                val isSelected = theme.id == selectedThemeId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) AppleCardBackground else Color(0xFFE5E5EA))
                        .border(1.dp, if (isSelected) AppleBlue else AppleCardBorder, RoundedCornerShape(10.dp))
                        .clickable { selectedThemeId = theme.id }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(theme.titleFa.substringBefore(" ("), color = if (isSelected) AppleBlue else AppleTextSecondary, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = {
                scope.launch {
                    when (kind) {
                        WidgetKind.SMALL -> preferences.setSmallWidgetItem(selectedIds.first(), appWidgetId)
                        WidgetKind.MEDIUM -> preferences.setMediumWidgetItems(selectedIds, appWidgetId)
                        WidgetKind.LARGE -> preferences.setLargeWidgetItems(selectedIds, appWidgetId)
                    }
                    preferences.setWidgetTheme(selectedThemeId)
                    ChandWidgetUpdater.updateOne(context, appWidgetId, kind.toUpdateType())
                    onSaved()
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = AppleBlue, contentColor = Color.White)
        ) {
            Text("ذخیره و به‌روزرسانی ویجت", fontWeight = FontWeight.Bold)
        }
    }
}

private enum class CategoryFilter(val title: String, val category: PriceCategory?) {
    ALL("همه", null),
    CURRENCY("ارز", PriceCategory.CURRENCY),
    CRYPTO("کریپتو", PriceCategory.CRYPTO),
    GOLD("طلا", PriceCategory.GOLD)
}

private fun WidgetKind.toUpdateType() = when (this) {
    WidgetKind.SMALL -> ChandWidgetType.SMALL
    WidgetKind.MEDIUM -> ChandWidgetType.MEDIUM
    WidgetKind.LARGE -> ChandWidgetType.LARGE
}

private fun List<String>.move(from: Int, to: Int): List<String> {
    if (from !in indices || to !in indices || from == to) return this
    return toMutableList().apply { add(to, removeAt(from)) }
}

private fun String.normalizeForSearch() = trim().lowercase()
    .replace('ي', 'ی')
    .replace('ك', 'ک')

@androidx.compose.runtime.Composable
private fun SelectionPreview(items: List<PriceItem>, title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppleCardBackground)
            .border(0.8.dp, AppleCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(title, color = AppleTextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            if (items.isEmpty()) {
                Text("دارایی انتخاب نشده است", color = AppleTextSecondary, fontSize = 13.sp)
            } else {
                items.forEachIndexed { index, item ->
                    Text("${index + 1}. ${item.symbol} — ${item.formattedPriceWithUnit}", color = AppleTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
