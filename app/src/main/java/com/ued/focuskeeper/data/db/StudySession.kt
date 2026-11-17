package com.ued.focuskeeper.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val course_name: String,
    val duration_minutes: Int,
    val completion_date: Long,
    val points: Int = 0
)