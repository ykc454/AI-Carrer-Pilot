package com.nextgendevs.aicareerpilot.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import com.nextgendevs.aicareerpilot.data.model.news.Question
import kotlinx.coroutines.flow.Flow

/** Offline cache of the Job Market / StackOverflow feed. */
@Entity(tableName = "cached_questions")
data class QuestionEntity(
    @PrimaryKey val link: String,
    val title: String,
    val tags: String,          // comma separated
    val viewCount: Int,
    val score: Int,
    val answerCount: Int,
    val position: Int,         // preserves the API's "hot" ordering
    val cachedAt: Long
)

fun QuestionEntity.toQuestion() = Question(
    title = title,
    link = link,
    tags = if (tags.isBlank()) emptyList() else tags.split(","),
    viewCount = viewCount,
    score = score,
    answerCount = answerCount
)

fun Question.toEntity(position: Int, cachedAt: Long) = QuestionEntity(
    link = link,
    title = title,
    tags = tags.joinToString(","),
    viewCount = viewCount,
    score = score,
    answerCount = answerCount,
    position = position,
    cachedAt = cachedAt
)

@Dao
interface QuestionDao {

    @Query("SELECT * FROM cached_questions ORDER BY position ASC")
    fun observeQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT MAX(cachedAt) FROM cached_questions")
    suspend fun lastCachedAt(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<QuestionEntity>)

    @Query("DELETE FROM cached_questions")
    suspend fun clear()

    /** Atomic swap so observers never see an empty list mid-refresh. */
    @Transaction
    suspend fun replaceAll(items: List<QuestionEntity>) {
        clear()
        insertAll(items)
    }
}
