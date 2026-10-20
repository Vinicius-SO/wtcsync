package br.com.fiap.wtcsync

import br.com.fiap.wtcsync.data.messages.*
import br.com.fiap.wtcsync.data.remote.PushApi
import br.com.fiap.wtcsync.data.remote.dto.MessageDto
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import org.junit.Assert.*
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

private class RegistrationApi : PushApi {
    val registrations = mutableListOf<Pair<String, String>>()
    var failure: Exception? = null
    var afterRegister: () -> Unit = {}
    override suspend fun register(authorization: String, body: Map<String, String>) {
        registrations += authorization to body.getValue("fcmToken")
        afterRegister()
        failure?.let { throw it }
    }
    override suspend fun revoke(authorization: String, body: Map<String, String>) = Unit
    override suspend fun delivered(authorization: String, id: String, body: Map<String, String>): MessageDto = error("unused")
}
class PushRegistrationTest {
    private val first = "first@example.invalid" to "jwt-first"
    private val second = "second@example.invalid" to "jwt-second"
    private fun http(code: Int) = HttpException(Response.error<Any>(code, "failure".toResponseBody()))
    @Test fun networkFailureCanBeRetriedByANewWorkerAfterProcessRestart() = runTest {
        val api = RegistrationApi(); api.failure = IOException("offline")
        assertEquals(RegistrationOutcome.RETRY, PushRegistrationAttempt(api, { first }, { "device" }).run())
        api.failure = null
        assertEquals(RegistrationOutcome.COMPLETE, PushRegistrationAttempt(api, { first }, { "device" }).run())
        assertEquals(listOf("Bearer jwt-first" to "device", "Bearer jwt-first" to "device"), api.registrations)
    }
    @Test fun loggedOutWorkerDoesNotFetchADeviceTokenOrRegister() = runTest {
        val api = RegistrationApi()
        assertEquals(RegistrationOutcome.COMPLETE, PushRegistrationAttempt(api, { null }, { error("must not fetch") }).run())
        assertTrue(api.registrations.isEmpty())
    }
    @Test fun accountChangedWhileFetchingFirebaseTokenIsNotRegisteredWithTheOldSession() = runTest {
        val api = RegistrationApi(); var current = first
        val attempt = PushRegistrationAttempt(api, { current }, { current = second; "device" })
        assertEquals(RegistrationOutcome.RETRY, attempt.run())
        assertTrue(api.registrations.isEmpty())
        assertEquals(RegistrationOutcome.COMPLETE, attempt.run())
        assertEquals(listOf("Bearer jwt-second" to "device"), api.registrations)
    }
    @Test fun accountChangedDuringTheRequestForcesANewBindingAttempt() = runTest {
        val api = RegistrationApi(); var current = first
        api.afterRegister = { current = second }
        val attempt = PushRegistrationAttempt(api, { current }, { "device" })
        assertEquals(RegistrationOutcome.RETRY, attempt.run())
        assertEquals(RegistrationOutcome.COMPLETE, attempt.run())
        assertEquals(listOf("Bearer jwt-first" to "device", "Bearer jwt-second" to "device"), api.registrations)
    }
    @Test fun permanentAuthFailuresStopButTransientFailuresAndAccountChangesRetry() = runTest {
        val api = RegistrationApi()
        val attempt = PushRegistrationAttempt(api, { first }, { "device" })
        api.failure = http(401); assertEquals(RegistrationOutcome.REJECTED, attempt.run())
        api.failure = http(403); assertEquals(RegistrationOutcome.REJECTED, attempt.run())
        api.failure = http(429); assertEquals(RegistrationOutcome.RETRY, attempt.run())
        api.failure = http(503); assertEquals(RegistrationOutcome.RETRY, attempt.run())
        var current = first
        api.afterRegister = { current = second }; api.failure = http(401)
        assertEquals(RegistrationOutcome.RETRY, PushRegistrationAttempt(api, { current }, { "device" }).run())
    }
    @Test fun cancellationDoesNotTurnIntoAnEndlessRetry() = runTest {
        val attempt = PushRegistrationAttempt(RegistrationApi(), { first }, { throw CancellationException("stopped") })
        try { attempt.run(); fail("Cancellation must propagate") } catch (_: CancellationException) { }
    }
}
