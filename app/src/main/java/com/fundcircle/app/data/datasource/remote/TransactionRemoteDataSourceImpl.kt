package com.fundcircle.app.data.datasource.remote

import com.fundcircle.app.data.dto.DepositDto
import com.fundcircle.app.data.dto.ExpenseDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
                        if (dto.depositId.isBlank()) dto.copy(depositId = doc.id) else dto
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
                        if (dto.expenseId.isBlank()) dto.copy(expenseId = doc.id) else dto
                    }
                }.orEmpty()
                trySend(expenses)
            }

        awaitClose {
            registration.remove()
        }
    }

    companion object {
        private const val COLLECTION_FUNDS = "funds"
        private const val COLLECTION_DEPOSITS = "deposits"
        private const val COLLECTION_EXPENSES = "expenses"
        private const val FIELD_DEPOSIT_DATE = "depositDate"
        private const val FIELD_EXPENSE_DATE = "expenseDate"
    }
}
