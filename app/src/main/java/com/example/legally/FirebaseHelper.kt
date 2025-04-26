package com.example.legally

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseHelper(context: Context) {
    private val db: FirebaseFirestore = Firebase.firestore

    suspend fun registrarUsuario(
        correo: String,
        nombre: String,
        tipoDocumento: String,
        documento: String,
        telefono: String,
        contrasena: String
    ): Boolean {
        return try {
            val usuario = hashMapOf(
                "correo" to correo,
                "nombre" to nombre,
                "tipo_documento" to tipoDocumento,
                "documento" to documento,
                "telefono" to telefono,
                "contrasena" to contrasena
            )

            db.collection("usuarios").document(correo).set(usuario).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun validarUsuario(correo: String, contraseña: String): Boolean {
        return false
    }

    suspend fun insertarArticulo(
        serial: String,
        idUsuario: String,
        nombre: String,
        marca: String,
        descripcion: String,
        tipo: String
    ): Boolean {
        return try {
            val articulo = hashMapOf(
                "serial" to serial,
                "id_usuario" to idUsuario,
                "nombre_articulo" to nombre,
                "marca_articulo" to marca,
                "descripcion_articulo" to descripcion,
                "tipo_articulo" to tipo,
                "fecha_registro" to System.currentTimeMillis()
            )

            db.collection("articulos").document(serial).set(articulo).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obtenerArticulosDeUsuario(idUsuario: String): List<Articulo> {
        return try {
            val query = db.collection("articulos")
                .whereEqualTo("id_usuario", idUsuario)
                .get()
                .await()

            query.documents.map { doc ->
                Articulo(
                    serial = doc.getString("serial") ?: "",
                    nombre_articulo = doc.getString("nombre_articulo") ?: "",
                    descripcion_articulo = doc.getString("descripcion_articulo") ?: "",
                    tipo_articulo = doc.getString("tipo_articulo") ?: "",
                    marca_articulo = doc.getString("marca_articulo") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun eliminarArticulo(serial: String): Boolean {
        return try {
            db.collection("articulos").document(serial).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun reportarArticuloComoPerdido(serial: String): Boolean {
        return try {
            db.collection("articulos").document(serial)
                .update("estado", "perdido").await()
            true
        } catch (e: Exception) {
            false
        }
    }
}