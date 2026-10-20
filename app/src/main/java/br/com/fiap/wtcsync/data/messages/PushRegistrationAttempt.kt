package br.com.fiap.wtcsync.data.messages

import br.com.fiap.wtcsync.data.remote.PushApi
import kotlinx.coroutines.CancellationException
import java.io.IOException
import retrofit2.HttpException

enum class RegistrationOutcome { COMPLETE, RETRY, REJECTED }

/** No credentials are copied into job input; every attempt uses the current persisted session. */
class PushRegistrationAttempt(private val api: PushApi,
    private val account: suspend () -> Pair<String, String>?,
    private val deviceToken: suspend () -> String) {
    suspend fun run(): RegistrationOutcome {
        var attempted: Pair<String, String>? = null
        return try {
            val before = account()
            attempted = before
            if (before == null) RegistrationOutcome.COMPLETE else {
                val fcm = deviceToken()
                if (before != account()) RegistrationOutcome.RETRY else {
                    api.register("Bearer ${before.second}", mapOf("fcmToken" to fcm))
                    if (before == account()) RegistrationOutcome.COMPLETE else RegistrationOutcome.RETRY
                }
            }
        } catch (e: CancellationException) { throw e
        } catch (e: HttpException) {
            if (attempted != account() || e.code() == 408 || e.code() == 429 || e.code() >= 500)
                RegistrationOutcome.RETRY else RegistrationOutcome.REJECTED
        } catch (_: IOException) { RegistrationOutcome.RETRY
        } catch (_: Exception) { RegistrationOutcome.RETRY }
    }
}
