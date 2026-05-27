package com.example.aicareerpilot.data.model.news



data class NewsResponse(

    val status: String,

    val totalResults: Int,

    val articles: List<NewsArticle>
)


