package com.fundcircle.app.data.datasource.remote

import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.data.dto.DepositDto
import com.fundcircle.app.data.dto.ExpenseDto
import com.fundcircle.app.domain.model.WriteStatus
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.WriteBatch
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class TransactionRemoteDataSourceImplTest {

    private val firestore: FirebaseFirestore = mockk(relaxed = true)
    private val batch: WriteBatch = mockk(relaxed = true)
    private val fundsCollection: CollectionReference = mockk(relaxed = true)
    private val fundDoc: DocumentReference = mockk(relaxed = true)
    private val depositsCollection: CollectionReference = mockk(relaxed = true)
    private val expensesCollection: CollectionReference = mockk(relaxed = true)
    private val membersCollection: CollectionReference = mockk(relaxed = true)
    private val txDoc: DocumentReference = mockk(relaxed = true)
    private val memberDoc: DocumentReference = mockk(relaxed = true)
    private val networkMonitor: NetworkMonitor = mockk()
    private val isOnline = MutableStateFlow(true)

    private lateinit var dataSource: TransactionRemoteDataSourceImpl

    private val fundId = "fund-test-100"
    private val memberId = "mem-1"

    @Before
    fun setUp() {
        mockkStatic(FieldValue::class)
        every { FieldValue.increment(any<Long>()) } answers { mockk(name = "increment(${firstArg<Long>()})") }
        every { firestore.batch() } returns batch
        every { firestore.collection("funds") } returns fundsCollection
        every { fundsCollection.document(fundId) } returns fundDoc
        every { fundDoc.collection("deposits") } returns depositsCollection
        every { fundDoc.collection("expenses") } returns expensesCollection
        every { fundDoc.collection("members") } returns membersCollection
        every { depositsCollection.document(any()) } returns txDoc
        every { expensesCollection.document(any()) } returns txDoc
        every { membersCollection.document(memberId) } returns memberDoc
        every { batch.commit() } returns Tasks.forResult(null)
        every { networkMonitor.isOnline } returns isOnline

        dataSource = TransactionRemoteDataSourceImpl(firestore, networkMonitor)
    }

    @After
    fun tearDown() {
        unmockkStatic(FieldValue::class)
    }

    private fun deposit(amount: Long = 50_000L) = DepositDto(
        depositId = "dep-001",
        fundId = fundId,
        memberId = memberId,
        amount = amount,
        depositDate = 1_000L,
        paymentMethod = "Cash"
    )

    private fun expense() = ExpenseDto(
        expenseId = "exp-001",
        fundId = fundId,
        amount = 12_000L,
        expenseDate = 1_000L,
        category = "Office & Administration",
        description = "Stationery",
        paymentMethod = "Cash"
    )

    private fun stubLedgerDoc(source: Source, isVoid: Boolean = false, exists: Boolean = true, amount: Long = 100_000L) {
        val snapshot: DocumentSnapshot = mockk(relaxed = true)
        every { snapshot.exists() } returns exists
        every { snapshot.getBoolean("isVoid") } returns isVoid
        every { snapshot.getLong("amount") } returns amount
        every { snapshot.getString("memberId") } returns memberId
        every { txDoc.get(source) } returns Tasks.forResult(snapshot)
    }

    @Test
    fun recordDeposit_writesDepositFundTotalsAndMemberTotalsInOneBatch() = runTest {
        val depositSlot = slot<Any>()
        val fundSlot = slot<Map<String, Any>>()
        val memberSlot = slot<Map<String, Any>>()
        every { batch.set(txDoc, capture(depositSlot)) } returns batch
        every { batch.update(fundDoc, capture(fundSlot)) } returns batch
        every { batch.update(memberDoc, capture(memberSlot)) } returns batch

        val before = System.currentTimeMillis()
        val result = dataSource.recordDeposit(fundId, deposit())

        assertThat(result.getOrNull()).isEqualTo(WriteStatus.SYNCED)
        verify { depositsCollection.document("dep-001") }

        // createdAt is when it was recorded, not the (possibly backdated) deposit date
        val written = depositSlot.captured as DepositDto
        assertThat(written.depositDate).isEqualTo(1_000L)
        assertThat(written.createdAt).isAtLeast(before)

        // Invariant 2: parent aggregates move by increment, paired with this deposit
        assertThat(fundSlot.captured.keys)
            .containsExactly("totalDeposits", "recordedBalance", "updatedAt", "lastTxId")
        assertThat(fundSlot.captured["lastTxId"]).isEqualTo("dep-001")

        // Member aggregates move in the same batch, so nothing scans the ledger
        assertThat(memberSlot.captured.keys).containsExactly("totalContributed", "depositCount")
        verify { FieldValue.increment(50_000L) }
        verify { FieldValue.increment(1L) }
        verify(exactly = 1) { batch.commit() }
    }

    @Test
    fun recordExpense_updatesFundTotals_andLeavesMemberTotalsAlone() = runTest {
        val expenseSlot = slot<Any>()
        val fundSlot = slot<Map<String, Any>>()
        every { batch.set(txDoc, capture(expenseSlot)) } returns batch
        every { batch.update(fundDoc, capture(fundSlot)) } returns batch

        val result = dataSource.recordExpense(fundId, expense())

        assertThat(result.getOrNull()).isEqualTo(WriteStatus.SYNCED)
        verify { expensesCollection.document("exp-001") }
        assertThat((expenseSlot.captured as ExpenseDto).createdAt).isGreaterThan(1_000L)
        assertThat(fundSlot.captured.keys)
            .containsExactly("totalExpenses", "recordedBalance", "updatedAt", "lastTxId")
        verify { FieldValue.increment(12_000L) }
        verify { FieldValue.increment(-12_000L) }
        verify(exactly = 0) { batch.update(memberDoc, any<Map<String, Any>>()) }
    }

    @Test
    fun recordDeposit_whenOffline_returnsPendingSyncWithoutWaitingForTheServer() = runTest {
        isOnline.value = false
        // A commit made offline only completes once the device reconnects
        every { batch.commit() } returns TaskCompletionSource<Void>().task

        val result = dataSource.recordDeposit(fundId, deposit())

        assertThat(result.getOrNull()).isEqualTo(WriteStatus.PENDING_SYNC)
        verify(exactly = 1) { batch.commit() }
    }

    @Test
    fun recordDeposit_whenServerNeverAcknowledges_returnsPendingSyncAfterTimeout() = runTest {
        every { batch.commit() } returns TaskCompletionSource<Void>().task

        val result = dataSource.recordDeposit(fundId, deposit())

        assertThat(result.getOrNull()).isEqualTo(WriteStatus.PENDING_SYNC)
    }

    @Test
    fun recordDeposit_whenServerRejectsTheWrite_returnsFailure() = runTest {
        every { batch.commit() } returns Tasks.forException(IllegalStateException("PERMISSION_DENIED"))

        val result = dataSource.recordDeposit(fundId, deposit())

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun voidTransaction_reversingDeposit_flipsDocAndLowersFundAndMemberTotals() = runTest {
        stubLedgerDoc(Source.DEFAULT, amount = 100_000L)
        val childSlot = slot<Map<String, Any>>()
        val fundSlot = slot<Map<String, Any>>()
        val memberSlot = slot<Map<String, Any>>()
        every { batch.update(txDoc, capture(childSlot)) } returns batch
        every { batch.update(fundDoc, capture(fundSlot)) } returns batch
        every { batch.update(memberDoc, capture(memberSlot)) } returns batch

        val result = dataSource.voidTransaction(
            fundId = fundId,
            isExpense = false,
            transactionId = "dep-100",
            originalAmount = 100_000L,
            voidReason = "Reversing accidental duplicate",
            adminEmail = "admin@example.com"
        )

        assertThat(result.getOrNull()).isEqualTo(WriteStatus.SYNCED)
        assertThat(childSlot.captured["isVoid"]).isEqualTo(true)
        assertThat(childSlot.captured["voidReason"]).isEqualTo("Reversing accidental duplicate")
        assertThat(childSlot.captured["voidedBy"]).isEqualTo("admin@example.com")
        assertThat(childSlot.captured).containsKey("voidedAt")

        assertThat(fundSlot.captured.keys)
            .containsExactly("totalDeposits", "recordedBalance", "updatedAt", "lastTxId")
        assertThat(fundSlot.captured["lastTxId"]).isEqualTo("dep-100")
        assertThat(memberSlot.captured.keys).containsExactly("totalContributed", "depositCount")
        verify { FieldValue.increment(-100_000L) }
        verify { FieldValue.increment(-1L) }
        verify(exactly = 1) { batch.commit() }
    }

    @Test
    fun voidTransaction_reversingExpense_restoresBalance_andLeavesMemberTotalsAlone() = runTest {
        stubLedgerDoc(Source.DEFAULT, amount = 25_000L)
        val fundSlot = slot<Map<String, Any>>()
        every { batch.update(fundDoc, capture(fundSlot)) } returns batch

        val result = dataSource.voidTransaction(
            fundId = fundId,
            isExpense = true,
            transactionId = "exp-200",
            originalAmount = 25_000L,
            voidReason = "Wrong vendor billed",
            adminEmail = "admin@example.com"
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(fundSlot.captured.keys)
            .containsExactly("totalExpenses", "recordedBalance", "updatedAt", "lastTxId")
        verify { FieldValue.increment(-25_000L) }
        verify { FieldValue.increment(25_000L) }
        verify(exactly = 0) { batch.update(memberDoc, any<Map<String, Any>>()) }
    }

    @Test
    fun voidTransaction_whenOffline_readsFromCacheAndQueuesTheReversal() = runTest {
        isOnline.value = false
        stubLedgerDoc(Source.CACHE)
        every { batch.commit() } returns TaskCompletionSource<Void>().task

        val result = dataSource.voidTransaction(
            fundId = fundId,
            isExpense = false,
            transactionId = "dep-100",
            originalAmount = 100_000L,
            voidReason = "Reversing duplicate",
            adminEmail = "admin@example.com"
        )

        assertThat(result.getOrNull()).isEqualTo(WriteStatus.PENDING_SYNC)
        verify(exactly = 0) { txDoc.get(Source.DEFAULT) }
    }

    @Test
    fun voidTransaction_whenAlreadyVoided_returnsFailureWithIllegalStateException() = runTest {
        stubLedgerDoc(Source.DEFAULT, isVoid = true)

        val result = dataSource.voidTransaction(
            fundId = fundId,
            isExpense = false,
            transactionId = "dep-100",
            originalAmount = 100_000L,
            voidReason = "Reversing duplicate",
            adminEmail = "admin@example.com"
        )

        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("already reversed")
        verify(exactly = 0) { batch.commit() }
    }

    @Test
    fun voidTransaction_whenDocumentNotFound_returnsFailureWithNoSuchElementException() = runTest {
        stubLedgerDoc(Source.DEFAULT, exists = false)

        val result = dataSource.voidTransaction(
            fundId = fundId,
            isExpense = false,
            transactionId = "dep-missing",
            originalAmount = 100_000L,
            voidReason = "Reversing missing",
            adminEmail = "admin@example.com"
        )

        assertThat(result.exceptionOrNull()).isInstanceOf(NoSuchElementException::class.java)
        verify(exactly = 0) { batch.commit() }
    }
}
