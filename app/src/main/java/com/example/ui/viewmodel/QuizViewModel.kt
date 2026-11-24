package com.example.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.QuizAudioHelper
import com.example.data.file.QuizFileFormat
import com.example.data.file.QuizResultExportData
import com.example.data.preferences.UserPreferences
import com.example.data.repository.QuizRepository
import com.example.model.AnsweredQuestion
import com.example.model.QuizCategoryId
import com.example.model.QuizQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface QuizUiState {
    data object Loading : QuizUiState
    data class Active(
        val category: QuizCategoryId,
        val questions: List<QuizQuestion>,
        val currentIndex: Int,
        val selectedOptionIndex: Int?,
        val scoreStars: Int,
        val answeredHistory: List<AnsweredQuestion>,
        val showExitDialog: Boolean,
        val preferences: UserPreferences
    ) : QuizUiState {
        val currentQuestion: QuizQuestion get() = questions[currentIndex]
        val isAnswered: Boolean get() = selectedOptionIndex != null
        val isLastQuestion: Boolean get() = currentIndex == questions.lastIndex
    }

    data class Completed(
        val category: QuizCategoryId,
        val totalQuestions: Int,
        val correctCount: Int,
        val earnedPoints: Int,
        val starRating: Int,
        val answeredHistory: List<AnsweredQuestion>
    ) : QuizUiState {
        fun toExportData(): QuizResultExportData = QuizResultExportData(
            category = category,
            totalQuestions = totalQuestions,
            correctCount = correctCount,
            earnedPoints = earnedPoints,
            starRating = starRating,
            answeredHistory = answeredHistory
        )
    }
}

data class ResultExportUiState(
    val isExporting: Boolean = false,
    val statusMessage: String? = null
)

class QuizViewModel(
    private val category: QuizCategoryId,
    private val repository: QuizRepository,
    private val audioHelper: QuizAudioHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _exportState = MutableStateFlow(ResultExportUiState())
    val exportState: StateFlow<ResultExportUiState> = _exportState.asStateFlow()

    val isSpeaking: StateFlow<Boolean> = audioHelper.isSpeaking

    init {
        startNewRound()
    }

    fun startNewRound() {
        audioHelper.stopSpeaking()
        _uiState.value = QuizUiState.Loading
        _exportState.value = ResultExportUiState()
        viewModelScope.launch {
            val prefs = repository.userPreferences.first()
            val questions = repository.loadQuizSession(
                category = category,
                difficulty = prefs.difficulty,
                maxQuestions = 8
            )
            _uiState.value = QuizUiState.Active(
                category = category,
                questions = questions,
                currentIndex = 0,
                selectedOptionIndex = null,
                scoreStars = 0,
                answeredHistory = emptyList(),
                showExitDialog = false,
                preferences = prefs
            )
            if (prefs.autoReadTts && questions.isNotEmpty()) {
                audioHelper.speak(questions.first().prompt)
            }
        }
    }

    fun selectAnswer(optionIndex: Int) {
        val current = _uiState.value as? QuizUiState.Active ?: return
        if (current.isAnswered) return

        audioHelper.stopSpeaking()
        val question = current.currentQuestion
        val isCorrect = optionIndex == question.correctIndex
        val newScore = if (isCorrect) current.scoreStars + 10 else current.scoreStars

        if (current.preferences.soundEnabled) {
            if (isCorrect) audioHelper.playCorrectChime() else audioHelper.playWrongChime()
        }

        val updatedHistory = current.answeredHistory + AnsweredQuestion(
            question = question,
            selectedIndex = optionIndex,
            isCorrect = isCorrect
        )

        _uiState.value = current.copy(
            selectedOptionIndex = optionIndex,
            scoreStars = newScore,
            answeredHistory = updatedHistory
        )
    }

    fun nextQuestionOrFinish() {
        val current = _uiState.value as? QuizUiState.Active ?: return
        audioHelper.stopSpeaking()

        if (current.isLastQuestion) {
            val total = current.questions.size
            val correctCount = current.answeredHistory.count { it.isCorrect }
            val accuracy = if (total > 0) correctCount.toFloat() / total else 0f
            val stars = when {
                accuracy >= 0.8f -> 3
                accuracy >= 0.5f -> 2
                else -> 1
            }
            if (current.preferences.soundEnabled) {
                audioHelper.playCelebrationFanfare()
            }
            _uiState.value = QuizUiState.Completed(
                category = current.category,
                totalQuestions = total,
                correctCount = correctCount,
                earnedPoints = current.scoreStars,
                starRating = stars,
                answeredHistory = current.answeredHistory
            )
        } else {
            val nextIdx = current.currentIndex + 1
            val nextQuestion = current.questions[nextIdx]
            _uiState.value = current.copy(
                currentIndex = nextIdx,
                selectedOptionIndex = null
            )
            if (current.preferences.autoReadTts) {
                audioHelper.speak(nextQuestion.prompt)
            }
        }
    }

    fun toggleReadCurrentQuestion() {
        val current = _uiState.value as? QuizUiState.Active ?: return
        if (isSpeaking.value) {
            audioHelper.stopSpeaking()
        } else {
            val textToRead = if (current.isAnswered) {
                current.currentQuestion.funFact
            } else {
                current.currentQuestion.prompt
            }
            audioHelper.speak(textToRead)
        }
    }

    fun setShowExitDialog(show: Boolean) {
        val current = _uiState.value as? QuizUiState.Active ?: return
        _uiState.value = current.copy(showExitDialog = show)
    }

    fun exportCompletedResult(uri: Uri, format: QuizFileFormat) {
        val completed = _uiState.value as? QuizUiState.Completed ?: return
        viewModelScope.launch {
            _exportState.value = _exportState.value.copy(isExporting = true)
            val result = repository.exportResultToUri(uri, completed.toExportData(), format)
            _exportState.value = result.fold(
                onSuccess = {
                    ResultExportUiState(
                        isExporting = false,
                        statusMessage = "Hasil kuis berhasil disimpan sebagai file ${format.extension.uppercase()}!"
                    )
                },
                onFailure = { error ->
                    ResultExportUiState(
                        isExporting = false,
                        statusMessage = error.message ?: "Gagal menyimpan file hasil."
                    )
                }
            )
        }
    }

    fun prepareShareFile(format: QuizFileFormat, onReady: (Uri) -> Unit) {
        val completed = _uiState.value as? QuizUiState.Completed ?: return
        viewModelScope.launch {
            _exportState.value = _exportState.value.copy(isExporting = true)
            val result = repository.createShareableResultFile(completed.toExportData(), format)
            result.fold(
                onSuccess = { uri ->
                    _exportState.value = _exportState.value.copy(isExporting = false)
                    onReady(uri)
                },
                onFailure = { error ->
                    _exportState.value = ResultExportUiState(
                        isExporting = false,
                        statusMessage = error.message ?: "Gagal membuat file untuk dibagikan."
                    )
                }
            )
        }
    }

    fun clearExportStatus() {
        _exportState.value = _exportState.value.copy(statusMessage = null)
    }

    override fun onCleared() {
        audioHelper.stopSpeaking()
        super.onCleared()
    }
}

class QuizViewModelFactory(
    private val category: QuizCategoryId,
    private val repository: QuizRepository,
    private val audioHelper: QuizAudioHelper
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return QuizViewModel(category, repository, audioHelper) as T
    }
}
