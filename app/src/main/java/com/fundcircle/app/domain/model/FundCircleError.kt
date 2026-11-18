package com.fundcircle.app.domain.model

/**
 * Exhaustive domain error taxonomy as specified in API Specification § 7.2.
 * Maps deterministic error scenarios across Validation, Authorization, Conflict, Business, and Network.
 */
sealed interface FundCircleError {
    val message: String

    sealed class Validation(override val message: String) : FundCircleError {
        data class InvalidAmount(val actual: Long) : Validation("Amount must be a whole positive integer >= 1.")
        data class FutureDateNotAllowed(val timestamp: Long) : Validation("Transaction date cannot be in the future.")
        data class DescriptionTooShort(val length: Int) : Validation("Description must be between 3 and 250 characters.")
        data class VoidReasonRequired(val length: Int) : Validation("A void reason of at least 5 characters is mandatory.")
    }

    sealed class Authorization(override val message: String) : FundCircleError {
        data object NotCircleMember : Authorization("You are not an active member of this circle.")
        data object AdminPrivilegeRequired : Authorization("This operation requires Circle Administrator privileges.")
        data object SessionExpired : Authorization("Your session has expired. Please sign in again.")
    }

    sealed class Conflict(override val message: String) : FundCircleError {
        data class TransactionAlreadyVoided(val txId: String) : Conflict("Transaction $txId has already been voided.")
        data object CurrencyLocked : Conflict("Base currency is permanently locked once transactions exist.")
    }

    sealed class Business(override val message: String) : FundCircleError {
        data object CircleLimitReached : Business("You have reached the maximum limit of 5 circles.")
        data object CannotDeleteActiveFund : Business("Circles with recorded deposits or expenses cannot be deleted.")
    }

    sealed class Network(override val message: String) : FundCircleError {
        data object OfflineNoCache : Network("Device is offline and requested data is not cached.")
        data object Timeout : Network("The operation timed out. Please check your network connection.")
    }
}
