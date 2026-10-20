package br.com.fiap.wtcsync.ui.messages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.wtcsync.data.remote.*
import br.com.fiap.wtcsync.theme.*
import br.com.fiap.wtcsync.ui.components.*

@Composable
fun SupportTicketBar(ticket: SupportTicket?, staff: Boolean, email: String, busy: Boolean,
    canClose: Boolean, onAction: (TicketAction) -> Unit, onHistory: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var confirmClose by remember { mutableStateOf(false) }
    val closed = ticket?.isClosed == true
    val owner = ticket?.assignedTo
    if (confirmClose) AlertDialog(
        onDismissRequest = { confirmClose = false }, containerColor = CardSurface,
        titleContentColor = TextPrimary, textContentColor = TextPrimary,
        title = { Text("Concluir atendimento?") },
        text = { Text("A conversa ficará em Concluídas. O histórico será preservado e uma nova mensagem do cliente reabrirá o atendimento.") },
        confirmButton = { WtcButton({ confirmClose = false; onAction(TicketAction.CLOSE) }, enabled = canClose && !busy) { Text("Concluir") } },
        dismissButton = { WtcButton({ confirmClose = false }, kind = WtcButtonKind.Text) { Text("Cancelar") } }
    )
    Surface(color = HeaderBg) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                    Text(ticket?.statusLabel() ?: "Aguardando", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    if (staff && owner != null && !closed) Text("Responsável: $owner", color = TextSecondary, fontSize = 12.sp)
                    if (!staff && closed) Text("Envie uma nova mensagem para retomar o atendimento.", color = TextSecondary, fontSize = 12.sp)
                }
                if (staff) Box {
                    IconButton(onClick = { expanded = true }, enabled = !busy) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Ações do atendimento", tint = TextSecondary)
                    }
                    DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                        if (closed) DropdownMenuItem(text = { Text("Reabrir atendimento") }, onClick = { expanded = false; onAction(TicketAction.REOPEN) })
                        else if (owner == null) DropdownMenuItem(text = { Text("Assumir atendimento") }, onClick = { expanded = false; onAction(TicketAction.CLAIM) })
                        else if (owner.equals(email, ignoreCase = true)) {
                            DropdownMenuItem(text = { Text("Devolver à fila") }, onClick = { expanded = false; onAction(TicketAction.RELEASE) })
                            DropdownMenuItem(text = { Text("Concluir atendimento") }, enabled = canClose,
                                onClick = { expanded = false; confirmClose = true })
                        }
                        DropdownMenuItem(text = { Text("Histórico do atendimento") }, onClick = { expanded = false; onHistory() })
                    }
                }
            }
        }
    }
}

@Composable
fun SupportHistoryDialog(entries: List<SupportHistoryEntry>, loading: Boolean, error: String?, onRetry: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, containerColor = CardSurface, titleContentColor = TextPrimary,
        textContentColor = TextPrimary, title = { Text("Histórico do atendimento") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (loading) CircularProgressIndicator()
                if (error != null) { Text(error, color = ErrorText); WtcButton(onRetry) { Text("Tentar novamente") } }
                if (!loading && error == null && entries.isEmpty()) Text("Nenhuma movimentação registrada.")
                LazyColumn(Modifier.fillMaxWidth().heightIn(max = 360.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(entries.reversed()) { event ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(when (event.action) {
                                "OPENED" -> "Atendimento criado"
                                "CLAIMED" -> "Atendimento assumido"
                                "RELEASED" -> "Devolvido à fila"
                                "CLOSED" -> "Atendimento concluído"
                                "REOPENED" -> "Atendimento reaberto"
                                else -> "Movimentação"
                            }, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                            event.actor?.let { Text(it, color = TextSecondary, fontSize = 12.sp) }
                            val time = runCatching { java.time.Instant.parse(event.at).atZone(java.time.ZoneId.systemDefault())
                                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) }.getOrDefault("")
                            Text(time, color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }, confirmButton = { WtcButton(onDismiss, kind = WtcButtonKind.Text) { Text("Fechar") } })
}
