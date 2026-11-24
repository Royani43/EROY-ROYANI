package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY id ASC")
    fun observeAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE categoryName = :categoryName ORDER BY id ASC")
    suspend fun getQuestionsByCategory(categoryName: String): List<QuestionEntity>

    @Query("SELECT * FROM questions ORDER BY id ASC")
    suspend fun getAllQuestionsOnce(): List<QuestionEntity>

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun countQuestions(): Int

    @Query("SELECT MAX(id) FROM questions")
    suspend fun getMaxId(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Query("DELETE FROM questions")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(questions: List<QuestionEntity>) {
        deleteAll()
        insertAll(questions)
    }
}
