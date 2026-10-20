package br.com.fiap.wtcsync
import br.com.fiap.wtcsync.util.*
import br.com.fiap.wtcsync.data.messages.receiptLabel
import org.junit.Test
import org.junit.Assert.*
class NotificationRoutingTest {
    @Test fun notificationTargetsOnlyTheSignedInRecipient() {
        val target = ChatNotificationTarget("Client@Example.invalid", "customer", "message")
        assertTrue(target.matches("client@example.invalid"))
        assertFalse(target.matches("other@example.invalid"))
        assertFalse(target.matches(null))
        assertFalse(target.copy(customerId = "").matches("client@example.invalid"))
    }
    @Test fun activeConversationDoesNotSuppressAnotherConversationOrAccount() {
        ChatVisibility.activate("client@example.invalid", "a")
        assertTrue(ChatVisibility.isActive("CLIENT@example.invalid", "a"))
        assertFalse(ChatVisibility.isActive("client@example.invalid", "b"))
        assertFalse(ChatVisibility.isActive("other@example.invalid", "a"))
        ChatVisibility.deactivate("client@example.invalid", "b")
        assertTrue(ChatVisibility.isActive("client@example.invalid", "a"))
        ChatVisibility.deactivate("client@example.invalid", "a")
        assertFalse(ChatVisibility.isActive("client@example.invalid", "a"))
    }
    @Test fun readStatusHasItsOwnLabel() {
        assertEquals("Enviado", receiptLabel("ENVIADO"))
        assertEquals("Entregue", receiptLabel("ENTREGUE"))
        assertEquals("Lido", receiptLabel("LIDO"))
        assertEquals("Falha", receiptLabel("FALHA"))
    }
}
