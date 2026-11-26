package com.fundcircle.app.data.datasource.remote

import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.domain.model.WriteStatus
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

private const val WRITE_ACK_TIMEOUT_MS = 4_000L
private const val NETWORK_CHECK_TIMEOUT_MS = 500L

/**
 * Firestore applies a write to the local cache at once, but the commit task
 * only completes when the server acknowledges it. Waiting for it offline
 * would leave the UI spinning, so offline writes, and writes the server
 * hasn't acknowledged within [WRITE_ACK_TIMEOUT_MS], are reported as
 * pending and sync in the background (Invariant 5). A server rejection
 * that arrives in time is still thrown.
 */
internal suspend fun Task<Void>.awaitSyncOrQueue(networkMonitor: NetworkMonitor): WriteStatus {
    if (!networkMonitor.isOnlineNow()) return WriteStatus.PENDING_SYNC
    return withTimeoutOrNull(WRITE_ACK_TIMEOUT_MS) {
        await()
        WriteStatus.SYNCED
    } ?: WriteStatus.PENDING_SYNC
}

/** Current connectivity; assumes online if the monitor doesn't answer quickly. */
internal suspend fun NetworkMonitor.isOnlineNow(): Boolean =
    withTimeoutOrNull(NETWORK_CHECK_TIMEOUT_MS) { isOnline.first() } ?: true
