package br.com.fiap.wtcsync.segments.ui.create

import br.com.fiap.wtcsync.ui.components.*
import br.com.fiap.wtcsync.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.fiap.wtcsync.theme.themedDrawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.wtcsync.theme.*

@Composable
fun CreateSegmentScreen(
    viewModel: CreateSegmentViewModel,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.success) {
        if (state.success) {
            viewModel.resetSuccess()
            onSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
    ) {
        // Header
        WtcScreenHeader("Novo segmento", onBack = onBackClick, backLabel = "Cancelar", actions = {
            WtcButton(onClick = { viewModel.submit() }, loading = state.isSubmitting) { Text("Salvar") }
        })

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            state.error?.let {
                Text(it, color = ErrorText, fontSize = 14.sp)
            }

            // Nome
            Text("NOME DO SEGMENTO", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = TextSecondary, letterSpacing = 0.44.sp)
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChanged,
                placeholder = { Text("Ex: Clientes VIP 2024") },
                singleLine = true,
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it, color = ErrorText) } },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BorderColor,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface
                )
            )

            // Descrição
            Text("DESCRIÇÃO", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = TextSecondary, letterSpacing = 0.44.sp)
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChanged,
                placeholder = { Text("Descreva o propósito deste segmento...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BorderColor,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface
                )
            )

            // Filtros
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = WtcShapes.medium,
                color = CardSurface,
                border = WtcBorders.subtle()
            ) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    Text("Filtros de seleção", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = TextPrimary)

                    // Tags
                    Text("TAGS", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = TextSecondary, letterSpacing = 0.44.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.tags.forEach { tag ->
                            Surface(
                                shape = WtcShapes.large,
                                color = YellowBadge,
                                border = WtcBorders.subtle()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(tag, fontSize = 13.sp, color = YellowText,
                                        fontWeight = FontWeight.Bold)
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = YellowText,
                                        modifier = Modifier.size(12.dp)
                                            .clickable { viewModel.onRemoveTag(tag) }
                                    )
                                }
                            }
                        }
                    }

                    // Status
                    Text("STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = TextSecondary, letterSpacing = 0.44.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Todos", "Active", "Inactive").forEach { option ->
                            val selected = state.status == option
                            WtcFilterChip(option, selected, onClick = { viewModel.onStatusChanged(option) })
                        }
                    }

                    // Score
                    Text("SCORE MÍNIMO", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = TextSecondary, letterSpacing = 0.44.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Slider(
                            value = state.minScore.toFloat(),
                            onValueChange = { viewModel.onScoreChanged(it.toInt()) },
                            valueRange = 0f..100f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = BrandAccent,
                                activeTrackColor = BrandAccent
                            )
                        )
                        Text(
                            text = "${state.minScore}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }

            // Pré-visualização
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = WtcShapes.medium,
                color = CardSurface,
                border = WtcBorders.subtle()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("PRÉ-VISUALIZAÇÃO", fontSize = 11.sp,
                            fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text("Clientes incluídos: ${state.clientCount}",
                            fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Text("Ver lista", fontSize = 14.sp, color = SaveText,
                        fontWeight = FontWeight.Bold)
                }
            }

            // Info
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = WtcShapes.medium,
                color = InfoBg
            ) {
                Text(
                    text = "Segmentos dinâmicos são atualizados automaticamente quando um novo cliente atende aos critérios acima.",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 13.sp,
                    color = InfoText
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
