package com.fundcircle.app.data.dto

import com.fundcircle.app.core.security.PrivacyMaskUtils
import com.fundcircle.app.domain.model.MemberKyc
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.serialization.Serializable

/**
 * Firestore DTO for /funds/{fundId}/members/{userId}/kyc/identity subcollection.
 * Invariant 3: Admin-scoped PII subcollection isolation.
 */
@IgnoreExtraProperties
@Serializable
data class MemberKycDto(
    val userId: String = "",
    val nidNumber: String = "",
    val nomineeName: String = "",
    val nomineePhone: String = "",
    val nomineeRelation: String = "",
    val verifiedByAdmin: Boolean = false,
    val updatedAt: Long = 0L
)

fun MemberKycDto.toDomain(): MemberKyc = MemberKyc(
    userId = userId,
    nidNumber = nidNumber,
    maskedNid = PrivacyMaskUtils.maskNid(nidNumber),
    nomineeName = nomineeName,
    nomineePhone = nomineePhone,
    nomineeRelation = nomineeRelation,
    verifiedByAdmin = verifiedByAdmin,
    updatedAt = updatedAt
)

fun MemberKyc.toDto(): MemberKycDto = MemberKycDto(
    userId = userId,
    nidNumber = nidNumber,
    nomineeName = nomineeName,
    nomineePhone = nomineePhone,
    nomineeRelation = nomineeRelation,
    verifiedByAdmin = verifiedByAdmin,
    updatedAt = updatedAt
)
