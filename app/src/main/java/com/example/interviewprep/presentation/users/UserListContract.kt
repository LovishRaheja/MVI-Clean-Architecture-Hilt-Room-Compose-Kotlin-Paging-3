package com.example.interviewprep.presentation.users

import androidx.paging.PagingData
import com.example.interviewprep.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * MVI Pattern Components
 * 
 * MVI = Model-View-Intent
 * - Model: UI State (single source of truth)
 * - View: Composables (observe state, send events)
 * - Intent: User actions/events
 */

/**
 * UI State - Represents the complete UI state
 * 
 * In MVI, state is immutable and unidirectional
 * Only ViewModel can modify it
 */
data class UserListState(
    val users: Flow<PagingData<User>> = emptyFlow(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * UI Events - User actions from the UI
 * 
 * These represent user intents/interactions
 */
sealed class UserListEvent {
    data object Refresh : UserListEvent()
    data class NavigateToDetail(val userId: Int) : UserListEvent()
}

/**
 * UI Effects - One-time events (side effects)
 * 
 * Effects are consumed once (like navigation, showing snackbar)
 * Different from State which is persistent
 */
sealed class UserListEffect {
    data class ShowError(val message: String) : UserListEffect()
    data class NavigateToDetail(val userId: Int) : UserListEffect()
}
