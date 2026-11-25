package com.fundcircle.app.data.repository

import com.fundcircle.app.core.common.IoDispatcher
import com.fundcircle.app.data.datasource.local.SessionLocalDataSource
import com.fundcircle.app.data.datasource.remote.FundRemoteDataSource
import com.fundcircle.app.data.dto.FundCircleDto
import com.fundcircle.app.data.dto.MemberDto
import com.fundcircle.app.data.dto.toDomain
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.normalizedEmail
import com.fundcircle.app.domain.repository.FundRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FundRepositoryImpl @Inject constructor(
    private val fundRemoteDataSource: FundRemoteDataSource,
    private val sessionLocalDataSource: SessionLocalDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FundRepository {

    override fun observeUserCircles(): Flow<List<FundCircle>> =
        sessionLocalDataSource.observeUserSession()
            .flatMapLatest { session ->
                val email = session?.email
                if (email.isNullOrBlank()) {
                    flowOf(emptyList())
                } else {
                    fundRemoteDataSource.observeUserCircles(email.normalizedEmail())
                        .map { dtos -> dtos.map { it.toDomain() } }
                }
            }.catch { e ->
                throw e.toDomainError()
            }.flowOn(ioDispatcher)

    override fun observeCircle(fundId: String): Flow<FundCircle> =
        fundRemoteDataSource.observeCircle(fundId)
            .map { dto ->
                dto?.toDomain() ?: throw FundCircleError.NotFound.ResourceNotFound
            }.catch { e ->
                throw e.toDomainError()
            }.flowOn(ioDispatcher)

    override suspend fun createCircle(
        name: String,
        currency: Currency,
        description: String?
    ): Result<String> = withContext(ioDispatcher) {
        val userSession = sessionLocalDataSource.getUserSession()
            ?: return@withContext Result.failure(FundCircleError.Authorization.SessionExpired)

        val existingCircles = try {
            fundRemoteDataSource.observeUserCircles(userSession.email).first()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emptyList()
        }

        if (existingCircles.size >= 5) {
            return@withContext Result.failure(FundCircleError.Business.CircleLimitReached)
        }

        val fundId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val fundDto = FundCircleDto(
            fundId = fundId,
            name = name,
            description = description,
            currency = currency.name,
            adminId = userSession.uid,
            adminEmail = userSession.email.normalizedEmail(),
            memberEmails = listOf(userSession.email.normalizedEmail()),
            memberCount = 1,
            totalDeposits = 0L,
            totalExpenses = 0L,
            recordedBalance = 0L,
            isLocked = false,
            createdAt = now,
            updatedAt = now
        )

        val adminMemberDto = MemberDto(
            userId = userSession.uid,
            memberId = "FC-001",
            email = userSession.email.normalizedEmail(),
            name = userSession.displayName,
            role = MemberRole.ADMIN.name,
            status = "ACTIVE",
            totalContributed = 0L,
            joinedAt = now,
            lastActiveAt = now
        )

        val result = try {
            fundRemoteDataSource.createCircle(fundDto, adminMemberDto)
        } catch (e: CancellationException) {
            throw e
        }
        if (result.isSuccess) {
            Result.success(fundId)
        } else {
            val error = result.exceptionOrNull()?.toDomainError() ?: Exception("Failed to create circle")
            if (error is CancellationException) throw error
            Result.failure(error)
        }
    }

    override suspend fun toggleCircleLock(fundId: String, isLocked: Boolean): Result<Unit> =
        withContext(ioDispatcher) {
            val result = try {
                fundRemoteDataSource.toggleCircleLock(fundId, isLocked)
            } catch (e: CancellationException) {
                throw e
            }
            if (result.isSuccess) {
                Result.success(Unit)
            } else {
                val error = result.exceptionOrNull()?.toDomainError() ?: Exception("Failed to toggle circle lock")
                if (error is CancellationException) throw error
                Result.failure(error)
            }
        }

    override suspend fun transferAdminRole(
        fundId: String,
        currentAdminUserId: String,
        newAdminUserId: String,
        newAdminEmail: String
    ): Result<Unit> = withContext(ioDispatcher) {
        val result = try {
            fundRemoteDataSource.transferAdminRole(
                fundId = fundId,
                currentAdminUserId = currentAdminUserId,
                newAdminUserId = newAdminUserId,
                newAdminEmail = newAdminEmail.normalizedEmail()
            )
        } catch (e: CancellationException) {
            throw e
        }
        if (result.isSuccess) {
            Result.success(Unit)
        } else {
            val error = result.exceptionOrNull()?.toDomainError() ?: Exception("Failed to transfer admin role")
            if (error is CancellationException) throw error
            Result.failure(error)
        }
    }

    private fun Throwable.toDomainError(): Throwable = when (this) {
        is CancellationException -> this
        is FirebaseNetworkException -> FundCircleError.Network.Timeout
        is FirebaseFirestoreException -> when (code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> FundCircleError.Authorization.NotAuthorized
            FirebaseFirestoreException.Code.NOT_FOUND -> FundCircleError.NotFound.ResourceNotFound
            FirebaseFirestoreException.Code.ALREADY_EXISTS -> FundCircleError.Conflict.DuplicateEntry()
            FirebaseFirestoreException.Code.FAILED_PRECONDITION -> FundCircleError.Conflict.CurrencyLocked
            FirebaseFirestoreException.Code.UNAVAILABLE -> FundCircleError.Network.Timeout
            else -> FundCircleError.Network.Timeout
        }
        is FundCircleError -> this
        else -> this
    }
}
