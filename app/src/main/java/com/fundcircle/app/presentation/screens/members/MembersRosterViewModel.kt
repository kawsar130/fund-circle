package com.fundcircle.app.presentation.screens.members

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.repository.AuthRepository
import com.fundcircle.app.domain.usecase.ObserveCircleMembersUseCase
import com.fundcircle.app.domain.usecase.ObserveCircleUseCase
import com.fundcircle.app.domain.usecase.ObserveDepositsUseCase
import com.fundcircle.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MembersRosterViewModel @Inject constructor(
    private val observeCircleMembersUseCase: ObserveCircleMembersUseCase,
    private val observeCircleUseCase: ObserveCircleUseCase,
    private val observeDepositsUseCase: ObserveDepositsUseCase,
    private val authRepository: AuthRepository,
    private val networkMonitor: NetworkMonitor,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<MembersRosterUiState, MembersRosterUiAction, MembersRosterUiEffect>(
    MembersRosterUiState()
) {
    private var dataCollectionJob: Job? = null

    init {
        val fundId: String? = savedStateHandle[KEY_FUND_ID]
        if (!fundId.isNullOrBlank()) {
            loadMembers(fundId)
        }
        observeNetwork()
    }

    override fun onAction(action: MembersRosterUiAction) {
        when (action) {
            is MembersRosterUiAction.Init -> loadMembers(action.fundId)
            is MembersRosterUiAction.SearchQueryChanged -> {
                val filtered = filterMembers(uiState.value.members, action.query)
                setState {
                    copy(
                        searchQuery = action.query,
                        filteredMembers = filtered
                    )
                }
            }
            is MembersRosterUiAction.SetSearchActive -> {
                setState { copy(isSearchActive = action.active) }
            }
            is MembersRosterUiAction.ClickMember -> {
                sendEffect(
                    MembersRosterUiEffect.NavigateToMemberDetail(
                        fundId = uiState.value.fundId,
                        userId = action.userId
                    )
                )
            }
            is MembersRosterUiAction.OpenAddMemberSheet -> {
                setState { copy(showAddMemberSheet = true) }
            }
            is MembersRosterUiAction.DismissAddMemberSheet -> {
                setState { copy(showAddMemberSheet = false) }
            }
            is MembersRosterUiAction.Refresh -> {
                loadMembers(uiState.value.fundId)
            }
            is MembersRosterUiAction.DismissError -> {
                setState { copy(errorMessage = null) }
            }
            is MembersRosterUiAction.NavigateToProfileEdit -> {
                sendEffect(MembersRosterUiEffect.NavigateToProfileEdit)
            }
            is MembersRosterUiAction.NavigateBack -> {
                sendEffect(MembersRosterUiEffect.NavigateBack)
            }
        }
    }

    private fun loadMembers(fundId: String) {
        if (fundId.isBlank()) return
        setState { copy(fundId = fundId, isLoading = true, errorMessage = null) }
        dataCollectionJob?.cancel()

        dataCollectionJob = viewModelScope.launch {
            combine(
                observeCircleUseCase(fundId),
                observeCircleMembersUseCase(fundId),
                observeDepositsUseCase(fundId),
                authRepository.userSession
            ) { circle, members, deposits, session ->
                val currentUid = session?.uid.orEmpty()
                val currentEmail = session?.email.orEmpty()
                val isAdmin = circle.adminId == currentUid ||
                        circle.adminEmail.equals(currentEmail, ignoreCase = true) ||
                        members.any { it.userId == currentUid && it.role == MemberRole.ADMIN }

                val counts = members.associate { member ->
                    val count = deposits.count { dep ->
                        !dep.isVoid && (dep.memberId == member.userId || dep.memberEmail.equals(member.email, ignoreCase = true))
                    }
                    member.userId to count
                }

                val filtered = filterMembers(members, uiState.value.searchQuery)

                setState {
                    copy(
                        isLoading = false,
                        circle = circle,
                        members = members,
                        filteredMembers = filtered,
                        depositCounts = counts,
                        isAdmin = isAdmin,
                        errorMessage = null
                    )
                }
            }.catch {
                setState {
                    copy(
                        isLoading = false,
                        errorMessage = UiText.StringResource(R.string.members_roster_error_loading)
                    )
                }
            }.collect { }
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                setState { copy(isOffline = !isOnline) }
            }
        }
    }

    private fun filterMembers(members: List<CircleMember>, query: String): List<CircleMember> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return members
        return members.filter { member ->
            member.name.contains(trimmed, ignoreCase = true) ||
                    member.phone.contains(trimmed, ignoreCase = true) ||
                    member.email.contains(trimmed, ignoreCase = true) ||
                    member.memberId.contains(trimmed, ignoreCase = true)
        }
    }

    companion object {
        const val KEY_FUND_ID = "fundId"
    }
}
