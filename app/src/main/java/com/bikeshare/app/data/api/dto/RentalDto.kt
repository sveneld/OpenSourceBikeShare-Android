package com.bikeshare.app.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RentRequest(
    @Json(name = "bikeNumber") val bikeNumber: Int,
)

@JsonClass(generateAdapter = true)
data class ReturnRequest(
    @Json(name = "bikeNumber") val bikeNumber: Int,
    @Json(name = "standName") val standName: String,
    @Json(name = "note") val note: String? = null,
)

@JsonClass(generateAdapter = true)
data class RentedBikeDto(
    @Json(name = "bikeNum") val bikeNum: Int,
    @Json(name = "currentCode") val currentCode: String? = null,
    @Json(name = "rentedSeconds") val rentedSeconds: Int? = null,
    @Json(name = "oldCode") val oldCode: String? = null,
)

@JsonClass(generateAdapter = true)
data class RevertRequest(
    @Json(name = "bikeNumber") val bikeNumber: Int,
)

@JsonClass(generateAdapter = true)
data class ForceRentRequest(
    @Json(name = "bikeNumber") val bikeNumber: Int,
)

@JsonClass(generateAdapter = true)
data class ForceReturnRequest(
    @Json(name = "bikeNumber") val bikeNumber: Int,
    @Json(name = "standName") val standName: String,
    @Json(name = "note") val note: String? = null,
)

@JsonClass(generateAdapter = true)
data class RentSystemResultDto(
    @Json(name = "error") val error: Boolean = false,
    @Json(name = "message") val message: String? = null,
    @Json(name = "code") val code: String? = null,
    @Json(name = "params") val params: RentSystemParamsDto? = null,
)

@JsonClass(generateAdapter = true)
data class RentSystemParamsDto(
    @Json(name = "bikeNumber") val bikeNumber: Int? = null,
    @Json(name = "currentCode") val currentCode: String? = null,
    @Json(name = "newCode") val newCode: String? = null,
    @Json(name = "standName") val standName: String? = null,
    @Json(name = "note") val note: String? = null,
    @Json(name = "creditChange") val creditChange: Double? = null,
    @Json(name = "creditCurrency") val creditCurrency: String? = null,
    @Json(name = "hasNote") val hasNote: String? = null,
    @Json(name = "hasCreditChange") val hasCreditChange: String? = null,
    @Json(name = "code") val code: String? = null,
    @Json(name = "minRequiredCredit") val minRequiredCredit: Double? = null,
    @Json(name = "count") val count: Int? = null,
    @Json(name = "stackTopBike") val stackTopBike: Int? = null,
)
