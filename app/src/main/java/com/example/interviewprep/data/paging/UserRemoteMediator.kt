package com.example.interviewprep.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.interviewprep.data.local.AppDatabase
import com.example.interviewprep.data.local.entity.UserEntity
import com.example.interviewprep.data.local.entity.toEntity
import com.example.interviewprep.data.remote.ApiService
import com.example.interviewprep.data.remote.dto.toDomain
import retrofit2.HttpException
import java.io.IOException

/**
 * RemoteMediator for Paging 3
 * 
 * Coordinates between network and database:
 * 1. Fetch from network
 * 2. Save to database
 * 3. Database becomes source of truth
 * 
 * This enables offline-first architecture
 */
@OptIn(ExperimentalPagingApi::class)
class UserRemoteMediator(
    private val apiService: ApiService,
    private val database: AppDatabase
) : RemoteMediator<Int, UserEntity>() {

    private val userDao = database.userDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, UserEntity>
    ): MediatorResult {
        return try {
            // Determine page to load
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    if (lastItem == null) {
                        1
                    } else {
                        // Calculate next page based on current data
                        (state.pages.sumOf { it.data.size } / state.config.pageSize) + 1
                    }
                }
            }

            // Fetch from network
            val users = apiService.getUsers(
                page = page,
                limit = state.config.pageSize
            )

            // Save to database
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    userDao.clearAll()
                }
                
                val userEntities = users.map { it.toDomain().toEntity() }
                userDao.insertUsers(userEntities)
            }

            MediatorResult.Success(
                endOfPaginationReached = users.isEmpty()
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}
