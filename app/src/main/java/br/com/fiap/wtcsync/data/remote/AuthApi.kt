package br.com.fiap.wtcsync.data.remote

import br.com.fiap.wtcsync.data.remote.dto.LoginRequest
import br.com.fiap.wtcsync.data.remote.dto.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET

interface AuthApi {

    @POST("api/auth/google")
    suspend fun loginGoogle(@Body request: Map<String, String>): LoginResponse

    @GET("api/auth/me")
    suspend fun me(): Map<String, String>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
