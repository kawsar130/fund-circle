package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.normalizedEmail
import com.fundcircle.app.domain.repository.MemberRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.util.Locale
import javax.inject.Inject

/**
 * Validates member inputs, auto-allocates next sequential Member ID (e.g. FC-005),
 * and dispatches atomic circle membership addition.
 */
class AddMemberUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    suspend operator fun invoke(
        fundId: String,
        name: String,
        phone: String,
        email: String
    ): Result<Unit> {
        val trimmedName = name.trim()
        if (trimmedName.length !in 2..100) {
            return Result.failure(FundCircleError.Validation.MemberNameRequired())
        }

        val trimmedPhone = phone.trim()
        if (!trimmedPhone.all { it.isDigit() } || trimmedPhone.length != 11) {
            return Result.failure(FundCircleError.Validation.InvalidPhone())
        }

        val trimmedEmail = email.normalizedEmail()
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!emailRegex.matches(trimmedEmail)) {
            return Result.failure(FundCircleError.Validation.InvalidEmail())
        }

        val existingMembers = try {
            memberRepository.observeMembers(fundId).first()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emptyList()
        }

        if (existingMembers.any { it.email.equals(trimmedEmail, ignoreCase = true) }) {
            return Result.failure(
                FundCircleError.Conflict.DuplicateEntry(
                    "Member with email $trimmedEmail already exists in this circle."
                )
            )
        }

        val nextMemberId = computeNextMemberId(existingMembers)

        return memberRepository.inviteMember(
            fundId = fundId,
            name = trimmedName,
            phone = trimmedPhone,
            email = trimmedEmail,
            memberId = nextMemberId
        )
    }

    companion object {
        fun computeNextMemberId(existingMembers: List<CircleMember>): String {
            val maxIndex = existingMembers.mapNotNull { member ->
                val id = member.memberId.trim()
                if (id.startsWith("FC-", ignoreCase = true)) {
                    id.substringAfter("FC-").toIntOrNull()
                } else null
            }.maxOrNull() ?: existingMembers.size

            val nextIndex = maxOf(1, maxIndex + 1)
            return String.format(Locale.US, "FC-%03d", nextIndex)
        }
    }
}
