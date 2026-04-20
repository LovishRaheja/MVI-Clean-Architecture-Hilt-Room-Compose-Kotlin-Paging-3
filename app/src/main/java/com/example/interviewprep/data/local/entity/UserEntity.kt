package com.example.interviewprep.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.interviewprep.domain.model.User

/**
 * Room database entity - Data layer
 * Separate from domain model for flexibility
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val company: String?
)

/**
 * Mapper extensions - Convert between layers
 */
fun UserEntity.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        avatarUrl = avatarUrl,
        company = company
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        email = email,
        avatarUrl = avatarUrl,
        company = company
    )
}
