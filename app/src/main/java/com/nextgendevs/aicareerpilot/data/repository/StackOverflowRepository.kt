package com.nextgendevs.aicareerpilot.data.repository

import com.nextgendevs.aicareerpilot.data.model.news.Question
import com.nextgendevs.aicareerpilot.data.remote.StackOverflowApi
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