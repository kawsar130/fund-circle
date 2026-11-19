package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes real-time members subcollection for a circle.
 */
class ObserveCircleMembersUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    operator fun invoke(fundId: String): Flow<List<CircleMember>> {
        return memberRepository.observeMembers(fundId)
    }
}
