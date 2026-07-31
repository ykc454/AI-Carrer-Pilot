package com.nextgendevs.aicareerpilot.presentation.viewmodel

import android.content.Context
import android.util.Log

import com.nextgendevs.aicareerpilot.R
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextgendevs.aicareerpilot.domain.repository.AuthRepository
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
private const val TAG = "GoogleLogin"

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<AuthUiState>(AuthUiState.Idle)

    val uiState: StateFlow<AuthUiState> =
        _uiState.asStateFlow()

    private val _isLoggedIn =
        MutableStateFlow(authRepository.isUserLoggedIn())

    val isLoggedIn: StateFlow<Boolean> =
        _isLoggedIn.asStateFlow()

    fun signInWithGoogle(context: Context) {

        if (_uiState.value is AuthUiState.Loading) return

        viewModelScope.launch {

            try {

                Log.d(TAG, "Starting Google Sign In")

                _uiState.value = AuthUiState.Loading

                val credentialManager =
                    CredentialManager.create(context)

                val googleIdOption =
                    GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setAutoSelectEnabled(false)
                        .setServerClientId(
                            context.getString(R.string.default_web_client_id)
                        )
                        .build()

                Log.d(TAG, "GoogleIdOption created")

                val request =
                    GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                Log.d(TAG, "Request built")

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                Log.d(TAG, "Credential received")

                val credential = result.credential

                if (
                    credential is CustomCredential &&
                    credential.type ==
                    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {

                    Log.d(TAG, "Google credential type matched")

                    val googleCredential =
                        GoogleIdTokenCredential.createFrom(
                            credential.data
                        )

                    val idToken = googleCredential.idToken

                    Log.d(TAG, "ID Token received")

                    val repoResult =
                        authRepository.googleLogin(idToken)

                    repoResult.onSuccess {

                        Log.d(TAG, "Firebase login success")

                        _uiState.value = AuthUiState.Success
                        _isLoggedIn.value = true
                    }

                    repoResult.onFailure {

                        Log.e(TAG, "Firebase login failed: ${it.message}")

                        _uiState.value =
                            AuthUiState.Error(
                                it.message ?: "Login Failed"
                            )
                    }

                } else {

                    Log.e(TAG, "Credential type mismatch")

                    _uiState.value =
                        AuthUiState.Error(
                            "Invalid credential type"
                        )
                }

            } catch (e: Exception) {

                Log.e(TAG, "Google Sign In Exception", e)

                _uiState.value =
                    AuthUiState.Error(
                        e.localizedMessage
                            ?: "Something went wrong"
                    )
            }
        }
    }
    fun loginWithEmail(
        email: String,
        password: String
    ) {

        if (_uiState.value is AuthUiState.Loading) return

        viewModelScope.launch {

            _uiState.value = AuthUiState.Loading

            val result = authRepository.loginWithEmail(email, password)

            result.onSuccess {
                _uiState.value = AuthUiState.Success
                _isLoggedIn.value = true
            }

            result.onFailure {
                _uiState.value =
                    AuthUiState.Error(
                        it.message ?: "Login Failed"
                    )
            }
        }
    }

    fun registerWithEmail(
        email: String,
        password: String
    ) {

        if (_uiState.value is AuthUiState.Loading) return

        viewModelScope.launch {

            _uiState.value = AuthUiState.Loading

            val result = authRepository.registerWithEmail(email, password)

            result.onSuccess {
                _uiState.value = AuthUiState.Success
                _isLoggedIn.value = true
            }

            result.onFailure {
                _uiState.value =
                    AuthUiState.Error(
                        it.message ?: "Registration Failed"
                    )
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _isLoggedIn.value = false
    }



    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}