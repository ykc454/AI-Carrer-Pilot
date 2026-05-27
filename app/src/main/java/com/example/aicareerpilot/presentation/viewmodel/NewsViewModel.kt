package com.example.aicareerpilot.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aicareerpilot.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.aicareerpilot.data.model.news.NewsArticle

sealed interface NewsUiState {

    object Loading : NewsUiState

    data class Success(
        val news: List<NewsArticle>
    ) : NewsUiState

    data class Error(
        val message: String
    ) : NewsUiState
}

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _state =
        MutableStateFlow<NewsUiState>(NewsUiState.Loading)

    val state: StateFlow<NewsUiState> = _state

    init {
        loadNews()
    }

    private fun loadNews() {

        viewModelScope.launch {

            try {

                val news = repository.getNews()

                _state.value =
                    NewsUiState.Success(news)

            } catch (e: Exception) {

                _state.value =
                    NewsUiState.Error(
                        e.message ?: "Unknown Error"
                    )
            }
        }
    }
}

