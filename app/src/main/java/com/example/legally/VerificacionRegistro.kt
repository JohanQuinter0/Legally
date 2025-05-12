package com.example.legally

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class VerificacionRegistro : AppCompatActivity() {

    private var correctOTP: String? = null
    private lateinit var correo: String
    private lateinit var nombre: String
    private lateinit var tipoDocumento: String
    private lateinit var documento: String
    private lateinit var telefono: String
    private lateinit var contrasena: String
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verificacionregistro)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }

        window.setBackgroundDrawableResource(R.color.white)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.confirmarRegistroOtp)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        obtenerDatosIntent()

        configurarVistas()

        val otpDigit1: EditText = findViewById(R.id.otpDigit5)
        val otpDigit2: EditText = findViewById(R.id.otpDigit6)
        val otpDigit3: EditText = findViewById(R.id.otpDigit7)
        val otpDigit4: EditText = findViewById(R.id.otpDigit8)
        setupOTPField(otpDigit1, otpDigit2)
        setupOTPField(otpDigit2, otpDigit3)
        setupOTPField(otpDigit3, otpDigit4)

    }

    private fun setupOTPField(current: EditText, next: EditText?) {
        current.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 1) {
                    next?.requestFocus()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun obtenerDatosIntent() {
        correctOTP = intent.getStringExtra("codigoOTP") ?: "0000"
        correo = intent.getStringExtra("correo") ?: ""
        nombre = intent.getStringExtra("nombre") ?: ""
        tipoDocumento = intent.getStringExtra("tipoDocumento") ?: ""
        documento = intent.getStringExtra("documento") ?: ""
        telefono = intent.getStringExtra("telefono") ?: ""
        contrasena = intent.getStringExtra("contrasena") ?: ""
    }

    private fun configurarVistas() {
        val btnConfirmar: Button = findViewById(R.id.btnConfirmarRegistro)
        val otp1: EditText = findViewById(R.id.otpDigit5)
        val otp2: EditText = findViewById(R.id.otpDigit6)
        val otp3: EditText = findViewById(R.id.otpDigit7)
        val otp4: EditText = findViewById(R.id.otpDigit8)
        val errorText: TextView = findViewById(R.id.errorMessage)

        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
        }

        btnConfirmar.setOnClickListener {
            val enteredOTP = otp1.text.toString() + otp2.text.toString() +
                    otp3.text.toString() + otp4.text.toString()

            if (enteredOTP == correctOTP) {
                errorText.visibility = TextView.GONE
                resetBorders(otp1, otp2, otp3, otp4)
                registrarUsuarioFirebase()
            } else {
                errorText.visibility = TextView.VISIBLE
                setErrorBorders(otp1, otp2, otp3, otp4)
            }
        }

        val overlayExito: CardView = findViewById(R.id.registroExitosoOverlay)
        overlayExito.findViewById<Button>(R.id.btnContinuar).setOnClickListener {
            startActivity(Intent(this, InicioSesion::class.java))
            finish()
        }
    }

    private fun registrarUsuarioFirebase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(correo, contrasena).await()

                if (authResult.user != null) {
                    val usuario = hashMapOf(
                        "nombre" to nombre,
                        "correo" to correo,
                        "tipo_documento" to tipoDocumento,
                        "documento" to documento,
                        "telefono" to telefono,
                        "contrasena" to contrasena,
                    )

                    db.collection("usuarios").document(correo)
                        .set(usuario)
                        .await()

                    withContext(Dispatchers.Main) {
                        mostrarRegistroExitoso()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@VerificacionRegistro,
                        "Error al registrar: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun setErrorBorders(vararg editTexts: EditText) {
        val errorDrawable = GradientDrawable().apply {
            setStroke(4, Color.RED)
            setColor(Color.WHITE)
            cornerRadius = 12f * resources.displayMetrics.density
        }
        editTexts.forEach { it.background = errorDrawable }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun resetBorders(vararg editTexts: EditText) {
        val normalDrawable = resources.getDrawable(R.drawable.otp_background, theme)
        editTexts.forEach { it.background = normalDrawable }
    }

    private fun mostrarRegistroExitoso() {
        val dialogView = layoutInflater.inflate(R.layout.registroexitoso, null)
        val dialog = android.app.AlertDialog.Builder(this, R.style.TransparentDialog)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()

        dialogView.findViewById<Button>(R.id.btnContinuar).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, InicioSesion::class.java))
            finish()
        }
    }
}