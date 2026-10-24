package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    @Transaction
    @Query("SELECT * FROM courses ORDER BY name ASC")
    fun getCoursesWithNotes(): Flow<List<CourseWithNotes>>

    @Query("SELECT * FROM courses ORDER BY name ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM course_notes WHERE courseId = :courseId ORDER BY timestamp DESC")
    fun getNotesForCourse(courseId: Long): Flow<List<CourseNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseNote(note: CourseNoteEntity): Long

    @Update
    suspend fun updateCourseNote(note: CourseNoteEntity)

    @Delete
    suspend fun deleteCourseNote(note: CourseNoteEntity)

    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteCourseById(courseId: Long)

    @Query("DELETE FROM course_notes WHERE id = :noteId")
    suspend fun deleteCourseNoteById(noteId: Long)
}
