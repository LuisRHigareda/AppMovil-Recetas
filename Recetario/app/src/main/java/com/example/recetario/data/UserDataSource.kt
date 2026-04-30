package com.example.recetario.data

import com.example.recetario.model.User

interface UserDataSource {
    suspend fun saveUser(user: User): Result<Unit>
    suspend fun getUser(uid: String): Result<User?>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun deleteUser(uid: String): Result<Unit>
}