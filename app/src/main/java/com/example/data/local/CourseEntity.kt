package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String,
    val creditHours: Int = 3,
    val targetGrade: Double = 4.0,
    val currentGrade: Double = 4.0,
    val colorHex: String = "#38BDF8"
)
