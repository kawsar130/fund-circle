package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.MemberKyc
import com.fundcircle.app.domain.repository.MemberRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateMemberKycUseCaseTest {

    private val memberRepository: MemberRepository = mockk(relaxed = true)
    private lateinit var useCase: UpdateMemberKycUseCase
    private val fundId = "fund-test-101"

    @Before
    fun setUp() {
        useCase = UpdateMemberKycUseCase(memberRepository)
    }

    @Test
    fun invoke_valid10DigitNid_succeeds() = runTest {
        val kyc = createKyc(nid = "1234567890")
        coEvery { memberRepository.updateMemberKyc(fundId, any()) } returns Result.success(Unit)

        val result = useCase(fundId, kyc)

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun invoke_valid13DigitNid_succeeds() = runTest {
        val kyc = createKyc(nid = "1234567890123")
        coEvery { memberRepository.updateMemberKyc(fundId, any()) } returns Result.success(Unit)

        val result = useCase(fundId, kyc)

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun invoke_valid17DigitNid_succeeds() = runTest {
        val kyc = createKyc(nid = "19882692015000123")
        coEvery { memberRepository.updateMemberKyc(fundId, any()) } returns Result.success(Unit)

        val result = useCase(fundId, kyc)

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun invoke_invalidNidLength_returnsInvalidNid() = runTest {
        val kyc = createKyc(nid = "123456789") // 9 digits
        val result = useCase(fundId, kyc)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.InvalidNid::class.java)
    }

    @Test
    fun invoke_nonNumericNid_returnsInvalidNid() = runTest {
        val kyc = createKyc(nid = "123456789A") // 10 chars with letter
        val result = useCase(fundId, kyc)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.InvalidNid::class.java)
    }

    @Test
    fun invoke_nomineeNameTooShort_returnsInvalidNomineeName() = runTest {
        val kyc = createKyc(nomineeName = "Al")
        val result = useCase(fundId, kyc)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.InvalidNomineeName::class.java)
    }

    @Test
    fun invoke_nomineePhoneInvalid_returnsInvalidPhone() = runTest {
        val kyc = createKyc(nomineePhone = "0181234567") // 10 digits
        val result = useCase(fundId, kyc)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.InvalidPhone::class.java)
    }

    @Test
    fun invoke_nomineeRelationBlank_returnsInvalidNomineeRelation() = runTest {
        val kyc = createKyc(nomineeRelation = "   ")
        val result = useCase(fundId, kyc)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.InvalidNomineeRelation::class.java)
    }

    private fun createKyc(
        nid: String = "19882692015000123",
        nomineeName: String = "Nasrin Akter",
        nomineePhone: String = "01812345678",
        nomineeRelation: String = "Spouse"
    ) = MemberKyc(
        userId = "usr-1",
        nidNumber = nid,
        maskedNid = "•••• •••• 0123",
        nomineeName = nomineeName,
        nomineePhone = nomineePhone,
        nomineeRelation = nomineeRelation
    )
}
