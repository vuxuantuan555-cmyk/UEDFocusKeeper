package com.ued.focuskeeper.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ued.focuskeeper.data.db.StudySession
import com.ued.focuskeeper.data.repository.StudySessionRepository
import com.ued.focuskeeper.service.TimerService
import com.ued.focuskeeper.service.TimerState
import com.ued.focuskeeper.worker.SaveSessionWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    application: Application,
    private val repository: StudySessionRepository
) : AndroidViewModel(application) {

    private val context = application

    // Timer state from Service singleton
    val timerState: StateFlow<TimerState> = TimerService.timerState

    // All study sessions from Room DB (sorted DESC by date)
    val sessions = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startTimer(courseName: String, durationMinutes: Int) {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_COURSE_NAME, courseName)
            putExtra(TimerService.EXTRA_DURATION_MINUTES, durationMinutes)
        }
        startService(intent)
    }

    fun pauseTimer() {
        sendAction(TimerService.ACTION_PAUSE)
    }

    fun resumeTimer() {
        sendAction(TimerService.ACTION_RESUME)
    }

    fun cancelTimer() {
        sendAction(TimerService.ACTION_CANCEL)
    }

    /**
     * Called when timer completes — schedule WorkManager to save session to DB
     */
    fun onTimerCompleted(courseName: String, durationMinutes: Int) {
        viewModelScope.launch {
            val inputData = Data.Builder()
                .putString(SaveSessionWorker.KEY_COURSE_NAME, courseName)
                .putInt(SaveSessionWorker.KEY_DURATION_MINUTES, durationMinutes)
                .putLong(SaveSessionWorker.KEY_COMPLETION_DATE, System.currentTimeMillis())
                .build()

            val request = OneTimeWorkRequestBuilder<SaveSessionWorker>()
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }

    private fun sendAction(action: String) {
        val intent = Intent(context, TimerService::class.java).apply {
            this.action = action
        }
        context.startService(intent)
    }

    private fun startService(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}
