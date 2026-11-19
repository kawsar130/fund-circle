package com.fundcircle.app.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Type-Safe Navigation Compose 2.8+ DSL routes.
 * Avoids fragile string-based navigation routes.
 */
@Serializable
sealed interface ScreenRoute {
    @Serializable data object Welcome : ScreenRoute
    @Serializable data object CircleHub : ScreenRoute
    @Serializable data object CreateCircle : ScreenRoute
    @Serializable data class Dashboard(val fundId: String) : ScreenRoute
    @Serializable data class TransactionsLedger(val fundId: String) : ScreenRoute
    @Serializable data class TransactionDetail(val fundId: String, val txId: String, val isExpense: Boolean) : ScreenRoute
    @Serializable data class MembersRoster(val fundId: String) : ScreenRoute
    @Serializable data class MemberDetail(val fundId: String, val memberId: String) : ScreenRoute
    @Serializable data class ProfileEdit(val fundId: String = "") : ScreenRoute
    @Serializable data class Reports(val fundId: String) : ScreenRoute
    @Serializable data class Settings(val fundId: String) : ScreenRoute
}
