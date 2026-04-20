package com.example.interviewprep.domain.repository

import androidx.paging.PagingData
import com.example.interviewprep.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Domain layer repository interface
 * Implementation details hidden in data layer
 */
interface UserRepository {
    /**
     * Get paginated users as Flow
     * Using Paging 3 for efficient loading
     */
    fun getUsers(): Flow<PagingData<User>>
    
    /**
     * Get single user by ID
     */
    suspend fun getUserById(id: Int): Result<User>
    
    /**
     * Refresh cache/database
     */
    suspend fun refreshUsers(): Result<Unit>
}
