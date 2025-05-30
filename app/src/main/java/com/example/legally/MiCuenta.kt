package com.example.legally

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.edit

class MiCuenta : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_micuenta)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.micuenta)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<ImageView>(R.id.back).setOnClickListener { finish() }

        val btnCerrarSesion: View = findViewById(R.id.btncerrarsesion)
        btnCerrarSesion.setOnClickListener {
            val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
            prefs.edit { clear() }

            val intent = Intent(this, InicioSesion::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val btnCambiarDatos: LinearLayout = findViewById(R.id.btnmisdatos)
        btnCambiarDatos.setOnClickListener {
            startActivity(Intent(this, MisDatos::class.java))
        }

        findViewById<LinearLayout>(R.id.btncambiarcontrasena).setOnClickListener {
            startActivity(Intent(this, RestablecerContrasena::class.java))
        }
    }
}