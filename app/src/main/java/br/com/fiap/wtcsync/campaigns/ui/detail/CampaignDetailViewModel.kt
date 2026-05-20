package br.com.fiap.wtcsync.campaigns.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.wtcsync.campaigns.data.CampaignRepository
import br.com.fiap.wtcsync.campaigns.domain.Campaign
import br.com.fiap.wtcsync.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CampaignDetailUiState(
    val campaign: Campaign? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isScheduling: Boolean = false,
    val isSending: Boolean = false,
    val actionError: String? = null,
    val actionSuccess: String? = null
)

class CampaignDetailViewModel(
    private val repository: CampaignRepository,
    private val campaignId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(CampaignDetailUiState())
    val uiState: StateFlow<CampaignDetailUiState> = _uiState

    init {
        loadCampaign()
    }

    fun loadCampaign() {
        viewModelScope.launch {
            _uiState.value = CampaignDetailUiState(isLoading = true)
            when (val result = repository.getCampaign(campaignId)) {
                is Resource.Success -> {
                    _uiState.value = CampaignDetailUiState(campaign = result.data)
                }
                is Resource.Error -> {
                    _uiState.value = CampaignDetailUiState(
                        error = result.message ?: "Campanha não encontrada"
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun scheduleCampaign(scheduledAt: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isScheduling = true,
                actionError = null,
                actionSuccess = null
            )
            when (val result = repository.scheduleCampaign(campaignId, scheduledAt)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        campaign = result.data,
                        isScheduling = false,
                        actionSuccess = "Campanha agendada com sucesso"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isScheduling = false,
                        actionError = result.message ?: "Erro ao agendar campanha"
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun sendCampaign() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSending = true,
                actionError = null,
                actionSuccess = null
            )
            when (val result = repository.sendCampaign(campaignId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        campaign = result.data,
                        isSending = false,
                        actionSuccess = "Campanha enviada com sucesso"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        actionError = result.message ?: "Erro ao enviar campanha"
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearActionFeedback() {
        _uiState.value = _uiState.value.copy(actionError = null, actionSuccess = null)
    }
}
