package br.com.fiap.wtcsync.campaigns.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcsync.campaigns.data.CampaignRepository
import br.com.fiap.wtcsync.campaigns.domain.CampaignAction
import br.com.fiap.wtcsync.campaigns.domain.CreateCampaignRequest
import br.com.fiap.wtcsync.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CreateCampaignUiState(
    val title: String = "",
    val body: String = "",
    val segmentId: String = "",
    val mediaUrl: String = "",
    val deeplink: String = "",
    val actions: List<ActionField> = listOf(ActionField()),
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val titleError: String? = null,
    val bodyError: String? = null,
    val segmentError: String? = null
)

data class ActionField(
    val action: String = "",
    val title: String = "",
    val url: String = ""
)

class CreateCampaignViewModel(
    private val repository: CampaignRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCampaignUiState())
    val uiState: StateFlow<CreateCampaignUiState> = _uiState

    fun onTitleChanged(value: String) {
        _uiState.value = _uiState.value.copy(title = value, titleError = null)
    }

    fun onBodyChanged(value: String) {
        _uiState.value = _uiState.value.copy(body = value, bodyError = null)
    }

    fun onSegmentIdChanged(value: String) {
        _uiState.value = _uiState.value.copy(segmentId = value, segmentError = null)
    }

    fun onMediaUrlChanged(value: String) {
        _uiState.value = _uiState.value.copy(mediaUrl = value)
    }

    fun onDeeplinkChanged(value: String) {
        _uiState.value = _uiState.value.copy(deeplink = value)
    }

    fun onActionChanged(index: Int, action: String) {
        val actions = _uiState.value.actions.toMutableList()
        if (index < actions.size) {
            actions[index] = actions[index].copy(action = action)
            _uiState.value = _uiState.value.copy(actions = actions)
        }
    }

    fun onActionTitleChanged(index: Int, title: String) {
        val actions = _uiState.value.actions.toMutableList()
        if (index < actions.size) {
            actions[index] = actions[index].copy(title = title)
            _uiState.value = _uiState.value.copy(actions = actions)
        }
    }

    fun onActionUrlChanged(index: Int, url: String) {
        val actions = _uiState.value.actions.toMutableList()
        if (index < actions.size) {
            actions[index] = actions[index].copy(url = url)
            _uiState.value = _uiState.value.copy(actions = actions)
        }
    }

    fun addAction() {
        _uiState.value = _uiState.value.copy(
            actions = _uiState.value.actions + ActionField()
        )
    }

    fun removeAction(index: Int) {
        val actions = _uiState.value.actions.toMutableList()
        if (actions.size > 1 && index < actions.size) {
            actions.removeAt(index)
            _uiState.value = _uiState.value.copy(actions = actions)
        }
    }

    fun submit() {
        val state = _uiState.value

        var hasError = false
        if (state.title.isBlank()) {
            _uiState.value = _uiState.value.copy(titleError = "Título é obrigatório")
            hasError = true
        }
        if (state.body.isBlank()) {
            _uiState.value = _uiState.value.copy(bodyError = "Mensagem é obrigatória")
            hasError = true
        }
        if (state.segmentId.isBlank()) {
            _uiState.value = _uiState.value.copy(segmentError = "Segmento é obrigatório")
            hasError = true
        }

        val hasPartialActions = state.actions.any {
            it.action.isNotBlank() || it.title.isNotBlank() || it.url.isNotBlank()
        }
        val validActions = if (hasPartialActions) {
            val incomplete = state.actions.filter {
                it.action.isNotBlank() || it.title.isNotBlank() || it.url.isNotBlank()
            }.filter {
                it.action.isBlank() || it.title.isBlank() || it.url.isBlank()
            }
            if (incomplete.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    error = "Preencha todos os campos dos botões de ação ou remova os incompletos"
                )
                hasError = true
            }
            state.actions.filter { it.action.isNotBlank() && it.title.isNotBlank() && it.url.isNotBlank() }
        } else {
            emptyList()
        }

        if (hasError) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            val request = CreateCampaignRequest(
                title = state.title,
                body = state.body,
                segmentId = state.segmentId,
                mediaUrl = state.mediaUrl.ifBlank { null },
                deeplink = state.deeplink.ifBlank { null },
                actions = validActions.map { CampaignAction(it.action, it.title) },
                actionUrls = validActions.associate { it.action to it.url }
            )
            when (val result = repository.createCampaign(request)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, success = true)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = result.message ?: "Erro ao criar campanha"
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(success = false)
    }
}
