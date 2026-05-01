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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResumeViewModel @Inject constructor(
    private val repository: ResumeRepository,
    private val pdfHelper: PdfHelper
) : ViewModel() {

    private val _analysisResult = MutableStateFlow<String?>(null)
    val analysisResult: StateFlow<String?> = _analysisResult
    private val _jobDescription = MutableStateFlow("")
    val jobDescription: StateFlow<String> = _jobDescription

    fun updateJD(newJD: String) {
        _jobDescription.value = newJD
    }
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // NEW: Observe history from the database.
    // StateIn converts the Flow to a StateFlow that Compose can watch easily.
    val analysisHistory: StateFlow<List<AnalysisRecord>> = repository.getHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UPDATED: Now takes a fileName string
    fun processResume(uri: Uri, fileName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val resumeText = pdfHelper.extractTextFromUri(uri)

            // Pass the JD from our state
            val result = repository.analyzeWithJD(fileName, resumeText, _jobDescription.value)

            _analysisResult.value = result
            _isLoading.value = false
        }
    }
}