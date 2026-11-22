package com.example.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed interface GoogleAuthResult {
    data class Success(val user: FirebaseUser) : GoogleAuthResult
    data object Cancelled : GoogleAuthResult
    data object NoSavedCredential : GoogleAuthResult
    data class Failure(val message: String) : GoogleAuthResult
}

object GoogleAuthHelper {
    private const val TAG = "GoogleAuthHelper"

    private fun isFirebaseReady(context: Context): Boolean =
        runCatching { FirebaseApp.getApps(context.applicationContext).isNotEmpty() }.getOrDefault(false)

    private fun resolveWebClientId(context: Context): String {
        val resId = context.resources.getIdentifier(
            "default_web_client_id",
            "string",
            context.packageName
        )
        return if (resId != 0) context.getString(resId) else ""
    }

    suspend fun attemptAutoSignIn(activityContext: Context): GoogleAuthResult {
        if (!isFirebaseReady(activityContext)) return GoogleAuthResult.NoSavedCredential
        val existing = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (existing != null) return GoogleAuthResult.Success(existing)

        return try {
            val webClientId = resolveWebClientId(activityContext)
            if (webClientId.isBlank()) {
                return GoogleAuthResult.NoSavedCredential
            }
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(activityContext)
            val result = credentialManager.getCredential(
                context = activityContext,
                request = request
            )
            exchangeGoogleCredential(result.credential)
        } catch (e: CancellationException) {
            throw e
        } catch (_: NoCredentialException) {
            GoogleAuthResult.NoSavedCredential
        } catch (_: GetCredentialCancellationException) {
            GoogleAuthResult.Cancelled
        } catch (e: Exception) {
            Log.d(TAG, "Silent sign-in unavailable: ${e.message}")
            GoogleAuthResult.NoSavedCredential
        }
    }

    suspend fun signInWithGoogle(activityContext: Context): GoogleAuthResult {
        if (!isFirebaseReady(activityContext)) {
            return GoogleAuthResult.Failure("Cloud Sync requires Firebase config (local progress is saved automatically).")
        }
        return try {
            val webClientId = resolveWebClientId(activityContext)
            if (webClientId.isBlank()) {
                return GoogleAuthResult.Failure("Google Sign-In client ID is not configured in this build.")
            }

            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val credentialManager = CredentialManager.create(activityContext)
            val result = credentialManager.getCredential(
                context = activityContext,
                request = request
            )
            exchangeGoogleCredential(result.credential)
        } catch (e: CancellationException) {
            throw e
        } catch (_: GetCredentialCancellationException) {
            GoogleAuthResult.Cancelled
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Google Sign-In credential error: ${e.type} - ${e.message}")
            GoogleAuthResult.Failure(
                e.message ?: "Google Sign-In could not complete. Please try again."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected sign-in error", e)
            GoogleAuthResult.Failure(e.message ?: "Sign-In failed. Please try again.")
        }
    }

    private suspend fun exchangeGoogleCredential(
        credential: androidx.credentials.Credential
    ): GoogleAuthResult {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(
                googleIdTokenCredential.idToken,
                null
            )
            val authResult = Firebase.auth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user
            return if (user != null) {
                GoogleAuthResult.Success(user)
            } else {
                GoogleAuthResult.Failure("Signed in, but user session was empty.")
            }
        }
        return GoogleAuthResult.Failure("Unsupported credential type returned.")
    }

    suspend fun signOut(context: Context) {
        withContext(NonCancellable) {
            runCatching {
                CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
            }
            if (isFirebaseReady(context)) {
                runCatching { Firebase.auth.signOut() }
            }
        }
    }
}
