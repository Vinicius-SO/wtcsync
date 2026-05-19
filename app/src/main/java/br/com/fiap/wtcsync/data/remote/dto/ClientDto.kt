package br.com.fiap.wtcsync.data.remote.dto

data class ClientDto(
    val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val status: String?,
    val score: Int?,
    val tags: List<String>?,
    val segmentId: String?
)

data class ClientProfileDto(
    val client: ClientDto,
    val lastMessages: List<MessageDto>,
    val openTasks: List<String>
)

data class CreateClientDto(
    val name: String,
    val email: String,
    val phone: String?,
    val status: String,
    val score: Int,
    val tags: List<String>,
    val notes: String?
)