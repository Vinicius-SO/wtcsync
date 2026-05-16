package br.com.fiap.wtcsync.data.remote

import br.com.fiap.wtcsync.data.remote.dto.LoginRequest
import br.com.fiap.wtcsync.data.remote.dto.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
