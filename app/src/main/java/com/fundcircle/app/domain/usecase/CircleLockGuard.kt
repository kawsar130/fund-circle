package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.repository.FundRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

private const val LOCK_LOOKUP_TIMEOUT_MS = 2_000L

/**
 * True only when the circle is known to be locked. If the circle can't be
 * read in time (for example offline with nothing cached), this returns false
 * and leaves the final decision to the security rules, which also reject
 * writes to a locked circle.
 */
internal suspend fun FundRepository.isCircleKnownLocked(fundId: String): Boolean = try {
    withTimeoutOrNull(LOCK_LOOKUP_TIMEOUT_MS) { observeCircle(fundId).first().isLocked } ?: false
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    false
}
