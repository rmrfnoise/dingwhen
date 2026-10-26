package com.dingwhen.app.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.dingwhen.app.i18n.LocalStrings
import com.dingwhen.app.ui.theme.LocalDwColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.Locale

/** 本机已安装的可启动应用。 */
data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable?
)

/**
 * 读取本机所有可启动应用。需要清单里的 `<queries>` 声明（Android 11+ 的包可见性限制），
 * 否则只能拿到系统默认可见的那几个。
 */
fun loadLaunchableApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val resolved = pm.queryIntentActivities(query, 0)
    val collator = Collator.getInstance(Locale.CHINA)
    val seen = HashSet<String>()
    return resolved.asSequence()
        .mapNotNull { it.activityInfo?.applicationInfo }
        .filter { it.packageName != context.packageName && seen.add(it.packageName) }
        .map { info ->
            InstalledApp(
                packageName = info.packageName,
                label = pm.getApplicationLabel(info).toString(),
                icon = runCatching { pm.getApplicationIcon(info) }.getOrNull()
            )
        }
        .sortedWith { a, b -> collator.compare(a.label, b.label) }
        .toList()
}

/** 包名对应的应用名；应用已卸载或取不到时返回 null。 */
fun appLabelOf(context: Context, packageName: String?): String? {
    if (packageName.isNullOrBlank()) return null
    return runCatching {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    }.getOrNull()
}

fun appIconOf(context: Context, packageName: String?): Drawable? {
    if (packageName.isNullOrBlank()) return null
    return runCatching {
        context.packageManager.getApplicationIcon(packageName)
    }.getOrNull()
}

/**
 * 应用选择器：底部抽屉，带图标、可按名称过滤。
 * 用 sheet 而不是对话框，列表可以一路滚到底，不会被按钮挤成半截。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerSheet(
    onDismiss: () -> Unit,
    onPick: (String) -> Unit
) {
    val context = LocalContext.current
    val text = LocalStrings.current
    var apps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var keyword by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        val loaded = withContext(Dispatchers.IO) { loadLaunchableApps(context) }
        apps = loaded
        loading = false
    }

    val filtered = remember(apps, keyword) {
        val k = keyword.trim()
        if (k.isEmpty()) apps else apps.filter {
            it.label.contains(k, ignoreCase = true) || it.packageName.contains(k, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.86f)
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Text(text.pickerTitle, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                text.pickerHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            TextField(
                value = keyword,
                onValueChange = { keyword = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text.searchApps,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = LocalDwColors.current.field,
                    unfocusedContainerColor = LocalDwColors.current.field,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(Modifier.height(6.dp))

            when {
                loading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 2.5.dp)
                }

                filtered.isEmpty() -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (apps.isEmpty()) text.noApps else text.noMatch,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(filtered, key = { it.packageName }) { app ->
                        AppRow(app) {
                            onPick(app.packageName)
                            onDismiss()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRow(app: InstalledApp, onClick: () -> Unit) {
    val bitmap = remember(app.packageName) {
        app.icon?.let { drawable ->
            runCatching { drawable.toBitmap(72, 72).asImageBitmap() }.getOrNull()
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LocalDwColors.current.field),
                contentAlignment = Alignment.Center
            ) {
                Text("·", style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                app.label,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
