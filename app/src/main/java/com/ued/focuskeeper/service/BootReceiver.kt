package com.ued.focuskeeper.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Khởi động lại TimerService sau khi thiết bị reboot
 * hoặc sau khi app bị kill (kết hợp với START_REDELIVER_INTENT)
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val state = TimerService.timerState.value
        // Chỉ restart nếu đang có phiên học chưa hoàn thành
        if (state.isStarted && !state.isCompleted && state.remainingSeconds > 0) {
            val serviceIntent = Intent(context, TimerService::class.java).apply {
                action = TimerService.ACTION_START
                putExtra(TimerService.EXTRA_COURSE_NAME, state.courseName)
                putExtra(TimerService.EXTRA_DURATION_MINUTES, state.durationMinutes)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}
