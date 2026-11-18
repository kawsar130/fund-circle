package com.fundcircle.app.presentation.screens.create

import app.cash.turbine.test
import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.usecase.CreateCircleUseCase
import com.fundcircle.app.domain.usecase.SetActiveCircleUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateCircleViewModelTest {

    private val createCircleUseCase: CreateCircleUseCase = mockk(relaxed = true)
    private val setActiveCircleUseCase: SetActiveCircleUseCase = mockk(relaxed = true)
    private val networkMonitor: NetworkMonitor = mockk(relaxed = true)
    private val isOnlineFlow = MutableStateFlow(true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: CreateCircleViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { networkMonitor.isOnline } returns isOnlineFlow
        viewModel = CreateCircleViewModel(
            createCircleUseCase = createCircleUseCase,
            setActiveCircleUseCase = setActiveCircleUseCase,
            networkMonitor = networkMonitor
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun networkOffline_updatesIsOfflineState() = runTest {
        isOnlineFlow.value = false
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isOffline).isTrue()
    }

    @Test
    fun nameChanged_updatesNameAndValidates() = runTest {
        viewModel.onAction(CreateCircleUiAction.NameChanged("A"))
        assertThat(viewModel.uiState.value.name).isEqualTo("A")
        assertThat(viewModel.uiState.value.nameError).isNotNull()
        assertThat(viewModel.uiState.value.isCreateEnabled).isFalse()

        viewModel.onAction(CreateCircleUiAction.NameChanged("Apex Capital"))
        assertThat(viewModel.uiState.value.name).isEqualTo("Apex Capital")
        assertThat(viewModel.uiState.value.nameError).isNull()
        assertThat(viewModel.uiState.value.isCreateEnabled).isTrue()
    }

    @Test
    fun currencySelected_updatesCurrency() = runTest {
        viewModel.onAction(CreateCircleUiAction.CurrencySelected(Currency.USD))
        assertThat(viewModel.uiState.value.currency).isEqualTo(Currency.USD)
    }

    @Test
    fun descriptionChanged_updatesDescriptionAndValidates() = runTest {
        val longDesc = "X".repeat(251)
        viewModel.onAction(CreateCircleUiAction.DescriptionChanged(longDesc))
        assertThat(viewModel.uiState.value.descriptionError).isNotNull()
        assertThat(viewModel.uiState.value.isCreateEnabled).isFalse()

        viewModel.onAction(CreateCircleUiAction.DescriptionChanged("Short valid desc"))
        assertThat(viewModel.uiState.value.descriptionError).isNull()
    }

    @Test
    fun createClicked_whenNameInvalid_setsNameErrorWithoutCallingUseCase() = runTest {
        viewModel.onAction(CreateCircleUiAction.NameChanged("  "))
        viewModel.onAction(CreateCircleUiAction.CreateClicked)

        assertThat(viewModel.uiState.value.nameError).isEqualTo(
            UiText.StringResource(R.string.create_circle_name_error)
        )
        coVerify(exactly = 0) { createCircleUseCase(any(), any(), any()) }
    }

    @Test
    fun createClicked_success_setsActiveCircleAndNavigatesToDashboard() = runTest {
        coEvery {
            createCircleUseCase("Syndicate One", Currency.BDT, null)
        } returns Result.success("fund-uuid-999")
        coEvery { setActiveCircleUseCase("fund-uuid-999") } returns Result.success(Unit)

        viewModel.onAction(CreateCircleUiAction.NameChanged("Syndicate One"))

        viewModel.uiEffect.test {
            viewModel.onAction(CreateCircleUiAction.CreateClicked)

            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(CreateCircleUiEffect.NavigateToDashboard("fund-uuid-999"))
            coVerify(exactly = 1) { setActiveCircleUseCase("fund-uuid-999") }
            assertThat(viewModel.uiState.value.isLoading).isFalse()
        }
    }

    @Test
    fun createClicked_circleLimitReached_emitsLimitReachedError() = runTest {
        coEvery {
            createCircleUseCase("Syndicate Six", Currency.EUR, null)
        } returns Result.failure(FundCircleError.Business.CircleLimitReached)

        viewModel.onAction(CreateCircleUiAction.NameChanged("Syndicate Six"))
        viewModel.onAction(CreateCircleUiAction.CurrencySelected(Currency.EUR))

        viewModel.uiEffect.test {
            viewModel.onAction(CreateCircleUiAction.CreateClicked)

            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(CreateCircleUiEffect.ShowSnackbar::class.java)
            val snackbar = effect as CreateCircleUiEffect.ShowSnackbar
            assertThat(snackbar.message).isEqualTo(
                UiText.StringResource(R.string.circle_error_limit_reached)
            )
            assertThat(viewModel.uiState.value.isLoading).isFalse()
            assertThat(viewModel.uiState.value.generalError).isEqualTo(
                UiText.StringResource(R.string.circle_error_limit_reached)
            )
        }
    }

    @Test
    fun backClicked_emitsNavigateBackEffect() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(CreateCircleUiAction.BackClicked)

            val effect = awaitItem()
            assertThat(effect).isEqualTo(CreateCircleUiEffect.NavigateBack)
        }
    }
}
