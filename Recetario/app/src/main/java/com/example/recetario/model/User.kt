package com.example.recetario.model

import com.google.firebase.firestore.Exclude

data class User(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val email: String = "",

    // @get:Exclude le dice a Firestore: "Finge que esto no existe cuando guardes o leas de la nube"
    @get:Exclude
    val password: String = ""
)