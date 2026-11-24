package com.example.data.file

import com.example.data.local.QuestionEntity
import com.example.model.AnsweredQuestion
import com.example.model.DifficultyLevel
import com.example.model.QuizCategoryId
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class QuizFileFormat(val extension: String, val mimeType: String, val label: String) {
    PDF("pdf", "application/pdf", "Dokumen PDF"),
    JSON("json", "application/json", "File JSON"),
    TXT("txt", "text/plain", "Catatan Teks (.txt)"),
    CSV("csv", "text/csv", "Tabel CSV (.csv)")
}

data class QuizResultExportData(
    val category: QuizCategoryId,
    val totalQuestions: Int,
    val correctCount: Int,
    val earnedPoints: Int,
    val starRating: Int,
    val answeredHistory: List<AnsweredQuestion>,
    val exportedAtMillis: Long = System.currentTimeMillis()
)

object QuizFileCodec {

    private val csvHeader = listOf(
        "id",
        "category",
        "difficulty",
        "prompt",
        "emojiCue",
        "optionA",
        "optionB",
        "optionC",
        "optionD",
        "correctIndex",
        "funFact"
    )

    fun questionsToJson(questions: List<QuestionEntity>, timestampMillis: Long = System.currentTimeMillis()): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("app", "Kuis Ceria Anak")
        root.put("exportedAt", formatIsoDate(timestampMillis))
        root.put("totalQuestions", questions.size)

        val array = JSONArray()
        questions.forEach { q ->
            val obj = JSONObject().apply {
                put("id", q.id)
                put("category", q.categoryName)
                put("difficulty", q.difficultyName)
                put("prompt", q.prompt)
                put("emojiCue", q.emojiCue)
                put("options", JSONArray().apply {
                    put(q.optionA)
                    put(q.optionB)
                    put(q.optionC)
                    put(q.optionD)
                })
                put("correctIndex", q.correctIndex)
                put("funFact", q.funFact)
            }
            array.put(obj)
        }
        root.put("questions", array)
        return root.toString(2)
    }

    fun questionsToCsv(questions: List<QuestionEntity>): String {
        val sb = StringBuilder()
        sb.appendLine(csvHeader.joinToString(",") { escapeCsv(it) })
        questions.forEach { q ->
            val row = listOf(
                q.id.toString(),
                q.categoryName,
                q.difficultyName,
                q.prompt,
                q.emojiCue,
                q.optionA,
                q.optionB,
                q.optionC,
                q.optionD,
                q.correctIndex.toString(),
                q.funFact
            )
            sb.appendLine(row.joinToString(",") { escapeCsv(it) })
        }
        return sb.toString()
    }

    fun parseQuestionsFromString(rawText: String): List<QuestionEntity> {
        val trimmed = rawText.trim().removePrefix("\uFEFF")
        if (trimmed.isEmpty()) return emptyList()

        return if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            parseQuestionsFromJson(trimmed)
        } else {
            parseQuestionsFromCsv(trimmed)
        }
    }

    fun parseQuestionsFromJson(jsonText: String): List<QuestionEntity> {
        val trimmed = jsonText.trim().removePrefix("\uFEFF")
        val array: JSONArray = when {
            trimmed.startsWith("{") -> {
                val root = JSONObject(trimmed)
                root.optJSONArray("questions") ?: JSONArray()
            }
            trimmed.startsWith("[") -> JSONArray(trimmed)
            else -> throw IllegalArgumentException("Format JSON tidak dikenali")
        }

        val result = mutableListOf<QuestionEntity>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val prompt = obj.optString("prompt", "").trim()
            if (prompt.isEmpty()) continue

            val optionsArray = obj.optJSONArray("options")
            val optA: String
            val optB: String
            val optC: String
            val optD: String
            if (optionsArray != null && optionsArray.length() >= 4) {
                optA = optionsArray.optString(0, "").trim()
                optB = optionsArray.optString(1, "").trim()
                optC = optionsArray.optString(2, "").trim()
                optD = optionsArray.optString(3, "").trim()
            } else {
                optA = obj.optString("optionA", "").trim()
                optB = obj.optString("optionB", "").trim()
                optC = obj.optString("optionC", "").trim()
                optD = obj.optString("optionD", "").trim()
            }
            if (optA.isEmpty() || optB.isEmpty() || optC.isEmpty() || optD.isEmpty()) continue

            val rawCategory = obj.optString("category", obj.optString("categoryName", QuizCategoryId.ANIMALS.name))
            val category = parseCategory(rawCategory)
            val rawDifficulty = obj.optString("difficulty", obj.optString("difficultyName", DifficultyLevel.EASY.name))
            val difficulty = parseDifficulty(rawDifficulty)
            val correctIndex = obj.optInt("correctIndex", 0).coerceIn(0, 3)
            val emojiCue = obj.optString("emojiCue", category.emoji).ifBlank { category.emoji }
            val funFact = obj.optString("funFact", "Terus semangat belajar hal-hal baru setiap hari!")
                .ifBlank { "Terus semangat belajar hal-hal baru setiap hari!" }
            val id = obj.optInt("id", i + 1).let { if (it > 0) it else i + 1 }

            result.add(
                QuestionEntity(
                    id = id,
                    categoryName = category.name,
                    difficultyName = difficulty.name,
                    prompt = prompt,
                    emojiCue = emojiCue,
                    optionA = optA,
                    optionB = optB,
                    optionC = optC,
                    optionD = optD,
                    correctIndex = correctIndex,
                    funFact = funFact
                )
            )
        }
        return result
    }

    fun parseQuestionsFromCsv(csvText: String): List<QuestionEntity> {
        val rows = parseCsvRows(csvText)
        if (rows.isEmpty()) return emptyList()

        val firstRowLower = rows.first().map { it.trim().lowercase(Locale.ROOT) }
        val hasHeader = firstRowLower.contains("prompt") || firstRowLower.contains("category")
        val dataRows = if (hasHeader) rows.drop(1) else rows

        val result = mutableListOf<QuestionEntity>()
        dataRows.forEachIndexed { idx, cols ->
            if (cols.size < 9) return@forEachIndexed
            // Support 11 columns (with id) or 10 columns (without id)
            val offset = if (cols.size >= 11 && cols[0].trim().toIntOrNull() != null) 1 else 0
            val id = if (offset == 1) cols[0].trim().toIntOrNull() ?: (idx + 1) else (idx + 1)

            val category = parseCategory(cols.getOrElse(offset) { "" })
            val difficulty = parseDifficulty(cols.getOrElse(offset + 1) { "" })
            val prompt = cols.getOrElse(offset + 2) { "" }.trim()
            val emojiCue = cols.getOrElse(offset + 3) { "" }.trim().ifBlank { category.emoji }
            val optA = cols.getOrElse(offset + 4) { "" }.trim()
            val optB = cols.getOrElse(offset + 5) { "" }.trim()
            val optC = cols.getOrElse(offset + 6) { "" }.trim()
            val optD = cols.getOrElse(offset + 7) { "" }.trim()
            val correctIndex = cols.getOrElse(offset + 8) { "0" }.trim().toIntOrNull()?.coerceIn(0, 3) ?: 0
            val funFact = cols.getOrElse(offset + 9) { "" }.trim()
                .ifBlank { "Terus semangat belajar hal-hal baru setiap hari!" }

            if (prompt.isNotEmpty() && optA.isNotEmpty() && optB.isNotEmpty() && optC.isNotEmpty() && optD.isNotEmpty()) {
                result.add(
                    QuestionEntity(
                        id = id,
                        categoryName = category.name,
                        difficultyName = difficulty.name,
                        prompt = prompt,
                        emojiCue = emojiCue,
                        optionA = optA,
                        optionB = optB,
                        optionC = optC,
                        optionD = optD,
                        correctIndex = correctIndex,
                        funFact = funFact
                    )
                )
            }
        }
        return result
    }

    fun quizResultToText(data: QuizResultExportData): String {
        val accuracy = if (data.totalQuestions > 0) (data.correctCount * 100) / data.totalQuestions else 0
        val stars = "★".repeat(data.starRating) + "☆".repeat((3 - data.starRating).coerceAtLeast(0))
        val sb = StringBuilder()
        sb.appendLine("============================================")
        sb.appendLine("       RAPOR KUIS CERIA ANAK INDONESIA      ")
        sb.appendLine("============================================")
        sb.appendLine("Kategori   : ${data.category.title}")
        sb.appendLine("Tanggal    : ${formatReadableDate(data.exportedAtMillis)}")
        sb.appendLine("Bintang    : $stars (${data.starRating}/3)")
        sb.appendLine("Benar      : ${data.correctCount} dari ${data.totalQuestions} soal")
        sb.appendLine("Akurasi    : $accuracy%")
        sb.appendLine("Skor Poin  : +${data.earnedPoints} bintang")
        sb.appendLine("--------------------------------------------")
        sb.appendLine("ULASAN JAWABAN:")
        sb.appendLine()

        data.answeredHistory.forEachIndexed { index, item ->
            val userText = item.question.options.getOrElse(item.selectedIndex) { "-" }
            val correctText = item.question.options.getOrElse(item.question.correctIndex) { "-" }
            val status = if (item.isCorrect) "[BENAR]" else "[PERLU BELAJAR]"
            sb.appendLine("${index + 1}. ${item.question.prompt}")
            sb.appendLine("   Status        : $status")
            sb.appendLine("   Jawabanmu     : $userText")
            if (!item.isCorrect) {
                sb.appendLine("   Jawaban Benar : $correctText")
            }
            sb.appendLine("   Tahukah Kamu? : ${item.question.funFact}")
            sb.appendLine()
        }
        sb.appendLine("============================================")
        sb.appendLine("Tetap semangat belajar sambil bermain!")
        return sb.toString()
    }

    fun quizResultToJson(data: QuizResultExportData): String {
        val accuracy = if (data.totalQuestions > 0) (data.correctCount * 100) / data.totalQuestions else 0
        val root = JSONObject().apply {
            put("app", "Kuis Ceria Anak")
            put("exportedAt", formatIsoDate(data.exportedAtMillis))
            put("category", data.category.name)
            put("categoryTitle", data.category.title)
            put("totalQuestions", data.totalQuestions)
            put("correctCount", data.correctCount)
            put("accuracyPercent", accuracy)
            put("earnedPoints", data.earnedPoints)
            put("starRating", data.starRating)
            put("answers", JSONArray().apply {
                data.answeredHistory.forEachIndexed { index, item ->
                    put(JSONObject().apply {
                        put("number", index + 1)
                        put("questionId", item.question.id)
                        put("prompt", item.question.prompt)
                        put("selectedIndex", item.selectedIndex)
                        put("selectedAnswer", item.question.options.getOrElse(item.selectedIndex) { "" })
                        put("correctIndex", item.question.correctIndex)
                        put("correctAnswer", item.question.options.getOrElse(item.question.correctIndex) { "" })
                        put("isCorrect", item.isCorrect)
                        put("funFact", item.question.funFact)
                    })
                }
            })
        }
        return root.toString(2)
    }

    private fun parseCategory(raw: String): QuizCategoryId {
        val norm = raw.trim().uppercase(Locale.ROOT)
        QuizCategoryId.playableCategories.firstOrNull { it.name == norm }?.let { return it }
        return QuizCategoryId.playableCategories.firstOrNull {
            it.title.equals(raw.trim(), ignoreCase = true)
        } ?: QuizCategoryId.ANIMALS
    }

    private fun parseDifficulty(raw: String): DifficultyLevel {
        val norm = raw.trim().uppercase(Locale.ROOT)
        DifficultyLevel.entries.firstOrNull { it.name == norm }?.let { return it }
        return when {
            norm.contains("MUDAH") || norm.contains("5") -> DifficultyLevel.EASY
            norm.contains("SERU") || norm.contains("SULIT") || norm.contains("8") -> DifficultyLevel.CHALLENGING
            else -> DifficultyLevel.EASY
        }
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')
        if (!needsQuotes) return value
        return "\"" + value.replace("\"", "\"\"") + "\""
    }

    private fun parseCsvRows(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val currentRow = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < text.length) {
            val c = text[i]
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < text.length && text[i + 1] == '"') {
                        currentField.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    currentField.append(c)
                }
            } else {
                when (c) {
                    '"' -> inQuotes = true
                    ',' -> {
                        currentRow.add(currentField.toString())
                        currentField.clear()
                    }
                    '\r' -> {
                        if (i + 1 < text.length && text[i + 1] == '\n') i++
                        currentRow.add(currentField.toString())
                        currentField.clear()
                        if (currentRow.any { it.isNotBlank() }) rows.add(currentRow.toList())
                        currentRow.clear()
                    }
                    '\n' -> {
                        currentRow.add(currentField.toString())
                        currentField.clear()
                        if (currentRow.any { it.isNotBlank() }) rows.add(currentRow.toList())
                        currentRow.clear()
                    }
                    else -> currentField.append(c)
                }
            }
            i++
        }
        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString())
            if (currentRow.any { it.isNotBlank() }) rows.add(currentRow.toList())
        }
        return rows
    }

    private fun formatIsoDate(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        return sdf.format(Date(millis))
    }

    fun formatReadableDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
        return sdf.format(Date(millis))
    }

    fun formatFileTimestamp(millis: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)
        return sdf.format(Date(millis))
    }
}
