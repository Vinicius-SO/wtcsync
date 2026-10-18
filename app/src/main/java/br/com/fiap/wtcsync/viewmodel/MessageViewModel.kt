package br.com.fiap.wtcsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcsync.data.local.SessionManager
import br.com.fiap.wtcsync.data.remote.MessageApi
import br.com.fiap.wtcsync.data.remote.dto.MessageDto
import br.com.fiap.wtcsync.data.remote.dto.MessageRequest
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient

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

    private val gson = Gson()
    private val disposables = CompositeDisposable()
    private var stompClient: StompClient? = null

    private val wsUrl = "wss://wtcsync-api.onrender.com/ws/websocket"

    init {
        loadConversation()
        connectWebSocket()
    }

    // Carrega apenas a conversa entre o operador logado e o cliente
    fun loadConversation(silent: Boolean = false) {
        val operadorId = sessionManager.currentEmail ?: run {
            _uiState.update { it.copy(isLoading = false, error = "Sessão ausente. Entre novamente.") }
            return
        }
        viewModelScope.launch {
            if (!silent) _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val messages = messageApi.getConversation(
                    senderId   = operadorId,
                    customerId = clienteId
                )
                _uiState.update { it.copy(messages = messages, isLoading = false) }
                markDelivered(messages)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun connectWebSocket() {
        val currentEmail = sessionManager.currentEmail ?: return

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl)

        val connectDisposable = stompClient!!.lifecycle().subscribe { event ->
            when (event.type) {
                ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED -> {
                    subscribeToMessages(currentEmail)
                }
                ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR -> {
                    // Fallback silencioso — mensagens ainda carregam via REST
                }
                else -> {}
            }
        }
        disposables.add(connectDisposable)
        stompClient!!.connect()
    }

    private fun subscribeToMessages(currentEmail: String) {
        val subDisposable = stompClient!!
            .topic("/topic/chat/$currentEmail")
            .subscribe { stompMessage ->
                try {
                    val message = gson.fromJson(stompMessage.payload, MessageDto::class.java)
                    // Ignora mensagens de outras conversas recebidas no mesmo tópico
                    if (message.customerId != clienteId) return@subscribe
                    val current = _uiState.value.messages.toMutableList()
                    if (current.none { it.id == message.id }) {
                        current.add(message)
                        _uiState.update { it.copy(messages = current) }
                    }
                } catch (_: Exception) {}
            }
        disposables.add(subDisposable)
    }

    private fun markDelivered(messages: List<MessageDto>) {
        val currentEmail = sessionManager.currentEmail ?: return
        viewModelScope.launch {
            messages
                .filter { it.senderId != currentEmail && it.status != "ENTREGUE" }
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

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
        stompClient?.disconnect()
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
