package com.example.interviewprep.presentation.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.interviewprep.domain.model.User

/**
 * Compose Screen with Paging 3
 * 
 * Key Compose + MVI concepts:
 * - hiltViewModel() for DI
 * - collectAsState() for StateFlow
 * - LaunchedEffect for side effects
 * - collectAsLazyPagingItems() for Paging 3
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    viewModel: UserListViewModel = hiltViewModel(),
    onNavigateToDetail: (Int) -> Unit = {}
) {
    // Collect state
    val state by viewModel.state.collectAsState()
    
    // Collect paging items
    val users = state.users.collectAsLazyPagingItems()
    
    // Handle one-time effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UserListEffect.NavigateToDetail -> {
                    onNavigateToDetail(effect.userId)
                }
                is UserListEffect.ShowError -> {
                    // Show snackbar or toast
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Users (MVI + Paging)") }
            )
        }
    ) { paddingValues ->
        UserListContent(
            users = users,
            modifier = Modifier.padding(paddingValues),
            onUserClick = { user ->
                viewModel.onEvent(UserListEvent.NavigateToDetail(user.id))
            },
            onRefresh = {
                viewModel.onEvent(UserListEvent.Refresh)
            }
        )
    }
}

/**
 * List content with Paging 3
 * 
 * LazyPagingItems handles:
 * - Loading states (initial, append, prepend)
 * - Error handling
 * - Retry logic
 */
@Composable
private fun UserListContent(
    users: LazyPagingItems<User>,
    modifier: Modifier = Modifier,
    onUserClick: (User) -> Unit,
    onRefresh: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            // Initial loading
            users.loadState.refresh is LoadState.Loading && users.itemCount == 0 -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            // Error
            users.loadState.refresh is LoadState.Error -> {
                ErrorContent(
                    message = "Failed to load users",
                    onRetry = { users.retry() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            // Content
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        count = users.itemCount,
                        key = { index -> users[index]?.id ?: index }
                    ) { index ->
                        users[index]?.let { user ->
                            UserItem(
                                user = user,
                                onClick = { onUserClick(user) }
                            )
                        }
                    }
                    
                    // Append loading indicator
                    if (users.loadState.append is LoadState.Loading) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    
                    // Append error
                    if (users.loadState.append is LoadState.Error) {
                        item {
                            ErrorContent(
                                message = "Failed to load more",
                                onRetry = { users.retry() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserItem(
    user: User,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium
            )
            user.company?.let { company ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = company,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
