package com.bikeshare.app.domain.update

import android.content.Context
import android.content.SharedPreferences
import com.bikeshare.app.BuildConfig
import com.bikeshare.app.data.api.github.GitHubApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateChecker @Inject constructor(
    @ApplicationContext context: Context,
    private val api: GitHubApiService,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * @param force bypass the 24h throttle (useful for testing / manual triggers)
     */
    suspend fun checkForUpdate(force: Boolean = false): UpdateInfo? {
        if (BuildConfig.UPDATE_CHECK_URL.isBlank()) return null

        if (!force && !shouldCheckNow()) return null

        val release = runCatching { api.getLatestRelease(BuildConfig.UPDATE_CHECK_URL) }
            .getOrNull()?.takeIf { it.isSuccessful }?.body()
            ?: return null

        prefs.edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply()

        if (release.draft || release.prerelease) return null

        val latest = release.tagName.removePrefix("v")
        val current = BuildConfig.VERSION_NAME

        return if (isNewer(latest, current)) {
            Timber.i("Update available: $current → $latest")
            UpdateInfo(latestVersion = latest, releaseUrl = release.htmlUrl)
        } else {
            null
        }
    }

    private fun shouldCheckNow(): Boolean {
        val lastCheck = prefs.getLong(KEY_LAST_CHECK, 0L)
        return System.currentTimeMillis() - lastCheck >= CHECK_INTERVAL_MILLIS
    }

    private fun isNewer(latest: String, current: String): Boolean {
        val l = latest.split('.').mapNotNull { it.toIntOrNull() }
        val c = current.split('.').mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(l.size, c.size)) {
            val a = l.getOrElse(i) { 0 }
            val b = c.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        return false
    }

    companion object {
        private const val PREFS_NAME = "update_checker"
        private const val KEY_LAST_CHECK = "last_check_millis"
        private val CHECK_INTERVAL_MILLIS = TimeUnit.HOURS.toMillis(24)
    }
}
