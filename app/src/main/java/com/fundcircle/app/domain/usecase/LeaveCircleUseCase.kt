package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.repository.MemberRepository
import javax.inject.Inject

/**
 * UseCase for voluntary stakeholder departure from a circle.
 * Invariant 4: Marks member as LEFT; zero hard deletes. Historical records remain in ledger.
 */
class LeaveCircleUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {

    suspend operator fun invoke(
        fundId: String,
        userId: String,
        circle: FundCircle
    ): Result<Unit> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID cannot be blank."))
        }
        if (circle.adminId == userId) {
            return Result.failure(IllegalStateException("Circle Admin cannot leave without transferring ownership first."))
        }

        return memberRepository.toggleMemberStatus(fundId, userId, MemberStatus.LEFT)
    }
}
