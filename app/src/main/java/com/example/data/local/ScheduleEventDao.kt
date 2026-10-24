package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleEventDao {
    @Query("SELECT * FROM schedule_events ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllEvents(): Flow<List<ScheduleEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(event: ScheduleEventEntity)

    @Query("DELETE FROM schedule_events WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM schedule_events")
    suspend fun clearAll()
}
