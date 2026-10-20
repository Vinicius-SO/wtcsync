package br.com.fiap.wtcsync.data.messages

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Persist before any network call. Entries remain scoped to the original account and chat. */
data class PendingMessage(
    val id: String,
    val ownerEmail: String,
    val customerId: String,
    val text: String,
    val createdAt: String,
    val attempts: Int = 0,
    val retryable: Boolean = true,
    val nextAttemptAt: Long = 0,
    val error: String? = null
)

interface MessageOutbox {
    fun observe(owner: String, customer: String): Flow<List<PendingMessage>>
    suspend fun list(owner: String, customer: String): List<PendingMessage>
    suspend fun enqueue(message: PendingMessage)
    suspend fun fail(message: PendingMessage)
    suspend fun acknowledge(owner: String, customer: String, ids: Set<String>)
}

object OutboxCodec {
    private val gson = Gson()
    fun encode(messages: List<PendingMessage>): String = gson.toJson(messages)
    fun decode(value: String?): List<PendingMessage> = if (value == null) emptyList() else
        gson.fromJson<List<PendingMessage>>(value, object : TypeToken<List<PendingMessage>>() {}.type) ?: emptyList()
}

private val Context.outboxDataStore by preferencesDataStore(name = "message_outbox")

class PersistentMessageOutbox(context: Context) : MessageOutbox {
    private val data = context.applicationContext.outboxDataStore
    private val key = stringPreferencesKey("pending_messages")
    private fun PendingMessage.belongs(owner: String, customer: String) =
        ownerEmail.equals(owner, ignoreCase = true) && customerId == customer
    // The persisted append order is authoritative, including across clock changes.
    override fun observe(owner: String, customer: String) = data.data.map { prefs ->
        OutboxCodec.decode(prefs[key]).filter { it.belongs(owner, customer) }
    }
    override suspend fun list(owner: String, customer: String) = observe(owner, customer).first()
    override suspend fun enqueue(message: PendingMessage) {
        data.edit { prefs ->
            val entries = OutboxCodec.decode(prefs[key])
            if (entries.none { it.belongs(message.ownerEmail, message.customerId) && it.id == message.id })
                prefs[key] = OutboxCodec.encode(entries + message)
        }
    }
    override suspend fun fail(message: PendingMessage) {
        data.edit { prefs ->
            // Do not recreate an entry already acknowledged by a concurrent refresh.
            prefs[key] = OutboxCodec.encode(OutboxCodec.decode(prefs[key]).map {
                if (it.belongs(message.ownerEmail, message.customerId) && it.id == message.id) message else it
            })
        }
    }
    override suspend fun acknowledge(owner: String, customer: String, ids: Set<String>) {
        if (ids.isEmpty()) return
        data.edit { prefs ->
            prefs[key] = OutboxCodec.encode(OutboxCodec.decode(prefs[key]).filterNot {
                it.belongs(owner, customer) && it.id in ids
            })
        }
    }
}
