    package com.example.aicareerpilot.di

    import com.example.aicareerpilot.data.remote.StackOverflowApi
    import dagger.Module
    import dagger.Provides
    import dagger.hilt.InstallIn
    import dagger.hilt.components.SingletonComponent
    import retrofit2.Retrofit
    import retrofit2.converter.gson.GsonConverterFactory
    import javax.inject.Singleton
    import kotlin.jvm.java

    @Module
    @InstallIn(SingletonComponent::class)
    object StackOverflowModule {

        @Provides
        @Singleton
        fun provideApi(): StackOverflowApi {

            return Retrofit.Builder()
                .baseUrl("https://api.stackexchange.com/2.3/")
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .build()
                .create(StackOverflowApi::class.java)
        }
    }