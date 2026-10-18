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
    val sentCount: Int = 0
)

class MessageViewModel(
    private val clienteId: String,
    private val messageApi: MessageApi,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessageUiState())
    val uiState: StateFlow<MessageUiState> = _uiState.asStateFlow()

    init {
        loadConversation()
        viewModelScope.launch {
            while (isActive) {
                delay(12_000L)
                loadConversation(silent = true)
            }
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
                val messages = messageApi.getInbox(clienteId)
                _uiState.update { it.copy(messages = messages, isLoading = false) }
                markDelivered(messages)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun markDelivered(messages: List<MessageDto>) {
        val currentEmail = sessionManager.currentEmail ?: return
        viewModelScope.launch {
            messages
                .filter { it.senderId != currentEmail && it.status == "ENVIADO" }
                .forEach { msg ->
                    try {
                        messageApi.updateStatus(msg.id, mapOf("status" to "ENTREGUE"))
                    } catch (_: Exception) {}
                }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
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
            } catch (e: Exception) {
                _uiState.update { it.copy(isSending = false, error = e.message) }
            }
        }
    }


}

class MessageViewModelFactory(
    private val clienteId: String,
    private val messageApi: MessageApi,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MessageViewModel(clienteId, messageApi, sessionManager) as T
}
