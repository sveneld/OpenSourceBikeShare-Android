package com.bikeshare.app.data.api.github

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface GitHubApiService {
    @GET
    suspend fun getLatestRelease(@Url url: String): Response<GitHubReleaseDto>
}
