package br.com.fiap.wtcsync.data.remote.dto

import br.com.fiap.wtcsync.data.model.enums.UserRole
import java.util.Locale

data class AuthProfile(
    val email: String,
    val role: String?,
    val id: String? = null,
    val name: String? = null
) {
    fun userRole(): UserRole? = when (role?.trim()?.uppercase(Locale.ROOT)?.removePrefix("ROLE_")) {
        "ADMIN", "OPERATOR" -> UserRole.OPERATOR
        "ATENDENTE" -> UserRole.ATENDENTE
        "CLIENT", "CLIENTE" -> UserRole.CLIENTE
        else -> null
    }
}

data class RegisterRequest(val email: String, val password: String, val name: String)
data class RegisterResponse(val id: String, val email: String)
