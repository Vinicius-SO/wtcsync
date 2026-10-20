package br.com.fiap.wtcsync

import androidx.lifecycle.ViewModelStore
import br.com.fiap.wtcsync.data.local.SessionManager
import br.com.fiap.wtcsync.data.messages.*
import br.com.fiap.wtcsync.data.remote.*
import br.com.fiap.wtcsync.data.remote.dto.*
import br.com.fiap.wtcsync.viewmodel.MessageViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mockito.*
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

private const val OWNER = "client@example.invalid"
private const val CUSTOMER = "customer-1"

private class MemoryOutbox : MessageOutbox {
    val entries = MutableStateFlow<List<PendingMessage>>(emptyList())
    var failWrites = false
    private fun PendingMessage.belongs(owner: String, customer: String) = ownerEmail.equals(owner, true) && customerId == customer
    override fun observe(owner: String, customer: String) = entries.map { values -> values.filter { it.belongs(owner, customer) } }
    override suspend fun list(owner: String, customer: String) = observe(owner, customer).first()
    override suspend fun enqueue(message: PendingMessage) { if (failWrites) throw IOException("disk"); entries.value += message }
    override suspend fun fail(message: PendingMessage) {
        if (failWrites) throw IOException("disk")
        entries.value = entries.value.map { if (it.id == message.id && it.belongs(message.ownerEmail, message.customerId)) message else it }
    }
    override suspend fun acknowledge(owner: String, customer: String, ids: Set<String>) {
        if (failWrites) throw IOException("disk")
        entries.value = entries.value.filterNot { it.belongs(owner, customer) && it.id in ids }
    }
}

private class FakeMessages : MessageApi {
    val remote = linkedMapOf<String, MessageDto>()
    val sends = mutableListOf<MessageRequest>()
    var sendFailure: Exception? = null
    var inboxFailure: Exception? = null
    var loseAcknowledgement = false
    var wrongCorrelation = false
    var gate: CompletableDeferred<Unit>? = null
    var inboxCalls = 0
    override suspend fun getInbox(customerId: String): List<MessageDto> {
        inboxCalls++
        inboxFailure?.let { throw it }
        val snapshot = remote.values.filter { it.customerId == customerId }
        gate?.await()
        return snapshot
    }
    override suspend fun getConversation(senderId: String, customerId: String) = getInbox(customerId)
    override suspend fun sendMessage(request: MessageRequest): MessageDto {
        sends += request
        sendFailure?.let { throw it }
        val key = "${request.senderId}/${request.customerId}/${request.clientMessageId}"
        val result = remote.getOrPut(key) {
            MessageDto("server-${remote.size}", request.senderId, request.customerId, request.text, "ENTREGUE",
                "2026-10-20T15:00:00Z", clientMessageId = request.clientMessageId)
        }
        if (loseAcknowledgement) { loseAcknowledgement = false; throw IOException("response lost after commit") }
        return if (wrongCorrelation) result.copy(clientMessageId = "wrong") else result
    }
    override suspend fun updateStatus(id: String, status: Map<String, String>) = remote.values.first { it.id == id }
    override suspend fun editMessage(id: String, body: Map<String, String>) = remote.values.first { it.id == id }
    override suspend fun deleteMessage(id: String) { remote.entries.removeAll { it.value.id == id } }
}

private class FakeSupport : SupportApi {
    override suspend fun history(id: String) = emptyList<SupportHistoryEntry>()
    override suspend fun close(id: String) = get(id).copy(status = "CLOSED")
    override suspend fun reopen(id: String) = get(id).copy(status = "WAITING")
    override suspend fun get(id: String) = SupportTicket(id, "Cliente", OWNER, null, null)
    override suspend fun queue() = emptyList<SupportTicket>()
    override suspend fun claim(id: String) = get(id)
    override suspend fun release(id: String) = get(id)
}

@OptIn(ExperimentalCoroutinesApi::class)
class ReliableChatTest {
    private val stores = mutableListOf<ViewModelStore>()
    @Before fun setup() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun cleanup() { stores.forEach { it.clear() }; Dispatchers.resetMain() }
    private fun pending(owner: String = OWNER) = PendingMessage("4cba1eb1-19d2-4674-a72b-6303297c022d", owner,
        CUSTOMER, "Texto preservado", "2026-10-20T15:00:00Z")
    private fun vm(api: FakeMessages, outbox: MemoryOutbox): MessageViewModel {
        val session = mock(SessionManager::class.java)
        `when`(session.currentEmail).thenReturn(OWNER)
        `when`(session.currentRole).thenReturn("CLIENTE")
        return MessageViewModel(CUSTOMER, api, session, FakeSupport(), outbox, connectInBackground = false).also {
            val store = ViewModelStore(); store.put("chat", it); stores += store
        }
    }
    private fun http(code: Int) = HttpException(Response.error<Any>(code, "error".toResponseBody()))

    @Test fun lostAcknowledgementAndProcessRestartReuseTheSameIdWithoutDuplicating() = runTest {
        val api = FakeMessages(); val disk = MemoryOutbox(); disk.enqueue(pending())
        var now = 100L
        val sender = ReliableMessageSender(api, disk, { OWNER }, { now })
        api.loseAcknowledgement = true
        assertTrue(sender.sendNext(OWNER, CUSTOMER) is SendAttempt.Failed)
        assertEquals(1, api.remote.size)
        assertEquals(1, disk.entries.value.size)
        assertNull(sender.sendNext(OWNER, CUSTOMER)) // backoff
        val restored = MemoryOutbox()
        restored.entries.value = OutboxCodec.decode(OutboxCodec.encode(disk.entries.value))
        now += 10_000L
        val freshSender = ReliableMessageSender(api, restored, { OWNER }, { now })
        assertTrue(freshSender.sendNext(OWNER, CUSTOMER) is SendAttempt.Sent)
        assertTrue(restored.entries.value.isEmpty())
        assertEquals(1, api.remote.size)
        assertEquals(1, api.sends.map { it.clientMessageId }.distinct().size)
        assertEquals(1, api.sends.map { it.text }.distinct().size)
    }
    @Test fun persistedQueueOrderSurvivesTimestampBoundariesAndClockChanges() = runTest {
        val api = FakeMessages(); val disk = MemoryOutbox()
        val first = pending().copy(createdAt = "2026-10-20T15:00:00Z")
        val second = pending().copy(id = "9bf8603a-8a59-4972-a6a5-a7f6583b82b2", createdAt = "2026-10-20T15:00:00.001Z")
        val third = pending().copy(id = "b53f22d1-fcf1-4ed8-bb4f-5d3a2cd62e71", createdAt = "2026-10-20T14:00:00Z")
        disk.entries.value = OutboxCodec.decode(OutboxCodec.encode(listOf(first, second, third)))
        val sender = ReliableMessageSender(api, disk, { OWNER })
        repeat(3) { assertTrue(sender.sendNext(OWNER, CUSTOMER) is SendAttempt.Sent) }
        assertEquals(listOf(first.id, second.id, third.id), api.sends.map { it.clientMessageId })
    }
    @Test fun permissionFailureStopsAutomaticRetriesButAllowsAnExplicitRetry() = runTest {
        val api = FakeMessages(); val disk = MemoryOutbox(); disk.enqueue(pending())
        val sender = ReliableMessageSender(api, disk, { OWNER }, { 1_000_000L })
        api.sendFailure = http(409)
        assertTrue(sender.sendNext(OWNER, CUSTOMER) is SendAttempt.Failed)
        assertFalse(disk.entries.value.first().retryable)
        api.sendFailure = null
        assertNull(sender.sendNext(OWNER, CUSTOMER))
        assertEquals(1, api.sends.size)
        assertTrue(sender.sendNext(OWNER, CUSTOMER, pending().id) is SendAttempt.Sent)
    }
    @Test fun switchingAccountsNeverSendsAnotherAccountsPendingText() = runTest {
        val api = FakeMessages(); val disk = MemoryOutbox(); disk.enqueue(pending())
        val sender = ReliableMessageSender(api, disk, { "other@example.invalid" })
        assertNull(sender.sendNext(OWNER, CUSTOMER, pending().id))
        assertTrue(api.sends.isEmpty())
        assertEquals(1, disk.entries.value.size)
    }
    @Test fun cancellationPreservesThePendingMessage() = runTest {
        val api = FakeMessages(); val disk = MemoryOutbox(); disk.enqueue(pending())
        api.sendFailure = CancellationException("closed")
        try { ReliableMessageSender(api, disk, { OWNER }).sendNext(OWNER, CUSTOMER); fail("must propagate cancellation")
        } catch (_: CancellationException) {}
        assertEquals(listOf(pending()), disk.entries.value)
    }
    @Test fun deletedReceiptIsAcknowledgedWithoutRecreatingTheMessage() = runTest {
        val api = FakeMessages(); val disk = MemoryOutbox(); disk.enqueue(pending()); api.sendFailure = http(410)
        assertTrue(ReliableMessageSender(api, disk, { OWNER }).sendNext(OWNER, CUSTOMER) is SendAttempt.AlreadyDeleted)
        assertTrue(disk.entries.value.isEmpty()); assertTrue(api.remote.isEmpty())
    }
    @Test fun anUnrelatedResponseCannotAcknowledgeThePendingMessage() = runTest {
        val api = FakeMessages(); val disk = MemoryOutbox(); disk.enqueue(pending()); api.wrongCorrelation = true
        assertTrue(ReliableMessageSender(api, disk, { OWNER }).sendNext(OWNER, CUSTOMER) is SendAttempt.Failed)
        assertEquals(1, disk.entries.value.size); assertFalse(disk.entries.value.first().retryable)
    }
    @Test fun refreshFailureKeepsLoadedMessagesVisible() = runTest {
        val api = FakeMessages(); val disk = MemoryOutbox()
        api.remote["existing"] = MessageDto("existing", OWNER, CUSTOMER, "Histórico", "ENTREGUE", "2026-10-20T14:00:00Z")
        val chat = vm(api, disk); runCurrent()
        assertEquals(1, chat.uiState.value.messages.size)
        api.inboxFailure = IOException("offline")
        chat.loadConversation(); runCurrent()
        assertEquals("Histórico", chat.uiState.value.messages.single().text)
        assertNotNull(chat.uiState.value.historyError)
        assertTrue(chat.uiState.value.hasLoadedConversation)
        assertFalse(chat.uiState.value.isLoading)
    }
    @Test fun diskFailureDoesNotClearTheComposerOrAttemptANetworkSend() = runTest {
        val api = FakeMessages(); val disk = MemoryOutbox(); val chat = vm(api, disk); runCurrent()
        disk.failWrites = true; chat.sendMessage("Não perder"); runCurrent()
        assertEquals(0, chat.uiState.value.queuedCount)
        assertTrue(api.sends.isEmpty()); assertNotNull(chat.uiState.value.error)
    }
    @Test fun networkFailureKeepsASafelyQueuedCopyAndDoesNotClearANewerDraftOnRetry() = runTest {
        val api = FakeMessages(); val disk = MemoryOutbox(); val chat = vm(api, disk); runCurrent()
        api.sendFailure = IOException("offline"); chat.sendMessage("Texto seguro"); runCurrent()
        assertEquals(1, chat.uiState.value.queuedCount)
        assertEquals("Texto seguro", chat.uiState.value.lastQueuedText)
        assertEquals("Texto seguro", disk.entries.value.single().text)
        assertNotNull(chat.uiState.value.pending.single().error)
        api.sendFailure = null; chat.retryPending(disk.entries.value.single().id); runCurrent()
        assertEquals(1, chat.uiState.value.queuedCount) // retry never emits another composer-clear event
        assertTrue(disk.entries.value.isEmpty())
        assertEquals("Texto seguro", chat.uiState.value.messages.single().text)
    }
    @Test fun slowRefreshesAreCoalescedAndCannotReplaceANewSendWithAnOldSnapshot() = runTest {
        val api = FakeMessages(); val disk = MemoryOutbox(); api.gate = CompletableDeferred()
        val chat = vm(api, disk); runCurrent()
        repeat(4) { chat.loadConversation(silent = true) }; runCurrent()
        assertEquals(1, api.inboxCalls)
        chat.sendMessage("Novo envio"); runCurrent()
        assertEquals("Novo envio", chat.uiState.value.messages.single().text)
        api.gate!!.complete(Unit); runCurrent()
        assertEquals(2, api.inboxCalls)
        assertEquals("Novo envio", chat.uiState.value.messages.single().text)
    }
}
