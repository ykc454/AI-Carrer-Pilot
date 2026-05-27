package com.example.aicareerpilot.data.model.news


data class NewsArticle(

    val source: Source?,

    val author: String?,

    val title: String,

    val description: String?,

    val url: String?,

    val urlToImage: String?,

    val publishedAt: String?,

    val content: String?
)


data class Source(

    val id: String?,

    val name: String?
)



