package com.bikeshare.app.util

import android.content.Context
import com.bikeshare.app.R
import com.bikeshare.app.data.api.dto.RentSystemParamsDto
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Renders server-side translation codes (e.g. `bike.rent.success`, `bike.rent.error.already_rented`)
 * into localized text using the user's device locale. The server now emits `code` + `params`
 * alongside the rendered `message`/`detail`; this lets the client localize independently.
 *
 * Falls back to the provided rendered string when the code is missing or unknown.
 */
@Singleton
class RentMessageRenderer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun render(code: String?, params: Map<String, Any?>?, fallback: String? = null): String {
        if (code.isNullOrBlank()) return fallback.orEmpty()
        val p = params ?: emptyMap()
        return when (code) {
            "bike.rent.success" -> renderRentSuccess(p)
            "bike.return.success" -> renderReturnSuccess(p)
            "bike.revert.success" -> renderRevertSuccess(p)

            "bike.rent.error.not_found" ->
                context.getString(R.string.api_bike_rent_error_not_found, p.intParam("bikeNumber") ?: 0)
            "bike.rent.error.already_rented_by_current_user" ->
                context.getString(
                    R.string.api_bike_rent_error_already_rented_by_current_user,
                    p.intParam("bikeNumber") ?: 0,
                    p.stringParam("currentCode").orEmpty(),
                )
            "bike.rent.error.already_rented" ->
                context.getString(R.string.api_bike_rent_error_already_rented, p.intParam("bikeNumber") ?: 0)
            "bike.rent.error.insufficient_credit" ->
                context.getString(
                    R.string.api_bike_rent_error_insufficient_credit,
                    p.numberParam("minRequiredCredit")?.toString().orEmpty(),
                    p.stringParam("creditCurrency").orEmpty(),
                )
            "bike.rent.error.zero_limit" ->
                context.getString(R.string.api_bike_rent_error_zero_limit)
            "bike.rent.error.limit" -> {
                val count = p.intParam("count") ?: 0
                context.resources.getQuantityString(R.plurals.api_bike_rent_error_limit, count, count)
            }
            "bike.rent.error.service_stand" ->
                context.getString(R.string.api_bike_rent_error_service_stand)
            "bike.rent.error.stack_top_bike" ->
                context.getString(
                    R.string.api_bike_rent_error_stack_top_bike,
                    p.intParam("bikeNumber") ?: 0,
                    p.intParam("stackTopBike") ?: 0,
                )

            "bike.return.error.stand_not_found" ->
                context.getString(R.string.api_bike_return_error_stand_not_found, p.stringParam("standName").orEmpty())
            "bike.return.error.no_rented_bikes" ->
                context.getString(R.string.api_bike_return_error_no_rented_bikes)

            "bike.revert.error.not_rented" ->
                context.getString(R.string.api_bike_revert_error_not_rented, p.intParam("bikeNumber") ?: 0)
            "bike.revert.error.no_stand_or_code" ->
                context.getString(R.string.api_bike_revert_error_no_stand_or_code, p.intParam("bikeNumber") ?: 0)

            else -> fallback.orEmpty()
        }
    }

    fun renderFromDto(code: String?, params: RentSystemParamsDto?, fallback: String? = null): String =
        render(code, params?.toParamsMap(), fallback)

    private fun renderRentSuccess(p: Map<String, Any?>): String {
        val main = context.getString(
            R.string.api_bike_rent_success,
            p.intParam("bikeNumber") ?: 0,
            p.stringParam("currentCode").orEmpty(),
            p.stringParam("newCode").orEmpty(),
        )
        val noteAppendix = if (p.flagParam("hasNote")) {
            context.getString(R.string.api_bike_rent_success_note_appendix, p.stringParam("note").orEmpty())
        } else ""
        return main + noteAppendix
    }

    private fun renderReturnSuccess(p: Map<String, Any?>): String {
        val main = context.getString(
            R.string.api_bike_return_success,
            p.intParam("bikeNumber") ?: 0,
            p.stringParam("standName").orEmpty(),
            p.stringParam("currentCode").orEmpty(),
        )
        val noteAppendix = if (p.flagParam("hasNote")) {
            context.getString(R.string.api_bike_return_success_note_appendix, p.stringParam("note").orEmpty())
        } else ""
        val creditAppendix = if (p.flagParam("hasCreditChange")) {
            context.getString(
                R.string.api_bike_return_success_credit_appendix,
                p.numberParam("creditChange")?.toString().orEmpty(),
                p.stringParam("creditCurrency").orEmpty(),
            )
        } else ""
        return main + noteAppendix + creditAppendix
    }

    private fun renderRevertSuccess(p: Map<String, Any?>): String =
        context.getString(
            R.string.api_bike_revert_success,
            p.intParam("bikeNumber") ?: 0,
            p.stringParam("standName").orEmpty(),
            p.stringParam("code").orEmpty(),
        )
}

/** Moshi parses JSON numbers as `Double`, so coerce defensively. */
private fun Map<String, Any?>.intParam(key: String): Int? = when (val v = this[key]) {
    is Number -> v.toInt()
    is String -> v.toIntOrNull()
    else -> null
}

private fun Map<String, Any?>.numberParam(key: String): Number? = when (val v = this[key]) {
    is Number -> v
    is String -> v.toDoubleOrNull()
    else -> null
}

private fun Map<String, Any?>.stringParam(key: String): String? = this[key]?.toString()

/** Server emits ICU select flags as the strings "true"/"false". */
private fun Map<String, Any?>.flagParam(key: String): Boolean = stringParam(key) == "true"

internal fun RentSystemParamsDto.toParamsMap(): Map<String, Any?> = mapOf(
    "bikeNumber" to bikeNumber,
    "currentCode" to currentCode,
    "newCode" to newCode,
    "standName" to standName,
    "note" to note,
    "creditChange" to creditChange,
    "creditCurrency" to creditCurrency,
    "hasNote" to hasNote,
    "hasCreditChange" to hasCreditChange,
    "code" to code,
    "minRequiredCredit" to minRequiredCredit,
    "count" to count,
    "stackTopBike" to stackTopBike,
)
