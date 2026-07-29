package com.nextgendevs.aicareerpilot.data.model.news
import com.google.gson.annotations.SerializedName


data class Question(

    @SerializedName("title")
    val title: String,

    @SerializedName("link")
    val link: String,

    @SerializedName("tags")
    val tags: List<String>,

    @SerializedName("view_count")
    val viewCount: Int,

    @SerializedName("score")
    val score: Int,

    @SerializedName("answer_count")
    val answerCount: Int
)
