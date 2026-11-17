package com.ued.focuskeeper.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ued.focuskeeper.ui.components.ArcTimerCanvas
import com.ued.focuskeeper.ui.theme.*
import com.ued.focuskeeper.viewmodel.TimerViewModel

@Composable
fun TimerScreen(
    onNavigateToHistory: () -> Unit,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val timerState by viewModel.timerState.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val scrollState = rememberScrollState()
    var courseName by remember { mutableStateOf("Lập trình Android") }
    var selectedMinutes by remember { mutableIntStateOf(25) }

    val notifPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    var savedFlag by remember { mutableStateOf(false) }
    LaunchedEffect(timerState.isCompleted) {
        if (timerState.isCompleted && !savedFlag) {
            savedFlag = true
            viewModel.onTimerCompleted(timerState.courseName, timerState.durationMinutes)
        }
        if (!timerState.isCompleted) savedFlag = false
    }

    val presets = listOf(1, 15, 25, 45, 60)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("TÊN MÔN HỌC", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, letterSpacing = 1.sp)
        OutlinedTextField(
            value = if (timerState.isStarted) timerState.courseName else courseName,
            onValueChange = { if (!timerState.isStarted) courseName = it },
            enabled = !timerState.isStarted,
            placeholder = { Text("Nhập tên môn học...", color = TextMuted) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardDark, unfocusedContainerColor = CardDark,
                disabledContainerColor = CardDark.copy(alpha = 0.5f),
                focusedBorderColor = BluePrimary, unfocusedBorderColor = BorderColor,
                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, disabledTextColor = TextMuted,
            ), singleLine = true
        )

        Text("THỜI GIAN (PHÚT)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, letterSpacing = 1.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { preset ->
                val selected = if (timerState.isStarted) timerState.durationMinutes == preset else selectedMinutes == preset
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(if (selected) BluePrimary else CardDark)
                        .border(1.dp, if (selected) BluePrimary else BorderColor, RoundedCornerShape(10.dp))
                        .clickable(enabled = !timerState.isStarted) { selectedMinutes = preset }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) { Text("$preset", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (selected) Color.White else TextMuted) }
            }
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val remaining = if (timerState.isStarted) timerState.remainingSeconds else selectedMinutes * 60L
            val total = if (timerState.isStarted) timerState.totalSeconds else selectedMinutes * 60L
            ArcTimerCanvas(remainingSeconds = remaining, totalSeconds = total, isRunning = timerState.isRunning, size = 220.dp)
        }

        // 3 NÚT ĐIỀU KHIỂN
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            if (!timerState.isRunning) {
                Button(
                    onClick = { if (timerState.isStarted) viewModel.resumeTimer() else viewModel.startTimer(courseName, selectedMinutes) },
                    modifier = Modifier.weight(2f).height(50.dp), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) { Text(if (timerState.isStarted) "▶  TIẾP TỤC" else "▶  BẮT ĐẦU", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp) }
            } else {
                Button(
                    onClick = { viewModel.pauseTimer() },
                    modifier = Modifier.weight(2f).height(50.dp), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = YellowAccent)
                ) { Text("⏸  TẠM DỪNG", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color.Black) }
            }
            if (timerState.isStarted) {
                Button(
                    onClick = { viewModel.cancelTimer() },
                    modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                ) { Text("✕  HUỶ", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp) }
            }
        }

        AnimatedVisibility(visible = sessions.isNotEmpty(), enter = fadeIn() + slideInVertically()) {
            OutlinedButton(
                onClick = onNavigateToHistory, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
            ) { Text("📋  Xem lịch sử (${sessions.size} phiên)  →", fontSize = 13.sp) }
        }
    }
}