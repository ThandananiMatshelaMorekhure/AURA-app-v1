package com.donation.auraappmarkup.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserPrefsDao {

    // Your original method (alias for insertPreferences)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreferences(prefs: UserPreferences)

    // Generated method - same functionality
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(userPrefs: UserPreferences)

    @Query("SELECT * FROM user_prefs WHERE userId = :userId LIMIT 1")
    suspend fun getPreferences(userId: String): UserPreferences?

    @Update
    suspend fun updatePreferences(userPrefs: UserPreferences)

    @Query("DELETE FROM user_prefs WHERE userId = :userId")
    suspend fun deletePreferences(userId: String)
}