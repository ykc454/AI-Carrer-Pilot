package com.nextgendevs.aicareerpilot.domain.repository

interface AuthRepository {

    suspend fun googleLogin(idToken: String): Result<String>

    suspend fun loginWithEmail(
        email: String,
        password: String
    ): Result<String>

    suspend fun registerWithEmail(
        email: String,
        password: String
    ): Result<String>

    fun logout()

    fun isUserLoggedIn(): Boolean

}