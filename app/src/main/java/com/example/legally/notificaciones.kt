package com.example.legally

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class notificaciones : AppCompatActivity() {

    private val db = Firebase.firestore
    private lateinit var contenedorNotis: LinearLayout
    private lateinit var miCorreo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificaciones)

        findViewById<ImageView>(R.id.back).setOnClickListener { finish() }

        contenedorNotis = findViewById<LinearLayout>(R.id.contenedorNotis)
        miCorreo = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
            .getString("correo_usuario", "") ?: return

        cargarNotificaciones()

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.notificaciones)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun cargarNotificaciones() {
        val textoInicial = findViewById<TextView>(R.id.notificacionestxt)

        db.collection("chats")
            .whereArrayContains("participantes", miCorreo)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { resultado ->
                if (!resultado.isEmpty) {
                    textoInicial.visibility = View.GONE
                }

                for (doc in resultado) {
                    val chatId = doc.id
                    val participantes = doc.get("participantes") as List<String>
                    val otroCorreo = participantes.first { it != miCorreo }
                    val ultimoMensaje = doc.getString("ultimoMensaje") ?: ""
                    val propietario = doc.getString("propietario") ?: ""

                    val esTuArticulo = propietario == miCorreo
                    val estadoLeido = doc.getBoolean("leido_$miCorreo") ?: false

                    db.collection("usuarios").document(otroCorreo).get()
                        .addOnSuccessListener { userDoc ->
                            val nombre = userDoc.getString("nombre") ?: "Usuario"

                            val vista = LayoutInflater.from(this)
                                .inflate(R.layout.chat_notificacion, contenedorNotis, false)

                            val txt = vista.findViewById<TextView>(R.id.alguientxt)
                            val imageView5 = vista.findViewById<ImageView>(R.id.imageView5)

                            if (estadoLeido) {
                                txt.text = "Chat con $nombre"
                                imageView5.visibility = View.GONE
                            } else {
                                txt.text = obtenerMensajeNotificacion(esTuArticulo, nombre)
                            }

                            val btn = vista.findViewById<Button>(R.id.revisarnotificacion)
                            btn.setOnClickListener {
                                val intent = Intent(this, Chat::class.java).apply {
                                    putExtra("chatId", chatId)
                                    putExtra("correoDestino", otroCorreo)
                                    putExtra("nombreDestino", nombre)
                                }
                                startActivity(intent)

                                db.collection("chats").document(chatId)
                                    .update("leido_$miCorreo", true)

                                txt.text = "Chat con $nombre"
                                imageView5.visibility = View.GONE
                            }

                            contenedorNotis.addView(vista, 0)
                        }
                }
            }
    }

    private fun obtenerMensajeNotificacion(esTuArticulo: Boolean, nombreUsuario: String): String {
        return if (esTuArticulo) {
            "Tienes una notificación nueva, revisa esta conversación con $nombreUsuario sobre un artículo perdido."
        } else {
            "Tienes una notificación nueva, revisa esta conversación con $nombreUsuario sobre un artículo perdido."
        }
    }
}
