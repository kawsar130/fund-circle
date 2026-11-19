package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Observes chronological deposit contributions filtered for a specific member.
 * Invariant 1: Contribution amounts are 64-bit Long integers.
 */
class GetMemberDepositsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(
        fundId: String,
        memberIdentifier: String,
        memberEmail: String? = null
    ): Flow<List<Deposit>> {
        return transactionRepository.observeDeposits(fundId).map { deposits ->
            deposits.filter { deposit ->
                deposit.memberId == memberIdentifier ||
                        deposit.memberEmail.equals(memberIdentifier, ignoreCase = true) ||
                        (memberEmail != null && deposit.memberEmail.equals(memberEmail, ignoreCase = true))
            }.sortedByDescending { it.depositDate }
        }
    }
}
