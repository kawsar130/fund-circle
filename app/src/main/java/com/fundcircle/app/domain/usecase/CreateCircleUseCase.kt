package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.repository.FundRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to validate circle parameters, enforce the 5-circle ceiling, and create a new circle workspace.
 */
class CreateCircleUseCase @Inject constructor(
    private val fundRepository: FundRepository
) {
    suspend operator fun invoke(
        name: String,
        currency: Currency,
        description: String?
    ): Result<String> {
        val trimmedName = name.trim()
        if (trimmedName.length !in 3..50) {
            return Result.failure(FundCircleError.Validation.InvalidName(trimmedName.length))
        }

        val trimmedDescription = description?.trim()?.ifBlank { null }
        if (trimmedDescription != null && trimmedDescription.length > 250) {
            return Result.failure(FundCircleError.Validation.DescriptionTooLong(trimmedDescription.length))
        }

        val existingCircles = try {
            fundRepository.observeUserCircles().first()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emptyList()
        }

        if (existingCircles.size >= 5) {
            return Result.failure(FundCircleError.Business.CircleLimitReached)
        }

        return fundRepository.createCircle(
            name = trimmedName,
            currency = currency,
            description = trimmedDescription
        )
    }
}
