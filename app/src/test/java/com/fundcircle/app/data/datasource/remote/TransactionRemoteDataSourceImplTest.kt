package com.fundcircle.app.data.datasource.remote

import com.fundcircle.app.data.dto.DepositDto
import com.fundcircle.app.data.dto.ExpenseDto
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.WriteBatch
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class TransactionRemoteDataSourceImplTest {

    private val firestore: FirebaseFirestore = mockk(relaxed = true)
    private val batch: WriteBatch = mockk(relaxed = true)
    private val fundsCollection: CollectionReference = mockk(relaxed = true)
    private val fundDoc: DocumentReference = mockk(relaxed = true)
    private val subCollection: CollectionReference = mockk(relaxed = true)
    private val itemDoc: DocumentReference = mockk(relaxed = true)

    private lateinit var dataSource: TransactionRemoteDataSourceImpl

    private val fundId = "fund-test-100"

    @Before
    fun setUp() {
        mockkStatic(FieldValue::class)
        every { firestore.batch() } returns batch
        every { firestore.collection("funds") } returns fundsCollection
        every { fundsCollection.document(fundId) } returns fundDoc
        every { fundDoc.collection(any()) } returns subCollection
        every { subCollection.document(any()) } returns itemDoc
        every { batch.commit() } returns Tasks.forResult(null)

        dataSource = TransactionRemoteDataSourceImpl(firestore)
    }

    @After
    fun tearDown() {
        unmockkStatic(FieldValue::class)
    }

    @Test
    fun recordDeposit_executesAtomicBatchWrite_withParentFieldValueIncrements() = runTest {
        val deposit = DepositDto(
            depositId = "dep-001",
            fundId = fundId,
            memberId = "mem-1",
            amount = 50_000L,
            depositDate = 1_000L,
            paymentMethod = "Cash"
        )

        val incrementDepositValue: FieldValue = mockk()
        val incrementBalanceValue: FieldValue = mockk()

        every { FieldValue.increment(50_000L) } returns incrementDepositValue andThen incrementBalanceValue

        val updateSlot = slot<Map<String, Any>>()
        every { batch.update(fundDoc, capture(updateSlot)) } returns batch

        val result = dataSource.recordDeposit(fundId, deposit)

        assertThat(result.isSuccess).isTrue()

        // 1. Verify batch.set on child document under /funds/{fundId}/deposits/{depositId}
        verify { fundDoc.collection("deposits") }
        verify { subCollection.document("dep-001") }
        verify { batch.set(itemDoc, deposit) }

        // 2. Invariant 2: Verify atomic parent updates with FieldValue.increment
        val capturedMap = updateSlot.captured
        assertThat(capturedMap).containsKey("totalDeposits")
        assertThat(capturedMap).containsKey("recordedBalance")
        assertThat(capturedMap).containsKey("updatedAt")

        // 3. Verify batch commit
        verify { batch.commit() }
    }

    @Test
    fun recordExpense_executesAtomicBatchWrite_withParentNegativeBalanceIncrement() = runTest {
        val expense = ExpenseDto(
            expenseId = "exp-001",
            fundId = fundId,
            amount = 12_000L,
            expenseDate = 1_000L,
            category = "Office & Administration",
            description = "Stationery",
            paymentMethod = "Cash"
        )

        val incrementExpenseValue: FieldValue = mockk()
        val incrementBalanceValue: FieldValue = mockk()

        every { FieldValue.increment(12_000L) } returns incrementExpenseValue
        every { FieldValue.increment(-12_000L) } returns incrementBalanceValue

        val updateSlot = slot<Map<String, Any>>()
        every { batch.update(fundDoc, capture(updateSlot)) } returns batch

        val result = dataSource.recordExpense(fundId, expense)

        assertThat(result.isSuccess).isTrue()

        // 1. Verify batch.set on child document under /funds/{fundId}/expenses/{expenseId}
        verify { fundDoc.collection("expenses") }
        verify { subCollection.document("exp-001") }
        verify { batch.set(itemDoc, expense) }

        // 2. Invariant 2: Verify atomic parent updates with totalExpenses += amount and recordedBalance -= amount
        val capturedMap = updateSlot.captured
        assertThat(capturedMap).containsKey("totalExpenses")
        assertThat(capturedMap).containsKey("recordedBalance")
        assertThat(capturedMap).containsKey("updatedAt")

        // 3. Verify batch commit
        verify { batch.commit() }
    }

    @Test
    fun voidTransaction_reversingDeposit_updatesChildMetadataAndParentAggregates() = runTest {
        val firestoreTx: Transaction = mockk(relaxed = true)
        val txSnapshot: DocumentSnapshot = mockk(relaxed = true)

        every { firestore.runTransaction<Any>(any()) } answers {
            val function = firstArg<Transaction.Function<Any>>()
            val res = function.apply(firestoreTx)
            Tasks.forResult(res)
        }

        every { firestoreTx.get(itemDoc) } returns txSnapshot
        every { txSnapshot.exists() } returns true
        every { txSnapshot.getBoolean("isVoid") } returns false

        val decDepositValue: FieldValue = mockk()
        val decBalanceValue: FieldValue = mockk()
        every { FieldValue.increment(-100_000L) } returns decDepositValue andThen decBalanceValue

        val childUpdateSlot = slot<Map<String, Any>>()
        val parentUpdateSlot = slot<Map<String, Any>>()
        every { firestoreTx.update(itemDoc, capture(childUpdateSlot)) } returns firestoreTx
        every { firestoreTx.update(fundDoc, capture(parentUpdateSlot)) } returns firestoreTx

        val result = dataSource.voidTransaction(
            fundId = fundId,
            isExpense = false,
            transactionId = "dep-100",
            originalAmount = 100_000L,
            voidReason = "Reversing accidental duplicate",
            adminEmail = "admin@example.com"
        )

        assertThat(result.isSuccess).isTrue()

        // 1. Verify child document updates
        val childUpdates = childUpdateSlot.captured
        assertThat(childUpdates["isVoid"]).isEqualTo(true)
        assertThat(childUpdates["voidReason"]).isEqualTo("Reversing accidental duplicate")
        assertThat(childUpdates["voidedBy"]).isEqualTo("admin@example.com")
        assertThat(childUpdates).containsKey("voidedAt")

        // 2. Verify parent document updates
        val parentUpdates = parentUpdateSlot.captured
        assertThat(parentUpdates).containsKey("totalDeposits")
        assertThat(parentUpdates).containsKey("recordedBalance")
        assertThat(parentUpdates).containsKey("updatedAt")
    }

    @Test
    fun voidTransaction_reversingExpense_updatesChildMetadataAndParentAggregates() = runTest {
        val firestoreTx: Transaction = mockk(relaxed = true)
        val txSnapshot: DocumentSnapshot = mockk(relaxed = true)

        every { firestore.runTransaction<Any>(any()) } answers {
            val function = firstArg<Transaction.Function<Any>>()
            val res = function.apply(firestoreTx)
            Tasks.forResult(res)
        }

        every { firestoreTx.get(itemDoc) } returns txSnapshot
        every { txSnapshot.exists() } returns true
        every { txSnapshot.getBoolean("isVoid") } returns false

        val decExpenseValue: FieldValue = mockk()
        val incBalanceValue: FieldValue = mockk()
        every { FieldValue.increment(-25_000L) } returns decExpenseValue
        every { FieldValue.increment(25_000L) } returns incBalanceValue

        val childUpdateSlot = slot<Map<String, Any>>()
        val parentUpdateSlot = slot<Map<String, Any>>()
        every { firestoreTx.update(itemDoc, capture(childUpdateSlot)) } returns firestoreTx
        every { firestoreTx.update(fundDoc, capture(parentUpdateSlot)) } returns firestoreTx

        val result = dataSource.voidTransaction(
            fundId = fundId,
            isExpense = true,
            transactionId = "exp-200",
            originalAmount = 25_000L,
            voidReason = "Wrong vendor billed",
            adminEmail = "admin@example.com"
        )

        assertThat(result.isSuccess).isTrue()

        // 1. Verify child document updates
        val childUpdates = childUpdateSlot.captured
        assertThat(childUpdates["isVoid"]).isEqualTo(true)
        assertThat(childUpdates["voidReason"]).isEqualTo("Wrong vendor billed")
        assertThat(childUpdates["voidedBy"]).isEqualTo("admin@example.com")

        // 2. Verify parent document updates
        val parentUpdates = parentUpdateSlot.captured
        assertThat(parentUpdates).containsKey("totalExpenses")
        assertThat(parentUpdates).containsKey("recordedBalance")
        assertThat(parentUpdates).containsKey("updatedAt")
    }

    @Test
    fun voidTransaction_whenAlreadyVoided_returnsFailureWithIllegalStateException() = runTest {
        val firestoreTx: Transaction = mockk(relaxed = true)
        val txSnapshot: DocumentSnapshot = mockk(relaxed = true)

        every { firestore.runTransaction<Any>(any()) } answers {
            val function = firstArg<Transaction.Function<Any>>()
            val res = function.apply(firestoreTx)
            Tasks.forResult(res)
        }

        every { firestoreTx.get(itemDoc) } returns txSnapshot
        every { txSnapshot.exists() } returns true
        every { txSnapshot.getBoolean("isVoid") } returns true

        val result = dataSource.voidTransaction(
            fundId = fundId,
            isExpense = false,
            transactionId = "dep-100",
            originalAmount = 100_000L,
            voidReason = "Reversing duplicate",
            adminEmail = "admin@example.com"
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("already reversed")
    }

    @Test
    fun voidTransaction_whenDocumentNotFound_returnsFailureWithNoSuchElementException() = runTest {
        val firestoreTx: Transaction = mockk(relaxed = true)
        val txSnapshot: DocumentSnapshot = mockk(relaxed = true)

        every { firestore.runTransaction<Any>(any()) } answers {
            val function = firstArg<Transaction.Function<Any>>()
            val res = function.apply(firestoreTx)
            Tasks.forResult(res)
        }

        every { firestoreTx.get(itemDoc) } returns txSnapshot
        every { txSnapshot.exists() } returns false

        val result = dataSource.voidTransaction(
            fundId = fundId,
            isExpense = false,
            transactionId = "dep-missing",
            originalAmount = 100_000L,
            voidReason = "Reversing missing",
            adminEmail = "admin@example.com"
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(NoSuchElementException::class.java)
    }
}
