package com.example.legally

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Registro : AppCompatActivity() {

    private lateinit var nombreInput: EditText
    private lateinit var correoInput: EditText
    private lateinit var documentoInput: EditText
    private lateinit var telefonoInput: EditText
    private lateinit var contrasenaInput: EditText
    private lateinit var confirmarContrasenaInput: EditText
    private lateinit var spinnerTipoDoc: Spinner

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        initViews()
        setupSpinner()
        setupWindowInsets()
    }

    private fun initViews() {
        nombreInput = findViewById(R.id.nombreInput)
        correoInput = findViewById(R.id.correoInput)
        documentoInput = findViewById(R.id.documentoInput)
        telefonoInput = findViewById(R.id.telefonoInput)
        contrasenaInput = findViewById(R.id.contrasenaInput)
        confirmarContrasenaInput = findViewById(R.id.confirmarContrasenaInput)
        spinnerTipoDoc = findViewById(R.id.spinner_tipo_doc)

        findViewById<ImageView>(R.id.back).setOnClickListener {
            navigateTo(Intro::class.java)
            finish() }

        findViewById<Button>(R.id.IniciarSesion).setOnClickListener {
            navigateTo(InicioSesion::class.java)
            finish()
        }

        findViewById<Button>(R.id.btnEmpezar).setOnClickListener {
            verificarDatosYEnviarOTP()
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.tipo_documento,
            R.layout.custom_spinner
        )
        adapter.setDropDownViewResource(R.layout.custom_spinner)
        spinnerTipoDoc.adapter = adapter

        spinnerTipoDoc.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(
                    if (position == 0) "#2C2C2C".toColorInt() else "#2C2C2C".toColorInt()
                )
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registro)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun verificarDatosYEnviarOTP() {
        val nombre = nombreInput.text.toString().trim()
        val correo = correoInput.text.toString().trim()
        val documento = documentoInput.text.toString().trim()
        val telefono = telefonoInput.text.toString().trim()
        val contrasena = contrasenaInput.text.toString().trim()
        val confirmarContrasena = confirmarContrasenaInput.text.toString().trim()
        val tipoDocumento = spinnerTipoDoc.selectedItem.toString()

        if (!validarCampos(nombre, correo, documento, telefono, contrasena, confirmarContrasena, tipoDocumento)) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = db.collection("usuarios")
                    .whereEqualTo("correo", correo)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    withContext(Dispatchers.Main) {
                        mostrarError("El correo ya está registrado", correoInput)
                    }
                    return@launch
                }

                val codigoOTP = MailSender.enviarCorreo(correo)

                withContext(Dispatchers.Main) {
                    if (codigoOTP != null) {
                        Toast.makeText(this@Registro, "Código enviado a $correo", Toast.LENGTH_LONG).show()
                        navegarAVerificacion(correo, nombre, tipoDocumento, documento, telefono, contrasena, codigoOTP)
                    } else {
                        Toast.makeText(this@Registro, "Error al enviar correo", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Registro, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validarCampos(
        nombre: String,
        correo: String,
        documento: String,
        telefono: String,
        contrasena: String,
        confirmarContrasena: String,
        tipoDocumento: String
    ): Boolean {
        resetearBordes()

        var valido = true

        if (nombre.isEmpty()) {
            mostrarError("Campo obligatorio", nombreInput)
            valido = false
        }

        if (correo.isEmpty()) {
            mostrarError("Campo obligatorio", correoInput)
            valido = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            mostrarError("Correo inválido", correoInput)
            valido = false
        }

        if (tipoDocumento == "Seleccione su tipo de documento" || spinnerTipoDoc.selectedItemPosition == 0) {
            Toast.makeText(this, "Seleccione un tipo de documento", Toast.LENGTH_SHORT).show()
            valido = false
        }

        if (documento.isEmpty()) {
            mostrarError("Campo obligatorio", documentoInput)
            valido = false
        }

        if (telefono.isEmpty()) {
            mostrarError("Campo obligatorio", telefonoInput)
            valido = false
        }

        if (contrasena.isEmpty()) {
            mostrarError("Campo obligatorio", contrasenaInput)
            valido = false
        } else if (contrasena.length < 6) {
            mostrarError("Mínimo 6 caracteres", contrasenaInput)
            valido = false
        }

        if (confirmarContrasena.isEmpty()) {
            mostrarError("Campo obligatorio", confirmarContrasenaInput)
            valido = false
        } else if (contrasena != confirmarContrasena) {
            mostrarError("Las contrasenas no coinciden", confirmarContrasenaInput)
            valido = false
        }

        if (!valido) {
            Toast.makeText(this, "Revise los campos marcados", Toast.LENGTH_SHORT).show()
        }

        return valido
    }

    private fun navegarAVerificacion(
        correo: String,
        nombre: String,
        tipoDocumento: String,
        documento: String,
        telefono: String,
        contrasena: String,
        codigoOTP: String
    ) {
        Intent(this, VerificacionRegistro::class.java).apply {
            putExtra("correo", correo)
            putExtra("nombre", nombre)
            putExtra("tipoDocumento", tipoDocumento)
            putExtra("documento", documento)
            putExtra("telefono", telefono)
            putExtra("contrasena", contrasena)
            putExtra("codigoOTP", codigoOTP)
            startActivity(this)
        }
    }

    private fun mostrarError(mensaje: String, input: EditText) {
        input.setBackgroundResource(R.drawable.edittext_background_error)
        input.error = mensaje
    }

    private fun resetearBordes() {
        listOf(nombreInput, correoInput, documentoInput, telefonoInput, contrasenaInput, confirmarContrasenaInput).forEach {
            it.setBackgroundResource(R.drawable.edittext_background)
            it.error = null
        }
    }

    private fun navigateTo(destination: Class<*>) {
        startActivity(Intent(this, destination))
    }
}