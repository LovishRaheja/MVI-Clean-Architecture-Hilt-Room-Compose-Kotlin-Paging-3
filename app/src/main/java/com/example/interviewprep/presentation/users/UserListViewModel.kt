package com.example.interviewprep.presentation.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.interviewprep.domain.usecase.GetUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel with MVI pattern
 * 
 * Key MVI principles:
 * 1. Single immutable state (StateFlow)
 * 2. Events flow in (function calls)
 * 3. Effects flow out (Channel for one-time events)
 * 4. Unidirectional data flow
 * 
 * @HiltViewModel - Enables Hilt injection in ViewModel
 * Constructor injection automatically provided by Hilt
 */
@HiltViewModel
class UserListViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {

    /**
     * MutableStateFlow - Internal state (private)
     * StateFlow - Public state (immutable view)
     * 
     * StateFlow is hot and always has value
     * Survives configuration changes
     */
    private val _state = MutableStateFlow(UserListState())
    val state: StateFlow<UserListState> = _state.asStateFlow()

    /**
     * Channel for one-time effects
     * 
     * Channel vs StateFlow:
     * - Channel: One-time events, consumed once
     * - StateFlow: Persistent state, always has latest value
     * 
     * Use Channel for: Navigation, Snackbar, Toasts
     * Use StateFlow for: UI state
     */
    private val _effect = Channel<UserListEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadUsers()
    }

    /**
     * Handle UI events
     * Single entry point for all user interactions
     */
    fun onEvent(event: UserListEvent) {
        when (event) {
            is UserListEvent.Refresh -> refresh()
            is UserListEvent.NavigateToDetail -> navigateToDetail(event.userId)
        }
    }

    /**
     * Load users with Paging 3
     * 
     * cachedIn(viewModelScope) caches the PagingData
     * Survives configuration changes
     */
    private fun loadUsers() {
        val users = getUsersUseCase()
            .cachedIn(viewModelScope)

        _state.update { it.copy(users = users) }
    }

    /**
     * Refresh users
     */
    private fun refresh() {
        loadUsers()
    }

    /**
     * Navigate to detail
     * Sends effect through Channel
     */
    private fun navigateToDetail(userId: Int) {
        viewModelScope.launch {
            _effect.send(UserListEffect.NavigateToDetail(userId))
        }
    }
}
