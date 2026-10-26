package com.dingwhen.app.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/** 每次回到前台自增，用来刷新权限状态这类"外部世界"的快照。 */
@Composable
fun rememberResumeTick(): Int {
    var tick by remember { mutableIntStateOf(0) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) tick++
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    return tick
}

@Composable
fun AppRoot(vm: AppViewModel = viewModel()) {
    val route by vm.route.collectAsStateWithLifecycle()
    val draft by vm.draft.collectAsStateWithLifecycle()
    val reminders by vm.reminders.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("dingwhen", Context.MODE_PRIVATE) }
    val tick = rememberResumeTick()

    // 首次启动进入权限引导
    LaunchedEffect(Unit) {
        if (!prefs.getBoolean("guide_shown", false)) {
            prefs.edit().putBoolean("guide_shown", true).apply()
            vm.openPermissions()
        }
    }

    BackHandler(enabled = route != Route.LIST) {
        vm.backToList()
    }

    when (route) {
        Route.LIST -> {
            // tick 变化时重新读一次权限状态
            val notificationGranted = remember(tick) { Permissions.notificationsEnabled(context) }
            val exactAlarmGranted = remember(tick) { Permissions.exactAlarmEnabled(context) }
            val loaded = reminders
            if (loaded == null) {
                // 首帧：数据库还没回来，只铺底色，避免空状态闪一下
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            } else {
                ListScreen(
                    reminders = loaded,
                    notificationGranted = notificationGranted,
                    exactAlarmGranted = exactAlarmGranted,
                    onAddManual = vm::openManualCreate,
                    onToggle = vm::toggle,
                    onDelete = vm::delete,
                    onEdit = vm::edit,
                    onOpenPermissions = vm::openPermissions
                )
            }
        }

        Route.EDITOR -> EditorScreen(
            draft = draft,
            onUpdate = vm::updateDraft,
            onSave = vm::saveDraft,
            onDelete = if (draft.id != null) vm::deleteCurrentDraft else null,
            onBack = vm::backToList
        )

        Route.PERMISSIONS -> PermissionsScreen(
            onBack = vm::backToList,
            onFinished = {
                vm.syncSchedule()
                vm.backToList()
            }
        )
    }
}
