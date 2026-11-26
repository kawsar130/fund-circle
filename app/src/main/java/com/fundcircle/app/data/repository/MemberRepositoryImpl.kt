package com.fundcircle.app.data.repository

import com.fundcircle.app.core.common.IoDispatcher
import com.fundcircle.app.data.datasource.remote.MemberRemoteDataSource
import com.fundcircle.app.data.dto.CircleMemberDto
import com.fundcircle.app.data.dto.toDomain
import com.fundcircle.app.data.dto.toDto
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.MemberKyc
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.normalizedEmail
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.repository.MemberRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemberRepositoryImpl @Inject constructor(
    private val memberRemoteDataSource: MemberRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MemberRepository {

    override fun observeMembers(fundId: String): Flow<List<CircleMember>> =
        memberRemoteDataSource.observeMembers(fundId)
            .map { dtos -> dtos.mapNotNull { runCatching { it.toDomain() }.getOrNull() } }
            .catch { e -> throw e.toDomainError() }
            .flowOn(ioDispatcher)

    override fun observeMember(fundId: String, userId: String): Flow<CircleMember?> =
        memberRemoteDataSource.observeMember(fundId, userId)
            .map { dto -> dto?.let { runCatching { it.toDomain() }.getOrNull() } }
            .catch { e -> throw e.toDomainError() }
            .flowOn(ioDispatcher)

    override suspend fun getMemberKyc(fundId: String, userId: String): Result<MemberKyc> =
        withContext(ioDispatcher) {
            try {
                memberRemoteDataSource.getMemberKyc(fundId, userId).fold(
                    onSuccess = { dto -> Result.success(dto.toDomain()) },
                    onFailure = { e -> Result.failure(e.toDomainError()) }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Result.failure(e.toDomainError())
            }
        }

    override suspend fun updateMemberKyc(fundId: String, kyc: MemberKyc): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                memberRemoteDataSource.updateMemberKyc(fundId, kyc.toDto()).fold(
                    onSuccess = { Result.success(Unit) },
                    onFailure = { e -> Result.failure(e.toDomainError()) }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Result.failure(e.toDomainError())
            }
        }

    override suspend fun inviteMember(
        fundId: String,
        name: String,
        phone: String,
        email: String,
        memberId: String
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            val userId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val memberDto = CircleMemberDto(
                userId = userId,
                memberId = memberId,
                email = email.normalizedEmail(),
                name = name,
                phone = phone,
                role = MemberRole.MEMBER.name,
                status = MemberStatus.ACTIVE.name,
                totalContributed = 0L,
                joinedAt = now,
                lastActiveAt = now
            )
            memberRemoteDataSource.inviteMember(fundId, memberDto).fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { e -> Result.failure(e.toDomainError()) }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e.toDomainError())
        }
    }

    override suspend fun toggleMemberStatus(
        fundId: String,
        userId: String,
        status: MemberStatus
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            memberRemoteDataSource.toggleMemberStatus(fundId, userId, status.name).fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { e -> Result.failure(e.toDomainError()) }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e.toDomainError())
        }
    }

    private fun Throwable.toDomainError(): Throwable = when (this) {
        is CancellationException -> this
        is FirebaseNetworkException -> FundCircleError.Network.Timeout
        is FirebaseFirestoreException -> when (code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> FundCircleError.Authorization.NotAuthorized
            FirebaseFirestoreException.Code.NOT_FOUND -> FundCircleError.NotFound.ResourceNotFound
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> FundCircleError.Network.Timeout
            else -> FundCircleError.Unexpected(code.name)
        }
        else -> this
    }
}
