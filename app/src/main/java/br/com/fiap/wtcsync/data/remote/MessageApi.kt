package br.com.fiap.wtcsync.data.remote

import br.com.fiap.wtcsync.data.remote.dto.MessageDto
import br.com.fiap.wtcsync.data.remote.dto.MessageRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface MessageApi {

    @GET("inbox/{customerId}")
    suspend fun getInbox(@Path("customerId") customerId: String): List<MessageDto>

    @POST("messages")
    suspend fun sendMessage(@Body request: MessageRequest): MessageDto

    @PATCH("messages/{id}/status")
    suspend fun updateStatus(
        @Path("id") id: String,
        @Body status: Map<String, String>
    ): MessageDto
}
