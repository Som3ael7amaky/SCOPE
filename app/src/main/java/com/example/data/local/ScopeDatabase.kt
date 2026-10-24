package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        StudyNoteEntity::class,
        ScheduleEventEntity::class,
        CourseEntity::class,
        CourseNoteEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ScopeDatabase : RoomDatabase() {
    abstract fun studyNoteDao(): StudyNoteDao
    abstract fun scheduleEventDao(): ScheduleEventDao
    abstract fun courseDao(): CourseDao

    companion object {
        @Volatile
        private var INSTANCE: ScopeDatabase? = null

        fun getInstance(context: Context): ScopeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScopeDatabase::class.java,
                    "scope_local.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
