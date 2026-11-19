package com.fundcircle.app.data.mapper

import com.fundcircle.app.data.dto.CircleMemberDto
import com.fundcircle.app.data.dto.DepositDto
import com.fundcircle.app.data.dto.ExpenseDto
import com.fundcircle.app.data.dto.toDomain
import com.fundcircle.app.data.dto.toDto
import com.fundcircle.app.data.dto.toTransactionItem
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.model.VoidMetadata
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TransactionDtoMapperTest {

    @Test
    fun depositDto_toTransactionItem_mapsAllFieldsCorrectly() {
        val dto = DepositDto(
            depositId = "dep-100",
            fundId = "fund-1",
            memberId = "mem-1",
            memberEmail = "member@example.com",
            memberName = "Rahim Uddin",
            amount = 150_000L,
            depositDate = 1_700_000_000_000L,
            paymentMethod = "Bank Transfer",
            note = "First tranche",
            isVoid = false
        )

        val item = dto.toTransactionItem()

        assertThat(item.id).isEqualTo("dep-100")
        assertThat(item.fundId).isEqualTo("fund-1")
        assertThat(item.isExpense).isFalse()
        assertThat(item.title).isEqualTo("Rahim Uddin")
        assertThat(item.amount).isEqualTo(150_000L)
        assertThat(item.timestamp).isEqualTo(1_700_000_000_000L)
        assertThat(item.paymentMethod).isEqualTo(PaymentMethod.BANK_TRANSFER)
        assertThat(item.isVoid).isFalse()
        assertThat(item.category).isNull()
        assertThat(item.subtitle).contains("Bank Transfer")
    }

    @Test
    fun depositDto_fallbackToEmailWhenMemberNameIsBlank() {
        val dto = DepositDto(
            depositId = "dep-101",
            fundId = "fund-1",
            memberEmail = "fallback@example.com",
            memberName = "",
            amount = 50_000L,
            depositDate = 1_700_000_000_000L,
            paymentMethod = "Cash"
        )

        val item = dto.toTransactionItem()

        assertThat(item.title).isEqualTo("fallback@example.com")
    }

    @Test
    fun depositDto_retainsVoidStateAndPositiveAmount() {
        val dto = DepositDto(
            depositId = "dep-void",
            fundId = "fund-1",
            memberName = "Tanvir",
            amount = 75_000L,
            depositDate = 1_700_000_000_000L,
            paymentMethod = "Mobile Banking",
            isVoid = true,
            voidReason = "Typo in entry"
        )

        val item = dto.toTransactionItem()

        assertThat(item.isVoid).isTrue()
        assertThat(item.amount).isEqualTo(75_000L)
    }

    @Test
    fun expenseDto_toTransactionItem_mapsAllFieldsCorrectly() {
        val dto = ExpenseDto(
            expenseId = "exp-200",
            fundId = "fund-1",
            amount = 25_000L,
            expenseDate = 1_700_000_050_000L,
            category = "Office & Administration",
            description = "Desk Supplies",
            paymentMethod = "Cash",
            paidBy = "admin@example.com",
            isVoid = false
        )

        val item = dto.toTransactionItem()

        assertThat(item.id).isEqualTo("exp-200")
        assertThat(item.fundId).isEqualTo("fund-1")
        assertThat(item.isExpense).isTrue()
        assertThat(item.title).isEqualTo("Desk Supplies")
        assertThat(item.amount).isEqualTo(25_000L)
        assertThat(item.timestamp).isEqualTo(1_700_000_050_000L)
        assertThat(item.paymentMethod).isEqualTo(PaymentMethod.CASH)
        assertThat(item.category).isEqualTo(ExpenseCategory.OFFICE_AND_ADMINISTRATION)
        assertThat(item.isVoid).isFalse()
        assertThat(item.subtitle).contains("Cash")
    }

    @Test
    fun expenseDto_fallbackToCategoryNameWhenDescriptionIsBlank() {
        val dto = ExpenseDto(
            expenseId = "exp-201",
            fundId = "fund-1",
            amount = 10_000L,
            expenseDate = 1_700_000_000_000L,
            category = "Travel & Transportation",
            description = "",
            paymentMethod = "Mobile Banking"
        )

        val item = dto.toTransactionItem()

        assertThat(item.title).isEqualTo("Travel & Transportation")
    }

    @Test
    fun paymentMethod_fromString_parsesAllVariantsGracefully() {
        assertThat(PaymentMethod.fromString("Cash")).isEqualTo(PaymentMethod.CASH)
        assertThat(PaymentMethod.fromString("cash")).isEqualTo(PaymentMethod.CASH)
        assertThat(PaymentMethod.fromString("BANK_TRANSFER")).isEqualTo(PaymentMethod.BANK_TRANSFER)
        assertThat(PaymentMethod.fromString("Bank Transfer")).isEqualTo(PaymentMethod.BANK_TRANSFER)
        assertThat(PaymentMethod.fromString("bank transfer")).isEqualTo(PaymentMethod.BANK_TRANSFER)
        assertThat(PaymentMethod.fromString("MOBILE_BANKING")).isEqualTo(PaymentMethod.MOBILE_BANKING)
        assertThat(PaymentMethod.fromString("Mobile Banking")).isEqualTo(PaymentMethod.MOBILE_BANKING)
        assertThat(PaymentMethod.fromString("unknown_method")).isEqualTo(PaymentMethod.CASH)
    }

    @Test
    fun expenseCategory_fromString_parsesAllCategoriesGracefully() {
        assertThat(ExpenseCategory.fromString("Office & Administration"))
            .isEqualTo(ExpenseCategory.OFFICE_AND_ADMINISTRATION)
        assertThat(ExpenseCategory.fromString("Travel & Transportation"))
            .isEqualTo(ExpenseCategory.TRAVEL_AND_TRANSPORTATION)
        assertThat(ExpenseCategory.fromString("Bank & Transaction Charges"))
            .isEqualTo(ExpenseCategory.BANK_AND_TRANSACTION_CHARGES)
        assertThat(ExpenseCategory.fromString("Legal & Documentation"))
            .isEqualTo(ExpenseCategory.LEGAL_AND_DOCUMENTATION)
        assertThat(ExpenseCategory.fromString("Meetings & Events"))
            .isEqualTo(ExpenseCategory.MEETINGS_AND_EVENTS)
        assertThat(ExpenseCategory.fromString("Utilities & Communication"))
            .isEqualTo(ExpenseCategory.UTILITIES_AND_COMMUNICATION)
        assertThat(ExpenseCategory.fromString("Rent & Maintenance"))
            .isEqualTo(ExpenseCategory.RENT_AND_MAINTENANCE)
        assertThat(ExpenseCategory.fromString("Others"))
            .isEqualTo(ExpenseCategory.OTHERS)
        assertThat(ExpenseCategory.fromString("unknown_category"))
            .isEqualTo(ExpenseCategory.OTHERS)
    }

    @Test
    fun depositDto_toDomain_and_toDto_bidirectionalMapping() {
        val dto = DepositDto(
            depositId = "dep-100",
            fundId = "fund-1",
            memberId = "mem-1",
            memberEmail = "member@example.com",
            memberName = "Rahim Uddin",
            amount = 150_000L,
            depositDate = 1_700_000_000_000L,
            paymentMethod = "Bank Transfer",
            note = "First tranche",
            isVoid = true,
            voidReason = "Reversal reason",
            voidedAt = 1_700_000_100_000L,
            voidedBy = "admin@fundcircle.app"
        )

        val domain = dto.toDomain()
        assertThat(domain.id).isEqualTo("dep-100")
        assertThat(domain.fundId).isEqualTo("fund-1")
        assertThat(domain.amount).isEqualTo(150_000L)
        assertThat(domain.depositDate).isEqualTo(1_700_000_000_000L)
        assertThat(domain.paymentMethod).isEqualTo(PaymentMethod.BANK_TRANSFER)
        assertThat(domain.note).isEqualTo("First tranche")
        assertThat(domain.isVoid).isTrue()
        assertThat(domain.voidMetadata?.reason).isEqualTo("Reversal reason")
        assertThat(domain.voidMetadata?.voidedAt).isEqualTo(1_700_000_100_000L)
        assertThat(domain.voidMetadata?.voidedBy).isEqualTo("admin@fundcircle.app")

        val mappedBackDto = domain.toDto()
        assertThat(mappedBackDto.depositId).isEqualTo(dto.depositId)
        assertThat(mappedBackDto.amount).isEqualTo(dto.amount)
        assertThat(mappedBackDto.isVoid).isTrue()
        assertThat(mappedBackDto.voidReason).isEqualTo(dto.voidReason)
    }

    @Test
    fun expenseDto_toDomain_and_toDto_bidirectionalMapping() {
        val dto = ExpenseDto(
            expenseId = "exp-200",
            fundId = "fund-1",
            amount = 25_000L,
            expenseDate = 1_700_000_050_000L,
            category = "Office & Administration",
            description = "Desk Supplies",
            paymentMethod = "Cash",
            paidBy = "admin@example.com",
            createdBy = "uid-1",
            creatorEmail = "admin@example.com",
            isVoid = false
        )

        val domain = dto.toDomain()
        assertThat(domain.id).isEqualTo("exp-200")
        assertThat(domain.fundId).isEqualTo("fund-1")
        assertThat(domain.amount).isEqualTo(25_000L)
        assertThat(domain.category).isEqualTo(ExpenseCategory.OFFICE_AND_ADMINISTRATION)
        assertThat(domain.description).isEqualTo("Desk Supplies")
        assertThat(domain.isVoid).isFalse()

        val mappedBackDto = domain.toDto()
        assertThat(mappedBackDto.expenseId).isEqualTo(dto.expenseId)
        assertThat(mappedBackDto.amount).isEqualTo(dto.amount)
        assertThat(mappedBackDto.category).isEqualTo("Office & Administration")
    }

    @Test
    fun circleMemberDto_toDomain_and_toDto_bidirectionalMapping() {
        val dto = CircleMemberDto(
            userId = "usr-1",
            memberId = "FC-002",
            email = "user@example.com",
            name = "Test User",
            role = "ADMIN",
            status = "ACTIVE",
            totalContributed = 300_000L,
            joinedAt = 1_000L,
            lastActiveAt = 2_000L
        )

        val domain = dto.toDomain()
        assertThat(domain.userId).isEqualTo("usr-1")
        assertThat(domain.memberId).isEqualTo("FC-002")
        assertThat(domain.role).isEqualTo(MemberRole.ADMIN)
        assertThat(domain.status).isEqualTo(MemberStatus.ACTIVE)
        assertThat(domain.totalContributed).isEqualTo(300_000L)

        val mappedBackDto = domain.toDto()
        assertThat(mappedBackDto.userId).isEqualTo(dto.userId)
        assertThat(mappedBackDto.role).isEqualTo("ADMIN")
        assertThat(mappedBackDto.status).isEqualTo("ACTIVE")
    }
}
