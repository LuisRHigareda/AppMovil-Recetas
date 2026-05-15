package com.example.recetario.data.firebase

data class UserFirebase(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val profileImageUri: String? = null,
    val passwordHash: String = ""
)