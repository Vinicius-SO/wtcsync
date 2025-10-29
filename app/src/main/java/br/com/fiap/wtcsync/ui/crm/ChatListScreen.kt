package br.com.fiap.wtcsync.ui.crm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.wtcsync.data.model.Cliente
import br.com.fiap.wtcsync.viewmodel.ChatListViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onChatClick: (Cliente) -> Unit = {},
    onNavigateToCampanha: () -> Unit = {} // ✅ Novo parâmetro pra navegar pra Campanha
) {
    var isSearching by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Mensagens Diretas") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar",
                            modifier = Modifier.size(26.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { isSearching = !isSearching }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar",
                            modifier = Modifier.size(26.dp))
                    }
                }
            )
        },
        floatingActionButton = { // ✅ Botão flutuante para ir pra Campanha
            FloatingActionButton(
                onClick = onNavigateToCampanha,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Ir para Campanha",
                    tint = MaterialTheme.colorScheme.onPrimary
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


