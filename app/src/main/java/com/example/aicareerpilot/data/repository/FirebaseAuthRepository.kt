package com.example.aicareerpilot.data.repository


import com.example.aicareerpilot.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
        private val firebaseAuth: FirebaseAuth
    ) : AuthRepository {

    override suspend fun googleLogin(idToken: String): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken,null)
            val result = firebaseAuth.signInWithCredential(credential).await()

            val uid = result.user?.uid
            if(uid != null){
                Result.success(uid)
            }else{
                Result.failure(Exception("User is null"))
            }

        }catch (e: Exception){
            Result.failure(e)
        }
    }


    override fun logout() {
        firebaseAuth.signOut()
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}