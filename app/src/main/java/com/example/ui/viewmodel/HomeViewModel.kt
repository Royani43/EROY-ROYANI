package com.example.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.file.QuizFileFormat
import com.example.data.local.QuestionEntity
import com.example.data.preferences.UserPreferences
import com.example.data.repository.QuizRepository
import com.example.model.CategorySummary
import com.example.model.DifficultyLevel
import com.example.model.MascotBuddy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Ready(
        val preferences: UserPreferences,
        val categories: List<CategorySummary>,
        val totalQuestionsAvailable: Int
    ) : HomeUiState
}

data class QuestionFileUiState(
    val isBusy: Boolean = false,
    val pendingImportQuestions: List<QuestionEntity>? = null,
    val feedbackMessage: String? = null
)

class HomeViewModel(
    private val repository: QuizRepository
) : ViewModel() {

    private val _fileState = MutableStateFlow(QuestionFileUiState())
    val fileState: StateFlow<QuestionFileUiState> = _fileState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = repository.userPreferences
        .flatMapLatest { prefs ->
            repository.observeCategorySummaries(prefs.difficulty).combine(
                repository.userPreferences
            ) { summaries, latestPrefs ->
                HomeUiState.Ready(
                    preferences = latestPrefs,
                    categories = summaries,
                    totalQuestionsAvailable = summaries.sumOf { it.questionCount }
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState.Loading
        )

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setSoundEnabled(enabled) }
    }

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setMusicEnabled(enabled) }
    }

    fun setAutoReadTts(enabled: Boolean) {
        viewModelScope.launch { repository.setAutoReadTts(enabled) }
    }

    fun setDifficulty(level: DifficultyLevel) {
        viewModelScope.launch { repository.setDifficulty(level) }
    }

    fun setMascot(mascot: MascotBuddy) {
        viewModelScope.launch { repository.setMascot(mascot) }
    }

    fun exportQuestionBank(uri: Uri, format: QuizFileFormat) {
        viewModelScope.launch {
            _fileState.value = _fileState.value.copy(isBusy = true)
            val result = repository.exportQuestionBankToUri(uri, format)
            _fileState.value = result.fold(
                onSuccess = { count ->
                    _fileState.value.copy(
                        isBusy = false,
                        feedbackMessage = "Berhasil menyimpan $count soal ke file ${format.extension.uppercase()}!"
                    )
                },
                onFailure = { error ->
                    _fileState.value.copy(
                        isBusy = false,
                        feedbackMessage = error.message ?: "Gagal menyimpan file soal."
                    )
                }
            )
        }
    }

    fun inspectImportFile(uri: Uri) {
        viewModelScope.launch {
            _fileState.value = _fileState.value.copy(isBusy = true)
            val result = repository.parseQuestionsFromUri(uri)
            _fileState.value = result.fold(
                onSuccess = { parsed ->
                    _fileState.value.copy(
                        isBusy = false,
                        pendingImportQuestions = parsed
                    )
                },
                onFailure = { error ->
                    _fileState.value.copy(
                        isBusy = false,
                        feedbackMessage = error.message ?: "File soal tidak valid."
                    )
                }
            )
        }
    }

    fun confirmImportQuestions(replaceExisting: Boolean) {
        val pending = _fileState.value.pendingImportQuestions ?: return
        viewModelScope.launch {
            _fileState.value = _fileState.value.copy(isBusy = true, pendingImportQuestions = null)
            val result = repository.applyImportedQuestions(pending, replaceExisting)
            _fileState.value = result.fold(
                onSuccess = { count ->
                    val action = if (replaceExisting) "mengganti bank soal dengan" else "menambahkan"
                    _fileState.value.copy(
                        isBusy = false,
                        feedbackMessage = "Berhasil $action $count soal dari file!"
                    )
                },
                onFailure = { error ->
                    _fileState.value.copy(
                        isBusy = false,
                        feedbackMessage = error.message ?: "Gagal mengimpor soal."
                    )
                }
            )
        }
    }

    fun dismissImportDialog() {
        _fileState.value = _fileState.value.copy(pendingImportQuestions = null)
    }

    fun resetToBundledQuestions() {
        viewModelScope.launch {
            _fileState.value = _fileState.value.copy(isBusy = true)
            val result = repository.resetToBundledQuestions()
            _fileState.value = result.fold(
                onSuccess = { count ->
                    _fileState.value.copy(
                        isBusy = false,
                        feedbackMessage = "Bank soal dipulihkan ke $count soal bawaan (soal_kuis.json)."
                    )
                },
                onFailure = { error ->
                    _fileState.value.copy(
                        isBusy = false,
                        feedbackMessage = error.message ?: "Gagal memulihkan soal bawaan."
                    )
                }
            )
        }
    }

    fun clearFileFeedback() {
        _fileState.value = _fileState.value.copy(feedbackMessage = null)
    }
}

class HomeViewModelFactory(
    private val repository: QuizRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repository) as T
    }
}
