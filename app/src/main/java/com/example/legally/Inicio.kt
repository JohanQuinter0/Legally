package com.example.legally

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Inicio : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)

        val btnNotificacion = findViewById<ImageView>(R.id.btnnotificacion)
        btnNotificacion.setOnClickListener {
            startActivity(Intent(this, notificaciones::class.java))
        }

        val btnUser = findViewById<ImageView>(R.id.btnuser)
        btnUser.setOnClickListener {
            startActivity(Intent(this, micuenta::class.java))
        }

        val registrarArticuloBtn = findViewById<Button>(R.id.btnregistrar)
        registrarArticuloBtn.setOnClickListener {
            startActivity(Intent(this, RegistroArticulo::class.java))
        }

        val misArtsBtn = findViewById<Button>(R.id.btnmisarts)
        misArtsBtn.setOnClickListener {
            startActivity(Intent(this, MisArticulos::class.java))
        }

        val verificarArtBtn = findViewById<Button>(R.id.btnverificar)
        verificarArtBtn.setOnClickListener {
            startActivity(Intent(this, VerificarArticulo::class.java))
        }

        val mapaBtn = findViewById<Button>(R.id.btnmapa)
        mapaBtn.setOnClickListener {
            startActivity(Intent(this, Mapa::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.inicio)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}