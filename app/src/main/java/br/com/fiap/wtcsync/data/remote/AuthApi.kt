package br.com.fiap.wtcsync.data.remote

import br.com.fiap.wtcsync.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header

interface AuthApi {
    @POST("api/auth/google")
    suspend fun loginGoogle(@Body request: Map<String, String>): LoginResponse

    @GET("api/auth/me")
    suspend fun me(@Header("Authorization") authorization: String): AuthProfile

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse
}
