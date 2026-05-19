package br.com.fiap.wtcsync.ui.crm

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Clientes") })
        }
    ) { padding ->
        Text(
            text = "Lista de Clientes",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            textAlign = TextAlign.Center
        )
    }
}
