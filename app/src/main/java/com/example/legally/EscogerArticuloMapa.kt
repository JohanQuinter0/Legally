package com.example.legally

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class EscogerArticuloMapa : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private val listaArticulos = mutableListOf<Articulo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escoger_articulo_mapa)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }

        window.setBackgroundDrawableResource(R.color.white)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mis_articulos)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.rvArticulos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
        }

        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val correoUsuario = prefs.getString("correo_usuario", null)

        if (correoUsuario == null) {
            Toast.makeText(this, "No se encontró el usuario actual", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("articulos")
            .whereEqualTo("estado", "perdido")
            .whereEqualTo("id_usuario", correoUsuario)
            .get()
            .addOnSuccessListener { result ->
                listaArticulos.clear()
                for (doc in result) {
                    val articulo = doc.toObject(Articulo::class.java)
                    articulo.serial = doc.id
                    listaArticulos.add(articulo)
                }
                recyclerView.adapter = ArticuloAdapter(listaArticulos) { articulo ->
                    val intent = Intent(this, Mapa::class.java)
                    intent.putExtra("SELECT_LOCATION", true)
                    intent.putExtra("ARTICULO_ID", articulo.serial)
                    @Suppress("DEPRECATION")
                    startActivityForResult(intent, 1001)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando articulos perdidos", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            finish()
        }
    }
}