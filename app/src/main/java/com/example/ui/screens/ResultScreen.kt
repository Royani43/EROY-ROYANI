package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.file.QuizFileCodec
import com.example.data.file.QuizFileFormat
import com.example.model.AnsweredQuestion
import com.example.ui.components.ConfettiCelebrationOverlay
import com.example.ui.components.TactileButton
import com.example.ui.theme.BerryError
import com.example.ui.theme.BerryErrorContainer
import com.example.ui.theme.CoralLip
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.MintSuccessContainer
import com.example.ui.theme.SkyLip
import com.example.ui.theme.SkySecondary
import com.example.ui.theme.SunTertiary
import com.example.ui.viewmodel.QuizUiState
import com.example.ui.viewmodel.ResultExportUiState

@Composable
fun ResultScreen(
    state: QuizUiState.Completed,
    exportState: ResultExportUiState,
    onExportResultToUri: (Uri, QuizFileFormat) -> Unit,
    onPrepareShareFile: (QuizFileFormat, (Uri) -> Unit) -> Unit,
    onClearExportStatus: () -> Unit,
    onPlayAgain: () -> Unit,
    onExitToHome: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val safeCategoryName = state.category.name.lowercase()

    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(QuizFileFormat.PDF.mimeType)
    ) { uri ->
        if (uri != null) onExportResultToUri(uri, QuizFileFormat.PDF)
    }

    val saveTxtLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(QuizFileFormat.TXT.mimeType)
    ) { uri ->
        if (uri != null) onExportResultToUri(uri, QuizFileFormat.TXT)
    }

    val saveJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(QuizFileFormat.JSON.mimeType)
    ) { uri ->
        if (uri != null) onExportResultToUri(uri, QuizFileFormat.JSON)
    }

    LaunchedEffect(exportState.statusMessage) {
        val msg = exportState.statusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        onClearExportStatus()
    }

    val accuracyPct = if (state.totalQuestions > 0) {
        (state.correctCount * 100) / state.totalQuestions
    } else 0

    val headline = when (state.starRating) {
        3 -> "Juara Hebat!"
        2 -> "Keren Sekali!"
        else -> "Semangat Terus!"
    }
    val subMessage = when (state.starRating) {
        3 -> "Kamu menjawab hampir semua soal dengan tepat!"
        2 -> "Bagus! Sedikit lagi kamu meraih 3 bintang emas!"
        else -> "Belajar sambil bermain itu seru, yuk coba sekali lagi!"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Celebration Header Card
                item {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(2.dp, SunTertiary.copy(alpha = 0.45f)),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("result_summary_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 1..3) {
                                    val filled = i <= state.starRating
                                    Icon(
                                        imageVector = if (filled) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                                        contentDescription = if (filled) "Bintang Emas" else "Bintang Kosong",
                                        tint = if (filled) SunTertiary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(if (i == 2) 56.dp else 44.dp)
                                    )
                                }
                            }

                            Text(
                                text = headline,
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = subMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatPill(
                                    label = "Benar",
                                    value = "${state.correctCount}/${state.totalQuestions}",
                                    bg = MintSuccessContainer,
                                    ink = MintSuccess,
                                    modifier = Modifier.weight(1f)
                                )
                                StatPill(
                                    label = "Bintang",
                                    value = "+${state.earnedPoints}",
                                    bg = MaterialTheme.colorScheme.tertiaryContainer,
                                    ink = SunTertiary,
                                    modifier = Modifier.weight(1f)
                                )
                                StatPill(
                                    label = "Akurasi",
                                    value = "$accuracyPct%",
                                    bg = MaterialTheme.colorScheme.secondaryContainer,
                                    ink = SkySecondary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Export Result to File Card
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.5.dp, SkySecondary.copy(alpha = 0.35f)),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("result_export_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.export_result_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.export_result_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (exportState.isExporting) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val ts = QuizFileCodec.formatFileTimestamp()
                                        savePdfLauncher.launch("rapor_kuis_${safeCategoryName}_$ts.pdf")
                                    },
                                    enabled = !exportState.isExporting,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MintSuccess,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .testTag("result_export_pdf_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PictureAsPdf,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = stringResource(R.string.save_pdf_button))
                                }

                                OutlinedButton(
                                    onClick = {
                                        val ts = QuizFileCodec.formatFileTimestamp()
                                        saveTxtLauncher.launch("rapor_kuis_${safeCategoryName}_$ts.txt")
                                    },
                                    enabled = !exportState.isExporting,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .testTag("result_export_txt_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Description,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = stringResource(R.string.save_txt_button))
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val ts = QuizFileCodec.formatFileTimestamp()
                                        saveJsonLauncher.launch("rapor_kuis_${safeCategoryName}_$ts.json")
                                    },
                                    enabled = !exportState.isExporting,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .testTag("result_export_json_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Code,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = stringResource(R.string.save_json_button))
                                }

                                OutlinedButton(
                                    onClick = {
                                        onPrepareShareFile(QuizFileFormat.PDF) { uri ->
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = QuizFileFormat.PDF.mimeType
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                putExtra(
                                                    Intent.EXTRA_SUBJECT,
                                                    "Rapor Kuis Ceria Anak - ${state.category.title}"
                                                )
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(
                                                Intent.createChooser(shareIntent, "Bagikan File Rapor PDF")
                                            )
                                        }
                                    },
                                    enabled = !exportState.isExporting,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .testTag("result_share_file_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = stringResource(R.string.share_pdf_button))
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        TactileButton(
                            onClick = onPlayAgain,
                            containerColor = CoralPrimary,
                            lipColor = CoralLip,
                            tag = "result_play_again_button",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Text(
                                    text = stringResource(R.string.play_again),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                            }
                        }

                        TactileButton(
                            onClick = onExitToHome,
                            containerColor = SkySecondary,
                            lipColor = SkyLip,
                            tag = "result_home_button",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Home,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Text(
                                    text = stringResource(R.string.back_to_home),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Review Section Header
                item {
                    Text(
                        text = stringResource(R.string.review_answers_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                items(state.answeredHistory) { item ->
                    ReviewAnswerCard(item = item)
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (state.starRating >= 2) {
                ConfettiCelebrationOverlay()
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    bg: Color,
    ink: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = bg,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = ink,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReviewAnswerCard(item: AnsweredQuestion) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.5.dp,
            if (item.isCorrect) MintSuccess.copy(alpha = 0.4f) else BerryError.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (item.isCorrect) MintSuccessContainer else BerryErrorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = item.question.emojiCue, fontSize = 20.sp)
                }
                Text(
                    text = item.question.prompt,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (item.isCorrect) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Benar",
                        tint = MintSuccess
                    )
                }
            }

            val correctText = item.question.options[item.question.correctIndex]
            val userText = item.question.options.getOrElse(item.selectedIndex) { "-" }

            if (!item.isCorrect) {
                Text(
                    text = "Jawabanmu: $userText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BerryError
                )
            }
            Text(
                text = "Jawaban benar: $correctText",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MintSuccess
            )
            Text(
                text = item.question.funFact,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
