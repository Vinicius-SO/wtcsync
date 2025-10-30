package br.com.fiap.wtcsync.data.model
import br.com.fiap.wtcsync.data.model.enums.UserRole

data class User(
    val uid: String,
    val name: String?,
    val email: String?,
    val role: UserRole = UserRole.CLIENTE // padrão: cliente
)