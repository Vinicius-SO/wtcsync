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
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

data class MessageUiState(
    val messages: List<MessageDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSending: Boolean = false,
    val sentCount: Int = 0,
    val ticket: br.com.fiap.wtcsync.data.remote.SupportTicket? = null,
    val connected: Boolean = false,
    val changingAssignment: Boolean = false,
    val isEditing: Boolean = false,
    val editError: String? = null,
    val editedCount: Int = 0
)

class MessageViewModel(
    private val clienteId: String,
    private val messageApi: MessageApi,
    private val sessionManager: SessionManager,
    private val supportApi: br.com.fiap.wtcsync.data.remote.SupportApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessageUiState())
    val uiState: StateFlow<MessageUiState> = _uiState.asStateFlow()

    val isStaff: Boolean get() = sessionManager.currentRole in setOf("OPERATOR", "ADMIN", "ATENDENTE")
    val canReply: Boolean get() = !isStaff || (_uiState.value.ticket?.assignedTo == sessionManager.currentEmail)

    init {
        viewModelScope.launch {
            br.com.fiap.wtcsync.data.remote.SupportSocket(sessionManager).listen(
                { connected -> _uiState.update { it.copy(connected = connected) } },
                { loadConversation(silent = true) }
            )
        }
        loadConversation()
        viewModelScope.launch {
            while (isActive) {
                delay(12_000L)
                loadConversation(silent = true)
            }
        }
    }

    fun changeAssignment(release: Boolean = false) {
        if (_uiState.value.changingAssignment) return
        viewModelScope.launch {
            _uiState.update { it.copy(changingAssignment = true, error = null) }
            try {
                val ticket = if (release) supportApi.release(clienteId) else supportApi.claim(clienteId)
                _uiState.update { it.copy(ticket = ticket) }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = if (e is retrofit2.HttpException && e.code() == 409)
                    "Outro operador assumiu este atendimento. Atualize a conversa." else "Não foi possível alterar o atendimento.") }
                loadConversation(silent = true)
            } finally { _uiState.update { it.copy(changingAssignment = false) } }
        }
    }

    // Histórico de atendimento compartilhado; a API verifica o perfil e o cliente autenticado.
    fun loadConversation(silent: Boolean = false) {
        val operadorId = sessionManager.currentEmail ?: run {
            _uiState.update { it.copy(isLoading = false, error = "Sessão ausente. Entre novamente.") }
            return
        }
        viewModelScope.launch {
            if (!silent) _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val ticket = supportApi.get(clienteId)
                val messages = messageApi.getInbox(clienteId)
                _uiState.update { it.copy(messages = messages, ticket = ticket, isLoading = false, error = null) }
                markDelivered(messages)
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun markDelivered(messages: List<MessageDto>) {
        val currentEmail = sessionManager.currentEmail ?: return
        if (isStaff && !canReply) return
        viewModelScope.launch {
            messages
                .filter { (if (isStaff) it.senderId == _uiState.value.ticket?.customerEmail else it.senderId != currentEmail) && it.status == "ENVIADO" }
                .forEach { msg ->
                    try {
                        messageApi.updateStatus(msg.id, mapOf("status" to "ENTREGUE"))
                    } catch (_: Exception) {}
                }
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

    fun sendMessage(text: String) {
        if (text.isBlank() || !canReply || _uiState.value.isSending) return
        val senderId = sessionManager.currentEmail ?: run {
            _uiState.update { it.copy(error = "Sessão expirada. Faça login novamente.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            try {
                messageApi.sendMessage(MessageRequest(senderId, clienteId, text))
                _uiState.update { it.copy(isSending = false, sentCount = it.sentCount + 1) }
                loadConversation(silent = true)
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSending = false, error = e.message) }
            }
        }
    }


}

class MessageViewModelFactory(
    private val clienteId: String,
    private val messageApi: MessageApi,
    private val sessionManager: SessionManager,
    private val supportApi: br.com.fiap.wtcsync.data.remote.SupportApi
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MessageViewModel(clienteId, messageApi, sessionManager, supportApi) as T
}
