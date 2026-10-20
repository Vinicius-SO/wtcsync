package br.com.fiap.wtcsync.data.remote

import br.com.fiap.wtcsync.data.remote.dto.MessageDto
import br.com.fiap.wtcsync.data.remote.dto.MessageRequest
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MessageApi {

    // Mantido para compatibilidade — busca todas as mensagens de um cliente
    @GET("inbox/{customerId}")
    suspend fun getInbox(@Path("customerId") customerId: String): List<MessageDto>

    // NOVO — busca apenas a conversa entre um operador e um cliente específicos
    @GET("conversation")
    suspend fun getConversation(
        @Query("senderId") senderId: String,
        @Query("customerId") customerId: String
    ): List<MessageDto>

    @POST("messages")
    suspend fun sendMessage(@Body request: MessageRequest): MessageDto

    @DELETE("messages/{id}")
    suspend fun deleteMessage(@Path("id") id: String)

    @PATCH("messages/{id}")
    suspend fun editMessage(@Path("id") id: String, @Body body: Map<String, String>): MessageDto

    @PATCH("messages/{id}/status")
    suspend fun updateStatus(
        @Path("id") id: String,
        @Body status: Map<String, String>
    ): MessageDto
}
