package br.com.fiap.wtcsync.data.remote.dto

data class MessageDto(
    val id: String,
    val senderId: String,
    val customerId: String,
    val text: String,
    val status: String,
    val createdAt: String?
)

data class MessageRequest(
    val senderId: String,
    val customerId: String,
    val text: String
)
