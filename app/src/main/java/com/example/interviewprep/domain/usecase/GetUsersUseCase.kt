package com.example.interviewprep.domain.usecase

import androidx.paging.PagingData
import com.example.interviewprep.domain.model.User
import com.example.interviewprep.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case for getting paginated users
 * 
 * Use cases encapsulate business logic and can:
 * - Combine multiple repository calls
 * - Apply business rules
 * - Transform data
 * 
 * For simple CRUD, you might skip use cases and call repository directly
 * Use cases shine when you have complex business logic
 */
class GetUsersUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<PagingData<User>> {
        return repository.getUsers()
    }
}
