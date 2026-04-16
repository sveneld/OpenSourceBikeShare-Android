package com.bikeshare.app.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "userId") val userId: Int,
    @Json(name = "userName") val username: String? = null,
    @Json(name = "city") val city: String? = null,
    @Json(name = "mail") val mail: String? = null,
    @Json(name = "number") val number: String? = null,
    @Json(name = "privileges") val privileges: Int? = null,
    @Json(name = "credit") val credit: Double? = null,
    @Json(name = "userLimit") val userLimit: Int? = null,
    @Json(name = "isNumberConfirmed") val isNumberConfirmed: Int? = null,
    @Json(name = "registrationDate") val registrationDate: String? = null,
)

@JsonClass(generateAdapter = true)
data class UserLimitsDto(
    @Json(name = "limit") val limit: Int? = null,
    @Json(name = "rented") val rented: Int? = null,
    @Json(name = "userCredit") val userCredit: Double? = null,
    @Json(name = "freeTimeMinutes") val freeTimeMinutes: Int? = null,
    @Json(name = "privileges") val privileges: Int? = null,
)

@JsonClass(generateAdapter = true)
data class CreditHistoryItemDto(
    @Json(name = "date") val date: String,
    @Json(name = "amount") val amount: Double,
    @Json(name = "type") val type: String,
    @Json(name = "balance") val balance: Double,
)

@JsonClass(generateAdapter = true)
data class TripItemDto(
    @Json(name = "rentTime") val rentTime: String,
    @Json(name = "bikeNumber") val bikeNumber: Int,
    @Json(name = "returnTime") val returnTime: String? = null,
    @Json(name = "standName") val standName: String? = null,
    @Json(name = "fromStandName") val fromStandName: String? = null,
)

@JsonClass(generateAdapter = true)
data class ChangeCityRequest(
    @Json(name = "city") val city: String,
)

@JsonClass(generateAdapter = true)
data class AddCreditRequest(
    @Json(name = "multiplier") val multiplier: Int,
)
