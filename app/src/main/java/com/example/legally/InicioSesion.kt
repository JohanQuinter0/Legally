package com.example.legally

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class InicioSesion : AppCompatActivity() {

    private lateinit var correoInput: EditText
    private lateinit var contraseñaInput: EditText
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_iniciosesion)

        setupViews()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.InicioSesion)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupViews() {
        correoInput = findViewById(R.id.UsuarioInput)
        contraseñaInput = findViewById(R.id.ContraseñaInput)

        findViewById<ImageView>(R.id.back).setOnClickListener {
            startActivity(Intent(this, Intro::class.java))
            finish() }

        findViewById<Button>(R.id.Registrarme).setOnClickListener {
            startActivity(Intent(this, Registro::class.java))
            finish()
        }

        findViewById<TextView>(R.id.Olvidastecontraseña).setOnClickListener {
            startActivity(Intent(this, RestablecerContrasena::class.java))
        }

        findViewById<Button>(R.id.BtnIniciarSesion).setOnClickListener {
            iniciarSesion()
        }

        val overlayInicioSesionExito: CardView = findViewById(R.id.inicioSesionExitosoOverlay)
        overlayInicioSesionExito.findViewById<Button>(R.id.btnContinuar).setOnClickListener {
            startActivity(Intent(this, Inicio::class.java))
            finish()
        }
    }

    private fun iniciarSesion() {
        val correo = correoInput.text.toString().trim()
        val contraseña = contraseñaInput.text.toString().trim()

        if (!validarCampos(correo, contraseña)) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = db.collection("usuarios")
                    .whereEqualTo("correo", correo)
                    .whereEqualTo("contrasena", contraseña)
                    .get()
                    .await()

                withContext(Dispatchers.Main) {
                    if (!snapshot.isEmpty) {
                        guardarDatosUsuario(correo)
                        mostrarInicioSesionExitoso()
                    } else {
                        mostrarErrorAutenticacion()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("InicioSesion", "Error en Firestore", e)
                    mostrarErrorAutenticacion()
                }
            }
        }
    }

    private fun validarCampos(correo: String, contraseña: String): Boolean {
        var valido = true
        val errorMessage: TextView = findViewById(R.id.errorMessage)

        if (correo.isEmpty()) {
            correoInput.setBackgroundResource(R.drawable.edittext_background_error)
            valido = false
        } else {
            correoInput.setBackgroundResource(R.drawable.edittext_background)
        }

        if (contraseña.isEmpty()) {
            contraseñaInput.setBackgroundResource(R.drawable.edittext_background_error)
            valido = false
        } else {
            contraseñaInput.setBackgroundResource(R.drawable.edittext_background)
        }

        if (!valido) {
            errorMessage.visibility = TextView.VISIBLE
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
        } else {
            errorMessage.visibility = TextView.INVISIBLE
        }

        return valido
    }

    private fun guardarDatosUsuario(correo: String) {
        val prefs = getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("correo_usuario", correo).apply()
    }

    private fun mostrarInicioSesionExitoso() {
        val dialogView = layoutInflater.inflate(R.layout.inicio_sesion_exitoso, null)
        val dialog = android.app.AlertDialog.Builder(this, R.style.TransparentDialog)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()

        dialogView.findViewById<Button>(R.id.btnContinuar).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, Inicio::class.java))
            finish()
        }
    }

    private fun mostrarErrorAutenticacion() {
        val errorMessage: TextView = findViewById(R.id.errorMessage)
        errorMessage.visibility = TextView.VISIBLE
        Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
        contraseñaInput.setBackgroundResource(R.drawable.edittext_background_error)
    }
}