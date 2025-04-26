package com.example.legally

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet.Layout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class micuenta : AppCompatActivity() {
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

        val BtnCerrarSesion: View = findViewById(R.id.btncerrarsesion)
        BtnCerrarSesion.setOnClickListener {
            val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
            prefs.edit().clear().apply()

            val intent = Intent(this, InicioSesion::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val BtnCambiarDatos: LinearLayout = findViewById(R.id.btnmisdatos)
        BtnCambiarDatos.setOnClickListener {
            startActivity(Intent(this, MisDatos::class.java))
        }
    }
}