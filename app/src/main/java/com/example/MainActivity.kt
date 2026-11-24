package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.audio.QuizAudioHelper
import com.example.data.local.QuizDatabase
import com.example.data.preferences.SettingsDataStore
import com.example.data.repository.QuizRepository
import com.example.model.QuizCategoryId
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.theme.KuisCeriaTheme
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.HomeViewModelFactory
import com.example.ui.viewmodel.QuizViewModel
import com.example.ui.viewmodel.QuizViewModelFactory
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

const val ROUTE_HOME = "home"
const val ROUTE_QUIZ = "quiz/{categoryName}"

class MainActivity : ComponentActivity() {

    private lateinit var audioHelper: QuizAudioHelper
    private lateinit var repository: QuizRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = QuizDatabase.getInstance(applicationContext)
        val settingsDataStore = SettingsDataStore(applicationContext)
        repository = QuizRepository(db.questionDao(), settingsDataStore, applicationContext)
        audioHelper = QuizAudioHelper(applicationContext)

        lifecycleScope.launch {
            repository.userPreferences
                .map { it.musicEnabled }
                .distinctUntilChanged()
                .collect { enabled ->
                    audioHelper.setMusicEnabled(enabled)
                }
        }

        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> audioHelper.onAppForegroundChanged(true)
                Lifecycle.Event.ON_PAUSE -> audioHelper.onAppForegroundChanged(false)
                else -> Unit
            }
        })

        setContent {
            KuisCeriaTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_HOME
                ) {
                    composable(ROUTE_HOME) {
                        val homeViewModel: HomeViewModel = viewModel(
                            factory = HomeViewModelFactory(repository)
                        )
                        val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
                        val fileState by homeViewModel.fileState.collectAsStateWithLifecycle()

                        HomeScreen(
                            uiState = uiState,
                            fileState = fileState,
                            onStartCategory = { category ->
                                navController.navigate("quiz/${category.name}")
                            },
                            onToggleSound = homeViewModel::setSoundEnabled,
                            onToggleMusic = homeViewModel::setMusicEnabled,
                            onToggleAutoTts = homeViewModel::setAutoReadTts,
                            onSelectDifficulty = homeViewModel::setDifficulty,
                            onSelectMascot = homeViewModel::setMascot,
                            onExportQuestionBank = homeViewModel::exportQuestionBank,
                            onInspectImportFile = homeViewModel::inspectImportFile,
                            onConfirmImportQuestions = homeViewModel::confirmImportQuestions,
                            onDismissImportDialog = homeViewModel::dismissImportDialog,
                            onResetBundledQuestions = homeViewModel::resetToBundledQuestions,
                            onClearFileFeedback = homeViewModel::clearFileFeedback
                        )
                    }

                    composable(
                        route = ROUTE_QUIZ,
                        arguments = listOf(
                            navArgument("categoryName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val categoryName = backStackEntry.arguments?.getString("categoryName")
                            ?: QuizCategoryId.DAILY_MIX.name
                        val category = QuizCategoryId.fromName(categoryName)

                        val quizViewModel: QuizViewModel = viewModel(
                            key = "quiz_$categoryName",
                            factory = QuizViewModelFactory(category, repository, audioHelper)
                        )
                        val uiState by quizViewModel.uiState.collectAsStateWithLifecycle()
                        val exportState by quizViewModel.exportState.collectAsStateWithLifecycle()
                        val isSpeaking by quizViewModel.isSpeaking.collectAsStateWithLifecycle()

                        QuizScreen(
                            uiState = uiState,
                            exportState = exportState,
                            isSpeaking = isSpeaking,
                            onSelectAnswer = quizViewModel::selectAnswer,
                            onNextQuestion = quizViewModel::nextQuestionOrFinish,
                            onToggleReadAloud = quizViewModel::toggleReadCurrentQuestion,
                            onSetExitDialog = quizViewModel::setShowExitDialog,
                            onExportResultToUri = quizViewModel::exportCompletedResult,
                            onPrepareShareFile = quizViewModel::prepareShareFile,
                            onClearExportStatus = quizViewModel::clearExportStatus,
                            onPlayAgain = quizViewModel::startNewRound,
                            onExitToHome = {
                                audioHelper.stopSpeaking()
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        audioHelper.shutdown()
        super.onDestroy()
    }
}
