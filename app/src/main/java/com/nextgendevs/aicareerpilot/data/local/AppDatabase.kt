package com.nextgendevs.aicareerpilot.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nextgendevs.aicareerpilot.data.model.gemini_response.AnalysisRecord

@Database(
    entities = [AnalysisRecord::class, QuestionEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun analysisDao(): AnalysisDao
    abstract fun questionDao(): QuestionDao

    companion object {

        /** v2 -> v3: adds sync metadata (keeps user history) + question cache table. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE analysis_records ADD COLUMN remoteId TEXT NOT NULL DEFAULT ''")
                // Give every existing row a unique, stable cloud id.
                db.execSQL("UPDATE analysis_records SET remoteId = lower(hex(randomblob(16))) WHERE remoteId = ''")
                db.execSQL("ALTER TABLE analysis_records ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE analysis_records SET updatedAt = timestamp")
                db.execSQL("ALTER TABLE analysis_records ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")
                db.execSQL("ALTER TABLE analysis_records ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_analysis_records_remoteId` ON `analysis_records` (`remoteId`)")

                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `cached_questions` (
                        `link` TEXT NOT NULL, `title` TEXT NOT NULL, `tags` TEXT NOT NULL,
                        `viewCount` INTEGER NOT NULL, `score` INTEGER NOT NULL,
                        `answerCount` INTEGER NOT NULL, `position` INTEGER NOT NULL,
                        `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`link`))"""
                )
            }
        }
    }
}
