package br.com.fiap.wtcsync.ui.messages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.fiap.wtcsync.theme.*
import br.com.fiap.wtcsync.ui.components.*

@Composable
fun EditMessageDialog(
    text: String,
    onTextChange: (String) -> Unit,
    saving: Boolean,
    changed: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        containerColor = CardSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = { Text("Editar mensagem") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = text, onValueChange = onTextChange,
                    label = { Text("Mensagem") }, readOnly = saving,
                    minLines = 2, maxLines = 8, modifier = Modifier.fillMaxWidth()
                )
                Text("A alteração ficará visível para todos na conversa.", color = TextSecondary)
                if (error != null) Text(error, color = ErrorText)
            }
        },
        confirmButton = {
            WtcButton(onSave, enabled = text.isNotBlank() && changed, loading = saving) { Text("Salvar") }
        },
        dismissButton = {
            WtcButton(onDismiss, enabled = !saving, kind = WtcButtonKind.Text) { Text("Cancelar") }
        }
    )
}
