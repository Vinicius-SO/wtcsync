package br.com.fiap.wtcsync.ui.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.fiap.wtcsync.theme.*
import br.com.fiap.wtcsync.ui.components.*

@Composable
fun ClientMoreScreen(onLogout: () -> Unit) {
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(BackgroundCream)) {
        WtcScreenHeader("Outras")
        WtcButton({ confirmLogout = true }, modifier = Modifier.padding(16.dp).fillMaxWidth(),
            kind = WtcButtonKind.DangerText) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Sair da conta")
        }
    }
    if (confirmLogout) AlertDialog(
        onDismissRequest = { confirmLogout = false },
        containerColor = CardSurface, titleContentColor = TextPrimary, textContentColor = TextPrimary,
        title = { Text("Sair da conta?") },
        text = { Text("Você precisará entrar novamente para acessar suas conversas e seus dados.") },
        confirmButton = {
            WtcButton({ confirmLogout = false; onLogout() }, kind = WtcButtonKind.DangerText) { Text("Sair") }
        },
        dismissButton = {
            WtcButton({ confirmLogout = false }, kind = WtcButtonKind.Text) { Text("Cancelar") }
        }
    )
}
