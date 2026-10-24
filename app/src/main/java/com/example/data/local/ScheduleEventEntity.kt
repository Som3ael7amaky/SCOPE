package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ClassOccurrence

@Entity(tableName = "schedule_events")
data class ScheduleEventEntity(
    @PrimaryKey val id: String,
    val subjectId: String = "",
    val title: String = "",
    val eventType: String = "RECURRING",
    val category: String = "ACADEMIC",
    val categoryColorHex: String = "#38BDF8",
    val dayOfWeek: Int = 1,
    val specificDate: String = "",
    val startTime: String = "09:00",
    val endTime: String = "10:30",
    val room: String = "القاعة 101",
    val audioUri: String = "",
    val hasAlarm: Boolean = true,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toClassOccurrence(): ClassOccurrence {
        return ClassOccurrence(
            id = id,
            subjectId = subjectId,
            subjectName = title,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            room = room,
            eventType = eventType,
            category = category,
            categoryColorHex = categoryColorHex,
            specificDate = specificDate,
            audioUri = audioUri,
            hasAlarm = hasAlarm,
            notes = notes
        )
    }

    companion object {
        fun fromClassOccurrence(occ: ClassOccurrence): ScheduleEventEntity {
            return ScheduleEventEntity(
                id = occ.id.ifBlank { java.util.UUID.randomUUID().toString() },
                subjectId = occ.subjectId,
                title = occ.subjectName,
                eventType = occ.eventType,
                category = occ.category,
                categoryColorHex = occ.categoryColorHex,
                dayOfWeek = occ.dayOfWeek,
                specificDate = occ.specificDate,
                startTime = occ.startTime,
                endTime = occ.endTime,
                room = occ.room,
                audioUri = occ.audioUri,
                hasAlarm = occ.hasAlarm,
                notes = occ.notes
            )
        }
    }
}
