package com.example.legally

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView

class InformacionUsuario : AppCompatActivity() {

    private val db = Firebase.firestore
    private lateinit var imageView: CircleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_informacion_usuario)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.informacionUsuario)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.back).setOnClickListener { finish() }

        imageView = findViewById(R.id.imguser)
        val correoDestino = intent.getStringExtra("correoDestino") ?: return

        val btnReportarUser: LinearLayout = findViewById(R.id.btnReportarUser)
        btnReportarUser.setOnClickListener {
            val intent = Intent(this, ReportarUsuario::class.java)
            intent.putExtra("correo_reportado", correoDestino)
            startActivity(intent)
        }

        cargarImagenPerfil(correoDestino)

    }

    private fun cargarImagenPerfil(correoDestino: String) {
        val usuarioRef = db.collection("usuarios").document(correoDestino)
        usuarioRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val imagenUrl = document.getString("imagen_perfil")
                val nombreUsuario = document.getString("nombre") ?: "Usuario"

                findViewById<TextView>(R.id.nombre).text = nombreUsuario

                if (!imagenUrl.isNullOrEmpty() && imagenUrl.startsWith("data:image")) {
                    try {
                        GlideApp.with(this)
                            .load(imagenUrl)
                            .placeholder(R.drawable.userperfilimg)
                            .error(R.drawable.userperfilimg)
                            .into(imageView)
                    } catch (_: Exception) {
                        imageView.setImageResource(R.drawable.userperfilimg)
                    }
                } else if (!imagenUrl.isNullOrEmpty() && imagenUrl.startsWith("http")) {
                    try {
                        GlideApp.with(this)
                            .load(imagenUrl)
                            .placeholder(R.drawable.userperfilimg)
                            .error(R.drawable.userperfilimg)
                            .into(imageView)
                    } catch (_: Exception) {
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