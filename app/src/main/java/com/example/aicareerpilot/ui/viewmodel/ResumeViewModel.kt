package com.example.aicareerpilot.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aicareerpilot.data.model.AnalysisRecord
import com.example.aicareerpilot.data.repository.ResumeRepository
import com.example.aicareerpilot.util.PdfHelper
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
    private val repository: ResumeRepository,
    private val pdfHelper: PdfHelper
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

    // 3. Observe historical records from the database
    val analysisHistory: StateFlow<List<AnalysisRecord>> = repository.getHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 4. Refactored processing with precise state transitions and error handling
    fun processResume(uri: Uri, fileName: String) {
        // Prevent launching a new process if we are already loading
        if (_uiState.value is ResumeUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = ResumeUiState.Loading

            try {
                // Step A: Extract text from PDF
                val resumeText = pdfHelper.extractTextFromUri(uri)
                if (resumeText.isNullOrBlank()) {
                    _uiState.value = ResumeUiState.Error("Could not extract any readable text from the PDF.")
                    return@launch
                }

                // Step B: Send to repository/API for analysis
                val result = repository.analyzeWithJD(fileName, resumeText, _jobDescription.value)

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
                repository.deleteRecord(record)
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