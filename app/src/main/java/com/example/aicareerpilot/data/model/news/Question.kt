package com.example.aicareerpilot.data.model.news
import com.google.gson.annotations.SerializedName


data class Question(
    val title: String,

    val link: String,

    val tags: List<String>,

    @SerializedName("view_count")
    val viewCount: Int,

    val score: Int,

    @SerializedName("answer_count")
    val answerCount: Int
)
