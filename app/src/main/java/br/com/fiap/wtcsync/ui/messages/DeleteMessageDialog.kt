package br.com.fiap.wtcsync.ui.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import br.com.fiap.wtcsync.theme.*
import br.com.fiap.wtcsync.ui.components.*

@Composable
fun DeleteMessageDialog(deleting: Boolean, error: String?, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = { if (!deleting) onDismiss() },
        containerColor = CardSurface, titleContentColor = TextPrimary, textContentColor = TextPrimary,
        title = { Text("Apagar mensagem?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("A mensagem será removida para todos na conversa. Essa ação não pode ser desfeita.")
                if (error != null) Text(error, color = ErrorText)
            }
        },
        confirmButton = {
            WtcButton(onConfirm, loading = deleting, kind = WtcButtonKind.DangerText) { Text("Apagar") }
        },
        dismissButton = {
            WtcButton(onDismiss, enabled = !deleting, kind = WtcButtonKind.Text) { Text("Cancelar") }
        }
    )
}
