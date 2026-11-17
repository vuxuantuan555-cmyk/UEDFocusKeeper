package com.ued.focuskeeper

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ued.focuskeeper.ui.FocusKeeperNavigation
import com.ued.focuskeeper.ui.Routes
import com.ued.focuskeeper.ui.theme.UEDFocusKeeperTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val ACTION_OPEN_HISTORY = "com.ued.focuskeeper.OPEN_HISTORY"
    }

    // Dùng biến state để quản lý màn hình bắt đầu
    private var startDest by mutableStateOf(Routes.TIMER)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Kiểm tra xem mở app từ thông báo hay mở bình thường
        if (intent?.action == ACTION_OPEN_HISTORY) {
            startDest = Routes.HISTORY
        } else {
            startDest = Routes.TIMER
        }

        setContent {
            UEDFocusKeeperTheme {
                // Xoá dòng navController = navController cũ bị lỗi đi
                // Chỉ truyền startDestination vào thôi
                FocusKeeperNavigation(startDestination = startDest)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // Khi click thông báo lúc app đang chạy, buộc Activity khởi động lại
        // để vào onCreate xử lý đúng startDestination
        if (intent.action == ACTION_OPEN_HISTORY) {
            finish()
            startActivity(intent)
        }
    }
}