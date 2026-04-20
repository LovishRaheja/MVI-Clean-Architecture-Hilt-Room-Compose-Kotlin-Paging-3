package com.example.interviewprep.data.remote

import com.example.interviewprep.data.remote.dto.UserDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface
 * 
 * Example using JSONPlaceholder API
 * In real interview, replace with actual API
 */
interface ApiService {
    /**
     * Get paginated users
     * @param page Page number (starts at 1)
     * @param limit Items per page
     */
    @GET("users")
    suspend fun getUsers(
        @Query("_page") page: Int,
        @Query("_limit") limit: Int
    ): List<UserDto>
    
    /**
     * Get single user by ID
     */
    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") id: Int
    ): UserDto
}
