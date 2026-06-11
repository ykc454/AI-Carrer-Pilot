package com.example.aicareerpilot.data.model.news

import com.google.gson.annotations.SerializedName


data class QuestionsResponse(
    @SerializedName("items")
    val items: List<Question>
)
