package com.nextgendevs.aicareerpilot.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextgendevs.aicareerpilot.data.model.gemini_response.AnalysisRecord
import com.nextgendevs.aicareerpilot.domain.usecases.AnalyzeResumeUseCase
import com.nextgendevs.aicareerpilot.domain.usecases.CanAnalyzeUseCase
import com.nextgendevs.aicareerpilot.domain.usecases.DeleteRecordUseCase
import com.nextgendevs.aicareerpilot.domain.usecases.GetHistoryUseCase
import com.nextgendevs.aicareerpilot.domain.usecases.GetRemainingAttemptsUseCase
import com.nextgendevs.aicareerpilot.domain.usecases.IncrementUsageUseCase
import com.nextgendevs.aicareerpilot.util.DocumentHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ResumeUiState {
    object Idle : ResumeUiState
    object Loading : ResumeUiState
    data class Success(val analysisResult: String) : ResumeUiState
    data class Error(val message: String) : ResumeUiState
}
@HiltViewModel
class ResumeViewModel @Inject constructor(
    private val analyzeResumeUseCase: AnalyzeResumeUseCase,
    private val getHistoryUseCase: GetHistoryUseCase,
    private val deleteRecordUseCase: DeleteRecordUseCase,
    private val documentHelper: DocumentHelper,
    private val canAnalyzeUseCase: CanAnalyzeUseCase,
    private val incrementUsageUseCase: IncrementUsageUseCase,
    private val getRemainingAttemptsUseCase: GetRemainingAttemptsUseCase
) : ViewModel() {

    // 1. Single source of truth for the analysis workflow state
    private val _uiState = MutableStateFlow<ResumeUiState>(ResumeUiState.Idle)
    val uiState: StateFlow<ResumeUiState> = _uiState.asStateFlow()

    // 2. Keep track of the JD text field state
    private val _jobDescription = MutableStateFlow("")
    val jobDescription: StateFlow<String> = _jobDescription

    fun updateJD(newJD: String) {
        _jobDescription.value = newJD
    }

    fun clearSessionData() {
        _jobDescription.value = ""
    }

    // 3. Observe historical records from the database
    val analysisHistory: StateFlow<List<AnalysisRecord>> =
        getHistoryUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    // 4.keep track of attempts
    private val _remainingAttempts =
        MutableStateFlow(3)

    val remainingAttempts: StateFlow<Int> =
        _remainingAttempts.asStateFlow()

    init {
        loadRemainingAttempts()
    }
    private fun loadRemainingAttempts() {

        viewModelScope.launch {

            _remainingAttempts.value =
                getRemainingAttemptsUseCase()
        }
    }

    fun refreshRemainingAttempts() {
        viewModelScope.launch {
            _remainingAttempts.value =
                getRemainingAttemptsUseCase()
        }
    }

    // 5. Refactored processing with precise state transitions and error handling
    fun processResume(uri: Uri, fileName: String) {
        // Prevent launching a new process if we are already loading
        if (_uiState.value is ResumeUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = ResumeUiState.Loading

            try {
                val supported =
                    fileName.endsWith(".pdf", true) ||
                            fileName.endsWith(".docx", true)

                if (!supported) {

                    _uiState.value =
                        ResumeUiState.Error(
                            "Only PDF and DOCX files are supported."
                        )

                    return@launch
                }

                // Check daily limit
                if (!canAnalyzeUseCase()) {

                    _uiState.value =
                        ResumeUiState.Error(
                            "Daily limit reached. You can analyze only 3 resumes per day."
                        )

                    return@launch
                }

                // Step A: Extract text from PDF
                val resumeText =
                    documentHelper.extractTextFromUri(
                        uri = uri,
                        fileName = fileName
                    )
                if (resumeText.isBlank()) {
                    _uiState.value = ResumeUiState.Error("Could not extract any readable text from the PDF.")
                    return@launch
                }

                // Step B: Send to repository/API for analysis
                val result = analyzeResumeUseCase(
                    fileName,
                    resumeText,
                    _jobDescription.value
                )
                // Increase today's count
                incrementUsageUseCase()
                //remaining attempts update
                _remainingAttempts.value =
                    getRemainingAttemptsUseCase()

                // Step C: Push Success state
                _uiState.value = ResumeUiState.Success(result)

            } catch (e: Exception) {
                // Step D: Catch network failures, file access errors, etc.
                _uiState.value = ResumeUiState.Error(e.localizedMessage ?: "An unexpected error occurred.")
            }
        }
    }

    fun deleteRecord(record: AnalysisRecord) {
        viewModelScope.launch {
            try {
                deleteRecordUseCase(record)
            } catch (e: Exception) {
                // Optional: You could expose a separate single-event channel (like a SharedFlow)
                // to show a SnackBar if a deletion fails.
            }
        }
    }

    // Helper to reset back to Idle if the user wants to clear the current result
    fun resetUiState() {
        _uiState.value = ResumeUiState.Idle
    }
}