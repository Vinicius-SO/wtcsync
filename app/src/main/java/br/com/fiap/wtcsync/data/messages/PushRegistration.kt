package br.com.fiap.wtcsync.data.messages

import android.content.Context
import br.com.fiap.wtcsync.WtcSyncApp
import br.com.fiap.wtcsync.data.local.SessionManager
import br.com.fiap.wtcsync.data.remote.PushApi
import com.google.firebase.messaging.FirebaseMessaging
import androidx.work.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class PushRegistration(private val context: Context, private val session: SessionManager,
    private val api: PushApi, private val scope: CoroutineScope) {
    init {
        scope.launch {
            session.token.collectLatest { token ->
                if (token.isNullOrBlank()) br.com.fiap.wtcsync.util.NotificationHelper.clearAll(context)
                else refresh(force = true)
            }
        }
    }
    fun refresh(force: Boolean = false) {
        val request = OneTimeWorkRequestBuilder<PushRegistrationWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS).build()
        // Session/token changes append a serial attempt, including when an old request is in flight.
        // Resuming the app keeps the existing retry/backoff instead of restarting it.
        WorkManager.getInstance(context).enqueueUniqueWork("push-registration",
            if (force) ExistingWorkPolicy.APPEND_OR_REPLACE else ExistingWorkPolicy.KEEP, request)
    }
    fun revoke(authorization: String?) {
        br.com.fiap.wtcsync.util.NotificationHelper.clearAll(context)
        if (authorization == null) return
        scope.launch {
            try {
                val fcm = FirebaseMessaging.getInstance().token.await()
                api.revoke("Bearer $authorization", mapOf("fcmToken" to fcm))
            } catch (e: CancellationException) { throw e
            } catch (_: Exception) { /* Payload account checks also protect offline logout. */ }
        }
    }
}

class PushRegistrationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as WtcSyncApp
        return when (PushRegistrationAttempt(app.apiService.pushApi,
            { app.sessionManager.accountSnapshot() }, { FirebaseMessaging.getInstance().token.await() }).run()) {
            RegistrationOutcome.COMPLETE -> Result.success()
            RegistrationOutcome.RETRY -> Result.retry()
            RegistrationOutcome.REJECTED -> Result.failure()
        }
    }
}
