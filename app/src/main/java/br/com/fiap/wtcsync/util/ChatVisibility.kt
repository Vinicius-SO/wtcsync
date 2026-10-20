package br.com.fiap.wtcsync.util

object ChatVisibility {
    @Volatile private var active: Pair<String, String>? = null
    fun activate(email: String, customerId: String) { active = email.lowercase() to customerId }
    fun deactivate(email: String, customerId: String) {
        if (isActive(email, customerId)) active = null
    }
    fun isActive(email: String, customerId: String) = active == (email.lowercase() to customerId)
}

data class ChatNotificationTarget(val recipient: String, val customerId: String, val requestId: String) {
    fun matches(email: String?) = !email.isNullOrBlank() && recipient.equals(email, ignoreCase = true) && customerId.isNotBlank()
}
