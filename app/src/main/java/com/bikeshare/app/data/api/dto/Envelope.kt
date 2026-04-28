package com.bikeshare.app.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Generic success envelope: { data: T, meta?: { requestId, timestamp } } — meta optional for resilience */
@JsonClass(generateAdapter = true)
data class ApiEnvelope<T>(
    @Json(name = "data") val data: T,
    @Json(name = "meta") val meta: ResponseMeta? = null,
)

@JsonClass(generateAdapter = true)
data class ResponseMeta(
    @Json(name = "requestId") val requestId: String? = null,
    @Json(name = "timestamp") val timestamp: String? = null,
)

/** application/problem+json error body */
@JsonClass(generateAdapter = true)
data class ProblemDetail(
    @Json(name = "type") val type: String,
    @Json(name = "title") val title: String,
    @Json(name = "status") val status: Int,
    @Json(name = "detail") val detail: String,
    @Json(name = "instance") val instance: String,
    @Json(name = "requestId") val requestId: String,
    @Json(name = "code") val code: String? = null,
    @Json(name = "params") val params: Map<String, Any?>? = null,
)
