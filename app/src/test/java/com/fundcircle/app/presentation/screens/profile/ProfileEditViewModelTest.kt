package com.fundcircle.app.presentation.screens.profile

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.MemberKyc
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.model.UserSession
import com.fundcircle.app.domain.repository.AuthRepository
import com.fundcircle.app.domain.usecase.GetMemberKycUseCase
import com.fundcircle.app.domain.usecase.ObserveCircleUseCase
import com.fundcircle.app.domain.usecase.ObserveCurrentMemberUseCase
import com.fundcircle.app.domain.usecase.UpdateMemberKycUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileEditViewModelTest {

    private val getMemberKycUseCase: GetMemberKycUseCase = mockk(relaxed = true)
    private val updateMemberKycUseCase: UpdateMemberKycUseCase = mockk(relaxed = true)
    private val observeCircleUseCase: ObserveCircleUseCase = mockk(relaxed = true)
    private val observeCurrentMemberUseCase: ObserveCurrentMemberUseCase = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private val fundId = "fund-test-101"
    private val userId = "usr-1"

    private val sampleSession = UserSession(
        uid = userId,
        email = "rahim@example.com",
        displayName = "Rahim Uddin",
        photoUrl = null,
        idToken = "token",
        activeCircleId = fundId
    )

    private val sampleCircle = FundCircle(
        id = fundId,
        name = "Green Valley Pool",
        description = null,
        currency = Currency.BDT,
        adminId = "admin-1",
        adminEmail = "admin@example.com",
        memberEmails = listOf("admin@example.com", "rahim@example.com"),
        memberCount = 2,
        totalDeposits = 250_000L,
        totalExpenses = 0L,
        recordedBalance = 250_000L,
        isLocked = false,
        createdAt = 1000L
    )

    private val sampleMember = CircleMember(
        userId = userId,
        memberId = "FC-002",
        email = "rahim@example.com",
        name = "Rahim Uddin",
        phone = "01722222222",
        role = MemberRole.MEMBER,
        status = MemberStatus.ACTIVE,
        totalContributed = 250_000L,
        joinedAt = 1000L,
        lastActiveAt = 2000L
    )

    private val sampleKyc = MemberKyc(
        userId = userId,
        nidNumber = "19882692015000123",
        maskedNid = "•••• •••• 0123",
        nomineeName = "Nasrin Akter",
        nomineePhone = "01812345678",
        nomineeRelation = "Spouse"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { authRepository.userSession } returns MutableStateFlow(sampleSession)
        coEvery { authRepository.getActiveCircleId() } returns fundId
        every { observeCircleUseCase(fundId) } returns flowOf(sampleCircle)
        every { observeCurrentMemberUseCase(fundId, userId, "rahim@example.com") } returns flowOf(sampleMember)
        coEvery { getMemberKycUseCase(fundId, userId) } returns Result.success(sampleKyc)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ProfileEditViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("fundId" to fundId))
        return ProfileEditViewModel(
            getMemberKycUseCase = getMemberKycUseCase,
            updateMemberKycUseCase = updateMemberKycUseCase,
            observeCircleUseCase = observeCircleUseCase,
            observeCurrentMemberUseCase = observeCurrentMemberUseCase,
            authRepository = authRepository,
            savedStateHandle = savedStateHandle
        )
    }

    @Test
    fun init_loadsKycAndPopulatesState() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.fullName).isEqualTo("Rahim Uddin")
        assertThat(state.nidNumber).isEqualTo("19882692015000123")
        assertThat(state.maskedNid).isEqualTo("•••• •••• 0123")
        assertThat(state.nomineeName).isEqualTo("Nasrin Akter")
        assertThat(state.nomineePhone).isEqualTo("01812345678")
        assertThat(state.nomineeRelation).isEqualTo("Spouse")
    }

    @Test
    fun save_whenInvalidInputs_showsValidationErrors() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ProfileEditUiAction.NidChanged("123")) // Invalid NID length
        viewModel.onAction(ProfileEditUiAction.NomineeNameChanged("A")) // Too short
        viewModel.onAction(ProfileEditUiAction.NomineePhoneChanged("123")) // Invalid phone
        viewModel.onAction(ProfileEditUiAction.NomineeRelationChanged("")) // Blank

        viewModel.onAction(ProfileEditUiAction.Save)

        val state = viewModel.uiState.value
        assertThat(state.nidError).isNotNull()
        assertThat(state.nomineeNameError).isNotNull()
        assertThat(state.nomineePhoneError).isNotNull()
        assertThat(state.nomineeRelationError).isNotNull()
        assertThat(state.isSaving).isFalse()
    }

    @Test
    fun save_whenValid_persistsKycAndEmitsNavigateBack() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { updateMemberKycUseCase(fundId, any()) } returns Result.success(Unit)

        viewModel.uiEffect.test {
            viewModel.onAction(ProfileEditUiAction.Save)
            advanceUntilIdle()

            val snackbarEffect = awaitItem()
            assertThat(snackbarEffect).isInstanceOf(ProfileEditUiEffect.ShowSnackbar::class.java)

            val backEffect = awaitItem()
            assertThat(backEffect).isEqualTo(ProfileEditUiEffect.NavigateBack)
        }

        assertThat(viewModel.uiState.value.isSuccess).isTrue()
    }

    @Test
    fun nidInput_clampedTo17Digits() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ProfileEditUiAction.NidChanged("12345678901234567890"))
        assertThat(viewModel.uiState.value.nidNumber).isEqualTo("12345678901234567")
    }

    @Test
    fun nomineePhoneInput_clampedTo11Digits() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ProfileEditUiAction.NomineePhoneChanged("01812345678999"))
        assertThat(viewModel.uiState.value.nomineePhone).isEqualTo("01812345678")
    }

    @Test
    fun invitedMember_loadsAndSavesKycUnderTheirMemberDocument_notTheirAuthUid() = runTest {
        val memberDocId = "invite-uuid-42"
        val invitedMember = sampleMember.copy(userId = memberDocId)
        every { observeCurrentMemberUseCase(fundId, userId, "rahim@example.com") } returns flowOf(invitedMember)
        coEvery { getMemberKycUseCase(fundId, memberDocId) } returns Result.success(sampleKyc.copy(userId = memberDocId))
        val savedKyc = slot<MemberKyc>()
        coEvery { updateMemberKycUseCase(fundId, capture(savedKyc)) } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.userId).isEqualTo(memberDocId)
        assertThat(viewModel.uiState.value.nomineeName).isEqualTo("Nasrin Akter")

        viewModel.onAction(ProfileEditUiAction.Save)
        advanceUntilIdle()

        assertThat(savedKyc.captured.userId).isEqualTo(memberDocId)
    }
}
