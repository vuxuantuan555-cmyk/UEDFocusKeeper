package com.ued.focuskeeper.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ued.focuskeeper.MainActivity
import com.ued.focuskeeper.R

object NotificationHelper {
    private const val CHANNEL_ID_COMPLETE = "completion_channel"
    private const val NOTIF_ID_COMPLETE = 2001

    fun showCompletionNotification(context: Context, courseName: String) {
        createCompletionChannel(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.ued.focuskeeper.OPEN_HISTORY"
            // Flags giúp dọn dẹp Stack để MainActivity nhận diện đúng Intent mới
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIF_ID_COMPLETE,
            openIntent,
            // Sử dụng FLAG_IMMUTABLE cho Android 12+ và UPDATE_CURRENT để làm mới dữ liệu
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_COMPLETE)
            .setContentTitle("✅ Phiên học hoàn tất!")
            .setContentText("Môn học: $courseName")
            .setSmallIcon(R.drawable.ic_check)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID_COMPLETE, notification)
    }

    private fun createCompletionChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID_COMPLETE,
            "Hoàn thành",
            NotificationManager.IMPORTANCE_HIGH
        )
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }
}