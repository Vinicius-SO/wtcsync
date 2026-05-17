package br.com.fiap.wtcsync.ui.crm

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
import br.com.fiap.wtcsync.ui.components.StatusBadge
import br.com.fiap.wtcsync.ui.components.TagChip
import br.com.fiap.wtcsync.viewmodel.ClientesViewModel
import br.com.fiap.wtcsync.viewmodel.ClientesViewModelFactory

@Composable
fun ClientesScreen(
    onCreateClick: () -> Unit = {},
    onClienteClick: (id: String) -> Unit = {}
) {
    val app = LocalContext.current.applicationContext as WtcSyncApp
    val viewModel: ClientesViewModel = viewModel(
        factory = ClientesViewModelFactory(app.apiService.clientApi)
    )
    val uiState by viewModel.uiState.collectAsState()

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
                    shape = CircleShape,
                    color = ListBorderLight,
                    border = BorderStroke(1.5.dp, BorderColor)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp), tint = TextSecondary)
                    }
                }
                Text("WTC Sync", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Surface(
                modifier = Modifier.shadow(2.dp, RoundedCornerShape(4.dp)).clickable(onClick = onCreateClick),
                shape = RoundedCornerShape(4.dp),
                color = YellowBadge,
                border = BorderStroke(2.dp, BorderColor)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = YellowText, modifier = Modifier.size(12.dp))
                    Text("+ Novo", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = YellowText)
                }
            }
        }

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = YellowBadge)
            }
            uiState.error != null -> Column(
                Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Erro ao carregar clientes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Text(uiState.error!!, fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.loadClients() },
                    colors = ButtonDefaults.buttonColors(containerColor = YellowBadge)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Tentar novamente", color = TextPrimary)
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Clientes",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(12.dp))
                    // Search field
                    Surface(
                        color = Color.White,
                        border = BorderStroke(2.dp, BorderColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (uiState.searchQuery.isEmpty()) {
                                    Text("Buscar por nome ou email", color = TextSecondary, fontSize = 15.sp)
                                }
                                BasicTextField(
                                    value = uiState.searchQuery,
                                    onValueChange = { viewModel.onSearchChanged(it) },
                                    textStyle = TextStyle(fontSize = 15.sp, color = TextPrimary),
                                    cursorBrush = SolidColor(TextPrimary),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    // Status filter pills
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Todos", "Active", "Inactive", "Prospect").forEach { filter ->
                            val isActive = uiState.selectedFilter == filter
                            Surface(
                                onClick = { viewModel.onFilterChanged(filter) },
                                shape = RoundedCornerShape(50),
                                color = if (isActive) TextPrimary else Color.Transparent,
                                border = BorderStroke(1.5.dp, if (isActive) TextPrimary else ListBorderLight)
                            ) {
                                Text(
                                    text = filter,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    fontSize = 13.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isActive) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                if (uiState.clients.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nenhum cliente encontrado", color = TextSecondary, fontSize = 16.sp)
                        }
                    }
                } else {
                    items(uiState.clients, key = { it.id }) { cliente ->
                        ClientCard(cliente = cliente, onClick = { onClienteClick(cliente.id) })
                        Spacer(Modifier.height(12.dp))
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun ClientCard(cliente: ClientDto, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(2.dp, ListBorderLight),
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            ClientAvatar(name = cliente.name, size = 52.dp, shape = RoundedCornerShape(8.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cliente.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    StatusBadge(status = cliente.status)
                }
                Text(
                    text = cliente.email,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cliente.tags?.take(3)?.forEach { tag -> TagChip(label = tag) }
                    }
                    Text(
                        text = "Score ${cliente.score ?: 0}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
