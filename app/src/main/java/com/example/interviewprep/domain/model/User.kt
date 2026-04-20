package com.example.interviewprep.domain.model

/**
 * Domain layer entity - Pure business logic model
 * No Android/Framework dependencies
 */
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val company: String?
)
