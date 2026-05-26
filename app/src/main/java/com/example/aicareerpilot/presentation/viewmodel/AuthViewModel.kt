package com.example.aicareerpilot.presentation.viewmodel

import android.content.Context

import com.example.aicareerpilot.R
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aicareerpilot.domain.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
sealed interface AuthUiState {

    object Idle : AuthUiState

    object Loading : AuthUiState

    object Success : AuthUiState

    data class Error(
        val message: String
    ) : AuthUiState
}
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<AuthUiState>(AuthUiState.Idle)

    val uiState: StateFlow<AuthUiState> =
        _uiState.asStateFlow()

    fun signInWithGoogle(
        context: Context
    ) {

        if (_uiState.value is AuthUiState.Loading) return

        viewModelScope.launch {

            try {

                _uiState.value = AuthUiState.Loading

                val credentialManager =
                    CredentialManager.create(context)

                val googleIdOption =
                    GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(
                            context.getString(R.string.default_web_client_id)
                        )
                        .build()

                val request =
                    GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential

                if (
                    credential is CustomCredential &&
                    credential.type ==
                    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {

                    val googleCredential =
                        GoogleIdTokenCredential.createFrom(
                            credential.data
                        )

                    val idToken = googleCredential.idToken

                    val repoResult =
                        authRepository.googleLogin(idToken)

                    repoResult
                        .onSuccess {

                            _uiState.value =
                                AuthUiState.Success
                        }

                        .onFailure {

                            _uiState.value =
                                AuthUiState.Error(
                                    it.message ?: "Login Failed"
                                )
                        }
                }

            } catch (e: Exception) {

                _uiState.value =
                    AuthUiState.Error(
                        e.localizedMessage
                            ?: "Something went wrong"
                    )
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun logout() {
        authRepository.logout()
    }
}