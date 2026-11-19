package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes a single member in a circle by userId.
 */
class ObserveMemberUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    operator fun invoke(fundId: String, userId: String): Flow<CircleMember?> {
        return memberRepository.observeMember(fundId, userId)
    }
}
