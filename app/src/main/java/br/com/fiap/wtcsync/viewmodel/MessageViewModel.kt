package br.com.fiap.wtcsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcsync.data.local.SessionManager
import br.com.fiap.wtcsync.data.remote.MessageApi
import br.com.fiap.wtcsync.data.remote.dto.MessageDto
import br.com.fiap.wtcsync.data.remote.dto.MessageRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

data class MessageUiState(
    val messages: List<MessageDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSending: Boolean = false,
    val queuedCount: Int = 0,
    val lastQueuedText: String? = null,
    val isQueueing: Boolean = false,
    val pending: List<br.com.fiap.wtcsync.data.messages.PendingMessage> = emptyList(),
    val sendingId: String? = null,
    val historyError: String? = null,
    val hasLoadedConversation: Boolean = false,
    val ticket: br.com.fiap.wtcsync.data.remote.SupportTicket? = null,
    val connected: Boolean = false,
    val changingAssignment: Boolean = false,
    val supportHistory: List<br.com.fiap.wtcsync.data.remote.SupportHistoryEntry> = emptyList(),
    val loadingSupportHistory: Boolean = false,
    val supportHistoryError: String? = null,
    val isEditing: Boolean = false,
    val editError: String? = null,
    val editedCount: Int = 0,
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
    val deletedCount: Int = 0
)

class MessageViewModel(
    private val clienteId: String,
    private val messageApi: MessageApi,
    private val sessionManager: SessionManager,
    private val supportApi: br.com.fiap.wtcsync.data.remote.SupportApi,
    private val outbox: br.com.fiap.wtcsync.data.messages.MessageOutbox,
    private val connectInBackground: Boolean = true
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessageUiState())
    val uiState: StateFlow<MessageUiState> = _uiState.asStateFlow()

    private val deletedMessageIds = mutableSetOf<String>()
    private val ownerEmail = sessionManager.currentEmail.orEmpty().lowercase(java.util.Locale.ROOT)
    private val sender = br.com.fiap.wtcsync.data.messages.ReliableMessageSender(messageApi, outbox, { sessionManager.currentEmail })
    private var drainJob: kotlinx.coroutines.Job? = null
    private var loadJob: kotlinx.coroutines.Job? = null
    private var reloadRequested = false
    private var messageRevision = 0L
    private val receiptsInFlight = mutableSetOf<String>()
    private fun ownsSession() = ownerEmail.isNotBlank() && ownerEmail.equals(sessionManager.currentEmail, ignoreCase = true)

    val isStaff: Boolean get() = sessionManager.currentRole in setOf("OPERATOR", "ADMIN", "ATENDENTE")
    val canReply: Boolean get() = !isStaff || (_uiState.value.ticket?.isClosed != true && _uiState.value.ticket?.assignedTo == sessionManager.currentEmail)

    init {
        viewModelScope.launch {
            try {
                outbox.observe(ownerEmail, clienteId).collect { pending ->
                    _uiState.update { it.copy(pending = pending) }
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(error = "Não foi possível recuperar as mensagens pendentes. Tente novamente.") }
            }
        }
        drainPending()
        if (connectInBackground) viewModelScope.launch {
            br.com.fiap.wtcsync.data.remote.SupportSocket(sessionManager).listen(
                { connected -> _uiState.update { it.copy(connected = connected) }; if (connected) drainPending() },
                { loadConversation(silent = true) }
            )
        }
        loadConversation()
        if (connectInBackground) viewModelScope.launch {
            while (isActive) {
                delay(12_000L)
                loadConversation(silent = true)
                drainPending()
            }
        }
    }

    fun changeAssignment(release: Boolean = false) = updateTicket(
        if (release) br.com.fiap.wtcsync.data.remote.TicketAction.RELEASE else br.com.fiap.wtcsync.data.remote.TicketAction.CLAIM)

    fun updateTicket(action: br.com.fiap.wtcsync.data.remote.TicketAction) {
        if (_uiState.value.changingAssignment || !isStaff || !ownsSession()) return
        if (action == br.com.fiap.wtcsync.data.remote.TicketAction.CLOSE &&
            (_uiState.value.isSending || _uiState.value.isQueueing || _uiState.value.pending.isNotEmpty())) {
            _uiState.update { it.copy(error = "Aguarde a confirmação das mensagens pendentes antes de concluir.") }
            return
        }
        _uiState.update { it.copy(changingAssignment = true, error = null) }
        viewModelScope.launch {
            try {
                val ticket = when (action) {
                    br.com.fiap.wtcsync.data.remote.TicketAction.CLAIM -> supportApi.claim(clienteId)
                    br.com.fiap.wtcsync.data.remote.TicketAction.RELEASE -> supportApi.release(clienteId)
                    br.com.fiap.wtcsync.data.remote.TicketAction.CLOSE -> supportApi.close(clienteId)
                    br.com.fiap.wtcsync.data.remote.TicketAction.REOPEN -> supportApi.reopen(clienteId)
                }
                _uiState.update { it.copy(ticket = ticket) }
                loadConversation(silent = true)
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = if (e is retrofit2.HttpException && e.code() == 409)
                    "O atendimento mudou. Atualize a conversa e confira o responsável e o status."
                    else "Não foi possível atualizar o atendimento. Tente novamente.") }
                loadConversation(silent = true)
            } finally { _uiState.update { it.copy(changingAssignment = false) } }
        }
    }

    fun loadSupportHistory() {
        if (!isStaff || _uiState.value.loadingSupportHistory || !ownsSession()) return
        _uiState.update { it.copy(loadingSupportHistory = true, supportHistoryError = null) }
        viewModelScope.launch {
            try { val history = supportApi.history(clienteId); _uiState.update { it.copy(supportHistory = history) }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (_: Exception) { _uiState.update { it.copy(supportHistoryError = "Não foi possível carregar o histórico do atendimento.") }
            } finally { _uiState.update { it.copy(loadingSupportHistory = false) } }
        }
    }

    fun dismissError() { _uiState.update { it.copy(error = null, historyError = null) } }

    fun retryConnection() { dismissError(); loadConversation(silent = true); drainPending() }

    // Coalesce polling and WebSocket reloads; a slow request must be allowed to finish.
    fun loadConversation(silent: Boolean = false) {
        if (!ownsSession()) return
        if (loadJob?.isActive == true) { reloadRequested = true; return }
        loadJob = viewModelScope.launch {
            val revision = messageRevision
            if (!silent && !_uiState.value.hasLoadedConversation)
                _uiState.update { it.copy(isLoading = true) }
            var warning: String? = null
            try {
                try {
                    val ticket = supportApi.get(clienteId)
                    if (ownsSession()) _uiState.update { it.copy(ticket = ticket) }
                } catch (e: kotlinx.coroutines.CancellationException) { throw e
                } catch (_: Exception) { warning = "Não foi possível atualizar o atendimento. Tente novamente." }
                val messages = messageApi.getInbox(clienteId)
                if (!ownsSession()) return@launch
                if (revision == messageRevision) {
                    _uiState.update { it.copy(
                        messages = messages.filterNot { message -> message.id in deletedMessageIds },
                        hasLoadedConversation = true, historyError = warning
                    ) }
                } else { reloadRequested = true }
                try {
                    outbox.acknowledge(ownerEmail, clienteId, messages.filter {
                        it.senderId.equals(ownerEmail, ignoreCase = true)
                    }.mapNotNull { it.clientMessageId }.toSet())
                } catch (e: kotlinx.coroutines.CancellationException) { throw e
                } catch (_: Exception) { _uiState.update { it.copy(error = "Não foi possível atualizar as confirmações de envio. Tente novamente.") } }
                markDelivered(messages)
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) {
                val error = if (e is retrofit2.HttpException && e.code() in setOf(401, 403))
                    "Não foi possível acessar a conversa. Confira sua sessão e tente novamente."
                    else "Não foi possível atualizar a conversa. Verifique sua conexão e tente novamente."
                _uiState.update { it.copy(historyError = error) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
            if (reloadRequested) { reloadRequested = false; loadJob = null; loadConversation(silent = true) }
        }
    }


    private fun isRecipient(message: MessageDto): Boolean = ownsSession() &&
        !message.senderId.equals(ownerEmail, ignoreCase = true) &&
        (!isStaff || (canReply && message.senderId.equals(_uiState.value.ticket?.customerEmail, ignoreCase = true)))

    fun markVisibleRead(ids: Set<String>, resumed: Boolean) {
        if (!resumed || !ownsSession()) return
        acknowledge(_uiState.value.messages.filter { it.id in ids && isRecipient(it) && it.status in setOf("ENVIADO", "ENTREGUE") }, "LIDO")
    }

    private fun markDelivered(messages: List<MessageDto>) {
        acknowledge(messages.filter { isRecipient(it) && it.status == "ENVIADO" }, "ENTREGUE")
    }

    private fun acknowledge(messages: List<MessageDto>, status: String) {
        messages.forEach { message ->
            val key = "${message.id}:$status"
            if (!receiptsInFlight.add(key)) return@forEach
            viewModelScope.launch {
                try {
                    if (!ownsSession()) return@launch
                    val updated = messageApi.updateStatus(message.id, mapOf("status" to status))
                    if (!ownsSession()) return@launch
                    messageRevision++
                    _uiState.update { state -> state.copy(messages = state.messages.map {
                        if (it.id == updated.id && receiptRank(updated.status) >= receiptRank(it.status))
                            it.copy(status = updated.status) else it
                    }) }
                } catch (e: kotlinx.coroutines.CancellationException) { throw e
                } catch (_: Exception) { /* Retry on the next load/visibility update. */
                } finally { receiptsInFlight.remove(key) }
            }
        }
    }

    private fun receiptRank(status: String) = when (status) { "LIDO" -> 2; "ENTREGUE" -> 1; else -> 0 }

    fun clearDeleteError() { _uiState.update { it.copy(deleteError = null) } }

    fun deleteMessage(id: String) {
        if (_uiState.value.isDeleting) return
        val message = _uiState.value.messages.find { it.id == id }
        if (message != null && !message.senderId.equals(sessionManager.currentEmail, ignoreCase = true)) return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deleteError = null) }
            try {
                try {
                    messageApi.deleteMessage(id)
                } catch (e: retrofit2.HttpException) {
                    // An earlier attempt may have succeeded despite a lost response.
                    if (e.code() != 404) throw e
                }
                deletedMessageIds.add(id)
                messageRevision++
                _uiState.update { state -> state.copy(
                    messages = state.messages.filterNot { it.id == id },
                    deletedCount = state.deletedCount + 1
                ) }
                loadConversation(silent = true)
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) {
                val error = when ((e as? retrofit2.HttpException)?.code()) {
                    401 -> "Sua sessão expirou. Entre novamente para apagar."
                    403 -> "Você só pode apagar mensagens enviadas por você."
                    else -> "Não foi possível apagar a mensagem. Tente novamente."
                }
                _uiState.update { it.copy(deleteError = error) }
            } finally { _uiState.update { it.copy(isDeleting = false) } }
        }
    }

    fun clearEditError() { _uiState.update { it.copy(editError = null) } }

    fun editMessage(id: String, text: String) {
        if (text.isBlank() || _uiState.value.isEditing) return
        val message = _uiState.value.messages.find { it.id == id } ?: return
        if (!message.senderId.equals(sessionManager.currentEmail, ignoreCase = true)) return
        viewModelScope.launch {
            _uiState.update { it.copy(isEditing = true, editError = null) }
            try {
                val updated = messageApi.editMessage(id, mapOf("text" to text))
                messageRevision++
                _uiState.update { state -> state.copy(
                    messages = state.messages.map { if (it.id == id) updated else it },
                    editedCount = state.editedCount + 1
                ) }
                loadConversation(silent = true)
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) {
                val message = when ((e as? retrofit2.HttpException)?.code()) {
                    401 -> "Sua sessão expirou. Entre novamente para salvar."
                    403 -> "Você só pode editar mensagens enviadas por você."
                    404 -> "Esta mensagem não está mais disponível."
                    400 -> "A mensagem não pode ficar vazia."
                    else -> "Não foi possível salvar a edição. Seu texto foi mantido; tente novamente."
                }
                _uiState.update { it.copy(editError = message) }
            } finally { _uiState.update { it.copy(isEditing = false) } }
        }
    }

    fun retryPending(id: String) { drainPending(forceId = id) }

    private fun drainPending(forceId: String? = null) {
        if (!ownsSession() || drainJob?.isActive == true) return
        drainJob = viewModelScope.launch {
            var force = forceId
            try {
                while (isActive && ownsSession()) {
                    val first = outbox.list(ownerEmail, clienteId).firstOrNull() ?: break
                    if (force == null && (!first.retryable || first.nextAttemptAt > System.currentTimeMillis())) break
                    _uiState.update { it.copy(isSending = true, sendingId = first.id) }
                    when (val result = sender.sendNext(ownerEmail, clienteId, force)) {
                        is br.com.fiap.wtcsync.data.messages.SendAttempt.Sent -> {
                            messageRevision++
                            if (result.message.id !in deletedMessageIds) _uiState.update { state -> state.copy(
                                messages = (state.messages.filterNot { it.id == result.message.id } + result.message)
                            ) }
                            loadConversation(silent = true)
                        }
                        is br.com.fiap.wtcsync.data.messages.SendAttempt.AlreadyDeleted -> {
                            _uiState.update { it.copy(error = "Esta mensagem já havia sido enviada e apagada.") }
                            loadConversation(silent = true)
                        }
                        else -> break
                    }
                    force = null
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(error = "Não foi possível atualizar as mensagens pendentes. Seu texto foi mantido.") }
            } finally { _uiState.update { it.copy(isSending = false, sendingId = null) } }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || !canReply || !ownsSession() || _uiState.value.isQueueing) return
        _uiState.update { it.copy(isQueueing = true) }
        viewModelScope.launch {
            try {
                outbox.enqueue(br.com.fiap.wtcsync.data.messages.PendingMessage(
                    id = java.util.UUID.randomUUID().toString(), ownerEmail = ownerEmail,
                    customerId = clienteId, text = text, createdAt = java.time.Instant.now().toString()
                ))
                _uiState.update { it.copy(queuedCount = it.queuedCount + 1, lastQueuedText = text, error = null) }
                drainPending()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(error = "Não foi possível guardar sua mensagem. O texto foi mantido; tente novamente.") }
            } finally { _uiState.update { it.copy(isQueueing = false) } }
        }
    }


}

class MessageViewModelFactory(
    private val clienteId: String,
    private val messageApi: MessageApi,
    private val sessionManager: SessionManager,
    private val supportApi: br.com.fiap.wtcsync.data.remote.SupportApi,
    private val outbox: br.com.fiap.wtcsync.data.messages.MessageOutbox
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MessageViewModel(clienteId, messageApi, sessionManager, supportApi, outbox) as T
}
