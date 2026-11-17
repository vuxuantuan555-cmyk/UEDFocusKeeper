package com.ued.focuskeeper.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: StudySession)

    @Query("SELECT * FROM study_sessions ORDER BY completion_date DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Query("SELECT COUNT(*) FROM study_sessions")
    suspend fun getCount(): Int
    @Query("SELECT COALESCE(SUM(points), 0) FROM study_sessions")
    fun getTotalCoins(): Flow<Int>
}
