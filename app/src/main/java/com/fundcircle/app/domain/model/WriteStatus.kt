package com.fundcircle.app.domain.model

/**
 * Outcome of a ledger write (Invariant 5).
 *
 * Firestore applies writes to the local cache immediately and syncs them
 * later, so a write made offline succeeds locally before the server has
 * seen it. Callers use this to tell the user whether it is already synced.
 */
enum class WriteStatus {
    /** The server acknowledged the write. */
    SYNCED,

    /** The write is saved on this device and will sync when the connection returns. */
    PENDING_SYNC
}
