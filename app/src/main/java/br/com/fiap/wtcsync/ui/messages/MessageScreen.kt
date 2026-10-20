package br.com.fiap.wtcsync.ui.messages

import br.com.fiap.wtcsync.ui.components.*
import br.com.fiap.wtcsync.theme.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.fiap.wtcsync.theme.themedDrawBehind
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
    navController: NavController,
    showBackButton: Boolean = true
) {
    val app = LocalContext.current.applicationContext as WtcSyncApp
    val viewModel: MessageViewModel = viewModel(
        key = clienteId,
        factory = MessageViewModelFactory(clienteId, app.apiService.messageApi, app.sessionManager, app.apiService.supportApi)
    )
    val uiState by viewModel.uiState.collectAsState()
    val currentEmail = app.sessionManager.currentEmail ?: ""
    var messageText by rememberSaveable(clienteId) { mutableStateOf("") }
    var lastSentCount by rememberSaveable(clienteId) { mutableStateOf(0) }
    var editingId by rememberSaveable(clienteId) { mutableStateOf<String?>(null) }
    var editingText by rememberSaveable(clienteId) { mutableStateOf("") }
    var originalText by rememberSaveable(clienteId) { mutableStateOf("") }
    var lastEditedCount by rememberSaveable(clienteId) { mutableStateOf(0) }
    var deletingId by rememberSaveable(clienteId) { mutableStateOf<String?>(null) }
    var lastDeletedCount by rememberSaveable(clienteId) { mutableStateOf(0) }
    LaunchedEffect(uiState.deletedCount) {
        if (uiState.deletedCount > lastDeletedCount) deletingId = null
        lastDeletedCount = uiState.deletedCount
    }
    if (deletingId != null) {
        DeleteMessageDialog(
            deleting = uiState.isDeleting,
            error = uiState.deleteError,
            onDismiss = { deletingId = null; viewModel.clearDeleteError() },
            onConfirm = { deletingId?.let { viewModel.deleteMessage(it) } }
        )
    }
    LaunchedEffect(uiState.editedCount) {
        if (uiState.editedCount > lastEditedCount) editingId = null
        lastEditedCount = uiState.editedCount
    }
    if (editingId != null) {
        EditMessageDialog(
            text = editingText,
            onTextChange = { editingText = it },
            saving = uiState.isEditing,
            changed = editingText != originalText,
            error = uiState.editError,
            onDismiss = { editingId = null; viewModel.clearEditError() },
            onSave = { editingId?.let { viewModel.editMessage(it, editingText) } }
        )
    }
    LaunchedEffect(uiState.sentCount) {
        if (uiState.sentCount > lastSentCount) messageText = ""
        lastSentCount = uiState.sentCount
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadConversation(silent = true)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = BackgroundCream,
        topBar = {
            Column {
            ChatHeader(
                contactName = uiState.ticket?.customerName ?: contactName,
                onContactClick = if (viewModel.isStaff) ({
                    navController.navigate("clientes/${android.net.Uri.encode(clienteId)}/editar?fromChat=true")
                }) else null,
                onBackClick = { navController.popBackStack() },
                showBackButton = showBackButton,
                connectionLabel = if (uiState.connected) "Atualização em tempo real" else "Reconectando · atualização periódica"
            )
            Row(Modifier.fillMaxWidth().background(HeaderBg).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically) {
                val owner = uiState.ticket?.assignedTo
                Text(if (owner == null) "Aguardando atendimento" else if (viewModel.isStaff) "Responsável: $owner" else "Em atendimento",
                    modifier = Modifier.weight(1f), color = TextSecondary, fontSize = 12.sp)
                if (viewModel.isStaff && (owner == null || owner == currentEmail)) {
                    WtcButton(kind = WtcButtonKind.Text, enabled = !uiState.changingAssignment && !uiState.isSending,
                onClick = { viewModel.changeAssignment(release = owner == currentEmail) }) { Text(if (owner == currentEmail) "Devolver à fila" else "Assumir atendimento") }
                }
            }
            }
        },
        bottomBar = {
            if (viewModel.canReply) ChatInputBar(
                text = messageText,
                onTextChange = { messageText = it },
                isSending = uiState.isSending,
                onSendClick = {
                    // Keep the draft until the API confirms delivery.
                    viewModel.sendMessage(messageText)
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SaveText, strokeWidth = 2.dp)
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
                items(uiState.messages.sortedBy { it.createdAt }.reversed(), key = { it.id }) { message ->
                    MessageBubble(message = message, currentEmail = currentEmail, onEdit = {
                        viewModel.clearEditError()
                        editingId = message.id
                        editingText = message.text
                        originalText = message.text
                    }, onDelete = {
                        viewModel.clearDeleteError()
                        deletingId = message.id
                    })
                }
            }
        }
    }
}

@Composable
private fun ChatHeader(contactName: String, onBackClick: () -> Unit, showBackButton: Boolean, connectionLabel: String, onContactClick: (() -> Unit)?) {
    WtcScreenHeader(
        title = contactName,
        subtitle = connectionLabel,
        onBack = if (showBackButton) onBackClick else null,
        onTitleClick = onContactClick,
        leading = { ClientAvatar(name = contactName, size = 38.dp); Spacer(Modifier.width(8.dp)) }
    )
}

@Composable
fun MessageBubble(message: MessageDto, currentEmail: String, onEdit: (() -> Unit)? = null, onDelete: (() -> Unit)? = null) {
    val isMe = message.senderId.equals(currentEmail, ignoreCase = true)
    var menuExpanded by remember(message.id) { mutableStateOf(false) }
    val bubbleColor = if (isMe) YellowBadge else CardSurface
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
                        text = formatTime(message.createdAt) + if (message.editedAt != null) " · editada" else "",
                        modifier = Modifier.weight(1f),
                        fontSize = 10.sp,
                        color = textColor.copy(alpha = 0.8f)
                    )
                    if (isMe) {
                        val delivered = message.status == "ENTREGUE"
                        val statusColor = if (delivered) InfoText else BorderColor
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
                            if (onEdit != null || onDelete != null) Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Opções da mensagem", tint = textColor)
                                }
                                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                    if (onEdit != null) DropdownMenuItem(text = { Text("Editar") }, onClick = {
                                        menuExpanded = false
                                        onEdit()
                                    })
                                    if (onDelete != null) DropdownMenuItem(text = { Text("Apagar", color = ErrorText) }, onClick = {
                                        menuExpanded = false
                                        onDelete()
                                    })
                                }
                            }
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
            .themedDrawBehind { palette ->
                drawLine(palette.BorderColor, Offset(0f, 0f), Offset(size.width, 0f), 2f)
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = CardSurface,
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
        WtcButton(onClick = onSendClick, enabled = !isSending && text.isNotBlank()) {
            if (isSending) CircularProgressIndicator(Modifier.size(20.dp), color = LocalContentColor.current, strokeWidth = 2.dp)
            else Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar mensagem", modifier = Modifier.size(20.dp))
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
