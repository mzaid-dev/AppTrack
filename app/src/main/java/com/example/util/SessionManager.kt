package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

/**
 * Production-grade persistent SessionManager for AppTrack.
 * Backed by Android SharedPreferences for zero-latency, instant cold-start session resolution.
 * Automatically synchronizes with Firebase Auth if configured.
 */
class SessionManager(context: Context) {

    private val appContext: Context = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "apptrack_session_prefs"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_USER_ROLE = "key_user_role"
        private const val KEY_CONTRACT_ID = "key_contract_id"
        private const val KEY_LAST_LOGIN_TIMESTAMP = "key_last_login_timestamp"
    }

    /**
     * Synchronous, zero-latency check if the user has an active persistent session.
     */
    fun isLoggedIn(): Boolean {
        if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            return true
        }
        // Fallback: check Firebase Auth if initialized
        return try {
            if (FirebaseApp.getApps(appContext).isNotEmpty()) {
                FirebaseAuth.getInstance().currentUser != null
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Persist user session data immediately with synchronous commit or fast apply.
     */
    fun saveSession(email: String, role: String = "CLIENT", contractId: String = "") {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_EMAIL, email.trim())
            .putString(KEY_USER_ROLE, role.ifBlank { "CLIENT" })
            .putString(KEY_CONTRACT_ID, contractId)
            .putLong(KEY_LAST_LOGIN_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    /**
     * Retrieve the stored user email, with fallback to Firebase Auth.
     */
    fun getUserEmail(): String {
        val storedEmail = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        if (storedEmail.isNotBlank()) return storedEmail
        return try {
            if (FirebaseApp.getApps(appContext).isNotEmpty()) {
                FirebaseAuth.getInstance().currentUser?.email ?: ""
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Retrieve the user role (default "CLIENT").
     */
    fun getUserRole(): String {
        return prefs.getString(KEY_USER_ROLE, "CLIENT") ?: "CLIENT"
    }

    /**
     * Retrieve the user's selected contract id.
     */
    fun getContractId(): String {
        return prefs.getString(KEY_CONTRACT_ID, "") ?: ""
    }

    /**
     * Retrieve user display name from Firebase Auth or email.
     */
    fun getUserDisplayName(): String {
        return try {
            if (FirebaseApp.getApps(appContext).isNotEmpty()) {
                val fb = FirebaseAuth.getInstance().currentUser
                val name = fb?.displayName
                if (!name.isNullOrBlank()) return name
                val email = fb?.email
                if (!email.isNullOrBlank()) return email.substringBefore("@")
            }
            val email = getUserEmail()
            if (email.isNotBlank()) email.substringBefore("@") else "User"
        } catch (e: Exception) {
            val email = getUserEmail()
            if (email.isNotBlank()) email.substringBefore("@") else "User"
        }
    }

    /**
     * Clear all session data on logout.
     */
    fun clearSession() {
        prefs.edit()
            .remove(KEY_IS_LOGGED_IN)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_ROLE)
            .remove(KEY_CONTRACT_ID)
            .remove(KEY_LAST_LOGIN_TIMESTAMP)
            .apply()
    }
}
