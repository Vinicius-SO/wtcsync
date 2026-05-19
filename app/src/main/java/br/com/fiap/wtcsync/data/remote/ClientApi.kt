package br.com.fiap.wtcsync.data.remote

import br.com.fiap.wtcsync.data.remote.dto.ClientDto
import br.com.fiap.wtcsync.data.remote.dto.ClientProfileDto
import br.com.fiap.wtcsync.data.remote.dto.CreateClientDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ClientApi {

    @GET("api/clients")
    suspend fun getClients(): List<ClientDto>

    @GET("api/clients/{id}")
    suspend fun getClientById(@Path("id") id: String): ClientDto

    @GET("api/clients/{id}/profile")
    suspend fun getClientProfile(@Path("id") id: String): ClientProfileDto

    @POST("api/clients")
    suspend fun createClient(@Body request: CreateClientDto): ClientDto

    @PUT("api/clients/{id}")
    suspend fun updateClient(@Path("id") id: String, @Body request: CreateClientDto): ClientDto
}