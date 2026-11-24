package com.example

import com.example.data.local.BundledQuestionBank
import com.example.model.QuizCategoryId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun bundledQuestionBank_hasAllCategoriesAndValidAnswers() {
        val questions = BundledQuestionBank.allQuestions
        assertEquals(60, questions.size)

        QuizCategoryId.playableCategories.forEach { category ->
            val count = questions.count { it.categoryName == category.name }
            assertTrue("Category $category should have 10 questions", count == 10)
        }

        questions.forEach { entity ->
            val domain = entity.toDomain()
            assertEquals(4, domain.options.size)
            assertTrue(domain.correctIndex in 0..3)
            assertTrue(domain.prompt.isNotBlank())
            assertTrue(domain.funFact.isNotBlank())
        }
    }
}
