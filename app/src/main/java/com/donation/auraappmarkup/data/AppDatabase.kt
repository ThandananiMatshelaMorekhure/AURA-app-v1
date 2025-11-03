package com.donation.auraappmarkup.data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.donation.auraappmarkup.db.UserPreferences
import com.donation.auraappmarkup.db.UserPrefsDao

@Database(entities = [UserPreferences::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userPrefsDao(): UserPrefsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aura_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}