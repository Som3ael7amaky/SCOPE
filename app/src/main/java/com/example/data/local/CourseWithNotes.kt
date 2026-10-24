package com.example.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class CourseWithNotes(
    @Embedded val course: CourseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "courseId"
    )
    val notes: List<CourseNoteEntity>
)
