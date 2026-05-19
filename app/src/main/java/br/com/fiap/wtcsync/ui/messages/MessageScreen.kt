package br.com.fiap.wtcsync.ui.messages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.com.fiap.wtcsync.WtcSyncApp
import br.com.fiap.wtcsync.data.remote.dto.MessageDto
import br.com.fiap.wtcsync.theme.BackgroundCream
import br.com.fiap.wtcsync.theme.BorderColor
import br.com.fiap.wtcsync.theme.HeaderBg
import br.com.fiap.wtcsync.theme.ListBorderLight
import br.com.fiap.wtcsync.theme.TextPrimary
import br.com.fiap.wtcsync.theme.TextSecondary
import br.com.fiap.wtcsync.theme.YellowBadge
import br.com.fiap.wtcsync.theme.YellowText
import br.com.fiap.wtcsync.ui.components.ClientAvatar
import br.com.fiap.wtcsync.viewmodel.MessageViewModel
import br.com.fiap.wtcsync.viewmodel.MessageViewModelFactory

@Composable
fun MessageScreen(
    clienteId: String,
    contactName: String,
    navController: NavController
) {
    val app = LocalContext.current.applicationContext as WtcSyncApp
    val viewModel: MessageViewModel = viewModel(
        key = clienteId,
        factory = MessageViewModelFactory(clienteId, app.apiService.messageApi, app.sessionManager)
    )
    val uiState by viewModel.uiState.collectAsState()
    val currentEmail = app.sessionManager.currentEmail ?: ""
    var messageText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = BackgroundCream,
        topBar = {
            ChatHeader(
                contactName = contactName,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            ChatInputBar(
                text = messageText,
                onTextChange = { messageText = it },
                isSending = uiState.isSending,
                onSendClick = {
                    viewModel.sendMessage(messageText)
                    messageText = ""
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = YellowBadge, strokeWidth = 2.dp)
            }

            uiState.error != null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Erro: ${uiState.error}",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }

            uiState.messages.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ClientAvatar(name = contactName, size = 64.dp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Inicie uma conversa com $contactName",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                reverseLayout = true,
                verticalArrangement = Arrangement.Bottom,
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(uiState.messages.sortedBy { it.createdAt }.reversed()) { message ->
                    MessageBubble(message = message, currentEmail = currentEmail)
                }
            }
        }
    }
}

@Composable
private fun ChatHeader(contactName: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBg)
            .height(56.dp)
            .drawBehind {
                drawLine(BorderColor, Offset(0f, size.height), Offset(size.width, size.height), 2f)
            }
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        ClientAvatar(name = contactName, size = 38.dp, showOnlineIndicator = true)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
        ) {
            Text(
                text = contactName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                )
                Spacer(Modifier.width(4.dp))
                Text("Online", fontSize = 11.sp, color = TextSecondary)
            }
        }
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Mais opções",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun MessageBubble(message: MessageDto, currentEmail: String) {
    val isMe = message.senderId == currentEmail
    val bubbleColor = if (isMe) YellowBadge else Color.White
    val textColor = if (isMe) YellowText else TextPrimary
    val border = if (isMe) BorderStroke(1.5.dp, BorderColor) else BorderStroke(1.5.dp, ListBorderLight)
    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    val shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isMe) 16.dp else 4.dp,
        bottomEnd = if (isMe) 4.dp else 16.dp
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        contentAlignment = alignment
    ) {
        Surface(
            shape = shape,
            color = bubbleColor,
            border = border,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .fillMaxWidth(0.78f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(text = message.text, fontSize = 15.sp, color = textColor, lineHeight = 21.sp)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(message.createdAt),
                        fontSize = 10.sp,
                        color = textColor.copy(alpha = 0.6f)
                    )
                    if (isMe) {
                        val delivered = message.status == "ENTREGUE"
                        val statusColor = if (delivered) Color(0xFF1565C0) else BorderColor
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = if (delivered) "Entregue" else "Enviado",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            )
                            Icon(
                                imageVector = if (delivered) Icons.Default.DoneAll else Icons.Default.Done,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = statusColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    isSending: Boolean,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBg)
            .drawBehind {
                drawLine(BorderColor, Offset(0f, 0f), Offset(size.width, 0f), 2f)
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, ListBorderLight)
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Mensagem...", color = TextSecondary, fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                maxLines = 5,
                enabled = !isSending
            )
        }
        Spacer(Modifier.width(10.dp))
        Surface(
            onClick = onSendClick,
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = YellowBadge,
            border = BorderStroke(2.dp, BorderColor)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = YellowText, strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = YellowText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return ""
    return try {
        val timePart = createdAt.substringAfter("T").substringBefore(".")
        timePart.substring(0, 5)
    } catch (_: Exception) {
        ""
    }
}
