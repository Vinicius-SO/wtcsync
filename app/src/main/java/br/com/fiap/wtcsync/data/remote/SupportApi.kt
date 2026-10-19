package br.com.fiap.wtcsync.data.remote

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class SupportTicket(
    val customerId: String,
    val customerName: String?,
    val customerEmail: String?,
    val assignedTo: String?,
    val requestedAt: String?
)
interface SupportApi {
    @GET("api/support/queue") suspend fun queue(): List<SupportTicket>
    @GET("api/support/{id}") suspend fun get(@Path("id") id: String): SupportTicket
    @POST("api/support/{id}/claim") suspend fun claim(@Path("id") id: String): SupportTicket
    @POST("api/support/{id}/release") suspend fun release(@Path("id") id: String): SupportTicket
}
