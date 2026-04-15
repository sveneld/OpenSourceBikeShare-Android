package com.bikeshare.app.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DailyReportDto(
    @Json(name = "day") val day: String,
    @Json(name = "rentCount") val rentCount: Int,
    @Json(name = "returnCount") val returnCount: Int,
)

@JsonClass(generateAdapter = true)
data class UserReportDto(
    @Json(name = "userId") val userId: Int,
    @Json(name = "userName") val username: String,
    @Json(name = "rentCount") val rentCount: Int,
    @Json(name = "returnCount") val returnCount: Int,
    @Json(name = "totalActionCount") val totalActionCount: Int,
)

@JsonClass(generateAdapter = true)
data class InactiveBikeDto(
    @Json(name = "bikeNum") val bikeNum: Int,
    @Json(name = "standName") val standName: String,
    @Json(name = "lastMoveTime") val lastMoveTime: String,
    @Json(name = "inactiveDays") val inactiveDays: Int,
)
