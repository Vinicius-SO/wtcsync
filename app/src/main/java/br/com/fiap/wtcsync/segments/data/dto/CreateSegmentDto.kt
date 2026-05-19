package br.com.fiap.wtcsync.segments.data.dto

data class CreateSegmentDto(
    val name: String,
    val description: String,
    val tags: List<String>,
    val status: String,
    val minScore: Int
)