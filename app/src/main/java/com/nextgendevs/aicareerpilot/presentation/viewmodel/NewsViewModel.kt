package com.nextgendevs.aicareerpilot.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextgendevs.aicareerpilot.data.model.news.Question
import com.nextgendevs.aicareerpilot.data.repository.StackOverflowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

sealed interface DiscussionUiState {

    object Loading : DiscussionUiState

    data class Success(
        val questions: List<Question>
    ) : DiscussionUiState

    data class Error(
        val message: String
    ) : DiscussionUiState
}

@HiltViewModel
class DiscussionViewModel @Inject constructor(
    private val repository: StackOverflowRepository
) : ViewModel() {

    private val _state =
        MutableStateFlow<DiscussionUiState>(
            DiscussionUiState.Loading
        )

    val state: StateFlow<DiscussionUiState> = _state

    init {
        loadQuestions()
    }

    private fun loadQuestions() {

        viewModelScope.launch {

            try {

                val data = repository.getQuestions()

                _state.value =
                    DiscussionUiState.Success(data)

            } catch (e: UnknownHostException) {

                _state.value =
                    DiscussionUiState.Error(
                        "Turn on your internet connection to see developer trends."
                    )

            } catch (e: SocketTimeoutException) {

                _state.value =
                    DiscussionUiState.Error(
                        "Network timeout. Please check your connection and try again."
                    )

            } catch (e: Exception) {

                _state.value =
                    DiscussionUiState.Error(
                        "Something went wrong. Please try again."
                    )
            }
        }
    }
}