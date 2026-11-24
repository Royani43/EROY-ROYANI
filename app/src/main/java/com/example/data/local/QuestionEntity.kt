package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.DifficultyLevel
import com.example.model.QuizCategoryId
import com.example.model.QuizQuestion

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: Int,
    val categoryName: String,
    val difficultyName: String,
    val prompt: String,
    val emojiCue: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctIndex: Int,
    val funFact: String
) {
    fun toDomain(): QuizQuestion = QuizQuestion(
        id = id,
        category = QuizCategoryId.fromName(categoryName),
        difficulty = DifficultyLevel.fromName(difficultyName),
        prompt = prompt,
        emojiCue = emojiCue,
        options = listOf(optionA, optionB, optionC, optionD),
        correctIndex = correctIndex,
        funFact = funFact
    )
}
