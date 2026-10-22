package br.com.fiap.wtcsync.ui.crm

import br.com.fiap.wtcsync.ui.components.*
import br.com.fiap.wtcsync.theme.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.saveable.rememberSaveable
import br.com.fiap.wtcsync.data.remote.*
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.wtcsync.WtcSyncApp
import br.com.fiap.wtcsync.data.remote.dto.ClientDto
import br.com.fiap.wtcsync.theme.BackgroundCream
import br.com.fiap.wtcsync.theme.BorderColor
import br.com.fiap.wtcsync.theme.HeaderBg
import br.com.fiap.wtcsync.theme.ListBorderLight
import br.com.fiap.wtcsync.theme.TextPrimary
import br.com.fiap.wtcsync.theme.TextSecondary
import br.com.fiap.wtcsync.theme.YellowBadge
import br.com.fiap.wtcsync.theme.YellowText
import br.com.fiap.wtcsync.ui.components.ClientAvatar
import br.com.fiap.wtcsync.viewmodel.ChatListViewModel
import br.com.fiap.wtcsync.viewmodel.ChatListViewModelFactory

@Composable
fun ChatListScreen(
    onChatClick: (clienteId: String, contactName: String) -> Unit = { _, _ -> },
    onCreateClick: () -> Unit = {}
) {
    val app = LocalContext.current.applicationContext as WtcSyncApp
    val viewModel: ChatListViewModel = viewModel(
        factory = ChatListViewModelFactory(
            clientApi = app.apiService.clientApi,
            messageApi = app.apiService.messageApi,
            sessionManager = app.sessionManager,
            context = app,
            supportApi = app.apiService.supportApi
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    var isSearching by remember { mutableStateOf(false) }
    var selectedFilter by rememberSaveable { mutableStateOf(TicketFilter.OPEN) }
    val displayedClients = uiState.clientsForFilter(selectedFilter, app.sessionManager.currentEmail)

    Column(modifier = Modifier.fillMaxSize().background(BackgroundCream)) {
        // Header
        WtcScreenHeader("Mensagens", subtitle = if (uiState.connected) "Atualização em tempo real" else "Reconectando", actions = {
            IconButton(onClick = { isSearching = !isSearching }) {
                Icon(Icons.Default.Search, contentDescription = "Buscar conversa", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
            WtcCreateButton(onCreateClick)
        })

        if (isSearching) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = { Text("Buscar contato...", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaveText,
                    unfocusedBorderColor = SaveText,
                    focusedContainerColor = InputSurface,
                    unfocusedContainerColor = InputSurface
                )
            )
        }

        // Filter pills
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TicketFilter.entries.forEach { filter ->
                ChatFilterPill(label = filter.label, isActive = selectedFilter == filter, onClick = { selectedFilter = filter })
            }
        }

        uiState.error?.let { message ->
            WtcFeedback(message, onDismiss = { viewModel.dismissError() })
            WtcButton({ viewModel.loadClients(silent = true) }, kind = WtcButtonKind.Text) { Text("Tentar novamente") }
        }
        when {
            uiState.isLoading && uiState.clients.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SaveText)
            }
            displayedClients.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhuma conversa encontrada", color = TextSecondary)
            }
            else -> {
                LazyColumn(modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(displayedClients, key = { it.id }) { cliente ->
                        Box(Modifier.animateItem(
                            fadeInSpec = null, fadeOutSpec = null,
                            placementSpec = if (LocalReduceMotion.current) androidx.compose.animation.core.snap() else androidx.compose.animation.core.tween(WtcMotion.MEDIUM)
                        )) {
                        ChatConversationItem(
                            cliente = cliente,
                            lastMessage = uiState.lastMessages[cliente.id] ?: "",
                            ticket = uiState.tickets.firstOrNull { it.customerId == cliente.id },
                            unreadCount = uiState.unreadCounts[cliente.id] ?: 0,
                            onClick = { onChatClick(cliente.id, cliente.name) }
                        )
                        }

                    }

                }
            }
        }
    }
}

@Composable
private fun ChatFilterPill(label: String, isActive: Boolean, onClick: () -> Unit) {
    WtcFilterChip(label, isActive, onClick)
}

@Composable
private fun ChatConversationItem(
    cliente: ClientDto,
    lastMessage: String,
    ticket: SupportTicket?,
    unreadCount: Int,
    onClick: () -> Unit
) {
    val hasUnread = unreadCount > 0
    WtcCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ClientAvatar(name = cliente.name, size = 44.dp, showOnlineIndicator = false)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(ticket?.statusLabel() ?: "Aguardando", color = if (ticket?.isClosed == true) SuccessText else TextSecondary,
                fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = cliente.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = lastMessage.ifBlank { cliente.email },
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (ticket?.assignedTo != null && !ticket.isClosed) Text("Responsável: ${ticket.assignedTo}",
                color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (hasUnread) {
                Surface(color = BrandAccent, shape = androidx.compose.foundation.shape.CircleShape) {
                    Text(if (unreadCount > 99) "99+" else unreadCount.toString(), color = OnBrandAccent,
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
    }
}
}
