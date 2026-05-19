package br.com.fiap.wtcsync.segments.data.dto

data class SegmentDto(
    val id: String,
    val name: String,
    val description: String,
    val tags: List<String>,
    val status: String,
    val minScore: Int,
    val clientCount: Int
)