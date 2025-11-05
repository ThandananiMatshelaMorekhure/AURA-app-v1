package com.donation.auraappmarkup

import com.google.firebase.database.Exclude
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SymptomEntry(
    var id: String = "",
    var userId: String = "",
    var date: String = "",
    var symptoms: List<String> = emptyList(),
    var mood: String = "",
    var flow: String = "",
    var notes: String = "",
    var timestamp: Long = System.currentTimeMillis()
) {
    // Required empty constructor for Firebase
    constructor() : this("", "", "", emptyList(), "", "", "", 0)

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "date" to date,
            "symptoms" to symptoms,
            "mood" to mood,
            "flow" to flow,
            "notes" to notes,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): SymptomEntry {
            return SymptomEntry(
                id = map["id"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                date = map["date"] as? String ?: "",
                symptoms = (map["symptoms"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                mood = map["mood"] as? String ?: "",
                flow = map["flow"] as? String ?: "",
                notes = map["notes"] as? String ?: "",
                timestamp = map["timestamp"] as? Long ?: 0
            )
        }

        fun getCurrentDate(): String {
            return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        }
    }
}