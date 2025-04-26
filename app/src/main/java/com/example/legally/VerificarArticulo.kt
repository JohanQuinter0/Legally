package com.example.legally

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class VerificarArticulo : AppCompatActivity() {

    private lateinit var spinnerTipoArt: Spinner
    private lateinit var serialInput: EditText
    private lateinit var btnBuscar: Button
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verificar_articulo)

        inicializarVistas()
        configurarSpinner()
        configurarEventos()
    }

    private fun inicializarVistas() {

        findViewById<ImageView>(R.id.back).setOnClickListener { finish() }

        serialInput = findViewById(R.id.serialInput)
        btnBuscar = findViewById(R.id.btnBuscarArt)
        spinnerTipoArt = findViewById(R.id.spinner_tipo_art)
    }

    private fun configurarSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.tipo_articulo,
            R.layout.custom_spinner
        ).apply {
            setDropDownViewResource(R.layout.custom_spinner)
        }

        spinnerTipoArt.adapter = adapter
        spinnerTipoArt.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor("#2C2C2C".toColorInt())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun configurarEventos() {
        btnBuscar.setOnClickListener {
            validarYBuscarArticulo()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.verificar_articulo)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun validarYBuscarArticulo() {
        val serial = serialInput.text.toString().trim()

        if (serial.isEmpty()) {
            serialInput.setBackgroundResource(R.drawable.edittext_background_error)
            Toast.makeText(this, "Ingresa un serial", Toast.LENGTH_SHORT).show()
            return
        }

        verificarArticulo(serial)
    }

    private fun verificarArticulo(serial: String) {
        db.collection("articulos").document(serial).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    manejarArticuloEncontrado(doc, serial)
                } else {
                    mostrarOverlay(R.layout.articulo_no_encontrado)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al buscar artículo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun manejarArticuloEncontrado(doc: com.google.firebase.firestore.DocumentSnapshot, serial: String) {
        val estado = doc.getString("estado")?.lowercase() ?: "activo"
        when (estado) {
            "robado", "perdido" -> mostrarOverlay(R.layout.articulo_perdido, serial)
            else -> mostrarOverlay(R.layout.articulo_legally, serial)
        }
    }

    private fun mostrarOverlay(layoutResId: Int, serial: String? = null) {
        val view = LayoutInflater.from(this).inflate(layoutResId, null)
        val dialog = AlertDialog.Builder(this, R.style.TransparentDialog)
            .setView(view)
            .setCancelable(true)
            .create()

        dialog.show()

        configurarBotonesOverlay(view, serial, layoutResId, dialog)
    }

    private fun configurarBotonesOverlay(view: View, serial: String?, layoutResId: Int, dialog: AlertDialog) {
        view.findViewById<Button?>(R.id.btnContinuar)?.setOnClickListener {
            it.context.startActivity(Intent(this, VerificarArticulo::class.java))
        }

        view.findViewById<Button?>(R.id.btnVerArt)?.setOnClickListener {
            serial?.let { iniciarVistaArticulo(it, layoutResId == R.layout.articulo_perdido) }

            dialog.dismiss()
        }
    }


    private fun iniciarVistaArticulo(serial: String, esPerdido: Boolean) {
        Intent(this, ArtView::class.java).apply {
            putExtra("serial", serial)
            putExtra("modo_verificacion", true)
            putExtra("es_perdido", esPerdido)
            startActivity(this)
        }
    }
}