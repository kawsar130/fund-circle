package com.fundcircle.app.core.network

import kotlinx.coroutines.flow.Flow

/**
 * Invariant 5: Offline-First Resilience.
 * Continuously observes device network connectivity to coordinate offline cache banners.
 */
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}
