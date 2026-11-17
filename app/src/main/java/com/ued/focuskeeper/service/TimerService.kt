package com.ued.focuskeeper.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.ued.focuskeeper.MainActivity
import com.ued.focuskeeper.R
import com.ued.focuskeeper.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerService : Service() {

    companion object {
        const val CHANNEL_ID_TIMER = "timer_channel"
        const val NOTIF_ID_TIMER = 1001

        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_CANCEL = "ACTION_CANCEL"

        const val EXTRA_COURSE_NAME = "course_name"
        const val EXTRA_DURATION_MINUTES = "duration_minutes"

        // Shared state accessible from ViewModel
        private val _timerState = MutableStateFlow(TimerState())
        val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

        fun resetState() {
            _timerState.value = TimerState()
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val courseName = intent.getStringExtra(EXTRA_COURSE_NAME) ?: "Môn học"
                val durationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 25)
                startTimer(courseName, durationMinutes)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_CANCEL -> {
                cancelTimer()
                stopSelf()
                return START_NOT_STICKY
            }
        }
        // START_REDELIVER_INTENT: if killed, restarts with last intent
        return START_REDELIVER_INTENT
    }

    private fun startTimer(courseName: String, durationMinutes: Int) {
        val totalSeconds = durationMinutes * 60L
        _timerState.value = TimerState(
            courseName = courseName,
            durationMinutes = durationMinutes,
            totalSeconds = totalSeconds,
            remainingSeconds = totalSeconds,
            isRunning = true,
            isStarted = true
        )
        startForeground(NOTIF_ID_TIMER, buildTimerNotification(courseName, totalSeconds))
        startCountdown()
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = false)
        updateNotification()
    }

    private fun resumeTimer() {
        _timerState.value = _timerState.value.copy(isRunning = true)
        startCountdown()
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        _timerState.value = TimerState()
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (_timerState.value.remainingSeconds > 0 && _timerState.value.isRunning) {
                delay(1000L)
                val current = _timerState.value
                if (!current.isRunning) break
                val newRemaining = current.remainingSeconds - 1
                _timerState.value = current.copy(remainingSeconds = newRemaining)
                updateNotification()

                if (newRemaining <= 0) {
                    onTimerComplete()
                    break
                }
            }
        }
    }

    private fun onTimerComplete() {
        val state = _timerState.value
        _timerState.value = state.copy(isRunning = false, isCompleted = true)
        // Show completion notification
        NotificationHelper.showCompletionNotification(
            context = this,
            courseName = state.courseName
        )
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification() {
        val state = _timerState.value
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID_TIMER, buildTimerNotification(state.courseName, state.remainingSeconds))
    }

    private fun buildTimerNotification(courseName: String, remainingSeconds: Long): Notification {
        val mins = remainingSeconds / 60
        val secs = remainingSeconds % 60
        val timeStr = String.format("%02d:%02d", mins, secs)

        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID_TIMER)
            .setContentTitle("⏱ Đang học: $courseName")
            .setContentText("Thời gian còn lại: $timeStr")
            .setSmallIcon(R.drawable.ic_timer)
            .setContentIntent(openIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID_TIMER,
            "Timer Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Hiển thị tiến trình đếm giờ"
        }
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }
}

data class TimerState(
    val courseName: String = "",
    val durationMinutes: Int = 25,
    val totalSeconds: Long = 25 * 60L,
    val remainingSeconds: Long = 25 * 60L,
    val isRunning: Boolean = false,
    val isStarted: Boolean = false,
    val isCompleted: Boolean = false
)
