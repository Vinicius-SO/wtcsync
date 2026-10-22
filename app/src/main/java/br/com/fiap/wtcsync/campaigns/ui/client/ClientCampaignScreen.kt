package br.com.fiap.wtcsync.campaigns.ui.client

import br.com.fiap.wtcsync.ui.components.*
import br.com.fiap.wtcsync.theme.*
import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.fiap.wtcsync.theme.themedDrawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.wtcsync.R
import br.com.fiap.wtcsync.WtcSyncApp
import br.com.fiap.wtcsync.campaigns.data.CampaignRepository
import br.com.fiap.wtcsync.campaigns.domain.Campaign
import br.com.fiap.wtcsync.theme.BackgroundCream
import br.com.fiap.wtcsync.theme.BorderColor
import br.com.fiap.wtcsync.theme.FooterBg
import br.com.fiap.wtcsync.theme.FooterBorder
import br.com.fiap.wtcsync.theme.FooterText
import br.com.fiap.wtcsync.theme.HeaderBg
import br.com.fiap.wtcsync.theme.ListBorderLight
import br.com.fiap.wtcsync.theme.ListDraftBg
import br.com.fiap.wtcsync.theme.ListDraftText
import br.com.fiap.wtcsync.theme.ListScheduledBg
import br.com.fiap.wtcsync.theme.ListScheduledText
import br.com.fiap.wtcsync.theme.ListSentBg
import br.com.fiap.wtcsync.theme.ListSentText
import br.com.fiap.wtcsync.theme.ListStatsIcon
import br.com.fiap.wtcsync.theme.TextPrimary
import br.com.fiap.wtcsync.theme.TextSecondary
import br.com.fiap.wtcsync.theme.YellowBadge
import br.com.fiap.wtcsync.theme.YellowText
import br.com.fiap.wtcsync.util.Resource
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ClientCampaignUiState(
    val campaign: Campaign? = null,
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null
)

class ClientCampaignViewModel(
    private val repository: CampaignRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientCampaignUiState(isLoading = true))
    val uiState: StateFlow<ClientCampaignUiState> = _uiState

    private var loadJob: kotlinx.coroutines.Job? = null

    fun loadLastCampaign() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = ClientCampaignUiState(isLoading = true)
            when (val result = repository.latestReceivedCampaign()) {
                is Resource.Success -> {
                    val lastCampaign = result.data
                    if (lastCampaign != null) {
                        _uiState.value = ClientCampaignUiState(campaign = lastCampaign)
                    } else {
                        _uiState.value = ClientCampaignUiState(isEmpty = true)
                    }
                }
                is Resource.Error -> {
                    _uiState.value = ClientCampaignUiState(
                        error = result.message ?: "Erro ao carregar campanhas"
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }
}

class ClientCampaignViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = application as WtcSyncApp
        return ClientCampaignViewModel(
            repository = app.campaignRepository
        ) as T
    }
}

@Composable
fun ClientCampaignScreen(application: Application) {
    val viewModel: ClientCampaignViewModel = viewModel(
        factory = ClientCampaignViewModelFactory(application)
    )
    val state by viewModel.uiState.collectAsState()
    WtcRefreshOnResume { viewModel.loadLastCampaign() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
    ) {
        WtcScreenHeader("Última campanha", actions = {
            WtcButton({ viewModel.loadLastCampaign() }, enabled = !state.isLoading, kind = WtcButtonKind.Text) { Text("Atualizar") }
        })

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                ErrorState(
                    message = state.error!!,
                    onRetry = { viewModel.loadLastCampaign() }
                )
            }
            state.isEmpty -> {
                EmptyState()
            }
            state.campaign != null -> {
                CampaignDetailView(campaign = state.campaign!!)
            }
        }
    }
}

@Composable
private fun CampaignDetailView(campaign: Campaign) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (campaign.mediaUrl != null) {
            CampaignBanner(mediaUrl = campaign.mediaUrl)
        }

        CampaignInfoSection(campaign = campaign)

        if (campaign.body.isNotBlank()) {
            BodySection(body = campaign.body)
        }


        if (campaign.actions.isNotEmpty()) {
            ActionButtonsSection(campaign = campaign)
        }

        FooterSection(campaign = campaign)
    }
}

@Composable
private fun CampaignBanner(mediaUrl: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = WtcShapes.medium,
        color = CardSurface,
        border = WtcBorders.subtle()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(193.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = mediaUrl,
                    error = painterResource(id = R.drawable.publi_image),
                    placeholder = painterResource(id = R.drawable.publi_image)
                ),
                contentDescription = "Banner da Campanha",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun CampaignInfoSection(campaign: Campaign) {
    val statusLabel = when (campaign.status.lowercase()) {
        "draft" -> "RASCUNHO"
        "scheduled" -> "AGENDADA"
        "sent" -> "ENVIADA"
        else -> campaign.status.uppercase()
    }
    val (statusBg, statusTextColor) = when (campaign.status.lowercase()) {
        "sent" -> ListSentBg to ListSentText
        "scheduled" -> ListScheduledBg to ListScheduledText
        else -> ListDraftBg to ListDraftText
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            shape = WtcShapes.extraSmall,
            color = statusBg
        ) {
            Text(
                text = statusLabel,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = statusTextColor,
                letterSpacing = 0.44.sp
            )
        }
        Text(
            text = campaign.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            letterSpacing = (-0.48).sp,
            lineHeight = 30.sp
        )

    }
}

@Composable
private fun BodySection(body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = WtcShapes.medium,
        color = CardSurface,
        border = WtcBorders.subtle()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "MENSAGEM",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.44.sp
            )
            Text(
                text = body,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = TextPrimary,
                lineHeight = 26.sp
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(campaign: Campaign) {
    WtcCampaignActions(campaign)
}



@Composable
private fun FooterSection(campaign: Campaign) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = WtcCorners.Pill,
            color = FooterBg,
            border = WtcBorders.subtle()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = FooterText,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Recebida em ${campaign.createdAt.take(10).split("-").reversed().joinToString("/")}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FooterText,
                    letterSpacing = 0.44.sp
                )
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    WtcStatePanel("Não foi possível carregar a campanha", message, onRetry = onRetry)
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Nenhuma campanha recebida",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Você ainda não recebeu nenhuma campanha.",
            fontSize = 14.sp,
            color = TextSecondary
        )
    }
}
