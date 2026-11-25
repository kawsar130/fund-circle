package com.fundcircle.app.data.datasource.remote

import com.fundcircle.app.data.dto.DepositDto
import com.fundcircle.app.data.dto.ExpenseDto
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TransactionRemoteDataSource {

    override fun observeRecentDeposits(fundId: String, limit: Int): Flow<List<DepositDto>> = callbackFlow {
        val registration = firestore.collection(COLLECTION_FUNDS)
            .document(fundId)
            .collection(COLLECTION_DEPOSITS)
            .orderBy(FIELD_DEPOSIT_DATE, Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val deposits = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(DepositDto::class.java)?.let { dto ->
                        dto.copy(
                            depositId = if (dto.depositId.isBlank()) doc.id else dto.depositId,
                            fundId = if (dto.fundId.isBlank()) fundId else dto.fundId
                        )
                    }
                }.orEmpty()
                trySend(deposits)
            }

        awaitClose {
            registration.remove()
        }
    }

    override fun observeRecentExpenses(fundId: String, limit: Int): Flow<List<ExpenseDto>> = callbackFlow {
        val registration = firestore.collection(COLLECTION_FUNDS)
            .document(fundId)
            .collection(COLLECTION_EXPENSES)
            .orderBy(FIELD_EXPENSE_DATE, Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val expenses = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ExpenseDto::class.java)?.let { dto ->
                        dto.copy(
                            expenseId = if (dto.expenseId.isBlank()) doc.id else dto.expenseId,
                            fundId = if (dto.fundId.isBlank()) fundId else dto.fundId
                        )
                    }
                }.orEmpty()
                trySend(expenses)
            }

        awaitClose {
            registration.remove()
        }
    }

    override fun observeDeposits(fundId: String): Flow<List<DepositDto>> = callbackFlow {
        val registration = firestore.collection(COLLECTION_FUNDS)
            .document(fundId)
            .collection(COLLECTION_DEPOSITS)
            .orderBy(FIELD_DEPOSIT_DATE, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val deposits = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(DepositDto::class.java)?.let { dto ->
                        dto.copy(
                            depositId = if (dto.depositId.isBlank()) doc.id else dto.depositId,
                            fundId = if (dto.fundId.isBlank()) fundId else dto.fundId
                        )
                    }
                }.orEmpty()
                trySend(deposits)
            }

        awaitClose {
            registration.remove()
        }
    }

    override fun observeExpenses(fundId: String): Flow<List<ExpenseDto>> = callbackFlow {
        val registration = firestore.collection(COLLECTION_FUNDS)
            .document(fundId)
            .collection(COLLECTION_EXPENSES)
            .orderBy(FIELD_EXPENSE_DATE, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val expenses = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ExpenseDto::class.java)?.let { dto ->
                        dto.copy(
                            expenseId = if (dto.expenseId.isBlank()) doc.id else dto.expenseId,
                            fundId = if (dto.fundId.isBlank()) fundId else dto.fundId
                        )
                    }
                }.orEmpty()
                trySend(expenses)
            }

        awaitClose {
            registration.remove()
        }
    }

    override fun observeDeposit(fundId: String, depositId: String): Flow<DepositDto?> = callbackFlow {
        val registration = firestore.collection(COLLECTION_FUNDS)
            .document(fundId)
            .collection(COLLECTION_DEPOSITS)
            .document(depositId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val deposit = if (snapshot != null && snapshot.exists()) {
                    snapshot.toObject(DepositDto::class.java)?.let { dto ->
                        dto.copy(
                            depositId = if (dto.depositId.isBlank()) snapshot.id else dto.depositId,
                            fundId = if (dto.fundId.isBlank()) fundId else dto.fundId
                        )
                    }
                } else {
                    null
                }
                trySend(deposit)
            }

        awaitClose {
            registration.remove()
        }
    }

    override fun observeExpense(fundId: String, expenseId: String): Flow<ExpenseDto?> = callbackFlow {
        val registration = firestore.collection(COLLECTION_FUNDS)
            .document(fundId)
            .collection(COLLECTION_EXPENSES)
            .document(expenseId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val expense = if (snapshot != null && snapshot.exists()) {
                    snapshot.toObject(ExpenseDto::class.java)?.let { dto ->
                        dto.copy(
                            expenseId = if (dto.expenseId.isBlank()) snapshot.id else dto.expenseId,
                            fundId = if (dto.fundId.isBlank()) fundId else dto.fundId
                        )
                    }
                } else {
                    null
                }
                trySend(expense)
            }

        awaitClose {
            registration.remove()
        }
    }

    override suspend fun recordDeposit(fundId: String, deposit: DepositDto): Result<Unit> = try {
        val batch = firestore.batch()
        val fundRef = firestore.collection(COLLECTION_FUNDS).document(fundId)
        val depositRef = fundRef.collection(COLLECTION_DEPOSITS).document(deposit.depositId)

        batch.set(depositRef, deposit)
        batch.update(
            fundRef,
            mapOf(
                FIELD_TOTAL_DEPOSITS to FieldValue.increment(deposit.amount),
                FIELD_RECORDED_BALANCE to FieldValue.increment(deposit.amount),
                FIELD_UPDATED_AT to System.currentTimeMillis(),
                FIELD_LAST_TX_ID to deposit.depositId
            )
        )

        batch.commit().await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }

    override suspend fun recordExpense(fundId: String, expense: ExpenseDto): Result<Unit> = try {
        val batch = firestore.batch()
        val fundRef = firestore.collection(COLLECTION_FUNDS).document(fundId)
        val expenseRef = fundRef.collection(COLLECTION_EXPENSES).document(expense.expenseId)

        batch.set(expenseRef, expense)
        batch.update(
            fundRef,
            mapOf(
                FIELD_TOTAL_EXPENSES to FieldValue.increment(expense.amount),
                FIELD_RECORDED_BALANCE to FieldValue.increment(-expense.amount),
                FIELD_UPDATED_AT to System.currentTimeMillis(),
                FIELD_LAST_TX_ID to expense.expenseId
            )
        )

        batch.commit().await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }

    override suspend fun voidTransaction(
        fundId: String,
        isExpense: Boolean,
        transactionId: String,
        originalAmount: Long,
        voidReason: String,
        adminEmail: String
    ): Result<Unit> = try {
        firestore.runTransaction { transaction ->
            val fundRef = firestore.collection(COLLECTION_FUNDS).document(fundId)
            val subCollectionName = if (isExpense) COLLECTION_EXPENSES else COLLECTION_DEPOSITS
            val txRef = fundRef.collection(subCollectionName).document(transactionId)

            val txSnapshot = transaction.get(txRef)
            if (!txSnapshot.exists()) {
                throw NoSuchElementException("Transaction document $transactionId does not exist.")
            }

            val alreadyVoid = txSnapshot.getBoolean(FIELD_IS_VOID) ?: false
            if (alreadyVoid) {
                throw IllegalStateException("Transaction is already reversed.")
            }

            val docAmount = txSnapshot.getLong("amount")
            val effectiveAmount = if (docAmount != null && docAmount > 0L) docAmount else originalAmount

            val now = System.currentTimeMillis()
            val childUpdates = mapOf<String, Any>(
                FIELD_IS_VOID to true,
                FIELD_VOID_REASON to voidReason,
                FIELD_VOIDED_BY to adminEmail,
                FIELD_VOIDED_AT to now
            )
            transaction.update(txRef, childUpdates)

            val parentUpdates = if (isExpense) {
                mapOf<String, Any>(
                    FIELD_TOTAL_EXPENSES to FieldValue.increment(-effectiveAmount),
                    FIELD_RECORDED_BALANCE to FieldValue.increment(effectiveAmount),
                    FIELD_UPDATED_AT to now,
                    FIELD_LAST_TX_ID to transactionId
                )
            } else {
                mapOf<String, Any>(
                    FIELD_TOTAL_DEPOSITS to FieldValue.increment(-effectiveAmount),
                    FIELD_RECORDED_BALANCE to FieldValue.increment(-effectiveAmount),
                    FIELD_UPDATED_AT to now,
                    FIELD_LAST_TX_ID to transactionId
                )
            }
            transaction.update(fundRef, parentUpdates)
            null
        }.await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }

    companion object {
        private const val COLLECTION_FUNDS = "funds"
        private const val COLLECTION_DEPOSITS = "deposits"
        private const val COLLECTION_EXPENSES = "expenses"
        private const val FIELD_DEPOSIT_DATE = "depositDate"
        private const val FIELD_EXPENSE_DATE = "expenseDate"
        private const val FIELD_TOTAL_DEPOSITS = "totalDeposits"
        private const val FIELD_TOTAL_EXPENSES = "totalExpenses"
        private const val FIELD_RECORDED_BALANCE = "recordedBalance"
        private const val FIELD_UPDATED_AT = "updatedAt"

        /** Names the ledger document behind each aggregate change; firestore.rules verifies the pairing. */
        private const val FIELD_LAST_TX_ID = "lastTxId"
        private const val FIELD_IS_VOID = "isVoid"
        private const val FIELD_VOID_REASON = "voidReason"
        private const val FIELD_VOIDED_BY = "voidedBy"
        private const val FIELD_VOIDED_AT = "voidedAt"
    }
}
