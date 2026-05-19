package br.com.fiap.wtcsync.segments.ui.create

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
import androidx.compose.ui.draw.drawBehind
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderBg)
                .height(56.dp)
                .drawBehind {
                    drawLine(
                        color = BorderColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 2f
                    )
                }
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CANCELAR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.44.sp,
                modifier = Modifier.clickable(onClick = onBackClick)
            )
            Text(
                text = "Novo Segmento",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "SALVAR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (state.isSubmitting) TextSecondary else SaveText,
                letterSpacing = 0.44.sp,
                modifier = Modifier.clickable(enabled = !state.isSubmitting) { viewModel.submit() }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            state.error?.let {
                Text(it, color = Color.Red, fontSize = 14.sp)
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
                supportingText = state.nameError?.let { { Text(it, color = Color.Red) } },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BorderColor,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
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
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Filtros
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
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
                                shape = RoundedCornerShape(20.dp),
                                color = YellowBadge,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
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
                            Surface(
                                onClick = { viewModel.onStatusChanged(option) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) YellowBadge else Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                            ) {
                                Text(
                                    text = option,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontSize = 14.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) YellowText else TextSecondary
                                )
                            }
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
                                thumbColor = YellowBadge,
                                activeTrackColor = YellowBadge
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
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
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
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE8F4FD)
            ) {
                Text(
                    text = "Segmentos dinâmicos são atualizados automaticamente quando um novo cliente atende aos critérios acima.",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 13.sp,
                    color = Color(0xFF1565C0)
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}