package br.com.fiap.wtcsync.ui.client

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.wtcsync.WtcSyncApp
import br.com.fiap.wtcsync.data.remote.dto.ClientDto
import br.com.fiap.wtcsync.theme.*
import br.com.fiap.wtcsync.ui.components.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun ClientProfileScreen() {
    val app = LocalContext.current.applicationContext as WtcSyncApp
    val scope = rememberCoroutineScope()
    var client by remember { mutableStateOf<ClientDto?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val refresh: () -> Unit = {
        if (!loading) scope.launch {
            loading = true
            error = null
            try {
                client = app.apiService.clientApi.getMyClient()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                error = "Não foi possível carregar seus dados. Verifique a conexão e tente novamente."
            } finally {
                loading = false
            }
        }
    }
    WtcRefreshOnResume(refresh)
    Column(Modifier.fillMaxSize().background(BackgroundCream)) {
        WtcScreenHeader("Meu perfil", actions = {
            WtcButton(refresh, enabled = !loading, kind = WtcButtonKind.Text) { Text("Atualizar") }
        })
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> WtcStatePanel("Perfil indisponível", error, onRetry = refresh)
            client != null -> Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(color = CardSurface, shape = WtcShapes.medium, border = WtcBorders.subtle()) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        ProfileField("Nome", client!!.name)
                        ProfileField("E-mail", client!!.email)
                        ProfileField("Telefone", client!!.phone)
                    }
                }
                Text("Para corrigir seus dados, fale com nossa equipe na aba Chat.", color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(value?.takeIf { it.isNotBlank() } ?: "Não informado", color = TextPrimary,
            fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
