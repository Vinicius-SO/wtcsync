package br.com.fiap.wtcsync.ui.crm

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
import androidx.compose.ui.draw.drawBehind
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
            context = app
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    var isSearching by remember { mutableStateOf(false) }
    var selectedPill by remember { mutableStateOf("Todas") }

    val displayedClients = when (selectedPill) {
        "Não lidas" -> uiState.filteredClients.filter { (uiState.unreadCounts[it.id] ?: 0) > 0 }
        else        -> uiState.filteredClients
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundCream)) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderBg)
                .height(56.dp)
                .drawBehind {
                    drawLine(BorderColor, Offset(0f, size.height), Offset(size.width, size.height), 2f)
                }
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = ListBorderLight,
                    border = BorderStroke(1.5.dp, BorderColor)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp), tint = TextSecondary)
                    }
                }
                Column {
                    Text("Lista de Chats (Operador)", fontSize = 11.sp, color = TextSecondary)
                    Text("Mensagens", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { isSearching = !isSearching }) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = TextSecondary, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(4.dp))
                Surface(
                    modifier = Modifier.shadow(2.dp, RoundedCornerShape(4.dp)).clickable(onClick = onCreateClick),
                    shape = RoundedCornerShape(4.dp),
                    color = YellowBadge,
                    border = BorderStroke(2.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = YellowText, modifier = Modifier.size(12.dp))
                        Text("Novo", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = YellowText)
                    }
                }
            }
        }

        if (isSearching) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = { Text("Buscar contato...", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BorderColor,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        // Filter pills
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Todas", "Não lidas", "Grupos").forEach { pill ->
                ChatFilterPill(label = pill, isActive = selectedPill == pill, onClick = { selectedPill = pill })
            }
        }

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = YellowBadge)
            }
            uiState.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Erro: ${uiState.error}", color = TextSecondary)
            }
            displayedClients.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhuma conversa encontrada", color = TextSecondary)
            }
            else -> {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(displayedClients, key = { it.id }) { cliente ->
                        ChatConversationItem(
                            cliente = cliente,
                            lastMessage = uiState.lastMessages[cliente.id] ?: "",
                            unreadCount = uiState.unreadCounts[cliente.id] ?: 0,
                            onClick = { onChatClick(cliente.id, cliente.name) }
                        )
                        Divider(color = ListBorderLight, thickness = 1.dp)
                    }
                    item { PerformanceCard() }
                }
            }
        }
    }
}

@Composable
private fun ChatFilterPill(label: String, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (isActive) YellowBadge else Color.Transparent,
        border = BorderStroke(1.5.dp, if (isActive) BorderColor else ListBorderLight)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) YellowText else TextSecondary
        )
    }
}

@Composable
private fun ChatConversationItem(
    cliente: ClientDto,
    lastMessage: String,
    unreadCount: Int,
    onClick: () -> Unit
) {
    val hasUnread = unreadCount > 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ClientAvatar(name = cliente.name, size = 44.dp, showOnlineIndicator = true)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
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
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (hasUnread) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(YellowBadge, androidx.compose.foundation.shape.CircleShape)
                )
            }
        }
    }
}

@Composable
private fun PerformanceCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = YellowBadge,
        border = BorderStroke(2.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = "DESEMPENHO DA SEMANA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = YellowText,
                letterSpacing = 0.44.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("24", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Spacer(Modifier.width(8.dp))
                Text("Novas conversas", fontSize = 16.sp, color = TextPrimary)
            }
        }
    }
}
