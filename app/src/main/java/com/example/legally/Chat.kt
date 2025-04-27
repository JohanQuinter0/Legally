package com.example.legally

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import android.content.Context
import android.content.Intent
import de.hdodenhof.circleimageview.CircleImageView

class Chat : AppCompatActivity() {

    private val db = Firebase.firestore
    private lateinit var chatId: String
    private lateinit var miCorreo: String
    private lateinit var otroCorreo: String
    private lateinit var otroNombre: String

    private lateinit var mensajeInput: EditText
    private lateinit var btnEnviar: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MensajeAdapter
    private lateinit var imageView: CircleImageView
    private val listaMensajes = mutableListOf<Mensaje>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chat)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.userIcon).setOnClickListener {
            val intent = Intent(this, informacionUsuario::class.java)
            intent.putExtra("correoDestino", otroCorreo)
            startActivity(intent)
        }

        chatId = intent.getStringExtra("chatId") ?: return
        val correoDestino = intent.getStringExtra("correoDestino") ?: return
        val nombreDestino = intent.getStringExtra("nombreDestino") ?: "Usuario"
        val correoActual = getSharedPreferences("usuario_prefs", MODE_PRIVATE).getString("correo_usuario", "") ?: return

        findViewById<TextView>(R.id.userTitle).text = nombreDestino

        miCorreo = correoActual
        otroCorreo = correoDestino
        otroNombre = nombreDestino
        chatId = generarChatId(miCorreo, otroCorreo)

        findViewById<TextView>(R.id.userTitle).text = otroNombre
        findViewById<ImageView>(R.id.back).setOnClickListener { finish() }

        mensajeInput = findViewById(R.id.inputMensaje)
        btnEnviar = findViewById(R.id.btnEnviar)
        recyclerView = findViewById(R.id.recyclerMensajes)
        imageView = findViewById(R.id.userIcon)

        adapter = MensajeAdapter(listaMensajes, miCorreo)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnEnviar.setOnClickListener { enviarMensaje() }

        cargarMensajes()

        cargarImagenPerfil(correoDestino)
    }

    private fun cargarMensajes() {
        db.collection("chats").document(chatId)
            .collection("mensajes")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener

                listaMensajes.clear()
                for (doc in snapshots) {
                    val mensaje = doc.toObject(Mensaje::class.java)
                    listaMensajes.add(mensaje)
                }

                adapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(listaMensajes.size - 1)
            }
    }

    private fun enviarMensaje() {
        val texto = mensajeInput.text.toString().trim()
        if (texto.isEmpty()) return

        val mensaje = hashMapOf(
            "emisor" to miCorreo,
            "texto" to texto,
            "timestamp" to Timestamp.now()
        )

        db.collection("chats").document(chatId)
            .collection("mensajes")
            .add(mensaje)
            .addOnSuccessListener {
                mensajeInput.setText("")
            }

        db.collection("chats").document(chatId).set(
            mapOf(
                "participantes" to listOf(miCorreo, otroCorreo),
                "ultimoMensaje" to texto,
                "timestamp" to Timestamp.now()
            )
        )
    }

    private fun generarChatId(correo1: String, correo2: String): String {
        return listOf(correo1, correo2).sorted().joinToString("_")
    }

    private fun cargarImagenPerfil(correoDestino: String) {
        val usuarioRef = db.collection("usuarios").document(correoDestino)
        usuarioRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val imagenUrl = document.getString("imagen_perfil")

                if (!imagenUrl.isNullOrEmpty() && imagenUrl.startsWith("data:image")) {
                    try {
                        informacionUsuario.GlideApp.with(this)
                            .load(imagenUrl)
                            .placeholder(R.drawable.userperfilimg)
                            .error(R.drawable.userperfilimg)
                            .into(imageView)
                    } catch (e: Exception) {
                        imageView.setImageResource(R.drawable.userperfilimg)
                    }
                }
                else if (!imagenUrl.isNullOrEmpty() && imagenUrl.startsWith("http")) {
                    try {
                        informacionUsuario.GlideApp.with(this)
                            .load(imagenUrl)
                            .placeholder(R.drawable.userperfilimg)
                            .error(R.drawable.userperfilimg)
                            .into(imageView)
                    } catch (e: Exception) {
                        imageView.setImageResource(R.drawable.userperfilimg)
                    }
                } else {
                    imageView.setImageResource(R.drawable.userperfilimg)
                }
            } else {
                imageView.setImageResource(R.drawable.userperfilimg)
            }
        }.addOnFailureListener {
            imageView.setImageResource(R.drawable.userperfilimg)
        }
    }

    class GlideApp private constructor() {
        companion object {
            fun with(context: Context): RequestManager {
                return Glide.with(context)
            }
        }
    }

}
