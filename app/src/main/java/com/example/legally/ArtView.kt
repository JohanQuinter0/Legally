package com.example.legally

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.core.net.toUri

class ArtView : AppCompatActivity() {

    private val db = Firebase.firestore
    private var esModoVerificacion = false
    private var esArticuloPerdido = false
    private lateinit var serialArticulo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_art_view)

        serialArticulo = intent.getStringExtra("serial") ?: run {
            Toast.makeText(this, "Error: No se especificó artículo", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        esModoVerificacion = intent.getBooleanExtra("modo_verificacion", false)
        esArticuloPerdido = intent.getBooleanExtra("es_perdido", false)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.back).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnEncontrasteArt).setOnClickListener { mostrarOverlayConfirmacionEncontrado() }

        findViewById<Button>(R.id.btnReportarArt).setOnClickListener { mostrarConfirmacionReportar() }
        findViewById<TextView>(R.id.arteliminarcard).setOnClickListener { mostrarConfirmacionEliminar() }

        configurarVistaSegunEstado()
        cargarDatosArticulo(serialArticulo)
    }

    private fun configurarVistaSegunEstado() {
        val btnEliminar = findViewById<TextView>(R.id.arteliminarcard)
        val btnReportar = findViewById<Button>(R.id.btnReportarArt)
        val btnPolicia = findViewById<Button>(R.id.btnContactarPolicia)
        val btnPropietario = findViewById<Button>(R.id.btnContactarPropietario)
        val txtEscogerUbicacion = findViewById<TextView>(R.id.artescogerubicacion)
        val btnEncontrasteArt = findViewById<Button>(R.id.btnEncontrasteArt)

        db.collection("articulos").document(serialArticulo).get()
            .addOnSuccessListener { doc ->
                val estado = doc.getString("estado") ?: "activo"
                val propietario = doc.getString("id_usuario") ?: ""

                val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
                val correoActual = prefs.getString("correo_usuario", null)

                val esArticuloDeMiPropiedad = correoActual != null && correoActual == propietario

                if (esModoVerificacion) {
                    btnEliminar.visibility = View.GONE
                    btnReportar.visibility = View.GONE

                    if (estado == "perdido") {
                        configurarVistaArticuloPerdido()
                        if (!esArticuloDeMiPropiedad) {
                            btnPolicia.visibility = View.VISIBLE
                            btnPropietario.visibility = View.VISIBLE
                        }
                    }
                } else {
                    if (estado == "perdido") {
                        btnEliminar.visibility = View.GONE
                        btnReportar.visibility = View.GONE
                        txtEscogerUbicacion.visibility = View.VISIBLE
                        btnEncontrasteArt.visibility = View.VISIBLE
                    } else {
                        btnEliminar.visibility = View.VISIBLE
                        btnReportar.visibility = View.VISIBLE
                        txtEscogerUbicacion.visibility = View.GONE
                        btnEncontrasteArt.visibility = View.GONE
                    }

                    btnPolicia.visibility = View.GONE
                    btnPropietario.visibility = View.GONE

                    if (estado == "perdido" && !esArticuloDeMiPropiedad) {
                        btnPolicia.visibility = View.VISIBLE
                        btnPropietario.visibility = View.VISIBLE
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener estado del artículo", Toast.LENGTH_SHORT).show()
            }

        txtEscogerUbicacion.setOnClickListener {
            val intent = Intent(this, Mapa::class.java)
            intent.putExtra("SELECT_LOCATION", true)
            intent.putExtra("ARTICULO_ID", serialArticulo)
            startActivity(intent)
        }
    }

    private fun configurarVistaArticuloPerdido() {
        val rojo = ContextCompat.getColor(this, R.color.red)
        findViewById<TextView>(R.id.nombre_articulo_card).setTextColor(rojo)
        findViewById<TextView>(R.id.tipoartcard).setTextColor(rojo)
        findViewById<TextView>(R.id.misArtsTitle).apply {
            text = context.getString(R.string.articuloPerdido)
            setTextColor(rojo)
        }
        findViewById<ImageView>(R.id.back).setColorFilter(rojo)
        findViewById<ImageView>(R.id.iconMisArts).setColorFilter(rojo)

        findViewById<Button>(R.id.btnContactarPolicia).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = "tel:123".toUri()
            }
            startActivity(intent)
        }


        findViewById<Button>(R.id.btnContactarPropietario).setOnClickListener {
            contactarAlPropietario()
        }
    }

    private fun contactarAlPropietario() {
        db.collection("articulos").document(serialArticulo).get()
            .addOnSuccessListener { doc ->
                val propietario = doc.getString("id_usuario") ?: return@addOnSuccessListener
                val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
                val correoActual = prefs.getString("correo_usuario", null)
                if (correoActual != null) {
                    val chatId = listOf(correoActual, propietario).sorted().joinToString("_")

                    db.collection("usuarios").document(propietario).get()
                        .addOnSuccessListener { userDoc ->
                            val nombrePropietario = userDoc.getString("nombre") ?: "Usuario"

                            val intent = Intent(this, Chat::class.java).apply {
                                putExtra("chatId", chatId)
                                putExtra("correoDestino", propietario)
                                putExtra("nombreDestino", nombrePropietario)
                            }
                            startActivity(intent)
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al contactar al propietario", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarConfirmacionReportar() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.estas_seguro_reportar, null)
        val dialog = AlertDialog.Builder(this, R.style.TransparentDialog).setView(dialogView).create()

        dialogView.findViewById<Button>(R.id.btnseguroreportar).setOnClickListener {
            dialog.dismiss()
            reportarArticulo()
        }

        dialogView.findViewById<Button>(R.id.btnCancelar).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun reportarArticulo() {
        db.collection("articulos").document(serialArticulo).update("estado", "perdido")
            .addOnSuccessListener {
                mostrarExitoReporte()
                cargarDatosArticulo(serialArticulo)
                configurarVistaSegunEstado()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al reportar el artículo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarExitoReporte() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.reportar_exitoso, null)
        val dialog = AlertDialog.Builder(this, R.style.TransparentDialog).setView(dialogView).create()

        dialogView.findViewById<Button>(R.id.btnEntendido).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun mostrarConfirmacionEliminar() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.estas_seguro_eliminar, null)
        val dialog = AlertDialog.Builder(this, R.style.TransparentDialog).setView(dialogView).create()

        dialogView.findViewById<Button>(R.id.btnseguroeliminar).setOnClickListener {
            dialog.dismiss()
            eliminarArticulo()
        }

        dialogView.findViewById<Button>(R.id.btnCancelar).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun eliminarArticulo() {
        db.collection("articulos").document(serialArticulo).delete()
            .addOnSuccessListener { mostrarExitoEliminacion() }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar el artículo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarExitoEliminacion() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.eliminar_exitoso, null)
        val dialog = AlertDialog.Builder(this, R.style.TransparentDialog).setView(dialogView).create()

        dialogView.findViewById<Button>(R.id.btnregistrarnuevamente).setOnClickListener {
            startActivity(Intent(this, RegistroArticulo::class.java))
        }
        dialogView.findViewById<Button>(R.id.btnEntendido).setOnClickListener {
            val intent = Intent(this, MisArticulos::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
            finish()
        }

        dialog.show()
    }

    private fun mostrarOverlayConfirmacionEncontrado() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.estas_seguro_encontraste, null)
        val dialog = AlertDialog.Builder(this, R.style.TransparentDialog).setView(dialogView).create()

        dialogView.findViewById<Button>(R.id.btnseguroencontre).setOnClickListener {
            dialog.dismiss()
            marcarArticuloComoEncontrado()
        }

        dialogView.findViewById<Button>(R.id.btnCancelar).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun marcarArticuloComoEncontrado() {
        db.collection("articulos").document(serialArticulo)
            .update(mapOf("estado" to "activo", "ubicacionPerdida" to null))
            .addOnSuccessListener { mostrarOverlayFelicidadesEncontrado() }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar el estado", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarOverlayFelicidadesEncontrado() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.felicidades_encontraste, null)
        val dialog = AlertDialog.Builder(this, R.style.TransparentDialog).setView(dialogView).create()

        dialogView.findViewById<Button>(R.id.btnEntendido).setOnClickListener {
            dialog.dismiss()
            recreate()
        }

        dialog.show()
    }

    private fun cargarDatosArticulo(serial: String) {
        db.collection("articulos").document(serial).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val nombre = doc.getString("nombre_articulo") ?: ""
                    val tipo = doc.getString("tipo_articulo") ?: ""
                    val descripcion = doc.getString("descripcion_articulo") ?: ""
                    val estado = doc.getString("estado") ?: "activo"
                    val fecha = doc.getDate("fecha_registro")?.let {
                        android.text.format.DateFormat.getDateFormat(this).format(it)
                    } ?: "Sin fecha"
                    val marca = doc.getString("marca_articulo") ?: ""
                    val urlImagen = doc.getString("imagen_base64")

                    findViewById<TextView>(R.id.nombre_articulo_card).text =
                        getString(R.string.marcaNombre, marca, nombre)
                    findViewById<TextView>(R.id.tipoartcard).text = getString(R.string.tipo, tipo)
                    findViewById<TextView>(R.id.artserialcard).text = HtmlCompat.fromHtml("<b>S/N:</b> $serial", HtmlCompat.FROM_HTML_MODE_LEGACY)
                    findViewById<TextView>(R.id.artdetallescard).text = HtmlCompat.fromHtml("<b>Detalles:</b><br>$descripcion", HtmlCompat.FROM_HTML_MODE_LEGACY)
                    findViewById<TextView>(R.id.artregistradocard).text = HtmlCompat.fromHtml("<b>Marca:</b> $marca", HtmlCompat.FROM_HTML_MODE_LEGACY)
                    findViewById<TextView>(R.id.artestadocard).text = HtmlCompat.fromHtml("<b>Estado:</b> ${estado.capitalize()}", HtmlCompat.FROM_HTML_MODE_LEGACY)
                    findViewById<TextView>(R.id.artfechacard).text = HtmlCompat.fromHtml("<b>Fecha registro:</b> $fecha", HtmlCompat.FROM_HTML_MODE_LEGACY)

                    val ubicacion = doc.get("ubicacionPerdida") as? Map<*, *>
                    val direccion = ubicacion?.get("direccion") as? String
                    val txtUbicacion = findViewById<TextView>(R.id.txtUbicacionPerdida)

                    if (!direccion.isNullOrEmpty()) {
                        txtUbicacion.visibility = View.VISIBLE
                        txtUbicacion.text = HtmlCompat.fromHtml(
                            "<b>Ubicación de pérdida aproximada:</b> $direccion",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    } else {
                        txtUbicacion.visibility = View.GONE
                    }

                    val imageView = findViewById<ImageView>(R.id.imgartregistrado)
                    if (!urlImagen.isNullOrEmpty()) {
                        try {
                            val decoded = android.util.Base64.decode(urlImagen, android.util.Base64.DEFAULT)
                            val bitmap = android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                            imageView.setImageBitmap(bitmap)
                        } catch (_: Exception) {
                            imageView.setImageResource(R.drawable.placeholder_articulo)
                        }
                    } else {
                        imageView.setImageResource(R.drawable.placeholder_articulo)
                    }

                } else {
                    Toast.makeText(this, "Artículo no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    override fun onResume() {
        super.onResume()
        cargarDatosArticulo(serialArticulo)
    }

    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}