package br.com.fiap.wtcsync.campaigns.ui.create

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.wtcsync.theme.ActionCardBg
import br.com.fiap.wtcsync.theme.BackgroundCream
import br.com.fiap.wtcsync.theme.BorderColor
import br.com.fiap.wtcsync.theme.HeaderBg
import br.com.fiap.wtcsync.theme.SaveText
import br.com.fiap.wtcsync.theme.TextPrimary
import br.com.fiap.wtcsync.theme.TextSecondary
import br.com.fiap.wtcsync.theme.YellowBadge
import br.com.fiap.wtcsync.theme.YellowText

@Composable
fun CreateCampaignScreen(
    viewModel: CreateCampaignViewModel,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.success) {
        if (state.success) {
            viewModel.resetSuccess()
            onSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
    ) {
        CreateHeader(
            onCancelClick = onBackClick,
            isSubmitting = state.isSubmitting
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            state.error?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            BasicInfoSection(
                title = state.title,
                onTitleChanged = viewModel::onTitleChanged,
                titleError = state.titleError,
                body = state.body,
                onBodyChanged = viewModel::onBodyChanged,
                bodyError = state.bodyError,
                segmentId = state.segmentId,
                onSegmentIdChanged = viewModel::onSegmentIdChanged,
                segmentError = state.segmentError
            )

            MediaSection(
                mediaUrl = state.mediaUrl,
                onMediaUrlChanged = viewModel::onMediaUrlChanged
            )

            LinkSection(
                deeplink = state.deeplink,
                onDeeplinkChanged = viewModel::onDeeplinkChanged
            )

            ActionButtonsSection(
                actions = state.actions,
                onActionChanged = viewModel::onActionChanged,
                onActionTitleChanged = viewModel::onActionTitleChanged,
                onActionUrlChanged = viewModel::onActionUrlChanged,
                onAddAction = viewModel::addAction,
                onRemoveAction = viewModel::removeAction
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = viewModel::submit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = YellowBadge),
                enabled = !state.isSubmitting
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = YellowText,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = YellowText,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Criar Campanha",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = YellowText,
                        letterSpacing = (-0.18).sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateHeader(
    onCancelClick: () -> Unit,
    isSubmitting: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBg)
            .height(56.dp)
            .drawBehind {
                drawLine(
                    color = BorderColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f
                )
            }
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "CANCELAR",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.44.sp,
            modifier = Modifier.clickable(onClick = onCancelClick)
        )
        Text(
            text = "Nova Campanha",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = (-0.18).sp
        )
        Text(
            text = "ENVIAR",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSubmitting) TextSecondary else SaveText,
            letterSpacing = 0.44.sp
        )
    }
}

@Composable
private fun BasicInfoSection(
    title: String,
    onTitleChanged: (String) -> Unit,
    titleError: String?,
    body: String,
    onBodyChanged: (String) -> Unit,
    bodyError: String?,
    segmentId: String,
    onSegmentIdChanged: (String) -> Unit,
    segmentError: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChanged,
            label = { Text("Título da Campanha") },
            placeholder = { Text("Ex: Promoção de Verão") },
            singleLine = true,
            isError = titleError != null,
            supportingText = titleError?.let { { Text(it, color = Color.Red) } },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BorderColor,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        OutlinedTextField(
            value = body,
            onValueChange = onBodyChanged,
            label = { Text("Mensagem") },
            placeholder = { Text("Digite o conteúdo da sua notificação...") },
            minLines = 3,
            maxLines = 6,
            isError = bodyError != null,
            supportingText = bodyError?.let { { Text(it, color = Color.Red) } },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BorderColor,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        OutlinedTextField(
            value = segmentId,
            onValueChange = onSegmentIdChanged,
            label = { Text("Segmento ID") },
            placeholder = { Text("Ex: seg-123") },
            singleLine = true,
            isError = segmentError != null,
            supportingText = segmentError?.let { { Text(it, color = Color.Red) } },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BorderColor,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

@Composable
private fun MediaSection(
    mediaUrl: String,
    onMediaUrlChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "BANNER DA CAMPANHA (URL)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.44.sp
        )
        OutlinedTextField(
            value = mediaUrl,
            onValueChange = onMediaUrlChanged,
            placeholder = { Text("https://cdn.exemplo.com/banner.png") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BorderColor,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

@Composable
private fun LinkSection(
    deeplink: String,
    onDeeplinkChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "DEEPLINK / URL DE DESTINO",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.44.sp
        )
        OutlinedTextField(
            value = deeplink,
            onValueChange = onDeeplinkChanged,
            placeholder = { Text("https://app.wtcsync.com/promo") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = TextSecondary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BorderColor,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

@Composable
private fun ActionButtonsSection(
    actions: List<ActionField>,
    onActionChanged: (Int, String) -> Unit,
    onActionTitleChanged: (Int, String) -> Unit,
    onActionUrlChanged: (Int, String) -> Unit,
    onAddAction: () -> Unit,
    onRemoveAction: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ActionCardBg,
        border = BorderStroke(2.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BOTÕES DE AÇÃO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 0.44.sp
                )
                Surface(
                    onClick = onAddAction,
                    shape = RoundedCornerShape(8.dp),
                    color = YellowBadge,
                    border = BorderStroke(2.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = YellowText,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Adicionar",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = YellowText
                        )
                    }
                }
            }

            actions.forEachIndexed { index, action ->
                ActionFieldItem(
                    index = index,
                    action = action,
                    onActionChanged = { onActionChanged(index, it) },
                    onTitleChanged = { onActionTitleChanged(index, it) },
                    onUrlChanged = { onActionUrlChanged(index, it) },
                    onRemove = { onRemoveAction(index) },
                    canRemove = actions.size > 1
                )
            }
        }
    }
}

@Composable
private fun ActionFieldItem(
    index: Int,
    action: ActionField,
    onActionChanged: (String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onUrlChanged: (String) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "BOTÃO ${index + 1}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.44.sp
            )
            if (canRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remover",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onRemove)
                )
            }
        }

        OutlinedTextField(
            value = action.action,
            onValueChange = onActionChanged,
            label = { Text("ID da ação") },
            placeholder = { Text("Ex: btn1") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BorderColor,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        OutlinedTextField(
            value = action.title,
            onValueChange = onTitleChanged,
            label = { Text("Texto do botão") },
            placeholder = { Text("Comprar Agora") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BorderColor,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        OutlinedTextField(
            value = action.url,
            onValueChange = onUrlChanged,
            label = { Text("URL de destino") },
            placeholder = { Text("https://exemplo.com/acao") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BorderColor,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}
