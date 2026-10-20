package br.com.fiap.wtcsync.data.remote

import retrofit2.http.*

interface PushApi {
    @PATCH("api/users/me/fcm-token")
    suspend fun register(@Header("Authorization") authorization: String, @Body body: Map<String, String>)
    @HTTP(method = "DELETE", path = "api/users/me/fcm-token", hasBody = true)
    suspend fun revoke(@Header("Authorization") authorization: String, @Body body: Map<String, String>)
    @PATCH("messages/{id}/status")
    suspend fun delivered(@Header("Authorization") authorization: String, @Path("id") id: String,
        @Body body: Map<String, String>): br.com.fiap.wtcsync.data.remote.dto.MessageDto
}
