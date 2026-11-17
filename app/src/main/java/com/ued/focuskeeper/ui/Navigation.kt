package com.ued.focuskeeper.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ued.focuskeeper.ui.screens.HistoryScreen
import com.ued.focuskeeper.ui.screens.TimerScreen
import com.ued.focuskeeper.ui.theme.*

object Routes {
    const val TIMER = "timer"
    const val HISTORY = "history"
}

@Composable
fun FocusKeeperNavigation(startDestination: String = Routes.TIMER) {
    val navController = rememberNavController()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Surface(color = SurfaceDark, tonalElevation = 0.dp) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                TabRow(navController)
            }
        }
        NavHost(navController = navController, startDestination = startDestination, modifier = Modifier.fillMaxSize()) {
            composable(Routes.TIMER) { TimerScreen(onNavigateToHistory = { navController.navigate(Routes.HISTORY) }) }
            composable(Routes.HISTORY) { HistoryScreen(onBack = { navController.popBackStack() }) }
        }
    }
}

@Composable
fun TabRow(navController: androidx.navigation.NavController) {
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: Routes.TIMER

    androidx.compose.foundation.layout.Row(
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Surface(shape = RoundedCornerShape(8.dp), color = BluePrimary) {
            Text("UED", modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                fontWeight = FontWeight.ExtraBold, fontSize = 13.sp,
                color = androidx.compose.ui.graphics.Color.White)
        }
        androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
            Text("Người Giữ Tập Trung", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = TextPrimary)
            Text("Bài Thực Hành 6", fontSize = 10.sp, color = TextMuted)
        }
        listOf(Routes.TIMER to "⏱ Hẹn giờ", Routes.HISTORY to "📋 Lịch sử").forEach { (route, label) ->
            val selected = current == route
            Surface(shape = RoundedCornerShape(8.dp), color = if (selected) BluePrimary else CardDark,
                onClick = { if (!selected) navController.navigate(route) }) {
                Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    color = if (selected) androidx.compose.ui.graphics.Color.White else TextMuted)
            }
        }
    }
}