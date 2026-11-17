package com.ued.focuskeeper.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ued.focuskeeper.data.db.StudySession
import com.ued.focuskeeper.ui.theme.*
import com.ued.focuskeeper.viewmodel.TimerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Lịch Sử Học Tập", fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Text("${sessions.size} phiên hoàn thành", fontSize = 12.sp, color = TextMuted)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = BgDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item { WeeklyBarChart(sessions = sessions) }

            if (sessions.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📭", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("Chưa có phiên học nào hoàn thành", color = TextMuted, fontSize = 15.sp)
                        }
                    }
                }
            } else {
                itemsIndexed(sessions) { _, session -> SessionCard(session = session) }
            }
        }
    }
}

@Composable
fun WeeklyBarChart(sessions: List<StudySession>) {
    val dayMs = 86_400_000L
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val diffToMonday = if (currentDayOfWeek == Calendar.SUNDAY) -6 else 2 - currentDayOfWeek
    cal.add(Calendar.DAY_OF_YEAR, diffToMonday)
    val mondayStartMs = cal.timeInMillis

    val dayLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val minutesByDay = IntArray(7)

    sessions.forEach { s ->
        val sStartCal = Calendar.getInstance().apply {
            timeInMillis = s.completion_date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffDays = ((sStartCal.timeInMillis - mondayStartMs) / dayMs).toInt()
        if (diffDays in 0..6) minutesByDay[diffDays] += s.duration_minutes
    }

    val maxMins = minutesByDay.maxOrNull()?.coerceAtLeast(1) ?: 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("TUẦN NÀY (Phút)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                color = TextMuted, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                dayLabels.forEachIndexed { i, label ->
                    val mins = minutesByDay[i]
                    val pct = mins.toFloat() / maxMins

                    val today = Calendar.getInstance()
                    val todayIndex = if (today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) 6
                    else today.get(Calendar.DAY_OF_WEEK) - 2

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        if (mins > 0) {
                            Text("$mins", fontSize = 9.sp, color = BluePrimary, fontWeight = FontWeight.Bold)
                        } else {
                            Text("", fontSize = 9.sp)
                        }

                        Spacer(Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .fillMaxHeight(fraction = (pct * 0.6f).coerceAtLeast(0.02f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (mins > 0) BluePrimary else BorderColor.copy(alpha = 0.3f))
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = if (i == todayIndex) BluePrimary else TextMuted,
                            fontWeight = if (i == todayIndex) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SessionCard(session: StudySession) {
    val dateFormat = SimpleDateFormat("dd 'Tháng' MM, yyyy – HH:mm", Locale("vi"))
    val dateStr = dateFormat.format(Date(session.completion_date))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(12.dp))
                    .background(BluePrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text("📚", fontSize = 22.sp) }

            Column(modifier = Modifier.weight(1f)) {
                Text(session.course_name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary, maxLines = 1)
                Spacer(Modifier.height(3.dp))
                Text(dateStr, fontSize = 12.sp, color = TextMuted)
            }

            Box(
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    .background(BluePrimary.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("${session.duration_minutes} phút", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = BluePrimary)
            }
        }
    }
}