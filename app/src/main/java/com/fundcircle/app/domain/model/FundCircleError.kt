package com.fundcircle.app.domain.model

/**
 * Exhaustive domain error taxonomy as specified in API Specification § 7.2.
 * Maps deterministic error scenarios across Validation, Authorization, Conflict, Business, NotFound, and Network.
 */
sealed class FundCircleError(override val message: String) : Exception(message) {

    sealed class Validation(override val message: String) : FundCircleError(message) {
        data class InvalidAmount(val actual: Long) : Validation("Amount must be a whole positive integer >= 1.")
        data class FutureDateNotAllowed(val timestamp: Long) : Validation("Transaction date cannot be in the future.")
        data class InvalidName(val length: Int) : Validation("Circle name must be between 3 and 50 characters.")
        data class DescriptionTooShort(val length: Int) : Validation("Description must be between 3 and 250 characters.")
        data class DescriptionTooLong(val length: Int) : Validation("Description cannot exceed 250 characters.")
        data object MemberRequired : Validation("Contributing member must be selected.")
        data class VoidReasonRequired(val length: Int) : Validation("A void reason of at least 5 characters is mandatory.")
    }

    sealed class Authorization(override val message: String) : FundCircleError(message) {
        data object NotCircleMember : Authorization("You are not an active member of this circle.")
        data object AdminPrivilegeRequired : Authorization("This operation requires Circle Administrator privileges.")
        data object SessionExpired : Authorization("Your session has expired. Please sign in again.")
        data class AuthenticationFailed(val detail: String = "Authentication failed. Please try again.") : Authorization(detail)
        data object SignInCancelled : Authorization("Google Sign-In was cancelled.")
        data object NotAuthorized : Authorization("You do not have administrative permission for this action.")
    }

    sealed class Conflict(override val message: String) : FundCircleError(message) {
        data class TransactionAlreadyVoided(val txId: String) : Conflict("Transaction $txId has already been voided.")
        data object CurrencyLocked : Conflict("Base currency is permanently locked once transactions exist.")
        data class DuplicateEntry(val detail: String = "A circle or entry with this ID already exists.") : Conflict(detail)
    }

    sealed class Business(override val message: String) : FundCircleError(message) {
        data object CircleLimitReached : Business("You have reached the maximum limit of 5 circles.")
        data object CannotDeleteActiveFund : Business("Circles with recorded deposits or expenses cannot be deleted.")
    }

    sealed class NotFound(override val message: String) : FundCircleError(message) {
        data object ResourceNotFound : NotFound("The requested circle or transaction could not be found.")
    }

    sealed class Network(override val message: String) : FundCircleError(message) {
        data object OfflineNoCache : Network("Device is offline and requested data is not cached.")
        data object Timeout : Network("The operation timed out. Please check your network connection.")
    }
}
