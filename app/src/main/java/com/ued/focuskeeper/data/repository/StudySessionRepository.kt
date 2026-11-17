package com.ued.focuskeeper.data.repository

import com.ued.focuskeeper.data.db.FocusKeeperDatabase
import com.ued.focuskeeper.data.db.StudySession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudySessionRepository @Inject constructor(
    private val db: FocusKeeperDatabase
) {
    fun getAllSessions(): Flow<List<StudySession>> =
        db.studySessionDao().getAllSessions()

    suspend fun insertSession(session: StudySession) =
        db.studySessionDao().insert(session)
}
