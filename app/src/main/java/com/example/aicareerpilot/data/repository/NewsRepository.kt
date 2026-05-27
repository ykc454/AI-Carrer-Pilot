package com.example.aicareerpilot.data.repository


import com.example.aicareerpilot.BuildConfig
import com.example.aicareerpilot.data.remote.NewsApi
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val api: NewsApi
) {
    suspend fun getNews() =
        api.getJobNews(
            apiKey = BuildConfig.NEWS_API_KEY
        ).articles
}



