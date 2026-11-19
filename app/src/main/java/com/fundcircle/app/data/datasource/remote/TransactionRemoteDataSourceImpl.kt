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
                FIELD_UPDATED_AT to System.currentTimeMillis()
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
                FIELD_UPDATED_AT to System.currentTimeMillis()
            )
        )

        batch.commit().await()
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
    }
}
