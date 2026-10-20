package br.com.fiap.wtcsync.ui.crm

import br.com.fiap.wtcsync.ui.components.*
import br.com.fiap.wtcsync.theme.*
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.fiap.wtcsync.theme.themedDrawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.wtcsync.WtcSyncApp
import br.com.fiap.wtcsync.theme.BackgroundCream
import br.com.fiap.wtcsync.theme.BorderColor
import br.com.fiap.wtcsync.theme.HeaderBg
import br.com.fiap.wtcsync.theme.ListBorderLight
import br.com.fiap.wtcsync.theme.SaveText
import br.com.fiap.wtcsync.theme.TextPrimary
import br.com.fiap.wtcsync.theme.TextSecondary
import br.com.fiap.wtcsync.theme.YellowBadge
import br.com.fiap.wtcsync.theme.YellowText
import br.com.fiap.wtcsync.ui.components.TagChip
import br.com.fiap.wtcsync.viewmodel.ClientFormViewModel
import br.com.fiap.wtcsync.viewmodel.ClientFormViewModelFactory

@Composable
fun ClientFormScreen(
    clienteId: String?,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    fromConversation: Boolean = false
) {
    val app = LocalContext.current.applicationContext as WtcSyncApp
    val viewModel: ClientFormViewModel = viewModel(
        key = clienteId ?: "new",
        factory = ClientFormViewModelFactory(app.apiService.clientApi, clienteId)
    )
    val uiState by viewModel.uiState.collectAsState()
    var showTagDialog by remember { mutableStateOf(false) }
    var tagInput by remember { mutableStateOf("") }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSave()
    }

    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false; tagInput = "" },
            title = { Text("Adicionar tag", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    label = { Text("Tag") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BorderColor,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = CardSurface,
                        unfocusedContainerColor = CardSurface
                    )
                )
            },
            confirmButton = {
                WtcButton(kind = WtcButtonKind.Text, onClick = {
                    viewModel.addTag(tagInput)
                    tagInput = ""
                    showTagDialog = false
                }) { Text("Adicionar") }
            },
            dismissButton = {
                WtcButton(kind = WtcButtonKind.Text, onClick = { showTagDialog = false; tagInput = "" }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundCream)) {
        // Header
        WtcScreenHeader(if (clienteId == null) "Novo cliente" else "Editar cliente", onBack = onCancel,
            backLabel = if (fromConversation) "Voltar à conversa" else "Cancelar", actions = {
                WtcButton(onClick = viewModel::save, enabled = !uiState.isLoading, loading = uiState.isSaving) { Text("Salvar") }
            })

        // Error banner
        if (uiState.error != null) WtcFeedback(uiState.error!!)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Surface(
                border = BorderStroke(1.5.dp, ListBorderLight),
                shape = RoundedCornerShape(16.dp),
                color = CardSurface
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Avatar
                    Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = InputDisabled,
                            border = BorderStroke(2.dp, ListBorderLight)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(16.dp), tint = TextSecondary)
                            }
                        }
                        Surface(
                            modifier = Modifier.size(28.dp).align(Alignment.BottomEnd),
                            shape = CircleShape,
                            color = YellowBadge,
                            border = BorderStroke(2.dp, BorderColor)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.padding(5.dp), tint = YellowText)
                            }
                        }
                    }

                    // Nome
                    FormField(label = "NOME COMPLETO") {
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.onNameChange(it) },
                            placeholder = { Text("Ex: João Silva", color = TextSecondary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = formFieldColors()
                        )
                    }

                    // Email
                    FormField(label = "EMAIL") {
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.onEmailChange(it) },
                            placeholder = { Text("contato@empresa.com", color = TextSecondary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = formFieldColors()
                        )
                    }

                    // Telefone
                    FormField(label = "TELEFONE") {
                        OutlinedTextField(
                            value = uiState.phone,
                            onValueChange = { viewModel.onPhoneChange(it) },
                            placeholder = { Text("+55 (11) 99999-9999", color = TextSecondary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = formFieldColors()
                        )
                    }

                    // Status
                    FormField(label = "STATUS") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Active", "Inactive", "Prospect").forEach { option ->
                                val isSelected = uiState.status == option
                                Surface(
                                    onClick = { viewModel.onStatusChange(option) },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) YellowBadge else CardSurface,
                                    border = BorderStroke(
                                        if (isSelected) 2.dp else 1.5.dp,
                                        if (isSelected) BorderColor else ListBorderLight
                                    )
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = option,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) YellowText else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Lead Score
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("LEAD SCORE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 0.44.sp)
                            Text("${uiState.score.toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Slider(
                            value = uiState.score,
                            onValueChange = { viewModel.onScoreChange(it) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = YellowBadge,
                                activeTrackColor = YellowBadge,
                                inactiveTrackColor = ListBorderLight
                            )
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("FRIO", fontSize = 11.sp, color = TextSecondary)
                            Text("QUENTE", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    // Tags
                    FormField(label = "TAGS E SEGMENTOS") {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.tags.forEach { tag ->
                                TagChip(label = tag, onRemove = { viewModel.removeTag(tag) })
                            }
                            Surface(
                                onClick = { showTagDialog = true },
                                shape = RoundedCornerShape(50),
                                color = Color.Transparent,
                                border = BorderStroke(1.5.dp, BorderColor)
                            ) {
                                Text(
                                    "+ Adicionar",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Notas
                    FormField(label = "NOTAS DO CLIENTE") {
                        OutlinedTextField(
                            value = uiState.notes,
                            onValueChange = { viewModel.onNotesChange(it) },
                            placeholder = { Text("Escreva observações importantes aqui...", color = TextSecondary) },
                            minLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                            colors = formFieldColors()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormField(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 0.44.sp)
        content()
    }
}

@Composable
private fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BorderColor,
    unfocusedBorderColor = BorderColor,
    focusedContainerColor = CardSurface,
    unfocusedContainerColor = CardSurface
)
