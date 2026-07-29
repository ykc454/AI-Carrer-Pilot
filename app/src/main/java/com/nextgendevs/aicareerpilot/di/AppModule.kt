package com.nextgendevs.aicareerpilot.di

import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import androidx.room.Room
import com.nextgendevs.aicareerpilot.data.local.AnalysisDao
import com.nextgendevs.aicareerpilot.data.local.AppDatabase
import com.google.ai.client.generativeai.GenerativeModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.nextgendevs.aicareerpilot.BuildConfig
import com.nextgendevs.aicareerpilot.domain.repository.AuthRepository
import com.nextgendevs.aicareerpilot.domain.repository.ResumeRepository
import com.nextgendevs.aicareerpilot.data.repository.FirebaseAuthRepository
import com.nextgendevs.aicareerpilot.data.repository.ResumeRepositoryImpl
import com.nextgendevs.aicareerpilot.data.repository.UsageRepositoryImpl
import com.nextgendevs.aicareerpilot.domain.repository.UsageRepository
import com.nextgendevs.aicareerpilot.util.DocumentHelper
import com.google.firebase.firestore.FirebaseFirestore
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
            )
                .fallbackToDestructiveMigration()
                .build()
        }

        @Provides
        fun provideAnalysisDao(
            database: AppDatabase
        ): AnalysisDao {
            return database.analysisDao()
        }
        @Provides
        @Singleton
        fun provideFirestore(): FirebaseFirestore {
            return FirebaseFirestore.getInstance()
        }

        @Provides
        @Singleton
        fun provideUsageRepository(
            firestore: FirebaseFirestore,
            auth: FirebaseAuth
        ): UsageRepository {
            return UsageRepositoryImpl(
                firestore,
                auth
            )
        }
    }
}