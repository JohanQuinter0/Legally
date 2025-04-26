package com.example.legally

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RestablecerContrasena : AppCompatActivity() {
    private lateinit var correoInput: EditText
    private lateinit var btnRestablecer: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restablecercontrasena)

        findViewById<ImageView>(R.id.back).setOnClickListener { finish() }

        correoInput = findViewById(R.id.restablecerCorreoInput)
        btnRestablecer = findViewById(R.id.btnRestablecer)
        db = FirebaseFirestore.getInstance()

        btnRestablecer.setOnClickListener {
            validarCorreo()
        }
    }

    private fun validarCorreo() {
        val correo = correoInput.text.toString().trim()

        if (correo.isEmpty()) {
            correoInput.setBackgroundResource(R.drawable.edittext_background_error)
            findViewById<TextView>(R.id.errorMessage1).visibility = TextView.VISIBLE
            findViewById<TextView>(R.id.errorMessage).visibility = TextView.INVISIBLE
            Toast.makeText(this, "Ingresa un correo", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            correoInput.setBackgroundResource(R.drawable.edittext_background_error)
            findViewById<TextView>(R.id.errorMessage1).visibility = TextView.INVISIBLE
            findViewById<TextView>(R.id.errorMessage).visibility = TextView.VISIBLE
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = db.collection("usuarios")
                    .whereEqualTo("correo", correo)
                    .get()
                    .await()

                withContext(Dispatchers.Main) {
                    if (!querySnapshot.isEmpty) {
                        enviarCodigoPorCorreo(correo)
                    } else {
                        Toast.makeText(applicationContext, "El correo no está registrado", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Error al verificar el correo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun enviarCodigoPorCorreo(correo: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val codigoOTP = MailSender.enviarCorreo(correo)
            withContext(Dispatchers.Main) {
                if (codigoOTP != null) {
                    Toast.makeText(applicationContext, "Código enviado a $correo", Toast.LENGTH_LONG).show()

                    val intent = Intent(applicationContext, codigoVerificacion::class.java)
                    intent.putExtra("correo", correo)
                    intent.putExtra("codigoOTP", codigoOTP)
                    startActivity(intent)
                } else {
                    Toast.makeText(applicationContext, "Error al enviar correo", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}