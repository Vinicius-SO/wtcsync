package br.com.fiap.wtcsync.util

import android.app.*
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import br.com.fiap.wtcsync.MainActivity

object NotificationHelper {
    const val CHANNEL_ID = "messages_channel"
    fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL_ID, "Mensagens", NotificationManager.IMPORTANCE_HIGH)
        channel.description = "Notificações de novas mensagens"
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
    @Synchronized
    fun showMessageNotification(context: Context, recipient: String, customerId: String, messageId: String) {
        val prefs = context.getSharedPreferences("chat_notifications", Context.MODE_PRIVATE)
        val receipt = "$recipient:$messageId"
        if (prefs.getStringSet("seen", emptySet()).orEmpty().contains(receipt)) return
        val seen = prefs.getStringSet("seen", emptySet()).orEmpty().toMutableSet()
        seen.add(receipt)
        prefs.edit().putStringSet("seen", seen.toList().takeLast(300).toSet()).apply()
        if (ChatVisibility.isActive(recipient, customerId)) return
        createChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            action = "br.com.fiap.wtcsync.CHAT"
            data = android.net.Uri.parse("wtcsync://chat/${android.net.Uri.encode(recipient)}/${android.net.Uri.encode(customerId)}")
            putExtra("chatRecipient", recipient)
            putExtra("chatCustomerId", customerId)
            putExtra("chatRequestId", messageId)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("WTCSync").setContentText("Você recebeu uma nova mensagem.")
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true)
            .setOnlyAlertOnce(true).setContentIntent(pendingIntent).build()
        try { NotificationManagerCompat.from(context).notify("chat:$recipient:$customerId", 1, notification)
        } catch (_: SecurityException) { }
    }
    fun clearConversation(context: Context, email: String, customerId: String) {
        NotificationManagerCompat.from(context).cancel("chat:$email:$customerId", 1)
    }
    fun clearAll(context: Context) { NotificationManagerCompat.from(context).cancelAll() }
    fun target(intent: Intent?): ChatNotificationTarget? {
        val recipient = intent?.getStringExtra("chatRecipient") ?: return null
        val customer = intent.getStringExtra("chatCustomerId") ?: return null
        val request = intent.getStringExtra("chatRequestId") ?: return null
        return ChatNotificationTarget(recipient, customer, request)
    }
}
