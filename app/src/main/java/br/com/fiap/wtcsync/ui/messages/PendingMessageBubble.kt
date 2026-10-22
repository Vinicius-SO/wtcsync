package br.com.fiap.wtcsync.ui.messages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.wtcsync.data.messages.PendingMessage
import br.com.fiap.wtcsync.theme.*
import br.com.fiap.wtcsync.ui.components.*

@Composable
fun PendingMessageBubble(message: PendingMessage, sending: Boolean, canRetry: Boolean, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(vertical = 3.dp), contentAlignment = Alignment.CenterEnd) {
        Surface(color = MessageSentBg, shape = WtcCorners.message(outgoing = true), border = WtcBorders.subtle(),
            modifier = Modifier.padding(horizontal = 4.dp).fillMaxWidth(0.78f)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(message.text, color = MessageSentText, fontSize = 15.sp)
                Text(when {
                    sending -> "Enviando…"
                    message.error != null && !message.retryable -> "Não enviada"
                    else -> "Pendente"
                }, color = MessageSentText, fontSize = 12.sp)
                if (message.error != null && !sending) {
                    Text(message.error, color = MessageSentText, fontSize = 12.sp)
                    if (canRetry) WtcButton(onRetry, kind = WtcButtonKind.Secondary) { Text("Tentar novamente") }
                }
            }
        }
    }
}
