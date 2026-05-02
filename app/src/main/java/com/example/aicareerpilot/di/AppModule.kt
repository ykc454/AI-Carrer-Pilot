package com.example.aicareerpilot.di



import android.content.Context
import androidx.room.Room
import com.example.aicareerpilot.data.local.AnalysisDao
import com.example.aicareerpilot.data.local.AppDatabase
import com.example.aicareerpilot.util.PdfHelper
import com.google.ai.client.generativeai.GenerativeModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.aicareerpilot.BuildConfig

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-3.1-flash-lite-preview",
            apiKey = BuildConfig.API_KEY
        )
    }

    @Provides
    @Singleton
    fun providePdfHelper(@ApplicationContext context: Context): PdfHelper {
        return PdfHelper(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ai_career_pilot_db"
        ).build()
    }

    @Provides
    fun provideAnalysisDao(database: AppDatabase): AnalysisDao {
        return database.analysisDao()
    }
}