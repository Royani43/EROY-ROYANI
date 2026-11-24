package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.file.QuizFileCodec
import com.example.data.file.QuizFileFormat
import com.example.model.CategorySummary
import com.example.model.DifficultyLevel
import com.example.model.MascotBuddy
import com.example.model.QuizCategoryId
import com.example.ui.components.DecorativeStarsBackdrop
import com.example.ui.components.MascotAvatarBadge
import com.example.ui.components.TactileButton
import com.example.ui.theme.CoralLip
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.SkySecondary
import com.example.ui.theme.SunTertiary
import com.example.ui.viewmodel.HomeUiState
import com.example.ui.viewmodel.QuestionFileUiState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    fileState: QuestionFileUiState,
    onStartCategory: (QuizCategoryId) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onToggleAutoTts: (Boolean) -> Unit,
    onSelectDifficulty: (DifficultyLevel) -> Unit,
    onSelectMascot: (MascotBuddy) -> Unit,
    onExportQuestionBank: (Uri, QuizFileFormat) -> Unit,
    onInspectImportFile: (Uri) -> Unit,
    onConfirmImportQuestions: (Boolean) -> Unit,
    onDismissImportDialog: () -> Unit,
    onResetBundledQuestions: () -> Unit,
    onClearFileFeedback: () -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(QuizFileFormat.JSON.mimeType)
    ) { uri ->
        if (uri != null) {
            onExportQuestionBank(uri, QuizFileFormat.JSON)
        }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(QuizFileFormat.CSV.mimeType)
    ) { uri ->
        if (uri != null) {
            onExportQuestionBank(uri, QuizFileFormat.CSV)
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onInspectImportFile(uri)
        }
    }

    LaunchedEffect(fileState.feedbackMessage) {
        val message = fileState.feedbackMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onClearFileFeedback()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        when (uiState) {
            HomeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is HomeUiState.Ready -> {
                val prefs = uiState.preferences

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val columns = if (maxWidth >= 600.dp) 2 else 1

                    LazyColumn(
                        modifier = Modifier
                            .widthIn(max = 720.dp)
                            .fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // Top Mascot Greeting & Controls
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    MascotAvatarBadge(
                                        mascot = prefs.mascot,
                                        size = 56.dp,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable { showSettings = true }
                                            .testTag("home_mascot_badge")
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.app_name),
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = prefs.mascot.greeting,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        IconButton(
                                            onClick = { onToggleMusic(!prefs.musicEnabled) },
                                            modifier = Modifier.testTag("home_music_toggle")
                                        ) {
                                            Icon(
                                                imageVector = if (prefs.musicEnabled) {
                                                    Icons.Rounded.MusicNote
                                                } else {
                                                    Icons.Rounded.MusicOff
                                                },
                                                contentDescription = stringResource(R.string.music_theme_label),
                                                tint = if (prefs.musicEnabled) {
                                                    MaterialTheme.colorScheme.secondary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        IconButton(
                                            onClick = { onToggleSound(!prefs.soundEnabled) },
                                            modifier = Modifier.testTag("home_sound_toggle")
                                        ) {
                                            Icon(
                                                imageVector = if (prefs.soundEnabled) {
                                                    Icons.AutoMirrored.Rounded.VolumeUp
                                                } else {
                                                    Icons.AutoMirrored.Rounded.VolumeOff
                                                },
                                                contentDescription = stringResource(R.string.sound_effects_label),
                                                tint = if (prefs.soundEnabled) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        IconButton(
                                            onClick = { showSettings = true },
                                            modifier = Modifier.testTag("home_settings_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Settings,
                                                contentDescription = stringResource(R.string.settings_title),
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Hero Daily Challenge Banner
                        item {
                            DailyChallengeHeroCard(
                                difficulty = prefs.difficulty,
                                onStartDaily = { onStartCategory(QuizCategoryId.DAILY_MIX) },
                                onOpenDifficulty = { showSettings = true }
                            )
                        }

                        // Category Header + Difficulty Pill
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.choose_category),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { showSettings = true }
                                        .minimumInteractiveComponentSize()
                                        .testTag("home_difficulty_pill")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Tune,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = prefs.difficulty.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        // Category Cards (1 or 2 columns adaptively)
                        val rows = uiState.categories.chunked(columns)
                        items(rows.size) { rowIndex ->
                            val rowItems = rows[rowIndex]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                rowItems.forEach { summary ->
                                    CategoryPlayCard(
                                        summary = summary,
                                        onClick = { onStartCategory(summary.category) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowItems.size == 1 && columns == 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        // Quick Question File Manager Card
                        item {
                            QuestionBankFileQuickCard(
                                totalQuestions = uiState.totalQuestionsAvailable,
                                isBusy = fileState.isBusy,
                                onExportJson = {
                                    exportJsonLauncher.launch("bank_soal_${QuizFileCodec.formatFileTimestamp()}.json")
                                },
                                onImportFile = {
                                    importFileLauncher.launch(
                                        arrayOf(
                                            "application/json",
                                            "text/csv",
                                            "text/comma-separated-values",
                                            "text/plain",
                                            "*/*"
                                        )
                                    )
                                },
                                onOpenAllOptions = { showSettings = true }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                if (showSettings) {
                    SettingsBottomSheet(
                        preferences = prefs,
                        totalQuestionsAvailable = uiState.totalQuestionsAvailable,
                        isFileBusy = fileState.isBusy,
                        onDismiss = { showSettings = false },
                        onToggleSound = onToggleSound,
                        onToggleMusic = onToggleMusic,
                        onToggleAutoTts = onToggleAutoTts,
                        onSelectDifficulty = onSelectDifficulty,
                        onSelectMascot = onSelectMascot,
                        onExportJsonClick = {
                            exportJsonLauncher.launch("bank_soal_${QuizFileCodec.formatFileTimestamp()}.json")
                        },
                        onExportCsvClick = {
                            exportCsvLauncher.launch("bank_soal_${QuizFileCodec.formatFileTimestamp()}.csv")
                        },
                        onImportFileClick = {
                            importFileLauncher.launch(
                                arrayOf(
                                    "application/json",
                                    "text/csv",
                                    "text/comma-separated-values",
                                    "text/plain",
                                    "*/*"
                                )
                            )
                        },
                        onResetBundledClick = onResetBundledQuestions
                    )
                }

                val pendingImport = fileState.pendingImportQuestions
                if (pendingImport != null) {
                    AlertDialog(
                        onDismissRequest = onDismissImportDialog,
                        title = {
                            Text(
                                text = stringResource(R.string.import_dialog_title),
                                style = MaterialTheme.typography.headlineSmall
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(R.string.import_dialog_message, pendingImport.size),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = { onConfirmImportQuestions(false) },
                                modifier = Modifier.testTag("confirm_import_merge")
                            ) {
                                Text(text = stringResource(R.string.import_merge_button))
                            }
                        },
                        dismissButton = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = onDismissImportDialog,
                                    modifier = Modifier.testTag("cancel_import_button")
                                ) {
                                    Text(text = stringResource(R.string.cancel))
                                }
                                OutlinedButton(
                                    onClick = { onConfirmImportQuestions(true) },
                                    modifier = Modifier.testTag("confirm_import_replace")
                                ) {
                                    Text(text = stringResource(R.string.import_replace_button))
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionBankFileQuickCard(
    totalQuestions: Int,
    isBusy: Boolean,
    onExportJson: () -> Unit,
    onImportFile: () -> Unit,
    onOpenAllOptions: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, SkySecondary.copy(alpha = 0.35f)),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_file_bank_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SkySecondary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FolderOpen,
                            contentDescription = null,
                            tint = SkySecondary
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.file_bank_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.active_questions_badge, totalQuestions),
                            style = MaterialTheme.typography.labelMedium,
                            color = SkySecondary
                        )
                    }
                }

                TextButton(
                    onClick = onOpenAllOptions,
                    modifier = Modifier.testTag("home_file_more_options")
                ) {
                    Text(text = "CSV / Reset")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onExportJson,
                    enabled = !isBusy,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("home_export_json_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.export_json_button))
                }

                OutlinedButton(
                    onClick = onImportFile,
                    enabled = !isBusy,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("home_import_file_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FileUpload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.import_file_button))
                }
            }
        }
    }
}

@Composable
private fun DailyChallengeHeroCard(
    difficulty: DifficultyLevel,
    onStartDaily: () -> Unit,
    onOpenDifficulty: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(CoralPrimary, SunTertiary)
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            DecorativeStarsBackdrop(
                tint = Color.White.copy(alpha = 0.22f),
                modifier = Modifier.matchParentSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.22f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenDifficulty() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Mode: ${difficulty.label}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.daily_challenge_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                Text(
                    text = stringResource(R.string.daily_challenge_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.94f)
                )

                Spacer(modifier = Modifier.height(2.dp))

                TactileButton(
                    onClick = onStartDaily,
                    containerColor = Color.White,
                    lipColor = Color(0xFFE7D3BF),
                    tag = "start_daily_quiz_button",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = CoralLip,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.start_daily_quiz),
                            style = MaterialTheme.typography.titleLarge,
                            color = CoralLip
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryPlayCard(
    summary: CategorySummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = summary.category
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cat_card_scale"
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(2.dp, category.accentColor.copy(alpha = 0.28f)),
        shadowElevation = 2.dp,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = category.accentColor.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .minimumInteractiveComponentSize()
            .testTag("category_card_${category.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(category.containerColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.emoji,
                    fontSize = 30.sp
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = category.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = category.accentColor.copy(alpha = 0.14f)
            ) {
                Text(
                    text = stringResource(R.string.questions_count_format, summary.questionCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = category.accentColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
