package br.com.fiap.wtcsync.data.messages

import android.content.Context
import br.com.fiap.wtcsync.data.local.SessionManager
import br.com.fiap.wtcsync.data.remote.PushApi
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.tasks.await

class PushRegistration(private val context: Context, private val session: SessionManager,
    private val api: PushApi, private val scope: CoroutineScope) {
    private var refreshJob: Job? = null
    init {
        scope.launch {
            session.token.collectLatest { token ->
                if (token.isNullOrBlank()) {
                    refreshJob?.cancel()
                    br.com.fiap.wtcsync.util.NotificationHelper.clearAll(context)
                } else refresh()
            }
        }
    }
    fun refresh() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            // Retry transient registration failures independently of authentication.
            for (attempt in 0..3) {
                try {
                    val token = session.tokenSnapshot() ?: return@launch
                    val fcm = FirebaseMessaging.getInstance().token.await()
                    if (token != session.tokenSnapshot()) return@launch
                    api.register("Bearer $token", mapOf("fcmToken" to fcm))
                    return@launch
                } catch (e: CancellationException) { throw e
                } catch (_: Exception) { delay(5_000L * (attempt + 1)) }
            }
        }
    }
    fun revoke(authorization: String?) {
        refreshJob?.cancel()
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
