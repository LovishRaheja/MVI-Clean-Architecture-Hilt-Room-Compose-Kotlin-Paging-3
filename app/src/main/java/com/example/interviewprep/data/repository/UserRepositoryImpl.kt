package com.example.interviewprep.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.interviewprep.data.local.AppDatabase
import com.example.interviewprep.data.local.entity.toDomain
import com.example.interviewprep.data.local.entity.toEntity
import com.example.interviewprep.data.paging.UserRemoteMediator
import com.example.interviewprep.data.remote.ApiService
import com.example.interviewprep.data.remote.dto.toDomain
import com.example.interviewprep.domain.model.User
import com.example.interviewprep.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository implementation - Data Layer
 * 
 * Implements domain repository interface
 * Coordinates between local (Room) and remote (API) data sources
 */
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val database: AppDatabase
) : UserRepository {

    private val userDao = database.userDao()

    /**
     * Get paginated users using Paging 3
     * 
     * Key components:
     * - Pager: Main Paging 3 class
     * - PagingConfig: Configuration (page size, prefetch, etc.)
     * - RemoteMediator: Handles network + cache coordination
     * - PagingSource: Comes from Room DAO
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun getUsers(): Flow<PagingData<User>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            remoteMediator = UserRemoteMediator(
                apiService = apiService,
                database = database
            ),
            pagingSourceFactory = { userDao.getUsersPagingSource() }
        ).flow.map { pagingData ->
            // Map UserEntity to User (domain model)
            pagingData.map { it.toDomain() }
        }
    }

    /**
     * Get single user by ID
     * Try cache first, then network
     */
    override suspend fun getUserById(id: Int): Result<User> {
        return try {
            // Try cache first
            val cachedUser = userDao.getUserById(id)
            if (cachedUser != null) {
                return Result.success(cachedUser.toDomain())
            }

            // Fetch from network
            val userDto = apiService.getUserById(id)
            val user = userDto.toDomain()

            // Cache it
            userDao.insertUsers(listOf(user.toEntity()))

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Refresh - clear cache and reload
     */
    override suspend fun refreshUsers(): Result<Unit> {
        return try {
            userDao.clearAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
