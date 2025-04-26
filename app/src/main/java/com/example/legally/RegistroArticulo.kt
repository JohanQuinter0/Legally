package com.example.legally

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.Date

class RegistroArticulo : AppCompatActivity() {

    private lateinit var spinnerTipoArt: Spinner
    private val db = Firebase.firestore
    private var imagenUri: Uri? = null
    private lateinit var serialArticulo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_articulo)

        findViewById<ImageView>(R.id.back).setOnClickListener { finish() }

        configurarSpinner()

        findViewById<Button>(R.id.btnRegistrarArt).setOnClickListener {
            registrarArticulo()
        }

        findViewById<LinearLayout>(R.id.btnadjuntarfotoart).setOnClickListener {
            seleccionarImagen()
        }

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registroArticulo)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun configurarSpinner() {
        spinnerTipoArt = findViewById(R.id.spinner_tipo_art)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.tipo_articulo,
            R.layout.custom_spinner
        )
        adapter.setDropDownViewResource(R.layout.custom_spinner)
        spinnerTipoArt.adapter = adapter

        spinnerTipoArt.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor("#2C2C2C".toColorInt())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        launcherGaleria.launch(intent)
    }

    private val launcherGaleria = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            imagenUri = result.data!!.data
            Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun convertirImagenABase64(uri: Uri): String {
        val bitmap = withContext(Dispatchers.IO) {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        val byteArray = outputStream.toByteArray()

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun registrarArticulo() {
        val nombre = findViewById<TextView>(R.id.nombreArticuloInput).text.toString().trim()
        val marca = findViewById<TextView>(R.id.marcaInput).text.toString().trim()
        serialArticulo = findViewById<TextView>(R.id.serialInput).text.toString().trim()
        val descripcion = findViewById<TextView>(R.id.detallesInput).text.toString().trim()
        val tipo = spinnerTipoArt.selectedItem.toString()

        val prefs = getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE)
        val correoUsuario = prefs.getString("correo_usuario", "") ?: ""

        if (correoUsuario.isEmpty() || nombre.isEmpty() || marca.isEmpty() || serialArticulo.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val docRef = db.collection("articulos").document(serialArticulo)
                val docSnapshot = docRef.get().await()

                if (docSnapshot.exists()) {
                    val dueño = docSnapshot.getString("id_usuario")
                    if (dueño != correoUsuario) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@RegistroArticulo,
                                "Este artículo ya está registrado por otro usuario",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@launch
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@RegistroArticulo,
                                "Ya registraste este artículo",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return@launch
                    }
                }

                val base64 = if (imagenUri != null) convertirImagenABase64(imagenUri!!) else null

                val articulo = hashMapOf(
                    "serial" to serialArticulo,
                    "id_usuario" to correoUsuario,
                    "nombre_articulo" to nombre,
                    "marca_articulo" to marca,
                    "descripcion_articulo" to descripcion,
                    "tipo_articulo" to tipo,
                    "estado" to "activo",
                    "fecha_registro" to Date(),
                    "imagen_base64" to base64
                )

                docRef.set(articulo).await()

                withContext(Dispatchers.Main) {
                    mostrarExitoRegistro()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegistroArticulo, "Error al registrar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarExitoRegistro() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.registro_art_exitoso, null)
        val dialog = AlertDialog.Builder(this, R.style.TransparentDialog)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btnVerMisArts).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, MisArticulos::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        dialogView.findViewById<Button>(R.id.btnreportar).setOnClickListener {
            dialog.dismiss()
            mostrarConfirmacionReportar()
        }

        dialog.show()
    }

    private fun mostrarConfirmacionReportar() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.estas_seguro_reportar, null)
        val dialog = AlertDialog.Builder(this, R.style.TransparentDialog)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialogView.findViewById<Button>(R.id.btnseguroreportar).setOnClickListener {
            dialog.dismiss()
            reportarArticulo()
        }

        dialogView.findViewById<Button>(R.id.btnCancelar).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, MisArticulos::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        dialog.show()
    }

    private fun reportarArticulo() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.collection("articulos").document(serialArticulo)
                    .update("estado", "perdido").await()

                withContext(Dispatchers.Main) {
                    mostrarExitoReporte()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegistroArticulo, "Error al reportar el artículo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mostrarExitoReporte() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.reportar_exitoso, null)
        val dialog = AlertDialog.Builder(this, R.style.TransparentDialog)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btnEntendido).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, MisArticulos::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        dialog.show()
    }
}