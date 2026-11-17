package com.ued.focuskeeper.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ued.focuskeeper.data.db.StudySession
import com.ued.focuskeeper.data.repository.StudySessionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker chạy trong background để lưu phiên học vào Room Database.
 * Được schedule bởi TimerViewModel khi timer hoàn thành.
 */
@HiltWorker
class SaveSessionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: StudySessionRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_COURSE_NAME = "course_name"
        const val KEY_DURATION_MINUTES = "duration_minutes"
        const val KEY_COMPLETION_DATE = "completion_date"
    }

    override suspend fun doWork(): Result {
        return try {
            val courseName = inputData.getString(KEY_COURSE_NAME) ?: return Result.failure()
            val durationMinutes = inputData.getInt(KEY_DURATION_MINUTES, 0)
            val completionDate = inputData.getLong(KEY_COMPLETION_DATE, System.currentTimeMillis())

            val session = StudySession(
                course_name = courseName,
                duration_minutes = durationMinutes,
                completion_date = completionDate
            )
            repository.insertSession(session)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
