package com.bikeshare.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "bikeshare_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    } catch (e: Exception) {
        // Fallback to regular SharedPreferences if encrypted storage fails
        context.getSharedPreferences("bikeshare_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_PHONE_CONFIRMED = "phone_confirmed"
    }

    fun saveTokens(
        accessToken: String,
        refreshToken: String,
        phoneConfirmed: Boolean = true,
    ) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putBoolean(KEY_PHONE_CONFIRMED, phoneConfirmed)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getPhoneConfirmed(): Boolean = prefs.getBoolean(KEY_PHONE_CONFIRMED, true)

    fun setPhoneConfirmed(confirmed: Boolean) {
        prefs.edit().putBoolean(KEY_PHONE_CONFIRMED, confirmed).apply()
    }

    fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_PHONE_CONFIRMED)
            .apply()
    }

    fun hasTokens(): Boolean = getAccessToken() != null
}
