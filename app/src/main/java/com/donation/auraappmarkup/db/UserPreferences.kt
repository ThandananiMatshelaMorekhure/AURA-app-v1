package com.donation.auraappmarkup.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_prefs")
data class UserPreferences(
    @PrimaryKey val userId: String,
    val lastPeriod: String,
    val cycleLength: String,
    val periodDuration: String
)
