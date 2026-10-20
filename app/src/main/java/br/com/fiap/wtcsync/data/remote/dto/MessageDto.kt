package br.com.fiap.wtcsync.data.remote.dto

data class MessageDto(
    val id: String,
    val senderId: String,
    val customerId: String,
    val text: String,
    val status: String,
    val createdAt: String?,
    val editedAt: String? = null,
    val clientMessageId: String? = null
)

data class MessageRequest(
    val senderId: String,
    val customerId: String,
    val text: String,
    val clientMessageId: String? = null
)
