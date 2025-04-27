package com.example.legally

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class codigoVerificacion : AppCompatActivity() {

    private var correctOTP: String? = null
    private lateinit var correo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_codigoverificacion)

        findViewById<ImageView>(R.id.back).setOnClickListener { finish() }

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.digito1)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnContinuar: Button = findViewById(R.id.btnConfirmarRegistro)
        val otpDigit1: EditText = findViewById(R.id.otpDigit5)
        val otpDigit2: EditText = findViewById(R.id.otpDigit6)
        val otpDigit3: EditText = findViewById(R.id.otpDigit7)
        val otpDigit4: EditText = findViewById(R.id.otpDigit8)
        val errorMessage: TextView = findViewById(R.id.errorMessage)

        setupOTPField(otpDigit1, otpDigit2)
        setupOTPField(otpDigit2, otpDigit3)
        setupOTPField(otpDigit3, otpDigit4)

        correctOTP = intent.getStringExtra("codigoOTP")
        correo = intent.getStringExtra("correo") ?: ""

        btnContinuar.setOnClickListener {
            val enteredOTP = otpDigit1.text.toString() + otpDigit2.text.toString() +
                    otpDigit3.text.toString() + otpDigit4.text.toString()

            if (enteredOTP == correctOTP) {
                errorMessage.visibility = TextView.GONE
                resetBorders(otpDigit1, otpDigit2, otpDigit3, otpDigit4)

                val intent = Intent(this, contrasenaNueva::class.java)
                intent.putExtra("correo", correo)
                startActivity(intent)
                finish()
            } else {
                errorMessage.visibility = TextView.VISIBLE
                setErrorBorders(otpDigit1, otpDigit2, otpDigit3, otpDigit4)
            }
        }
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

    private fun setErrorBorders(vararg editTexts: EditText) {
        val errorDrawable = GradientDrawable().apply {
            setStroke(4, Color.RED)
            setColor(Color.WHITE)
            cornerRadius = 12f * Resources.getSystem().displayMetrics.density
        }
        editTexts.forEach { it.background = errorDrawable }
    }

    private fun resetBorders(vararg editTexts: EditText) {
        val normalDrawable = GradientDrawable().apply {
            setStroke(4, "#EDEEEF".toColorInt())
            setColor(Color.WHITE)
        }
        editTexts.forEach { it.background = normalDrawable }
    }
}