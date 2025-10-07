package com.donation.auraappmarkup.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.donation.auraappmarkup.models.Article

/**
 * Central Room Database
 * Holds both Article + UserPreferences tables
 * Replaces the old AppDatabase to avoid duplication.
 */
@Database(
    entities = [Article::class, UserPreferences::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ArticleDatabase : RoomDatabase() {

    abstract fun getArticleDao(): ArticleDAO
    abstract fun getUserPrefsDao(): UserPrefsDao

    companion object {
        @Volatile
        private var instance: ArticleDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context): ArticleDatabase =
            instance ?: synchronized(LOCK) {
                instance ?: buildDatabase(context).also { instance = it }
            }

        private fun buildDatabase(context: Context): ArticleDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                ArticleDatabase::class.java,
                "auraapp_database.db"
            )
                .fallbackToDestructiveMigration() // optional: auto-resets schema on mismatch
                .build()
    }
}
