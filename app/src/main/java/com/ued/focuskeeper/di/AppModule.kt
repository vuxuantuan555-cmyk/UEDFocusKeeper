package com.ued.focuskeeper.di

import android.content.Context
import androidx.room.Room
import com.ued.focuskeeper.data.db.FocusKeeperDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FocusKeeperDatabase =
        Room.databaseBuilder(
            context,
            FocusKeeperDatabase::class.java,
            "focus_keeper_db"
        )
            .addMigrations(FocusKeeperDatabase.MIGRATION_1_2)
            .build()
}
