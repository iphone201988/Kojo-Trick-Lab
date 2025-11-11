package com.example.newbase_2025.data.model

data class UserLoginData(
    val __v: Int? = 0,
    val _id: String?,
    val about: String?,
    val createdAt: String?,
    val deactivatedByAdmin: Boolean?,
    val deviceToken: String?,
    val deviceType: Int? = 0,
    val email: String?,
    val gender: Int? = 0,
    val isActive: Boolean?,
    val isDeleted: Boolean?,
    val location: String?,
    val phoneNumber: String?,
    val profileImage: String?,
    val role: Int,
    val socialId: String? ="",
    val subscription: Any?,
    val token: String?,
    val updatedAt: String?,
    val userName: String?
)
