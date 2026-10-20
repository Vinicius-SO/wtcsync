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

    @Volatile
    var currentToken: String? = null
        private set
    @Volatile
    var currentRole: String? = null
        private set
    @Volatile
    var currentEmail: String? = null
        private set

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val role: Flow<String?> = context.dataStore.data.map { it[ROLE_KEY] }
    val email: Flow<String?> = context.dataStore.data.map { it[EMAIL_KEY] }

    suspend fun tokenSnapshot(): String? = context.dataStore.data.first()[TOKEN_KEY]?.takeUnless { isExpired(it) }
    suspend fun accountSnapshot(): Pair<String, String>? {
        val prefs = context.dataStore.data.first()
        val token = prefs[TOKEN_KEY]?.takeUnless { isExpired(it) } ?: return null
        val email = prefs[EMAIL_KEY] ?: return null
        return email to token
    }

    suspend fun saveSession(token: String, role: String, email: String) {
        context.dataStore.edit {
            it[TOKEN_KEY] = token
            it[ROLE_KEY] = role
            it[EMAIL_KEY] = email
        }
        currentToken = token
        currentRole = role
        currentEmail = email
    }

    suspend fun restoreSession() {
        val prefs = context.dataStore.data.first()
        currentToken = prefs[TOKEN_KEY]
        currentRole = prefs[ROLE_KEY]
        currentEmail = prefs[EMAIL_KEY]
        if (currentToken != null && isExpired(currentToken!!)) clear()
    }

    suspend fun clear() {
        currentToken = null
        currentRole = null
        currentEmail = null
        br.com.fiap.wtcsync.util.NotificationHelper.clearAll(context)
        context.dataStore.edit {
            it.clear()
        }
    }

    fun isExpired(token: String): Boolean = try {
        val payload = String(android.util.Base64.decode(token.split(".")[1], android.util.Base64.URL_SAFE))
        val exp = org.json.JSONObject(payload).optLong("exp", 0L)
        exp <= 0L || exp <= System.currentTimeMillis() / 1000
    } catch (_: Exception) { true }

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
            // Do not log decoded JWT
            role
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Erro ao decodificar JWT", e)
            null
        }
    }
}
