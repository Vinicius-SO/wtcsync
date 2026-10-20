package br.com.fiap.wtcsync.data.messages

import br.com.fiap.wtcsync.WtcSyncApp
import br.com.fiap.wtcsync.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.work.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

class ChatMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        (application as WtcSyncApp).pushRegistration.refresh()
    }
    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        if (data["type"] != "chat") return
        val recipient = data["recipient"] ?: return
        val customerId = data["customerId"] ?: return
        val messageId = data["messageId"] ?: return
        val app = application as WtcSyncApp
        // Local storage only; never wait for a network call in the FCM handler.
        val account = runBlocking { withTimeoutOrNull(1500) { app.sessionManager.accountSnapshot() } } ?: return
        if (!recipient.equals(account.first, ignoreCase = true)) return
        NotificationHelper.showMessageNotification(this, recipient, customerId, messageId)
        val work = OneTimeWorkRequestBuilder<DeliveryReceiptWorker>()
            .setInputData(workDataOf("recipient" to recipient, "messageId" to messageId))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
        WorkManager.getInstance(this).enqueueUniqueWork("delivered:$recipient:$messageId", ExistingWorkPolicy.KEEP, work)
    }
}

class DeliveryReceiptWorker(context: android.content.Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as WtcSyncApp
        val recipient = inputData.getString("recipient") ?: return Result.success()
        val id = inputData.getString("messageId") ?: return Result.success()
        val account = app.sessionManager.accountSnapshot() ?: return Result.success()
        if (!recipient.equals(account.first, ignoreCase = true)) return Result.success()
        return try {
            app.apiService.pushApi.delivered("Bearer ${account.second}", id, mapOf("status" to "ENTREGUE"))
            Result.success()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e
        } catch (e: retrofit2.HttpException) {
            if (e.code() in setOf(408, 429) || e.code() >= 500) Result.retry() else Result.success()
        } catch (_: java.io.IOException) { Result.retry()
        } catch (_: Exception) { Result.failure() }
    }
}
