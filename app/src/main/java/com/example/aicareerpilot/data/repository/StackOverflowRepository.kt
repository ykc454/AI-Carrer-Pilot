package com.example.aicareerpilot.data.repository

import com.example.aicareerpilot.data.model.news.Question
import com.example.aicareerpilot.data.remote.StackOverflowApi
import javax.inject.Inject



class StackOverflowRepository @Inject constructor(
    private val api: StackOverflowApi
) {

    suspend fun getQuestions(): List<Question> {

        return api
            .getHotQuestions()
            .items
    }
}