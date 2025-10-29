package br.com.fiap.wtcsync.ui.crm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.wtcsync.data.model.Cliente
import br.com.fiap.wtcsync.viewmodel.ChatListViewModel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onChatClick: (Cliente) -> Unit = {},
    onNavigateToCampanha: () -> Unit = {}
) {
    var isSearching by remember { mutableStateOf(false) }

    // botao novo com firula
    val interactionSource = remember { MutableInteractionSource() }
    val pressed = interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed.value) 0.9f else 1f,
        label = ""
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Mensagens Diretas") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isSearching = !isSearching }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Buscar",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            )
        },
        //Estilo do botao pra campanha
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCampanha,
                containerColor = Color(0xFFFDCB2E),
                shape = CircleShape,
                modifier = Modifier
                    .scale(scale)
                    .size(64.dp),
                interactionSource = interactionSource,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Ir para Campanha",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (isSearching) {
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    label = { Text("Buscar contato") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                items(viewModel.clientesFiltrados) { cliente ->
                    ChatItem(cliente = cliente, onClick = { onChatClick(cliente) })
                }
            }
        }
    }
}

@Composable
fun ChatItem(cliente: Cliente, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = cliente.nome, style = MaterialTheme.typography.titleMedium)
            Text(text = cliente.ultimaMensagem, style = MaterialTheme.typography.bodyMedium)
        }
    }
}



