package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.MemberKyc
import com.fundcircle.app.domain.repository.MemberRepository
import javax.inject.Inject

/**
 * Validates NID and nominee parameters and persists KYC identity PII.
 * Enforces NID (10, 13, 17 digits), nominee name (3..100 chars), valid phone, and valid relation.
 */
class UpdateMemberKycUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    suspend operator fun invoke(fundId: String, kyc: MemberKyc): Result<Unit> {
        val trimmedNid = kyc.nidNumber.trim()
        val validNidLengths = setOf(10, 13, 17)
        if (!trimmedNid.all { it.isDigit() } || trimmedNid.length !in validNidLengths) {
            return Result.failure(FundCircleError.Validation.InvalidNid())
        }

        val trimmedNomineeName = kyc.nomineeName.trim()
        if (trimmedNomineeName.length !in 3..100) {
            return Result.failure(FundCircleError.Validation.InvalidNomineeName(trimmedNomineeName.length))
        }

        val trimmedNomineePhone = kyc.nomineePhone.trim()
        if (!trimmedNomineePhone.all { it.isDigit() } || trimmedNomineePhone.length != 11) {
            return Result.failure(FundCircleError.Validation.InvalidPhone())
        }

        val trimmedNomineeRelation = kyc.nomineeRelation.trim()
        if (trimmedNomineeRelation.isBlank()) {
            return Result.failure(FundCircleError.Validation.InvalidNomineeRelation())
        }

        val validatedKyc = kyc.copy(
            nidNumber = trimmedNid,
            nomineeName = trimmedNomineeName,
            nomineePhone = trimmedNomineePhone,
            nomineeRelation = trimmedNomineeRelation,
            updatedAt = System.currentTimeMillis()
        )

        return memberRepository.updateMemberKyc(fundId, validatedKyc)
    }
}
