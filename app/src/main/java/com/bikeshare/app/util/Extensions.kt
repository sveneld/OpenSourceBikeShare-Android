package com.bikeshare.app.util

import com.bikeshare.app.data.api.dto.ApiEnvelope
import com.bikeshare.app.data.api.dto.ProblemDetail
import com.squareup.moshi.Moshi
import retrofit2.Response
import timber.log.Timber

/** Safely execute an API call and wrap the result */
suspend fun <T> safeApiCall(
    moshi: Moshi,
    call: suspend () -> Response<ApiEnvelope<T>>,
): NetworkResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body.data)
            } else {
                NetworkResult.Error("Empty response body")
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val detail = try {
                errorBody?.let {
                    moshi.adapter(ProblemDetail::class.java).fromJson(it)
                }
            } catch (_: Exception) {
                null
            }
            NetworkResult.Error(
                message = detail?.detail ?: "HTTP ${response.code()}: ${response.message()}",
                code = response.code(),
                messageCode = detail?.code,
                messageParams = detail?.params,
            )
        }
    } catch (e: Exception) {
        Timber.e(e, "API call failed")
        NetworkResult.Error(e.localizedMessage ?: "Unknown error")
    }
}
