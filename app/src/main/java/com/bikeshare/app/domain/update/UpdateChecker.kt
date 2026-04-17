package com.bikeshare.app.domain.update

import com.bikeshare.app.BuildConfig
import com.bikeshare.app.data.api.github.GitHubApiService
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateChecker @Inject constructor(
    private val api: GitHubApiService,
) {
    suspend fun checkForUpdate(): UpdateInfo? {
        if (BuildConfig.UPDATE_CHECK_URL.isBlank()) return null

        val release = runCatching { api.getLatestRelease(BuildConfig.UPDATE_CHECK_URL) }
            .getOrNull()?.takeIf { it.isSuccessful }?.body()
            ?: return null

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
}
