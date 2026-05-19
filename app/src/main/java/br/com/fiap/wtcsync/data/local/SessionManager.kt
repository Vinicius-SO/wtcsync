package br.com.fiap.wtcsync.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val ROLE_KEY = stringPreferencesKey("user_role")
        private val EMAIL_KEY = stringPreferencesKey("user_email")
    }

    var currentToken: String? = null
        private set
    var currentRole: String? = null
        private set
    var currentEmail: String? = null
        private set

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val role: Flow<String?> = context.dataStore.data.map { it[ROLE_KEY] }
    val email: Flow<String?> = context.dataStore.data.map { it[EMAIL_KEY] }

    suspend fun saveSession(token: String, role: String, email: String) {
        currentToken = token
        currentRole = role
        currentEmail = email
        context.dataStore.edit {
            it[TOKEN_KEY] = token
            it[ROLE_KEY] = role
            it[EMAIL_KEY] = email
        }
    }

    suspend fun restoreSession() {
        val prefs = context.dataStore.data.first()
        currentToken = prefs[TOKEN_KEY]
        currentRole = prefs[ROLE_KEY]
        currentEmail = prefs[EMAIL_KEY]
    }

    suspend fun clear() {
        currentToken = null
        currentRole = null
        currentEmail = null
        context.dataStore.edit {
            it.clear()
        }
    }

    fun decodeRoleFromJwt(token: String): String? {
        return try {
            val payload = token.split(".")[1]
            val decoded = String(android.util.Base64.decode(payload, android.util.Base64.URL_SAFE))
            val json = org.json.JSONObject(decoded)
            val role = json.optString("role")
                .takeIf { it.isNotBlank() }
                ?: json.optString("roles")
                    ?.let { org.json.JSONArray(it).optString(0) }
                    ?.takeIf { it.isNotBlank() }
                ?: json.optString("user_role")
                    .takeIf { it.isNotBlank() }
            android.util.Log.d("SessionManager", "JWT payload: $decoded")
            role
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Erro ao decodificar JWT", e)
            null
        }
    }
}
