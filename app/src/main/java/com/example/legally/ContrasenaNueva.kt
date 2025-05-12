package com.example.legally

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.graphics.toColorInt

class ContrasenaNueva : AppCompatActivity() {

    private lateinit var inputNuevaContrasena: EditText
    private lateinit var inputConfirmarContrasena: EditText
    private lateinit var btnContinuar: Button
    private lateinit var correo: String
    private val db = FirebaseFirestore.getInstance()

    private fun mostrarDialogoExito() {
        val dialogView = layoutInflater.inflate(R.layout.restablecercontrasenaexitoso, null)
        val dialog = android.app.AlertDialog.Builder(this, R.style.TransparentDialog)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()

        val btnContinuar: Button = dialogView.findViewById(R.id.btnContinuar)
        btnContinuar.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, InicioSesion::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contrasenanueva)

        findViewById<ImageView>(R.id.back).setOnClickListener { finish() }

        correo = intent.getStringExtra("correo") ?: ""

        inputNuevaContrasena = findViewById(R.id.contraseñaInput2)
        inputConfirmarContrasena = findViewById(R.id.contraseñaInput3)
        btnContinuar = findViewById(R.id.btnRestablecer3)

        btnContinuar.isEnabled = false
        btnContinuar.setBackgroundColor("#EDEEEF".toColorInt())

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val nueva = inputNuevaContrasena.text.toString().trim()
                val confirmar = inputConfirmarContrasena.text.toString().trim()

                val camposLlenos = nueva.isNotEmpty() && confirmar.isNotEmpty()

                btnContinuar.isEnabled = camposLlenos
                btnContinuar.setBackgroundColor(
                    if (camposLlenos) "#30B0C7".toColorInt() else "#EDEEEF".toColorInt()
                )
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        inputNuevaContrasena.addTextChangedListener(textWatcher)
        inputConfirmarContrasena.addTextChangedListener(textWatcher)

        btnContinuar.setOnClickListener {
            val nuevaContrasena = inputNuevaContrasena.text.toString().trim()
            val confirmarContrasena = inputConfirmarContrasena.text.toString().trim()

            if (nuevaContrasena != confirmarContrasena) {
                inputConfirmarContrasena.setBackgroundResource(R.drawable.edittext_background_error)
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("usuarios")
                .whereEqualTo("correo", correo)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val docId = documents.documents[0].id

                        db.collection("usuarios")
                            .document(docId)
                            .update("contrasena", nuevaContrasena)
                            .addOnSuccessListener {
                                mostrarDialogoExito()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Correo no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error de conexión con Firestore", Toast.LENGTH_SHORT).show()
                }
        }
    }
}