package com.mariomanzano.kmm_nasa_explorer.network

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.mariomanzano.kmm_nasa_explorer.Error
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.utils.io.errors.IOException

typealias Result<T> = Either<Error, T>

fun Exception.toError(): Error = when (this) {
    is IOException -> Error.Connectivity
    is RedirectResponseException -> Error.Server(300)
    is ClientRequestException -> Error.Server(400)
    is ServerResponseException -> Error.Server(500)
    else -> Error.Unknown(message ?: "")
}

fun Throwable.toError(): Error = when (this) {
    is IOException -> Error.Connectivity
    is RedirectResponseException -> Error.Server(300)
    is ClientRequestException -> Error.Server(400)
    is ServerResponseException -> Error.Server(500)
    else -> Error.Unknown(message ?: "")
}

inline fun <T> tryCall(action: () -> T): Result<T> = try {
    action().right()
} catch (e: Exception) {
    e.toError().left()
}