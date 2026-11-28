package com.fundcircle.app.data.mapper

import com.fundcircle.app.core.security.PrivacyMaskUtils
import com.fundcircle.app.data.dto.CircleMemberDto
import com.fundcircle.app.data.dto.DepositDto
import com.fundcircle.app.data.dto.ExpenseDto
import com.fundcircle.app.data.dto.MemberKycDto
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
import com.google.firebase.firestore.util.CustomClassMapper
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
        assertThat(item.voidReason).isEqualTo("Typo in entry")
    }

    @Test
    fun transactionItem_hasNoVoidReasonUnlessReversed() {
        val active = DepositDto(
            depositId = "dep-1", fundId = "fund-1", memberName = "Rahim", amount = 500L,
            depositDate = 1_700_000_000_000L, paymentMethod = "Cash",
            isVoid = false, voidReason = "stale reason left on an active document"
        )
        val voidBlankReason = active.copy(isVoid = true, voidReason = "  ")

        assertThat(active.toTransactionItem().voidReason).isNull()
        assertThat(voidBlankReason.toTransactionItem().voidReason).isNull()
    }

    @Test
    fun expenseDto_toTransactionItem_carriesTheReversalReason() {
        val dto = ExpenseDto(
            expenseId = "exp-void", fundId = "fund-1", amount = 5_000L,
            expenseDate = 1_700_000_000_000L, category = "Office & Administration",
            description = "Office Supplies", paymentMethod = "Cash",
            isVoid = true, voidReason = "Duplicate bank payment voucher entry."
        )

        assertThat(dto.toTransactionItem().voidReason).isEqualTo("Duplicate bank payment voucher entry.")
    }

    @Test
    fun pendingSync_flowsFromTheSnapshotDtoToListItemsAndDomainModels() {
        val deposit = DepositDto(
            depositId = "dep-offline", fundId = "fund-1", memberName = "Rahim", amount = 700L,
            depositDate = 1_700_000_000_000L, paymentMethod = "Cash", isPendingSync = true
        )
        val expense = ExpenseDto(
            expenseId = "exp-offline", fundId = "fund-1", amount = 300L,
            expenseDate = 1_700_000_000_000L, category = "Office & Administration",
            description = "Printer paper", paymentMethod = "Cash", isPendingSync = true
        )

        assertThat(deposit.toTransactionItem().isPendingSync).isTrue()
        assertThat(deposit.toDomain().isPendingSync).isTrue()
        assertThat(expense.toTransactionItem().isPendingSync).isTrue()
        assertThat(expense.toDomain().isPendingSync).isTrue()
        assertThat(deposit.copy(isPendingSync = false).toTransactionItem().isPendingSync).isFalse()
    }

    @Test
    fun pendingSync_isNeverWrittenToFirestore() {
        // The same mapper batch.set() uses. The security rules reject documents with unknown fields.
        val deposit = DepositDto(
            depositId = "dep-offline", fundId = "fund-1", memberName = "Rahim", amount = 700L,
            depositDate = 1_700_000_000_000L, paymentMethod = "Cash", isPendingSync = true
        )
        val expense = ExpenseDto(
            expenseId = "exp-offline", fundId = "fund-1", amount = 300L,
            expenseDate = 1_700_000_000_000L, category = "Office & Administration",
            description = "Printer paper", paymentMethod = "Cash", isPendingSync = true
        )

        @Suppress("UNCHECKED_CAST")
        val depositFields = (CustomClassMapper.convertToPlainJavaTypes(deposit) as Map<String, Any?>).keys
        @Suppress("UNCHECKED_CAST")
        val expenseFields = (CustomClassMapper.convertToPlainJavaTypes(expense) as Map<String, Any?>).keys

        assertThat(depositFields).contains("isVoid")
        assertThat(depositFields).containsNoneOf("isPendingSync", "pendingSync")
        assertThat(expenseFields).contains("isVoid")
        assertThat(expenseFields).containsNoneOf("isPendingSync", "pendingSync")
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
    }

    @Test
    fun expenseDto_blankDescription_leavesTitleBlankForTheUiToShowTheLocalizedCategory() {
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

        assertThat(item.title).isEmpty()
        assertThat(item.category).isEqualTo(ExpenseCategory.TRAVEL_AND_TRANSPORTATION)
        assertThat(item.paymentMethod).isEqualTo(PaymentMethod.MOBILE_BANKING)
    }

    @Test
    fun wireValues_matchTheApiSpecificationExactly() {
        // These strings are what Firestore stores and the CSV exports; changing one breaks existing data.
        assertThat(PaymentMethod.entries.map { it.wireValue })
            .containsExactly("Cash", "Bank Transfer", "Mobile Banking").inOrder()
        assertThat(ExpenseCategory.entries.map { it.wireValue }).containsExactly(
            "Office & Administration",
            "Travel & Transportation",
            "Bank & Transaction Charges",
            "Legal & Documentation",
            "Meetings & Events",
            "Utilities & Communication",
            "Rent & Maintenance",
            "Others"
        ).inOrder()
    }

    @Test
    fun domainToDto_writesWireValuesNotEnumNames() {
        val expense = Expense(
            id = "exp-1",
            fundId = "fund-1",
            amount = 500L,
            expenseDate = 1_700_000_000_000L,
            category = ExpenseCategory.BANK_AND_TRANSACTION_CHARGES,
            description = "",
            paymentMethod = PaymentMethod.MOBILE_BANKING,
            paidBy = "Admin",
            createdBy = "uid-1",
            creatorEmail = "admin@example.com"
        )

        val dto = expense.toDto()

        assertThat(dto.category).isEqualTo("Bank & Transaction Charges")
        assertThat(dto.paymentMethod).isEqualTo("Mobile Banking")
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

    @Test
    fun memberKycDto_toDomain_and_toDto_bidirectionalMapping() {
        val dto = MemberKycDto(
            userId = "usr-1",
            nidNumber = "19882692015000123",
            nomineeName = "Nasrin Akter",
            nomineePhone = "01812345678",
            nomineeRelation = "Spouse",
            verifiedByAdmin = true,
            updatedAt = 5_000L
        )

        val domain = dto.toDomain()
        assertThat(domain.userId).isEqualTo("usr-1")
        assertThat(domain.nidNumber).isEqualTo("19882692015000123")
        assertThat(domain.maskedNid).isEqualTo("•••• •••• 0123")
        assertThat(domain.nomineeName).isEqualTo("Nasrin Akter")
        assertThat(domain.nomineePhone).isEqualTo("01812345678")
        assertThat(domain.nomineeRelation).isEqualTo("Spouse")
        assertThat(domain.verifiedByAdmin).isTrue()
        assertThat(domain.updatedAt).isEqualTo(5_000L)

        val mappedBackDto = domain.toDto()
        assertThat(mappedBackDto.userId).isEqualTo(dto.userId)
        assertThat(mappedBackDto.nidNumber).isEqualTo(dto.nidNumber)
        assertThat(mappedBackDto.nomineeName).isEqualTo(dto.nomineeName)
        assertThat(mappedBackDto.verifiedByAdmin).isTrue()
    }

    @Test
    fun privacyMaskUtils_masksProperly() {
        assertThat(PrivacyMaskUtils.maskNid("1234567890")).isEqualTo("•••• •••• 7890")
        assertThat(PrivacyMaskUtils.maskNid("123")).isEqualTo("•••• •••• ••••")
    }
}
