package br.com.fiap.wtcsync.data.messages

import br.com.fiap.wtcsync.data.remote.MessageApi
import br.com.fiap.wtcsync.data.remote.dto.MessageDto
import br.com.fiap.wtcsync.data.remote.dto.MessageRequest
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

sealed class SendAttempt {
    data class Sent(val message: MessageDto) : SendAttempt()
    data class Failed(val error: String) : SendAttempt()
    object AlreadyDeleted : SendAttempt()
}

/** Sends the oldest entry first. Every attempt uses the same immutable UUID and content. */
class ReliableMessageSender(
    private val api: MessageApi,
    private val outbox: MessageOutbox,
    private val currentAccount: () -> String?,
    private val clock: () -> Long = System::currentTimeMillis
) {
    suspend fun sendNext(owner: String, customer: String, forceId: String? = null): SendAttempt? {
        if (!owner.equals(currentAccount(), ignoreCase = true)) return null
        val entry = outbox.list(owner, customer).firstOrNull() ?: return null
        if (forceId != null && forceId != entry.id) return null
        if (forceId == null && (!entry.retryable || entry.nextAttemptAt > clock())) return null
        try {
            val response = api.sendMessage(MessageRequest(owner, customer, entry.text, entry.id))
            if (response.clientMessageId != entry.id || response.customerId != customer ||
                !response.senderId.equals(owner, ignoreCase = true)) {
                val error = "Não foi possível confirmar o envio. Atualize a conversa antes de tentar novamente."
                outbox.fail(entry.copy(retryable = false, error = error, attempts = entry.attempts + 1))
                return SendAttempt.Failed(error)
            }
            outbox.acknowledge(owner, customer, setOf(entry.id))
            return SendAttempt.Sent(response)
        } catch (e: CancellationException) { throw e
        } catch (e: Exception) {
            if (e is HttpException && e.code() == 410) {
                outbox.acknowledge(owner, customer, setOf(entry.id))
                return SendAttempt.AlreadyDeleted
            }
            val retryable = e is IOException || (e is HttpException && (e.code() >= 500 || e.code() in setOf(408, 429)))
            val error = when ((e as? HttpException)?.code()) {
                401, 403 -> "Envio não autorizado. Confira sua sessão e o atendimento antes de tentar novamente."
                409 -> "O atendimento mudou. Atualize a conversa antes de tentar novamente."
                400, 404 -> "Não foi possível enviar esta mensagem. Confira o atendimento e tente novamente."
                else -> if (retryable) "Envio não confirmado. Tentaremos novamente quando a conexão voltar."
                    else "Não foi possível confirmar o envio. Tente novamente."
            }
            val attempts = entry.attempts + 1
            val delay = minOf(60_000L, 4_000L shl minOf(attempts - 1, 4))
            outbox.fail(entry.copy(attempts = attempts, retryable = retryable,
                nextAttemptAt = clock() + delay, error = error))
            return SendAttempt.Failed(error)
        }
    }
}
