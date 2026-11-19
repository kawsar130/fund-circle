package com.fundcircle.app.data.repository

import com.fundcircle.app.core.common.IoDispatcher
import com.fundcircle.app.data.datasource.remote.MemberRemoteDataSource
import com.fundcircle.app.data.dto.toDomain
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.FundCircleError
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemberRepositoryImpl @Inject constructor(
    private val memberRemoteDataSource: MemberRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MemberRepository {

    override fun observeMembers(fundId: String): Flow<List<CircleMember>> =
        memberRemoteDataSource.observeMembers(fundId)
            .map { dtos -> dtos.map { it.toDomain() } }
            .catch { e -> throw e.toDomainError() }
            .flowOn(ioDispatcher)

    private fun Throwable.toDomainError(): Throwable = when (this) {
        is CancellationException -> this
        is FirebaseNetworkException -> FundCircleError.Network.Timeout
        is FirebaseFirestoreException -> when (code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> FundCircleError.Authorization.NotAuthorized
            FirebaseFirestoreException.Code.NOT_FOUND -> FundCircleError.NotFound.ResourceNotFound
            FirebaseFirestoreException.Code.UNAVAILABLE -> FundCircleError.Network.Timeout
            else -> FundCircleError.Network.Timeout
        }
        else -> this
    }
}
