package com.example.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.file.QuizFileCodec
import com.example.data.file.QuizFileFormat
import com.example.data.file.QuizPdfExporter
import com.example.data.file.QuizResultExportData
import com.example.data.local.BundledQuestionBank
import com.example.data.local.QuestionDao
import com.example.data.local.QuestionEntity
import com.example.data.preferences.SettingsDataStore
import com.example.data.preferences.UserPreferences
import com.example.model.CategorySummary
import com.example.model.DifficultyLevel
import com.example.model.MascotBuddy
import com.example.model.QuizCategoryId
import com.example.model.QuizQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class QuizRepository(
    private val questionDao: QuestionDao,
    private val settingsDataStore: SettingsDataStore,
    private val appContext: Context
) {
    val userPreferences: Flow<UserPreferences> = settingsDataStore.preferencesFlow

    suspend fun ensureSeeded() {
        if (questionDao.countQuestions() == 0) {
            val bundled = withContext(Dispatchers.IO) {
                BundledQuestionBank.loadFromAssets(appContext)
            }
            questionDao.insertAll(bundled)
        }
    }

    fun observeCategorySummaries(difficulty: DifficultyLevel): Flow<List<CategorySummary>> {
        return questionDao.observeAllQuestions().map { entities ->
            val filtered = if (difficulty == DifficultyLevel.ALL) {
                entities
            } else {
                entities.filter { it.difficultyName == difficulty.name }
            }
            QuizCategoryId.playableCategories.map { category ->
                CategorySummary(
                    category = category,
                    questionCount = filtered.count { it.categoryName == category.name }
                )
            }
        }
    }

    suspend fun loadQuizSession(
        category: QuizCategoryId,
        difficulty: DifficultyLevel,
        maxQuestions: Int = 8
    ): List<QuizQuestion> {
        ensureSeeded()
        val raw = if (category == QuizCategoryId.DAILY_MIX) {
            questionDao.getAllQuestionsOnce()
        } else {
            questionDao.getQuestionsByCategory(category.name)
        }
        val filtered = if (difficulty == DifficultyLevel.ALL) {
            raw
        } else {
            raw.filter { it.difficultyName == difficulty.name }
        }
        val pool = if (filtered.isNotEmpty()) filtered else raw
        return pool.shuffled()
            .take(maxQuestions)
            .map { it.toDomain() }
    }

    suspend fun exportQuestionBankToUri(uri: Uri, format: QuizFileFormat): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                ensureSeeded()
                val all = questionDao.getAllQuestionsOnce()
                val content = when (format) {
                    QuizFileFormat.CSV -> QuizFileCodec.questionsToCsv(all)
                    else -> QuizFileCodec.questionsToJson(all)
                }
                appContext.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(content.toByteArray(Charsets.UTF_8))
                    stream.flush()
                } ?: error("Tidak dapat membuka lokasi file tujuan")
                all.size
            }
        }

    suspend fun parseQuestionsFromUri(uri: Uri): Result<List<QuestionEntity>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val rawText = appContext.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                } ?: error("Tidak dapat membaca file yang dipilih")
                val parsed = QuizFileCodec.parseQuestionsFromString(rawText)
                if (parsed.isEmpty()) {
                    error("Tidak ada soal valid yang ditemukan di dalam file.")
                }
                parsed
            }
        }

    suspend fun applyImportedQuestions(
        questions: List<QuestionEntity>,
        replaceExisting: Boolean
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            if (questions.isEmpty()) error("Daftar soal kosong")
            if (replaceExisting) {
                val normalized = questions.mapIndexed { index, q -> q.copy(id = index + 1) }
                questionDao.replaceAll(normalized)
                normalized.size
            } else {
                val startId = (questionDao.getMaxId() ?: 0) + 1
                val normalized = questions.mapIndexed { index, q -> q.copy(id = startId + index) }
                questionDao.insertAll(normalized)
                normalized.size
            }
        }
    }

    suspend fun resetToBundledQuestions(): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val bundled = BundledQuestionBank.loadFromAssets(appContext)
            questionDao.replaceAll(bundled)
            bundled.size
        }
    }

    suspend fun exportResultToUri(
        uri: Uri,
        data: QuizResultExportData,
        format: QuizFileFormat
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            appContext.contentResolver.openOutputStream(uri)?.use { stream ->
                when (format) {
                    QuizFileFormat.PDF -> QuizPdfExporter.writePdf(data, stream)
                    QuizFileFormat.JSON -> {
                        stream.write(QuizFileCodec.quizResultToJson(data).toByteArray(Charsets.UTF_8))
                        stream.flush()
                    }
                    QuizFileFormat.TXT, QuizFileFormat.CSV -> {
                        stream.write(QuizFileCodec.quizResultToText(data).toByteArray(Charsets.UTF_8))
                        stream.flush()
                    }
                }
            } ?: error("Tidak dapat menulis ke file tujuan")
        }
    }

    suspend fun createShareableResultFile(
        data: QuizResultExportData,
        format: QuizFileFormat
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val exportDir = File(appContext.cacheDir, "exports").apply { mkdirs() }
            val safeCategory = data.category.name.lowercase()
            val timestamp = QuizFileCodec.formatFileTimestamp(data.exportedAtMillis)
            val file = File(exportDir, "rapor_kuis_${safeCategory}_$timestamp.${format.extension}")
            file.outputStream().use { stream ->
                when (format) {
                    QuizFileFormat.PDF -> QuizPdfExporter.writePdf(data, stream)
                    QuizFileFormat.JSON -> {
                        stream.write(QuizFileCodec.quizResultToJson(data).toByteArray(Charsets.UTF_8))
                        stream.flush()
                    }
                    QuizFileFormat.TXT, QuizFileFormat.CSV -> {
                        stream.write(QuizFileCodec.quizResultToText(data).toByteArray(Charsets.UTF_8))
                        stream.flush()
                    }
                }
            }
            FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                file
            )
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) = settingsDataStore.setSoundEnabled(enabled)
    suspend fun setMusicEnabled(enabled: Boolean) = settingsDataStore.setMusicEnabled(enabled)
    suspend fun setAutoReadTts(enabled: Boolean) = settingsDataStore.setAutoReadTts(enabled)
    suspend fun setDifficulty(level: DifficultyLevel) = settingsDataStore.setDifficulty(level)
    suspend fun setMascot(mascot: MascotBuddy) = settingsDataStore.setMascot(mascot)
}
