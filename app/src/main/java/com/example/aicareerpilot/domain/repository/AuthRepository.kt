package com.example.aicareerpilot.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    suspend fun googleLogin(idToken: String): Result<String>



    fun logout()

    fun isUserLoggedIn(): Boolean

}