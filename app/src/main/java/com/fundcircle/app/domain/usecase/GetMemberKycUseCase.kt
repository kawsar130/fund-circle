package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.MemberKyc
import com.fundcircle.app.domain.repository.MemberRepository
import javax.inject.Inject

/**
 * Retrieves strictly isolated KYC PII for a member.
 * Invariant 3: Admin-scoped and self-scoped identity details.
 */
class GetMemberKycUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    suspend operator fun invoke(fundId: String, userId: String): Result<MemberKyc> {
        return memberRepository.getMemberKyc(fundId, userId)
    }
}
