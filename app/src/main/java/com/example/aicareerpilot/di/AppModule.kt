package com.example.aicareerpilot.di

import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import androidx.room.Room
import com.example.aicareerpilot.data.local.AnalysisDao
import com.example.aicareerpilot.data.local.AppDatabase
import com.google.ai.client.generativeai.GenerativeModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.aicareerpilot.BuildConfig
import com.example.aicareerpilot.domain.repository.AuthRepository
import com.example.aicareerpilot.domain.repository.ResumeRepository
import com.example.aicareerpilot.data.repository.FirebaseAuthRepository
import com.example.aicareerpilot.data.repository.ResumeRepositoryImpl
import com.example.aicareerpilot.util.DocumentHelper
import dagger.Binds


@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindResumeRepository(
        impl: ResumeRepositoryImpl
    ): ResumeRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: FirebaseAuthRepository
    ): AuthRepository

    companion object {

        @Provides
        @Singleton
        fun provideGenerativeModel(): GenerativeModel {
            return GenerativeModel(
                modelName = "gemini-2.5-flash",
                apiKey = BuildConfig.GEMINI_API_KEY
            )
        }

        @Provides
        @Singleton
        fun provideDocumentHelper(
            @ApplicationContext context: Context
        ): DocumentHelper {
            return DocumentHelper(context)
        }

        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth {
            return FirebaseAuth.getInstance()
        }

        @Provides
        @Singleton
        fun provideDatabase(
            @ApplicationContext context: Context
        ): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "ai_career_pilot_db"
            ).build()
        }

        @Provides
        fun provideAnalysisDao(
            database: AppDatabase
        ): AnalysisDao {
            return database.analysisDao()
        }
    }
}