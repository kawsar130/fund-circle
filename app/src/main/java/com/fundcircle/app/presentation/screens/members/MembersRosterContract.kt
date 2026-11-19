package com.fundcircle.app.presentation.screens.members

import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.presentation.common.UiAction
import com.fundcircle.app.presentation.common.UiEffect
import com.fundcircle.app.presentation.common.UiState

/**
 * MVI Contract for Screen 08: Stakeholders Roster (CUJ-07).
 */
data class MembersRosterUiState(
    val isLoading: Boolean = true,
    val fundId: String = "",
    val circle: FundCircle? = null,
    val members: List<CircleMember> = emptyList(),
    val filteredMembers: List<CircleMember> = emptyList(),
    val depositCounts: Map<String, Int> = emptyMap(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isAdmin: Boolean = false,
    val isOffline: Boolean = false,
    val showAddMemberSheet: Boolean = false,
    val errorMessage: UiText? = null
) : UiState

sealed interface MembersRosterUiAction : UiAction {
    data class Init(val fundId: String) : MembersRosterUiAction
    data class SearchQueryChanged(val query: String) : MembersRosterUiAction
    data class SetSearchActive(val active: Boolean) : MembersRosterUiAction
    data class ClickMember(val userId: String) : MembersRosterUiAction
    data object OpenAddMemberSheet : MembersRosterUiAction
    data object DismissAddMemberSheet : MembersRosterUiAction
    data object Refresh : MembersRosterUiAction
    data object DismissError : MembersRosterUiAction
    data object NavigateToProfileEdit : MembersRosterUiAction
    data object NavigateBack : MembersRosterUiAction
}

sealed interface MembersRosterUiEffect : UiEffect {
    data class NavigateToMemberDetail(val fundId: String, val userId: String) : MembersRosterUiEffect
    data object NavigateToProfileEdit : MembersRosterUiEffect
    data object NavigateBack : MembersRosterUiEffect
    data class ShowSnackbar(val message: UiText) : MembersRosterUiEffect
}
