    package com.example.aicareerpilot.data.remote
    import com.example.aicareerpilot.data.model.news.QuestionsResponse
    import retrofit2.http.GET
    import retrofit2.http.Query

    interface StackOverflowApi {

        @GET("questions")
        suspend fun getHotQuestions(

            @Query("order")
            order: String = "desc",

            @Query("sort")
            sort: String = "hot",

            @Query("site")
            site: String = "stackoverflow",

            @Query("pagesize")
            pageSize: Int = 50

        ): QuestionsResponse
    }