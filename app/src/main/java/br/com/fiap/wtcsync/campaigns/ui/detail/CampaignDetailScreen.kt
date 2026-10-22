package br.com.fiap.wtcsync.campaigns.ui.detail

import br.com.fiap.wtcsync.ui.components.*
import br.com.fiap.wtcsync.theme.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import br.com.fiap.wtcsync.R
import br.com.fiap.wtcsync.campaigns.domain.Campaign
import br.com.fiap.wtcsync.data.model.enums.UserRole
import br.com.fiap.wtcsync.theme.BackgroundCream
import br.com.fiap.wtcsync.theme.BorderColor
import br.com.fiap.wtcsync.theme.FooterBg
import br.com.fiap.wtcsync.theme.FooterBorder
import br.com.fiap.wtcsync.theme.FooterText
import br.com.fiap.wtcsync.theme.HeaderBg
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
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun CampaignDetailScreen(
    viewModel: CampaignDetailViewModel,
    onBackClick: () -> Unit,
    userRole: UserRole? = null
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
    ) {
        DetailHeader(onBackClick = onBackClick)

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
                ErrorDetailState(
                    message = state.error!!,
                    onRetry = { viewModel.loadCampaign() },
                    onBack = onBackClick
                )
            }
            state.campaign != null -> {
                CampaignDetailContent(
                    campaign = state.campaign!!,
                    userRole = userRole,
                    isScheduling = state.isScheduling,
                    isSending = state.isSending,
                    actionError = state.actionError,
                    actionSuccess = state.actionSuccess,
                    onSchedule = { viewModel.scheduleCampaign(it) },
                    onSend = { viewModel.sendCampaign() },
                    onClearFeedback = { viewModel.clearActionFeedback() }
                )
            }
        }
    }
}

@Composable
private fun CampaignDetailContent(
    campaign: Campaign,
    userRole: UserRole?,
    isScheduling: Boolean,
    isSending: Boolean,
    actionError: String?,
    actionSuccess: String?,
    onSchedule: (String) -> Unit,
    onSend: () -> Unit,
    onClearFeedback: () -> Unit
) {
    var showScheduleDialog by remember { mutableStateOf(false) }

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

        if (actionError != null) {
            FeedbackMessage(text = actionError, isError = true, onDismiss = onClearFeedback)
        }
        if (actionSuccess != null) {
            FeedbackMessage(text = actionSuccess, isError = false, onDismiss = onClearFeedback)
        }

        if (userRole == UserRole.OPERATOR && campaign.status.lowercase() in listOf("draft", "scheduled")) {
            CampaignActions(
                status = campaign.status,
                isScheduling = isScheduling,
                isSending = isSending,
                onScheduleClick = { showScheduleDialog = true },
                onSendClick = onSend
            )
        }

        if (campaign.actions.isNotEmpty()) {
            ActionButtonsSection(campaign = campaign)
        }

        FooterSection(campaign = campaign)
    }

    if (showScheduleDialog) {
        ScheduleDateTimeDialog(
            onConfirm = { scheduledAt ->
                showScheduleDialog = false
                onSchedule(scheduledAt)
            },
            onDismiss = { showScheduleDialog = false }
        )
    }
}

@Composable
private fun CampaignActions(
    status: String,
    isScheduling: Boolean,
    isSending: Boolean,
    onScheduleClick: () -> Unit,
    onSendClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (status.lowercase() == "draft") {
            WtcButton(onClick = onScheduleClick,
                enabled = !isScheduling && !isSending,
                modifier = Modifier
                    .fillMaxWidth()
                    ) {
                if (isScheduling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ListScheduledText,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(text = if (isScheduling) "Agendando..." else "Agendar")
            }
        }

        WtcButton(onClick = onSendClick,
                enabled = !isScheduling && !isSending,
                modifier = Modifier
                .fillMaxWidth()
                ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = ListSentText,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(text = if (isSending) "Enviando..." else "Enviar Agora")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleDateTimeDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(0) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis() + 86400000L
    )
    val timePickerState = rememberTimePickerState(
        initialHour = 10,
        initialMinute = 0,
        is24Hour = true
    )

    if (step == 0) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                WtcButton(kind = WtcButtonKind.Text, onClick = { step = 1 }) {
                    Text("Próximo")
                }
            },
            dismissButton = {
                WtcButton(kind = WtcButtonKind.Text, onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (step == 1 && datePickerState.selectedDateMillis != null) {
        AlertDialog(
            onDismissRequest = { step = 0 },
            title = { Text("Selecione o horário") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                WtcButton(kind = WtcButtonKind.Text, onClick = {
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        val cal = java.util.Calendar.getInstance()
                        cal.timeInMillis = datePickerState.selectedDateMillis!!
                        cal.set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                        cal.set(java.util.Calendar.MINUTE, timePickerState.minute)
                        cal.set(java.util.Calendar.SECOND, 0)
                        cal.set(java.util.Calendar.MILLISECOND, 0)
                        onConfirm(sdf.format(cal.time))
                    }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                WtcButton(kind = WtcButtonKind.Text, onClick = { step = 0 }) {
                    Text("Voltar")
                }
            }
        )
    }
}

@Composable
private fun FeedbackMessage(text: String, isError: Boolean, onDismiss: () -> Unit) {
    WtcFeedback(text, isError, onDismiss)
}

@Composable
private fun DetailHeader(onBackClick: () -> Unit) {
    WtcScreenHeader("Campanha", onBack = onBackClick)
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
private fun StatsSection(campaign: Campaign) {
    val stats = campaign.stats
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = WtcShapes.medium,
        color = CardSurface,
        border = WtcBorders.subtle()
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
private fun ErrorDetailState(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    WtcStatePanel("Não foi possível carregar a campanha", message, onRetry = onRetry, onBack = onBack)
}
