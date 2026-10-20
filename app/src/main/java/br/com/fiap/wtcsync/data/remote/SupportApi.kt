package br.com.fiap.wtcsync.data.remote

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class SupportTicket(
    val customerId: String,
    val customerName: String?,
    val customerEmail: String?,
    val assignedTo: String?,
    val requestedAt: String?,
    val status: String? = null,
    val closedAt: String? = null
) {
    val state: String get() = status ?: if (assignedTo == null) "WAITING" else "IN_PROGRESS"
    val isClosed: Boolean get() = state == "CLOSED"
}

data class SupportHistoryEntry(val action: String, val actor: String?, val at: String?)
enum class TicketAction { CLAIM, RELEASE, CLOSE, REOPEN }
enum class TicketFilter(val label: String) {
    OPEN("Abertas"), WAITING("Aguardando"), ACTIVE("Em atendimento"), MINE("Meus"), CLOSED("Concluídas"), ALL("Todas")
}

fun SupportTicket.statusLabel(): String = when (state) {
    "CLOSED" -> "Concluído"
    "IN_PROGRESS" -> "Em atendimento"
    else -> "Aguardando"
}
interface SupportApi {
    @GET("api/support/queue") suspend fun queue(): List<SupportTicket>
    @GET("api/support/{id}") suspend fun get(@Path("id") id: String): SupportTicket
    @GET("api/support/{id}/history") suspend fun history(@Path("id") id: String): List<SupportHistoryEntry>
    @POST("api/support/{id}/close") suspend fun close(@Path("id") id: String): SupportTicket
    @POST("api/support/{id}/reopen") suspend fun reopen(@Path("id") id: String): SupportTicket
    @POST("api/support/{id}/claim") suspend fun claim(@Path("id") id: String): SupportTicket
    @POST("api/support/{id}/release") suspend fun release(@Path("id") id: String): SupportTicket
}
