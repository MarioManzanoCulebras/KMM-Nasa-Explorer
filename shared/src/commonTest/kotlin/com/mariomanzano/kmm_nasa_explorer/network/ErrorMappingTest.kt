package com.mariomanzano.kmm_nasa_explorer.network

import com.mariomanzano.kmm_nasa_explorer.Error
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ErrorMappingTest {

    @Test
    fun ioExceptionMapsToConnectivity() {
        val exception: Exception = IOException("no network")

        assertSame(Error.Connectivity, exception.toError())
    }

    @Test
    fun ioExceptionAsThrowableMapsToConnectivity() {
        val throwable: Throwable = IOException("no network")

        assertSame(Error.Connectivity, throwable.toError())
    }

    @Test
    fun anyOtherExceptionMapsToUnknownKeepingItsMessage() {
        val error = IllegalStateException("boom").toError()

        assertEquals("boom", assertIs<Error.Unknown>(error).message)
    }

    @Test
    fun exceptionWithoutMessageMapsToUnknownWithEmptyMessage() {
        val error = RuntimeException().toError()

        assertEquals("", assertIs<Error.Unknown>(error).message)
    }

    @Test
    fun nonExceptionThrowableMapsToUnknown() {
        val throwable: Throwable = object : Throwable("fatal") {}

        assertEquals("fatal", assertIs<Error.Unknown>(throwable.toError()).message)
    }

    @Test
    fun clientErrorResponseMapsToServer400() = runTest {
        val error = failureFor(HttpStatusCode.NotFound).toError()

        assertEquals(400, assertIs<Error.Server>(error).code)
    }

    @Test
    fun serverErrorResponseMapsToServer500() = runTest {
        val error = failureFor(HttpStatusCode.ServiceUnavailable).toError()

        assertEquals(500, assertIs<Error.Server>(error).code)
    }

    @Test
    fun redirectResponseMapsToServer300() = runTest {
        val error = failureFor(HttpStatusCode.Found, followRedirects = false).toError()

        assertEquals(300, assertIs<Error.Server>(error).code)
    }

    @Test
    fun tryCallWrapsTheResultInRight() {
        val result = tryCall { 42 }

        assertEquals(42, result.getOrNull())
    }

    @Test
    fun tryCallConvertsAThrownExceptionIntoLeft() {
        val result = tryCall<Int> { throw IOException("no network") }

        assertTrue(result.isLeft())
        assertSame(Error.Connectivity, result.leftOrNull())
    }

    @Test
    fun tryCallKeepsTheMessageOfUnexpectedExceptions() {
        val result = tryCall<Int> { throw IllegalArgumentException("bad input") }

        assertEquals("bad input", assertIs<Error.Unknown>(result.leftOrNull()).message)
    }

    /** Runs a request that Ktor rejects because of [status] and returns the thrown exception. */
    private suspend fun failureFor(
        status: HttpStatusCode,
        followRedirects: Boolean = true
    ): Exception {
        val client = HttpClient(
            MockEngine {
                respond(
                    content = "",
                    status = status,
                    headers = headersOf(HttpHeaders.Location, "https://example.test/moved")
                )
            }
        ) {
            expectSuccess = true
            this.followRedirects = followRedirects
        }
        val failure = runCatching { client.get("https://example.test/") }.exceptionOrNull()
        client.close()
        return assertIs<Exception>(failure, "Expected the request to fail with $status")
    }
}
