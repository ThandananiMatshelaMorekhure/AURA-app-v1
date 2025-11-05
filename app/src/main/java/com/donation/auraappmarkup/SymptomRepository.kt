package com.donation.auraappmarkup

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SymptomRepository {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw Exception("User not authenticated")
    }

    private fun getUserSymptomEntriesRef() = database.reference
        .child("users")
        .child(getCurrentUserId())
        .child("symptomEntries")

    // Add symptom entry to Firebase
    suspend fun addSymptomEntry(entry: SymptomEntry): String {
        val entryRef = getUserSymptomEntriesRef().push()
        entry.id = entryRef.key ?: throw Exception("Failed to generate entry ID")
        entry.userId = getCurrentUserId()
        entry.timestamp = System.currentTimeMillis()
        entryRef.setValue(entry.toMap()).await()
        return entry.id
    }

    // Update symptom entry in Firebase
    suspend fun updateSymptomEntry(entry: SymptomEntry): Boolean {
        return try {
            entry.timestamp = System.currentTimeMillis()
            getUserSymptomEntriesRef().child(entry.id).setValue(entry.toMap()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Delete symptom entry from Firebase
    suspend fun deleteSymptomEntry(entryId: String): Boolean {
        return try {
            getUserSymptomEntriesRef().child(entryId).removeValue().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Get all symptom entries as Flow for real-time updates
    fun getAllSymptomEntries(): Flow<List<SymptomEntry>> = callbackFlow {
        val entriesRef = getUserSymptomEntriesRef()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entries = mutableListOf<SymptomEntry>()
                snapshot.children.forEach { child ->
                    val entryMap = child.value as? Map<*, *>
                    entryMap?.let {
                        val entry = SymptomEntry.fromMap(it as Map<String, Any?>)
                        entries.add(entry)
                    }
                }
                trySend(entries)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        entriesRef.addValueEventListener(valueEventListener)

        awaitClose {
            entriesRef.removeEventListener(valueEventListener)
        }
    }

    // Get symptom entries sorted by date (newest first)
    fun getSymptomEntriesSorted(): Flow<List<SymptomEntry>> {
        return getAllSymptomEntries().map { entries ->
            entries.sortedByDescending { it.timestamp }
        }
    }

    // Get today's symptom entry if it exists
    suspend fun getTodaysEntry(): SymptomEntry? {
        val today = SymptomEntry.getCurrentDate()
        return try {
            val snapshot = getUserSymptomEntriesRef().get().await()
            snapshot.children.mapNotNull { child ->
                val entryMap = child.value as? Map<*, *>
                entryMap?.let { SymptomEntry.fromMap(it as Map<String, Any?>) }
            }.find { it.date == today }
        } catch (e: Exception) {
            null
        }
    }
}