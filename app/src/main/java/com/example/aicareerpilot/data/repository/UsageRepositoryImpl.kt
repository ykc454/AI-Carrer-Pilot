package com.example.aicareerpilot.data.repository

import com.example.aicareerpilot.domain.repository.UsageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateField.count
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject

class UsageRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UsageRepository {

    override suspend fun canAnalyze(): Boolean {

        val uid = auth.currentUser?.uid ?: return false

        val doc =
            firestore.collection("users")
                .document(uid)
                .get()
                .await()

        val today = LocalDate.now().toString()

        val count =
            doc.getLong("usageCount") ?: 0

        val lastDate =
            doc.getString("lastResetDate")

        return if (lastDate != today) {
            true
        } else {
            count < 3
        }
    }

    override suspend fun incrementUsage() {

        val uid = auth.currentUser?.uid ?: return

        val docRef =
            firestore.collection("users")
                .document(uid)

        val today =
            LocalDate.now().toString()

        val snapshot =
            docRef.get().await()

        val lastDate =
            snapshot.getString("lastResetDate")

        if (lastDate != today) {

            docRef.set(
                mapOf(
                    "usageCount" to 1,
                    "lastResetDate" to today
                ),
                SetOptions.merge()
            ).await()

        } else {

            docRef.update(
                "usageCount",
                FieldValue.increment(1)
            ).await()
        }
    }

    override suspend fun getRemainingAttempts(): Int {

        val uid = auth.currentUser?.uid ?: return 0

        val doc =
            firestore.collection("users")
                .document(uid)
                .get()
                .await()

        val today =
            LocalDate.now().toString()

        val lastDate =
            doc.getString("lastResetDate")

        val count =
            doc.getLong("usageCount")?.toInt() ?: 0

        return if (lastDate != today) {
            3
        } else {
            (3 - count).coerceAtLeast(0)
        }
    }
}