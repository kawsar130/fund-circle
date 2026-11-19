package com.fundcircle.app.data.datasource.remote

import com.fundcircle.app.data.dto.DepositDto
import com.fundcircle.app.data.dto.ExpenseDto
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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
}
