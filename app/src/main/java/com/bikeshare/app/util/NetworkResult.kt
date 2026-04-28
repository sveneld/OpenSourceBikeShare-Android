package com.bikeshare.app.util

/** Wrapper for API call results */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()

    /**
     * `message` is the server-rendered fallback (`detail` from problem+json or HTTP status).
     * `messageCode` + `messageParams` are the machine-readable code/params from the new
     * server contract — clients can use them to localize the error on-device.
     */
    data class Error(
        val message: String,
        val code: Int? = null,
        val messageCode: String? = null,
        val messageParams: Map<String, Any?>? = null,
    ) : NetworkResult<Nothing>()

    data object Loading : NetworkResult<Nothing>()
}
