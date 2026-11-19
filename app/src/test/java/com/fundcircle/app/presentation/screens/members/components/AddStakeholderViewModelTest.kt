package com.fundcircle.app.presentation.screens.members.components

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.repository.MemberRepository
import com.fundcircle.app.domain.usecase.AddMemberUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class AddStakeholderViewModelTest {

    private val addMemberUseCase: AddMemberUseCase = mockk(relaxed = true)
    private val memberRepository: MemberRepository = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private val fundId = "fund-test-101"

    private val sampleMembers = listOf(
        CircleMember(
            userId = "usr-1",
            memberId = "FC-001",
            email = "admin@example.com",
            name = "Admin",
            role = MemberRole.ADMIN,
            status = MemberStatus.ACTIVE,
            totalContributed = 0L,
            joinedAt = 1000L,
            lastActiveAt = 2000L
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { memberRepository.observeMembers(fundId) } returns flowOf(sampleMembers)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AddStakeholderViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("fundId" to fundId))
        return AddStakeholderViewModel(
            addMemberUseCase = addMemberUseCase,
            memberRepository = memberRepository,
            savedStateHandle = savedStateHandle
        )
    }

    @Test
    fun init_allocatesNextMemberId_FC002() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.allocatedMemberId).isEqualTo("FC-002")
    }

    @Test
    fun submit_whenInvalidInputs_showsValidationErrors() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(AddStakeholderUiAction.NameChanged(""))
        viewModel.onAction(AddStakeholderUiAction.PhoneChanged("123"))
        viewModel.onAction(AddStakeholderUiAction.EmailChanged("not-email"))

        viewModel.onAction(AddStakeholderUiAction.Submit)

        val state = viewModel.uiState.value
        assertThat(state.nameError).isNotNull()
        assertThat(state.phoneError).isNotNull()
        assertThat(state.emailError).isNotNull()
        assertThat(state.isSubmitting).isFalse()
    }

    @Test
    fun submit_whenValid_emitsMemberAddedEffect() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        coEvery {
            addMemberUseCase(fundId, "Tariqul Islam", "01712345678", "tariqul@gmail.com")
        } returns Result.success(Unit)

        viewModel.onAction(AddStakeholderUiAction.NameChanged("Tariqul Islam"))
        viewModel.onAction(AddStakeholderUiAction.PhoneChanged("01712345678"))
        viewModel.onAction(AddStakeholderUiAction.EmailChanged("tariqul@gmail.com"))

        viewModel.uiEffect.test {
            viewModel.onAction(AddStakeholderUiAction.Submit)
            advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(AddStakeholderUiEffect.MemberAdded::class.java)
            val added = effect as AddStakeholderUiEffect.MemberAdded
            assertThat(added.memberName).isEqualTo("Tariqul Islam")
            assertThat(added.memberId).isEqualTo("FC-002")

            val snackbar = awaitItem()
            assertThat(snackbar).isInstanceOf(AddStakeholderUiEffect.ShowSnackbar::class.java)
        }

        assertThat(viewModel.uiState.value.isSuccess).isTrue()
    }
}
