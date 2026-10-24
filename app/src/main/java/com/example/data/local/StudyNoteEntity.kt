package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.StudyNote

@Entity(tableName = "study_notes")
data class StudyNoteEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val subjectId: String,
    val subjectName: String,
    val timestamp: Long
) {
    fun toStudyNote(): StudyNote = StudyNote(
        id = id,
        title = title,
        content = content,
        subjectId = subjectId,
        subjectName = subjectName,
        timestamp = timestamp
    )

    companion object {
        fun fromStudyNote(note: StudyNote): StudyNoteEntity = StudyNoteEntity(
            id = note.id,
            title = note.title,
            content = note.content,
            subjectId = note.subjectId,
            subjectName = note.subjectName,
            timestamp = note.timestamp
        )
    }
}
