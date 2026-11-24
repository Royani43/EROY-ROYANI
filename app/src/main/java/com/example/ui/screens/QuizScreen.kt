package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.TactileButton
import com.example.ui.theme.BerryError
import com.example.ui.theme.BerryErrorContainer
import com.example.ui.theme.BerryErrorOnContainer
import com.example.ui.theme.CoralLip
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.MintSuccessContainer
import com.example.ui.theme.MintSuccessLip
import com.example.ui.theme.MintSuccessOnContainer
import com.example.ui.theme.SkyLip
import com.example.ui.theme.SkySecondary
import com.example.ui.theme.SunTertiary
import android.net.Uri
import com.example.data.file.QuizFileFormat
import com.example.ui.viewmodel.QuizUiState
import com.example.ui.viewmodel.ResultExportUiState

@Composable
fun QuizScreen(
    uiState: QuizUiState,
    exportState: ResultExportUiState,
    isSpeaking: Boolean,
    onSelectAnswer: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onToggleReadAloud: () -> Unit,
    onSetExitDialog: (Boolean) -> Unit,
    onExportResultToUri: (Uri, QuizFileFormat) -> Unit,
    onPrepareShareFile: (QuizFileFormat, (Uri) -> Unit) -> Unit,
    onClearExportStatus: () -> Unit,
    onPlayAgain: () -> Unit,
    onExitToHome: () -> Unit
) {
    when (uiState) {
        QuizUiState.Loading -> {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.safeDrawing
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        is QuizUiState.Completed -> {
            ResultScreen(
                state = uiState,
                exportState = exportState,
                onExportResultToUri = onExportResultToUri,
                onPrepareShareFile = onPrepareShareFile,
                onClearExportStatus = onClearExportStatus,
                onPlayAgain = onPlayAgain,
                onExitToHome = onExitToHome
            )
        }

        is QuizUiState.Active -> {
            BackHandler {
                onSetExitDialog(true)
            }

            val question = uiState.currentQuestion
            val progressTarget = (uiState.currentIndex + 1).toFloat() / uiState.questions.size.coerceAtLeast(1)
            val animatedProgress by animateFloatAsState(
                targetValue = progressTarget,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "quiz_progress"
            )

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.safeDrawing
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 680.dp)
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        // Top Bar: Close + Progress + Star Score
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                IconButton(
                                    onClick = { onSetExitDialog(true) },
                                    modifier = Modifier.testTag("quiz_close_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.leave_quiz)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = uiState.category.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = uiState.category.accentColor
                                    )
                                    Text(
                                        text = "${uiState.currentIndex + 1}/${uiState.questions.size}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    color = uiState.category.accentColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    strokeCap = StrokeCap.Round,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = SunTertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "${uiState.scoreStars}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Scrollable Question + Answers + Fun Fact
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Question Card
                            Surface(
                                shape = RoundedCornerShape(26.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                                shadowElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(question.category.containerColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = question.emojiCue,
                                            fontSize = 38.sp
                                        )
                                    }

                                    Text(
                                        text = question.prompt,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSpeaking) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { onToggleReadAloud() }
                                            .minimumInteractiveComponentSize()
                                            .testTag("quiz_read_aloud_button")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isSpeaking) Icons.Rounded.Stop
                                                else Icons.AutoMirrored.Rounded.VolumeUp,
                                                contentDescription = null,
                                                tint = if (isSpeaking) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = if (isSpeaking) stringResource(R.string.stop_reading)
                                                else stringResource(R.string.read_aloud),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (isSpeaking) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                            }

                            // Answer Options A, B, C, D
                            val letters = listOf("A", "B", "C", "D")
                            question.options.forEachIndexed { index, optionText ->
                                AnswerOptionCard(
                                    letter = letters.getOrElse(index) { "?" },
                                    text = optionText,
                                    index = index,
                                    isAnswered = uiState.isAnswered,
                                    isSelected = uiState.selectedOptionIndex == index,
                                    isCorrectOption = question.correctIndex == index,
                                    onClick = { onSelectAnswer(index) }
                                )
                            }

                            // Educational Fun Fact Banner after answer
                            AnimatedVisibility(
                                visible = uiState.isAnswered,
                                enter = fadeIn() + expandVertically()
                            ) {
                                val selectedWasCorrect = uiState.selectedOptionIndex == question.correctIndex
                                Surface(
                                    shape = RoundedCornerShape(22.dp),
                                    color = if (selectedWasCorrect) MintSuccessContainer else BerryErrorContainer,
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (selectedWasCorrect) MintSuccess else BerryError
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("quiz_feedback_banner")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (selectedWasCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Lightbulb,
                                                contentDescription = null,
                                                tint = if (selectedWasCorrect) MintSuccess else BerryError
                                            )
                                            Text(
                                                text = if (selectedWasCorrect) {
                                                    stringResource(R.string.correct_cheer)
                                                } else {
                                                    stringResource(R.string.wrong_cheer)
                                                },
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedWasCorrect) MintSuccessOnContainer else BerryErrorOnContainer
                                            )
                                        }
                                        Text(
                                            text = "${stringResource(R.string.fun_fact_title)} ${question.funFact}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (selectedWasCorrect) MintSuccessOnContainer else BerryErrorOnContainer
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        // Next / Results CTA
                        AnimatedVisibility(visible = uiState.isAnswered) {
                            val selectedWasCorrect = uiState.selectedOptionIndex == question.correctIndex
                            TactileButton(
                                onClick = onNextQuestion,
                                containerColor = if (selectedWasCorrect) MintSuccess else SkySecondary,
                                lipColor = if (selectedWasCorrect) MintSuccessLip else SkyLip,
                                tag = "quiz_next_button",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (uiState.isLastQuestion) {
                                            stringResource(R.string.see_results)
                                        } else {
                                            stringResource(R.string.next_question)
                                        },
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.showExitDialog) {
                    AlertDialog(
                        onDismissRequest = { onSetExitDialog(false) },
                        title = {
                            Text(
                                text = stringResource(R.string.exit_quiz_title),
                                style = MaterialTheme.typography.headlineSmall
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(R.string.exit_quiz_message),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = onExitToHome,
                                colors = ButtonDefaults.textButtonColors(contentColor = BerryError),
                                modifier = Modifier.testTag("confirm_exit_quiz")
                            ) {
                                Text(
                                    text = stringResource(R.string.leave_quiz),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { onSetExitDialog(false) },
                                modifier = Modifier.testTag("cancel_exit_quiz")
                            ) {
                                Text(
                                    text = stringResource(R.string.stay_in_quiz),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnswerOptionCard(
    letter: String,
    text: String,
    index: Int,
    isAnswered: Boolean,
    isSelected: Boolean,
    isCorrectOption: Boolean,
    onClick: () -> Unit
) {
    val containerColor: Color
    val borderColor: Color
    val badgeBg: Color
    val badgeInk: Color
    val textColor: Color

    when {
        isAnswered && isCorrectOption -> {
            containerColor = MintSuccessContainer
            borderColor = MintSuccess
            badgeBg = MintSuccess
            badgeInk = Color.White
            textColor = MintSuccessOnContainer
        }
        isAnswered && isSelected && !isCorrectOption -> {
            containerColor = BerryErrorContainer
            borderColor = BerryError
            badgeBg = BerryError
            badgeInk = Color.White
            textColor = BerryErrorOnContainer
        }
        else -> {
            containerColor = MaterialTheme.colorScheme.surface
            borderColor = MaterialTheme.colorScheme.outline
            badgeBg = MaterialTheme.colorScheme.primaryContainer
            badgeInk = MaterialTheme.colorScheme.onPrimaryContainer
            textColor = MaterialTheme.colorScheme.onSurface
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(2.dp, borderColor),
        shadowElevation = if (!isAnswered) 1.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = !isAnswered, onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag("answer_option_$index")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    style = MaterialTheme.typography.titleMedium,
                    color = badgeInk,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )

            if (isAnswered && isCorrectOption) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Benar",
                    tint = MintSuccess
                )
            } else if (isAnswered && isSelected && !isCorrectOption) {
                Icon(
                    imageVector = Icons.Rounded.HighlightOff,
                    contentDescription = "Salah",
                    tint = BerryError
                )
            }
        }
    }
}
