package br.com.fiap.wtcsync.campaigns.ui.client

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
import androidx.compose.ui.draw.drawBehind
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

    init {
        loadLastCampaign()
    }

    fun loadLastCampaign() {
        viewModelScope.launch {
            _uiState.value = ClientCampaignUiState(isLoading = true)
            when (val result = repository.listCampaigns()) {
                is Resource.Success -> {
                    val campaigns = result.data ?: emptyList()
                    val sentCampaigns = campaigns
                        .filter { it.status.equals("sent", ignoreCase = true) }
                        .sortedByDescending { it.createdAt }

                    val lastCampaign = sentCampaigns.firstOrNull()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
    ) {
        HeaderSection()

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
private fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBg)
            .height(56.dp)
            .drawBehind {
                drawLine(
                    color = ListBorderLight,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f
                )
            }
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Campanha Recebida",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = (-0.18).sp
        )
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

        StatsSection(campaign = campaign)

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
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = BorderColor, spotColor = BorderColor),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(2.dp, BorderColor)
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
            shape = RoundedCornerShape(4.dp),
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
        Text(
            text = "Criado por: ${campaign.createdBy}",
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun BodySection(body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(2.dp, BorderColor)
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
private fun StatsSection(campaign: Campaign) {
    val stats = campaign.stats
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(2.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "ESTATÍSTICAS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.44.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(label = "Target", value = "${stats.totalTargeted}")
                StatCard(label = "Entregues", value = "${stats.totalDelivered}")
                StatCard(label = "Lidas", value = "${stats.totalRead}")
                StatCard(label = "Falhas", value = "${stats.totalFailed}")
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )
    }
}

@Composable
private fun ActionButtonsSection(campaign: Campaign) {
    val openUrl = rememberOpenUrl()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        campaign.actions.forEach { action ->
            val url = campaign.actionUrls[action.action]
            Surface(
                onClick = { url?.let { openUrl(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = BorderColor.copy(alpha = 0.4f), spotColor = BorderColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                color = YellowBadge,
                border = BorderStroke(2.dp, BorderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = YellowText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = action.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = YellowText,
                        letterSpacing = 0.75.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberOpenUrl(): (String) -> Unit {
    val context = androidx.compose.ui.platform.LocalContext.current
    return { url ->
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
        context.startActivity(intent)
    }
}

@Composable
private fun FooterSection(campaign: Campaign) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = FooterBg,
            border = BorderStroke(1.dp, FooterBorder)
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
                    text = "Criado em ${campaign.createdAt.take(10)}",
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Erro ao carregar campanha",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = TextSecondary
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = YellowBadge)
        ) {
            Text("Tentar novamente", color = TextPrimary)
        }
    }
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
