package br.com.fiap.wtcsync

import br.com.fiap.wtcsync.data.remote.*
import br.com.fiap.wtcsync.data.remote.dto.ClientDto
import br.com.fiap.wtcsync.viewmodel.ChatListUiState
import org.junit.Assert.*
import org.junit.Test

class SupportTicketFiltersTest {
    private val email = "staff@example.invalid"
    private fun client(id: String) = ClientDto(id, "Cliente $id", "$id@example.invalid", null, null, null, null, null)
    @Test fun statusFiltersPreserveRecentMessageOrderAndDoNotMixConcludedWithWaiting() {
        val state = ChatListUiState(clients = listOf(client("waiting"), client("mine"), client("closed"), client("other")),
            tickets = listOf(
                SupportTicket("waiting", null, null, null, null, "WAITING"),
                SupportTicket("mine", null, null, email, null, "IN_PROGRESS"),
                SupportTicket("closed", null, null, null, null, "CLOSED"),
                SupportTicket("other", null, null, "other@example.invalid", null, "IN_PROGRESS")
            ), lastMessageTimes = mapOf("waiting" to 10L, "mine" to 30L, "closed" to 40L, "other" to 20L))
        fun ids(filter: TicketFilter) = state.clientsForFilter(filter, email).map { it.id }
        assertEquals(listOf("mine", "other", "waiting"), ids(TicketFilter.OPEN))
        assertEquals(listOf("waiting"), ids(TicketFilter.WAITING))
        assertEquals(listOf("mine", "other"), ids(TicketFilter.ACTIVE))
        assertEquals(listOf("mine"), ids(TicketFilter.MINE))
        assertEquals(listOf("closed"), ids(TicketFilter.CLOSED))
        assertEquals(listOf("closed", "mine", "other", "waiting"), ids(TicketFilter.ALL))
        assertTrue(state.clientsForFilter(TicketFilter.MINE, null).isEmpty())
        assertEquals(listOf("mine"), state.copy(searchQuery = "mine").clientsForFilter(TicketFilter.OPEN, email).map { it.id })
    }
    @Test fun olderTicketsFallBackToOwnerButExplicitClosedStatusAlwaysWins() {
        assertEquals("WAITING", SupportTicket("1", null, null, null, null).state)
        assertEquals("IN_PROGRESS", SupportTicket("1", null, null, email, null).state)
        assertTrue(SupportTicket("1", null, null, email, null, "CLOSED").isClosed)
        assertEquals("Concluído", SupportTicket("1", null, null, null, null, "CLOSED").statusLabel())
    }
}
