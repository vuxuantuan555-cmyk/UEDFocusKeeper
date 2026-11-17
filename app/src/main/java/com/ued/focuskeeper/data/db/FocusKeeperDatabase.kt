package com.ued.focuskeeper.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [StudySession::class], version = 2, exportSchema = false)
abstract class FocusKeeperDatabase : RoomDatabase() {
    abstract fun studySessionDao(): StudySessionDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE study_sessions ADD COLUMN points INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}