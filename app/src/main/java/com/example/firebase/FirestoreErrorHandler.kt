package com.example.firebase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import org.json.JSONObject

enum class OperationType {
    CREATE,
    UPDATE,
    DELETE,
    LIST,
    GET,
    WRITE
}

object FirestoreErrorHandler {
    private const val TAG = "FirestoreSecurity"

    fun handleFirestoreError(
        error: Exception,
        operationType: OperationType,
        path: String
    ): String {
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val payload = JSONObject().apply {
            put("error", error.message ?: error.javaClass.simpleName)
            put("operationType", operationType.name)
            put("path", path)
            put("authInfo", JSONObject().apply {
                put("userId", currentUser?.uid ?: "unauthenticated")
                put("email", currentUser?.email ?: "")
                put("isAuthenticated", currentUser != null)
            })
        }
        val json = payload.toString()
        Log.e(TAG, json, error)
        return json
    }
}
