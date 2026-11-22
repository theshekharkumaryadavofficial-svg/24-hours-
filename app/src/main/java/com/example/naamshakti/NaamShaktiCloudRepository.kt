package com.example.naamshakti

import android.content.Context
import com.example.R
import com.example.data.NaamJapDayEntity
import com.example.firebase.FirestoreErrorHandler
import com.example.firebase.OperationType
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NaamShaktiCloudRepository(
    context: Context
) {
    private val appContext = context.applicationContext

    private val db: FirebaseFirestore by lazy {
        val dbId = appContext.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private fun requireUserId(): String =
        Firebase.auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Google Sign-In required for Firebase cloud sync.")

    fun currentUserIdOrNull(): String? =
        Firebase.auth.currentUser?.uid?.takeIf { it.isNotBlank() }

    fun observeCloudProfile(userId: String): Flow<Result<FirestoreNaamShaktiProfile?>> {
        val path = "users/$userId/naam_shakti/profile"
        return db.document(path)
            .snapshots()
            .map { snapshot ->
                val profile = if (snapshot.exists()) {
                    snapshot.toObject(
                        FirestoreNaamShaktiProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
                Result.success(profile)
            }
            .catch { error ->
                if (error is Exception) {
                    FirestoreErrorHandler.handleFirestoreError(error, OperationType.GET, path)
                }
                emit(Result.failure(error))
            }
    }

    fun observeCloudHistory(userId: String): Flow<Result<List<FirestoreShaktiHistoryEvent>>> {
        val path = "users/$userId/shakti_history"
        return db.collection(path)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(80)
            .snapshots()
            .map { querySnapshot ->
                val list = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(
                        FirestoreShaktiHistoryEvent::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                Result.success(list)
            }
            .catch { error ->
                if (error is Exception) {
                    FirestoreErrorHandler.handleFirestoreError(error, OperationType.LIST, path)
                }
                emit(Result.failure(error))
            }
    }

    suspend fun upsertProfile(entity: NaamShaktiProfileEntity): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val path = "users/$uid/naam_shakti/profile"
        return try {
            val docRef = db.document(path)
            val exists = docRef.get().await().exists()
            if (!exists) {
                val createPayload = hashMapOf<String, Any>(
                    "userId" to uid,
                    "selectedWarriorId" to entity.selectedWarriorId.take(40),
                    "unlockedWarriorsCsv" to entity.unlockedWarriorsCsv.take(200),
                    "unlockedBattlefield" to entity.unlockedBattlefield,
                    "koRecoveryActive" to entity.koRecoveryActive,
                    "currentShaktiEnergy" to entity.currentShaktiEnergy.coerceAtLeast(0),
                    "lifetimeShaktiEarned" to entity.lifetimeShaktiEarned.coerceAtLeast(0),
                    "lifetimeShaktiSpent" to entity.lifetimeShaktiSpent.coerceAtLeast(0),
                    "syncedAllTimeNaam" to entity.syncedAllTimeNaam.coerceAtLeast(0),
                    "koBaselineAllTimeNaam" to entity.koBaselineAllTimeNaam.coerceAtLeast(0),
                    "battlesWon" to entity.battlesWon.coerceAtLeast(0),
                    "battlesPlayed" to entity.battlesPlayed.coerceAtLeast(0),
                    "highestMissionCleared" to entity.highestMissionCleared.coerceAtLeast(0),
                    "attackLevel" to entity.attackLevel.coerceIn(1, 50),
                    "defenseLevel" to entity.defenseLevel.coerceIn(1, 50),
                    "auraLevel" to entity.auraLevel.coerceIn(1, 50),
                    "transformationStage" to entity.transformationStage.coerceIn(0, 3),
                    "warriorXp" to entity.warriorXp.coerceAtLeast(0),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            } else {
                val updatePayload = hashMapOf<String, Any>(
                    "selectedWarriorId" to entity.selectedWarriorId.take(40),
                    "unlockedWarriorsCsv" to entity.unlockedWarriorsCsv.take(200),
                    "unlockedBattlefield" to entity.unlockedBattlefield,
                    "koRecoveryActive" to entity.koRecoveryActive,
                    "currentShaktiEnergy" to entity.currentShaktiEnergy.coerceAtLeast(0),
                    "lifetimeShaktiEarned" to entity.lifetimeShaktiEarned.coerceAtLeast(0),
                    "lifetimeShaktiSpent" to entity.lifetimeShaktiSpent.coerceAtLeast(0),
                    "syncedAllTimeNaam" to entity.syncedAllTimeNaam.coerceAtLeast(0),
                    "koBaselineAllTimeNaam" to entity.koBaselineAllTimeNaam.coerceAtLeast(0),
                    "battlesWon" to entity.battlesWon.coerceAtLeast(0),
                    "battlesPlayed" to entity.battlesPlayed.coerceAtLeast(0),
                    "highestMissionCleared" to entity.highestMissionCleared.coerceAtLeast(0),
                    "attackLevel" to entity.attackLevel.coerceIn(1, 50),
                    "defenseLevel" to entity.defenseLevel.coerceIn(1, 50),
                    "auraLevel" to entity.auraLevel.coerceIn(1, 50),
                    "transformationStage" to entity.transformationStage.coerceIn(0, 3),
                    "warriorXp" to entity.warriorXp.coerceAtLeast(0),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun syncNaamJapDay(day: NaamJapDayEntity): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val safeDateKey = day.dateKey.replace(Regex("[^a-zA-Z0-9._-]"), "").take(16)
        if (safeDateKey.length < 8) return Result.failure(IllegalArgumentException("Invalid date key"))
        val path = "users/$uid/naam_jap_days/$safeDateKey"
        return try {
            val docRef = db.document(path)
            val exists = docRef.get().await().exists()
            if (!exists) {
                val createPayload = hashMapOf<String, Any>(
                    "userId" to uid,
                    "dateKey" to safeDateKey,
                    "displayDate" to day.displayDate.ifBlank { safeDateKey }.take(30),
                    "dayName" to day.dayName.ifBlank { "Today" }.take(20),
                    "count" to day.count.coerceAtLeast(0),
                    "goalAtDay" to day.goalAtDay.coerceIn(1, 1_000_000),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            } else {
                val updatePayload = hashMapOf<String, Any>(
                    "displayDate" to day.displayDate.ifBlank { safeDateKey }.take(30),
                    "dayName" to day.dayName.ifBlank { "Today" }.take(20),
                    "count" to day.count.coerceAtLeast(0),
                    "goalAtDay" to day.goalAtDay.coerceIn(1, 1_000_000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun appendHistoryEvent(event: ShaktiHistoryEntity): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val safeId = event.eventId.replace(Regex("[^a-zA-Z0-9._-]"), "").take(64)
            .ifBlank { "evt_${System.currentTimeMillis()}" }
        val path = "users/$uid/shakti_history/$safeId"
        return try {
            val payload = hashMapOf<String, Any>(
                "userId" to uid,
                "eventType" to event.eventType,
                "title" to event.title.ifBlank { "Shakti Event" }.take(100),
                "subtitle" to event.subtitle.take(200),
                "shaktiDelta" to event.shaktiDelta.coerceIn(-1_000_000, 1_000_000),
                "naamCountSnapshot" to event.naamCountSnapshot.coerceAtLeast(0),
                "warriorId" to event.warriorId.ifBlank { "vajra_arjun" }.take(40),
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.document(path).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }
}
