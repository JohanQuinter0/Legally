package com.example.legally

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ReportarUsuario : AppCompatActivity() {

    private val db = Firebase.firestore
    private var motivoSeleccionado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportar_usuario)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.back).setOnClickListener { finish() }

        val motivos = listOf(
            findViewById<TextView>(R.id.dejoresponder),
            findViewById<TextView>(R.id.estafa),
            findViewById<TextView>(R.id.bullying),
            findViewById<TextView>(R.id.falsoreporte)
        )

        motivos.forEach { motivoTextView ->
            motivoTextView.setOnClickListener {
                // Resetear estilos
                motivos.forEach {
                    it.setTextColor(ContextCompat.getColor(this, R.color.almost_black))
                    it.paintFlags = it.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
                }

                // Aplicar estilo seleccionado
                motivoTextView.setTextColor(ContextCompat.getColor(this, R.color.principal))
                motivoTextView.paintFlags = motivoTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                motivoSeleccionado = motivoTextView.text.toString()
            }
        }

        findViewById<Button>(R.id.btnEnviarReporte).setOnClickListener {
            enviarReporte()
        }
    }

    private fun enviarReporte() {
        val motivo = motivoSeleccionado
        if (motivo == null) {
            Toast.makeText(this, "Selecciona un motivo", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val correoReportante = prefs.getString("correo_usuario", "") ?: ""
        val correoReportado = intent.getStringExtra("correo_reportado") ?: ""

        if (correoReportante.isEmpty()) {
            Toast.makeText(this, "No se identificó al usuario", Toast.LENGTH_SHORT).show()
            return
        }

        val reporte = hashMapOf(
            "reportante" to correoReportante,
            "reportado" to correoReportado,
            "motivo" to motivo,
            "fecha" to Timestamp.now()
        )

        db.collection("reportes")
            .add(reporte)
            .addOnSuccessListener {
                val intent = Intent(this, ReporteUsuarioExitoso::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al enviar reporte", Toast.LENGTH_SHORT).show()
            }
    }
}