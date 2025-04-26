package com.example.legally

data class Mensaje(
    val emisor: String = "",
    val texto: String = "",
    val timestamp: com.google.firebase.Timestamp? = null
)