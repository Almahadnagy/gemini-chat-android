package com.google.aistudio.geminichat.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Encrypted Storage Manager for safely storing the Gemini API Key on device hardware keystore.
 */
class SecurityManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getApiKey(): String? {
        return sharedPreferences.getString(KEY_GEMINI_API, null)
    }

    fun saveApiKey(apiKey: String) {
        sharedPreferences.edit()
            .putString(KEY_GEMINI_API, apiKey.trim())
            .apply()
    }

    fun deleteApiKey() {
        sharedPreferences.edit()
            .remove(KEY_GEMINI_API)
            .apply()
    }

    fun hasApiKey(): Boolean {
        return !getApiKey().isNullOrBlank()
    }

    companion object {
        private const val PREFS_FILE_NAME = "gemini_secure_prefs"
        private const val KEY_GEMINI_API = "encrypted_gemini_api_key"
    }
}
