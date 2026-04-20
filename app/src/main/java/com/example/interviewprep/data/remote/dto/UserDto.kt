package com.example.interviewprep.data.remote.dto

import com.example.interviewprep.domain.model.User
import com.google.gson.annotations.SerializedName

/**
 * API Response DTO (Data Transfer Object)
 * Separate from domain model to handle API-specific fields
 */
data class UserDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("avatar_url")
    val avatarUrl: String? = null,
    
    @SerializedName("company")
    val company: String? = null
)

/**
 * Mapper to domain model
 */
fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        avatarUrl = avatarUrl,
        company = company
    )
}
