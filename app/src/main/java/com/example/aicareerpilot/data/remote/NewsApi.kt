package com.example.aicareerpilot.data.remote

import com.example.aicareerpilot.data.model.news.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    @GET("v2/everything")
    suspend fun getJobNews(

        @Query("q")
        query: String =
            "software engineering jobs OR AI hiring OR tech careers",

        @Query("language")
        language: String = "en",

        @Query("sortBy")
        sortBy: String = "publishedAt",

        @Query("pageSize")
        pageSize: Int = 50,

        @Query("apiKey")
        apiKey: String

    ): NewsResponse
}




