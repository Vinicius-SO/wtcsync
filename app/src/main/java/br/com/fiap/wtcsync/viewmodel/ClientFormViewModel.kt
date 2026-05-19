package br.com.fiap.wtcsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcsync.data.remote.ClientApi
import br.com.fiap.wtcsync.data.remote.dto.CreateClientDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClientFormUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val status: String = "Active",
    val score: Float = 50f,
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

class ClientFormViewModel(
    private val clientApi: ClientApi,
    private val clienteId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientFormUiState())
    val uiState: StateFlow<ClientFormUiState> = _uiState.asStateFlow()

    init { if (clienteId != null) loadCliente() }

    fun loadCliente() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val client = clientApi.getClientById(clienteId!!)
                _uiState.update {
                    it.copy(
                        name = client.name,
                        email = client.email,
                        phone = client.phone ?: "",
                        status = client.status ?: "Active",
                        score = (client.score ?: 50).toFloat(),
                        tags = client.tags ?: emptyList(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onNameChange(v: String)   = _uiState.update { it.copy(name = v, error = null) }
    fun onEmailChange(v: String)  = _uiState.update { it.copy(email = v, error = null) }
    fun onPhoneChange(v: String)  = _uiState.update { it.copy(phone = v) }
    fun onStatusChange(v: String) = _uiState.update { it.copy(status = v) }
    fun onScoreChange(v: Float)   = _uiState.update { it.copy(score = v) }
    fun onNotesChange(v: String)  = _uiState.update { it.copy(notes = v) }
    fun addTag(tag: String) {
        val trimmed = tag.trim()
        if (trimmed.isNotBlank() && trimmed !in _uiState.value.tags)
            _uiState.update { it.copy(tags = it.tags + trimmed) }
    }
    fun removeTag(tag: String) = _uiState.update { it.copy(tags = it.tags - tag) }

    fun save() {
        val s = _uiState.value
        if (s.name.isBlank() || s.email.isBlank()) {
            _uiState.update { it.copy(error = "Nome e email são obrigatórios.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val dto = CreateClientDto(
                    name = s.name,
                    email = s.email,
                    phone = s.phone.ifBlank { null },
                    status = s.status,
                    score = s.score.toInt(),
                    tags = s.tags,
                    notes = s.notes.ifBlank { null }
                )
                if (clienteId == null) clientApi.createClient(dto)
                else clientApi.updateClient(clienteId, dto)
                _uiState.update { it.copy(isSaving = false, saved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}

class ClientFormViewModelFactory(
    private val clientApi: ClientApi,
    private val clienteId: String?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ClientFormViewModel(clientApi, clienteId) as T
}
