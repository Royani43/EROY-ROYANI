package com.example.data.file

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.OutputStream

object QuizPdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f
    private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2

    fun writePdf(data: QuizResultExportData, outputStream: OutputStream) {
        val wroteNative = runCatching {
            writeWithAndroidPdfDocument(data, outputStream)
        }.isSuccess
        if (!wroteNative) {
            writeFallbackPdf(data, outputStream)
        }
    }

    private fun writeWithAndroidPdfDocument(data: QuizResultExportData, outputStream: OutputStream) {
        val document = PdfDocument()
        try {
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            var y = MARGIN

            val bgPaint = Paint().apply {
                color = Color.rgb(255, 251, 245)
                style = Paint.Style.FILL
            }
            val headerPaint = Paint().apply {
                color = Color.rgb(255, 107, 53)
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val cardBgPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val borderPaint = Paint().apply {
                color = Color.rgb(229, 217, 200)
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
                isAntiAlias = true
            }
            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val subtitlePaint = Paint().apply {
                color = Color.argb(235, 255, 255, 255)
                textSize = 11.5f
                isAntiAlias = true
            }
            val sectionPaint = Paint().apply {
                color = Color.rgb(35, 31, 48)
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val bodyPaint = Paint().apply {
                color = Color.rgb(55, 50, 70)
                textSize = 11f
                isAntiAlias = true
            }
            val boldBodyPaint = Paint().apply {
                color = Color.rgb(35, 31, 48)
                textSize = 11.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val mutedPaint = Paint().apply {
                color = Color.rgb(110, 102, 124)
                textSize = 10f
                isAntiAlias = true
            }
            val successPaint = Paint().apply {
                color = Color.rgb(16, 155, 104)
                textSize = 10.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val errorPaint = Paint().apply {
                color = Color.rgb(220, 53, 86)
                textSize = 10.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            fun startNewPage() {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)
                y = MARGIN
            }

            // First page background
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

            // Header banner
            val headerRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 86f)
            canvas.drawRoundRect(headerRect, 18f, 18f, headerPaint)
            canvas.drawText("RAPOR KUIS CERIA ANAK", MARGIN + 20f, y + 34f, titlePaint)
            canvas.drawText(
                "Kategori: ${data.category.title}  •  ${QuizFileCodec.formatReadableDate(data.exportedAtMillis)}",
                MARGIN + 20f,
                y + 56f,
                subtitlePaint
            )
            val starsLabel = "Bintang: " + "★".repeat(data.starRating) + "☆".repeat((3 - data.starRating).coerceAtLeast(0))
            canvas.drawText(starsLabel, MARGIN + 20f, y + 74f, subtitlePaint)
            y += 104f

            // Summary card
            val accuracy = if (data.totalQuestions > 0) (data.correctCount * 100) / data.totalQuestions else 0
            val summaryRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 66f)
            canvas.drawRoundRect(summaryRect, 14f, 14f, cardBgPaint)
            canvas.drawRoundRect(summaryRect, 14f, 14f, borderPaint)

            val colWidth = CONTENT_WIDTH / 3f
            drawStatColumn(canvas, MARGIN, y + 26f, colWidth, "${data.correctCount}/${data.totalQuestions}", "Jawaban Benar", successPaint, mutedPaint)
            drawStatColumn(canvas, MARGIN + colWidth, y + 26f, colWidth, "+${data.earnedPoints}", "Poin Bintang", boldBodyPaint, mutedPaint)
            drawStatColumn(canvas, MARGIN + colWidth * 2f, y + 26f, colWidth, "$accuracy%", "Akurasi", boldBodyPaint, mutedPaint)
            y += 86f

            // Review header
            canvas.drawText("Ulasan Jawaban & Fakta Seru", MARGIN, y, sectionPaint)
            y += 16f

            data.answeredHistory.forEachIndexed { idx, item ->
                val promptLines = wrapText("${idx + 1}. ${item.question.prompt}", boldBodyPaint, CONTENT_WIDTH - 24f)
                val userText = item.question.options.getOrElse(item.selectedIndex) { "-" }
                val correctText = item.question.options.getOrElse(item.question.correctIndex) { "-" }
                val funFactLines = wrapText("Tahukah Kamu? ${item.question.funFact}", mutedPaint, CONTENT_WIDTH - 24f)

                val statusLinesCount = if (item.isCorrect) 1 else 2
                val cardHeight = 22f + (promptLines.size * 15f) + (statusLinesCount * 15f) + (funFactLines.size * 14f) + 12f

                if (y + cardHeight > PAGE_HEIGHT - MARGIN) {
                    startNewPage()
                }

                val itemRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + cardHeight)
                canvas.drawRoundRect(itemRect, 12f, 12f, cardBgPaint)
                canvas.drawRoundRect(itemRect, 12f, 12f, borderPaint)

                var lineY = y + 20f
                promptLines.forEach { line ->
                    canvas.drawText(line, MARGIN + 12f, lineY, boldBodyPaint)
                    lineY += 15f
                }

                if (item.isCorrect) {
                    canvas.drawText("✓ Jawabanmu: $userText (Benar)", MARGIN + 12f, lineY, successPaint)
                    lineY += 15f
                } else {
                    canvas.drawText("✗ Jawabanmu: $userText", MARGIN + 12f, lineY, errorPaint)
                    lineY += 15f
                    canvas.drawText("✓ Jawaban benar: $correctText", MARGIN + 12f, lineY, successPaint)
                    lineY += 15f
                }

                funFactLines.forEach { line ->
                    canvas.drawText(line, MARGIN + 12f, lineY, mutedPaint)
                    lineY += 14f
                }

                y += cardHeight + 10f
            }

            document.finishPage(page)
            document.writeTo(outputStream)
            outputStream.flush()
        } finally {
            document.close()
        }
    }

    private fun drawStatColumn(
        canvas: Canvas,
        startX: Float,
        baselineY: Float,
        width: Float,
        value: String,
        label: String,
        valuePaint: Paint,
        labelPaint: Paint
    ) {
        val valWidth = valuePaint.measureText(value)
        canvas.drawText(value, startX + (width - valWidth) / 2f, baselineY, valuePaint)
        val lblWidth = labelPaint.measureText(label)
        canvas.drawText(label, startX + (width - lblWidth) / 2f, baselineY + 18f, labelPaint)
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var current = StringBuilder()

        for (word in words) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current = StringBuilder(candidate)
            } else {
                if (current.isNotEmpty()) lines.add(current.toString())
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return if (lines.isEmpty()) listOf("") else lines
    }

    private fun writeFallbackPdf(data: QuizResultExportData, outputStream: OutputStream) {
        val textLines = QuizFileCodec.quizResultToText(data)
            .lines()
            .flatMap { line -> line.chunked(78).ifEmpty { listOf("") } }
            .take(46)

        val contentStream = buildString {
            appendLine("BT")
            appendLine("/F1 11 Tf")
            appendLine("40 790 Td")
            appendLine("15 TL")
            textLines.forEachIndexed { idx, line ->
                val sanitized = line
                    .replace("★", "*")
                    .replace("☆", "-")
                    .replace("\\", "\\\\")
                    .replace("(", "\\(")
                    .replace(")", "\\)")
                    .replace(Regex("[^\\x20-\\x7E]"), " ")
                if (idx == 0) {
                    appendLine("($sanitized) Tj")
                } else {
                    appendLine("T* ($sanitized) Tj")
                }
            }
            appendLine("ET")
        }

        val objects = listOf(
            "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n",
            "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n",
            "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n",
            "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\nendobj\n",
            "5 0 obj\n<< /Length ${contentStream.toByteArray(Charsets.US_ASCII).size} >>\nstream\n$contentStream\nendstream\nendobj\n"
        )

        val pdfBuilder = StringBuilder("%PDF-1.4\n")
        val offsets = mutableListOf(0)
        objects.forEach { obj ->
            offsets.add(pdfBuilder.toString().toByteArray(Charsets.US_ASCII).size)
            pdfBuilder.append(obj)
        }
        val xrefStart = pdfBuilder.toString().toByteArray(Charsets.US_ASCII).size
        pdfBuilder.append("xref\n0 ${objects.size + 1}\n")
        pdfBuilder.append("0000000000 65535 f \n")
        offsets.drop(1).forEach { offset ->
            pdfBuilder.append(String.format(java.util.Locale.US, "%010d 00000 n \n", offset))
        }
        pdfBuilder.append("trailer\n<< /Size ${objects.size + 1} /Root 1 0 R >>\n")
        pdfBuilder.append("startxref\n$xrefStart\n%%EOF\n")

        outputStream.write(pdfBuilder.toString().toByteArray(Charsets.US_ASCII))
        outputStream.flush()
    }
}
