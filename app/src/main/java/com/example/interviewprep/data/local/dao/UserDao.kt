package com.example.interviewprep.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.interviewprep.data.local.entity.UserEntity

/**
 * Room DAO with Paging 3 integration
 * 
 * Key points:
 * - PagingSource<Int, UserEntity> for pagination
 * - Int = page key type, UserEntity = data type
 * - Room automatically handles the paging
 */
@Dao
interface UserDao {
    /**
     * Returns PagingSource for Paging 3
     * Room generates implementation automatically
     */
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getUsersPagingSource(): PagingSource<Int, UserEntity>
    
    /**
     * Get single user
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?
    
    /**
     * Insert users, replace on conflict
     * Used for caching API responses
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
    
    /**
     * Clear all users - useful for refresh
     */
    @Query("DELETE FROM users")
    suspend fun clearAll()
}
