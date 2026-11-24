package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.file.QuizFileCodec
import com.example.data.file.QuizPdfExporter
import com.example.data.file.QuizResultExportData
import com.example.data.local.BundledQuestionBank
import com.example.model.AnsweredQuestion
import com.example.model.QuizCategoryId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Kuis Ceria Anak", appName)
    }

    @Test
    fun `bundled json asset loads all 60 questions`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val fromAsset = BundledQuestionBank.loadFromAssets(context)
        assertEquals(60, fromAsset.size)
        assertEquals(BundledQuestionBank.allQuestions.map { it.prompt }, fromAsset.map { it.prompt })
    }

    @Test
    fun `question bank json and csv round trip works`() {
        val sample = BundledQuestionBank.allQuestions.take(6)

        val json = QuizFileCodec.questionsToJson(sample)
        val parsedJson = QuizFileCodec.parseQuestionsFromString(json)
        assertEquals(sample.size, parsedJson.size)
        assertEquals(sample.map { it.prompt }, parsedJson.map { it.prompt })

        val csv = QuizFileCodec.questionsToCsv(sample)
        val parsedCsv = QuizFileCodec.parseQuestionsFromString(csv)
        assertEquals(sample.size, parsedCsv.size)
        assertEquals(sample.map { it.funFact }, parsedCsv.map { it.funFact })
    }

    @Test
    fun `result export generates valid pdf txt and json`() {
        val sampleQuestion = BundledQuestionBank.allQuestions.first().toDomain()
        val exportData = QuizResultExportData(
            category = QuizCategoryId.ANIMALS,
            totalQuestions = 1,
            correctCount = 1,
            earnedPoints = 10,
            starRating = 3,
            answeredHistory = listOf(
                AnsweredQuestion(
                    question = sampleQuestion,
                    selectedIndex = sampleQuestion.correctIndex,
                    isCorrect = true
                )
            )
        )

        val txt = QuizFileCodec.quizResultToText(exportData)
        assertTrue(txt.contains("RAPOR KUIS CERIA ANAK"))
        assertTrue(txt.contains(sampleQuestion.prompt))

        val json = QuizFileCodec.quizResultToJson(exportData)
        assertTrue(json.contains("\"accuracyPercent\": 100"))

        val pdfBytes = ByteArrayOutputStream().use { out ->
            QuizPdfExporter.writePdf(exportData, out)
            out.toByteArray()
        }
        assertTrue(pdfBytes.size > 500)
        val header = String(pdfBytes.copyOfRange(0, 4), Charsets.US_ASCII)
        assertEquals("%PDF", header)
    }

    @Test
    fun `kids theme music raw resource and launcher icon art exist`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val header = context.resources.openRawResource(R.raw.kids_theme).use { stream ->
            ByteArray(4).also { stream.read(it) }
        }
        assertEquals("RIFF", String(header, Charsets.US_ASCII))
        assertTrue(R.drawable.ic_launcher_art != 0)
    }
}
