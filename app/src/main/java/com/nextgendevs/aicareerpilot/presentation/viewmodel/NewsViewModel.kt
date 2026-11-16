package com.nextgendevs.aicareerpilot.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextgendevs.aicareerpilot.data.model.news.Question
import com.nextgendevs.aicareerpilot.data.repository.StackOverflowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

sealed interface DiscussionUiState {

    object Loading : DiscussionUiState

    data class Success(
        val questions: List<Question>,
        /** Non-null when showing cached data because the refresh failed. */
        val offlineMessage: String? = null,
        val isRefreshing: Boolean = false
    ) : DiscussionUiState

    data class Error(
        val message: String
    ) : DiscussionUiState
}

@HiltViewModel
class DiscussionViewModel @Inject constructor(
    private val repository: StackOverflowRepository
) : ViewModel() {

    private sealed interface RefreshStatus {
        data object Running : RefreshStatus
        data object Done : RefreshStatus
        data class Failed(val message: String) : RefreshStatus
    }

    private val refreshStatus = MutableStateFlow<RefreshStatus>(RefreshStatus.Running)

    /** Room is the single source of truth; the network only updates it. */
    val state: StateFlow<DiscussionUiState> =
        combine(repository.observeQuestions(), refreshStatus) { cached, status ->
            when {
                cached.isNotEmpty() -> DiscussionUiState.Success(
                    questions = cached,
                    offlineMessage = (status as? RefreshStatus.Failed)
                        ?.let { "Offline – showing saved trends." },
                    isRefreshing = status is RefreshStatus.Running
                )
                status is RefreshStatus.Failed -> DiscussionUiState.Error(status.message)
                status is RefreshStatus.Done -> DiscussionUiState.Success(emptyList())
                else -> DiscussionUiState.Loading
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DiscussionUiState.Loading
        )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshStatus.value = RefreshStatus.Running
            refreshStatus.value = try {
                repository.refresh()
                RefreshStatus.Done
            } catch (e: UnknownHostException) {
                RefreshStatus.Failed("Turn on your internet connection to see developer trends.")
            } catch (e: SocketTimeoutException) {
                RefreshStatus.Failed("Network timeout. Please check your connection and try again.")
            } catch (e: Exception) {
                RefreshStatus.Failed("Something went wrong. Please try again.")
            }
        }
    }
}
