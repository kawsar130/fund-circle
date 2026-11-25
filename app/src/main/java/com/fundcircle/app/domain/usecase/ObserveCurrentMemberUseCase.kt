package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.normalizedEmail
import com.fundcircle.app.domain.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Resolves the signed-in user's own member record in a circle.
 *
 * The circle creator's member document is keyed by their Auth UID, but invited members
 * are written before they ever sign in, so their document ID is a generated UUID. The
 * stable identity for them is their email, which is what `firestore.rules` checks too.
 * Always use the returned member's `userId` (the document ID) for self-service writes.
 */
class ObserveCurrentMemberUseCase @Inject constructor(
    private val memberRepository: MemberRepository
) {
    operator fun invoke(fundId: String, uid: String, email: String): Flow<CircleMember?> {
        val normalizedEmail = email.normalizedEmail()
        return memberRepository.observeMembers(fundId).map { members ->
            members.firstOrNull { uid.isNotBlank() && it.userId == uid }
                ?: members.firstOrNull {
                    normalizedEmail.isNotBlank() && it.email.normalizedEmail() == normalizedEmail
                }
        }
    }
}
