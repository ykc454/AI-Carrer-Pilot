package com.nextgendevs.aicareerpilot.domain.repository

interface AuthRepository {

    suspend fun googleLogin(idToken: String): Result<String>

    fun logout()

    fun isUserLoggedIn(): Boolean

}