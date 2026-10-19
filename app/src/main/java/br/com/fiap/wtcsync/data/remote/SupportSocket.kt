package br.com.fiap.wtcsync.data.remote

import br.com.fiap.wtcsync.data.local.SessionManager
import kotlinx.coroutines.*
import okhttp3.*
import java.util.concurrent.TimeUnit

/** Authenticated invalidation channel. REST remains the single source of truth. */
class SupportSocket(private val session: SessionManager) {
    companion object {
        private val client = OkHttpClient.Builder().pingInterval(25, TimeUnit.SECONDS).build()
    }
    suspend fun listen(onConnected: (Boolean) -> Unit, onChange: () -> Unit) {
        while (currentCoroutineContext().isActive) {
            val token = session.currentToken ?: return
            if (session.isExpired(token)) return
            val closed = CompletableDeferred<Unit>()
            val socket = client.newWebSocket(Request.Builder()
                .url("wss://wtcsync-api.onrender.com/ws/support")
                .header("Authorization", "Bearer $token").build(), object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    if (session.currentToken != token) { webSocket.close(1000, "Session changed"); return }
                    onConnected(true)
                    onChange() // Catch up after every reconnect.
                }
                override fun onMessage(webSocket: WebSocket, text: String) {
                    if (session.currentToken != token) { webSocket.close(1000, "Session changed"); return }
                    if (text.contains("support_changed")) onChange()
                }
                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) { webSocket.close(code, reason) }
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) { closed.complete(Unit) }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) { closed.complete(Unit) }
            })
            try { closed.await() } finally { socket.cancel(); onConnected(false) }
            delay(5_000)
        }
    }
}
