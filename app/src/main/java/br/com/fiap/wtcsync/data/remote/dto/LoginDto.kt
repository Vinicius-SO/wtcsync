package br.com.fiap.wtcsync.data.remote.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val email: String,
    val expiresIn: String
)
