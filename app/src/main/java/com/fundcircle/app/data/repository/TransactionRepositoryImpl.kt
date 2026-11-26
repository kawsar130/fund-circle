package com.fundcircle.app.data.repository

import com.fundcircle.app.core.common.IoDispatcher
import com.fundcircle.app.data.datasource.remote.TransactionRemoteDataSource
import com.fundcircle.app.data.dto.toDomain
import com.fundcircle.app.data.dto.toDto
import com.fundcircle.app.data.dto.toTransactionItem
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.TransactionItem
import com.fundcircle.app.domain.model.WriteStatus
import com.fundcircle.app.domain.repository.TransactionRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionRemoteDataSource: TransactionRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TransactionRepository {

    override fun observeRecentTransactions(fundId: String, limit: Int): Flow<List<TransactionItem>> =
        combine(
            transactionRemoteDataSource.observeRecentDeposits(fundId, limit),
            transactionRemoteDataSource.observeRecentExpenses(fundId, limit)
        ) { deposits, expenses ->
            val depositItems = deposits.mapNotNull { runCatching { it.toTransactionItem() }.getOrNull() }
            val expenseItems = expenses.mapNotNull { runCatching { it.toTransactionItem() }.getOrNull() }
            (depositItems + expenseItems)
                .sortedWith(compareByDescending<TransactionItem> { it.timestamp }.thenByDescending { it.id })
                .take(limit)
        }.catch { e ->
            throw e.toDomainError()
        }.flowOn(ioDispatcher)

    override fun observeDeposits(fundId: String): Flow<List<Deposit>> =
        transactionRemoteDataSource.observeDeposits(fundId)
            .map { dtos -> dtos.mapNotNull { runCatching { it.toDomain() }.getOrNull() } }
            .catch { e -> throw e.toDomainError() }
            .flowOn(ioDispatcher)

    override fun observeExpenses(fundId: String): Flow<List<Expense>> =
        transactionRemoteDataSource.observeExpenses(fundId)
            .map { dtos -> dtos.mapNotNull { runCatching { it.toDomain() }.getOrNull() } }
            .catch { e -> throw e.toDomainError() }
            .flowOn(ioDispatcher)

    override fun observeDeposit(fundId: String, depositId: String): Flow<Deposit?> =
        transactionRemoteDataSource.observeDeposit(fundId, depositId)
            .map { dto -> dto?.runCatching { toDomain() }?.getOrNull() }
            .catch { e -> throw e.toDomainError() }
            .flowOn(ioDispatcher)

    override fun observeExpense(fundId: String, expenseId: String): Flow<Expense?> =
        transactionRemoteDataSource.observeExpense(fundId, expenseId)
            .map { dto -> dto?.runCatching { toDomain() }?.getOrNull() }
            .catch { e -> throw e.toDomainError() }
            .flowOn(ioDispatcher)

    override suspend fun recordDeposit(fundId: String, deposit: Deposit): Result<WriteStatus> =
        withContext(ioDispatcher) {
            transactionRemoteDataSource.recordDeposit(fundId, deposit.toDto()).fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(it.toDomainError()) }
            )
        }

    override suspend fun recordExpense(fundId: String, expense: Expense): Result<WriteStatus> =
        withContext(ioDispatcher) {
            transactionRemoteDataSource.recordExpense(fundId, expense.toDto()).fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(it.toDomainError()) }
            )
        }

    override suspend fun voidTransaction(
        fundId: String,
        isExpense: Boolean,
        transactionId: String,
        originalAmount: Long,
        voidReason: String,
        adminEmail: String
    ): Result<WriteStatus> = withContext(ioDispatcher) {
        transactionRemoteDataSource.voidTransaction(
            fundId = fundId,
            isExpense = isExpense,
            transactionId = transactionId,
            originalAmount = originalAmount,
            voidReason = voidReason,
            adminEmail = adminEmail
        ).fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                val domainError = when (error) {
                    is IllegalStateException -> FundCircleError.Conflict.TransactionAlreadyVoided(transactionId)
                    is NoSuchElementException -> FundCircleError.NotFound.ResourceNotFound
                    else -> error.toDomainError()
                }
                Result.failure(domainError)
            }
        )
    }

    private fun Throwable.toDomainError(): Throwable = when (this) {
        is CancellationException -> this
        is FirebaseNetworkException -> FundCircleError.Network.Timeout
        is FirebaseFirestoreException -> when (code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> FundCircleError.Authorization.NotAuthorized
            FirebaseFirestoreException.Code.NOT_FOUND -> FundCircleError.NotFound.ResourceNotFound
            FirebaseFirestoreException.Code.ALREADY_EXISTS -> FundCircleError.Conflict.DuplicateEntry()
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> FundCircleError.Network.Timeout
            // FAILED_PRECONDITION usually means a missing index, not a locked currency
            else -> FundCircleError.Unexpected(code.name)
        }
        else -> this
    }
}
