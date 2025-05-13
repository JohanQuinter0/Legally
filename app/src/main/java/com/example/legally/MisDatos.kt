package com.example.legally

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale
import com.google.android.material.textfield.TextInputEditText

class MisDatos : AppCompatActivity() {

    private var imagenUri: Uri? = null
    private val db = Firebase.firestore
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var nombreInput: TextInputEditText
    private lateinit var correoInput: TextInputEditText
    private lateinit var telefonoInput: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_datos)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }

        window.setBackgroundDrawableResource(R.color.white)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.misDatos)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nombreInput = findViewById(R.id.nuevonombreInput)
        telefonoInput = findViewById(R.id.nuevotelefonoInput)

        findViewById<ImageView>(R.id.siguiente).setOnClickListener {
            actualizarDatos()
        }

        cargarDatosUsuario()

        findViewById<ImageView>(R.id.back).setOnClickListener { finish() }

        val btnCambiarPerfilImg: TextView = findViewById(R.id.btncambiarperfilimg)
        btnCambiarPerfilImg.setOnClickListener {
            seleccionarImagen()
        }

        cargarImagenDeFirebase()
    }

    @SuppressLint("IntentReset")
    private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        launcherGaleria.launch(intent)
    }

    private val launcherGaleria = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            imagenUri = result.data!!.data
            val imageView: CircleImageView = findViewById(R.id.userperfilimg)

            imageView.setImageURI(imagenUri)

            Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show()

            imagenUri?.let { uri ->
                scope.launch {
                    try {
                        val base64 = withContext(Dispatchers.IO) {
                            convertirImagenABase64(uri)
                        }
                        guardarImagenEnFirebase(base64)
                    } catch (e: Exception) {
                        Toast.makeText(this@MisDatos, "Error al procesar imagen: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun convertirImagenABase64(uri: Uri): String {
        @Suppress("DEPRECATION") val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val scaledBitmap = bitmap.scale(400, 400)

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()

        return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun guardarImagenEnFirebase(base64: String) {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val correoUsuario = prefs.getString("correo_usuario", "") ?: ""

        if (correoUsuario.isEmpty()) {
            Toast.makeText(this, "No se encontró el usuario", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            try {
                val docRef = db.collection("usuarios").document(correoUsuario)
                docRef.update("imagen_perfil", base64).await()
                Toast.makeText(this@MisDatos, "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MisDatos, "Error al guardar la imagen: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun cargarImagenDeFirebase() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val correoUsuario = prefs.getString("correo_usuario", "") ?: ""

        if (correoUsuario.isEmpty()) {
            Toast.makeText(this, "No se encontró el usuario", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            try {
                val document = withContext(Dispatchers.IO) {
                    db.collection("usuarios").document(correoUsuario).get().await()
                }

                if (document.exists()) {
                    val base64 = document.getString("imagen_perfil")
                    val imageView: CircleImageView = findViewById(R.id.userperfilimg)

                    if (!base64.isNullOrEmpty()) {
                        if (base64.startsWith("data:image")) {
                            GlideApp.with(this@MisDatos)
                                .load(base64)
                                .placeholder(R.drawable.userperfilimg)
                                .error(R.drawable.userperfilimg)
                                .into(imageView)
                        } else {
                            val bitmap = convertirBase64ABitmap(base64)
                            imageView.setImageBitmap(bitmap)
                        }
                    } else {
                        imageView.setImageResource(R.drawable.userperfilimg)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MisDatos, "Error al cargar imagen: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun convertirBase64ABitmap(base64: String): Bitmap? {
        try {
            val pureBase64 = base64.substringAfter(",")
            val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (_: Exception) {
            return null
        }
    }

    private fun cargarDatosUsuario() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val correoUsuario = prefs.getString("correo_usuario", "") ?: return

        db.collection("usuarios").document(correoUsuario).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    nombreInput.hint = document.getString("nombre") ?: "Nombre de usuario"
                    telefonoInput.hint = document.getString("telefono") ?: "Teléfono"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarDatos() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val correoUsuario = prefs.getString("correo_usuario", "") ?: return

        val nuevosDatos = mutableMapOf<String, Any>()

        val nuevoNombre = nombreInput.text.toString().trim()
        val nuevoTelefono = telefonoInput.text.toString().trim()

        if (nuevoNombre.isNotEmpty()) nuevosDatos["nombre"] = nuevoNombre
        if (nuevoTelefono.isNotEmpty()) nuevosDatos["telefono"] = nuevoTelefono

        if (nuevosDatos.isEmpty()) {
            Toast.makeText(this, "No hay datos para actualizar", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("usuarios").document(correoUsuario)
            .update(nuevosDatos)
            .addOnSuccessListener {
                Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show()
                cargarDatosUsuario()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
    }


    class GlideApp private constructor() {
        companion object {
            fun with(context: Context): RequestManager {
                return Glide.with(context)
            }
        }
    }

}