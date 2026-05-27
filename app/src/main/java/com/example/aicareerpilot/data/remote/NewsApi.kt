package com.example.aicareerpilot.data.remote

import com.example.aicareerpilot.data.model.news.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    @GET("v2/everything")
    suspend fun getJobNews(

        @Query("q")
        query: String = "software jobs OR hiring trends",

        @Query("apiKey")
        apiKey: String

    ): NewsResponse
}




