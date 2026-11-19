package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.repository.MemberRepository
import javax.inject.Inject

/**
 * Toggles a member's lifecycle status.
 * Invariant 4: Zero physical deletes. Marks member as ACTIVE, INACTIVE, or LEFT.
 */
class ToggleMemberStatusUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    suspend operator fun invoke(
        fundId: String,
        userId: String,
        status: MemberStatus
    ): Result<Unit> {
        return memberRepository.toggleMemberStatus(fundId, userId, status)
    }
}
