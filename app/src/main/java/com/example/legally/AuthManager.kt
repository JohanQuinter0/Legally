package com.example.legally

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthManager {
    private val auth: FirebaseAuth = Firebase.auth

    suspend fun registrarUsuario(
        correo: String,
        contraseña: String
    ): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(correo, contraseña).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun iniciarSesion(
        correo: String,
        contraseña: String
    ): Boolean {
        return try {
            auth.signInWithEmailAndPassword(correo, contraseña).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun obtenerUsuarioActual(): String? {
        return auth.currentUser?.email
    }
}