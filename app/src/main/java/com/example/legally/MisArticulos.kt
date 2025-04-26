package com.example.legally

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MisArticulos : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ArticuloAdapter
    private val db = Firebase.firestore
    private lateinit var tvMensaje: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mis_articulos)

        initViews()

        setupRecyclerView()

        cargarArticulos()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mis_articulos)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rvArticulos)
        tvMensaje = findViewById(R.id.aquiarts)

        findViewById<ImageView>(R.id.back).setOnClickListener {
            startActivity(Intent(this, Inicio::class.java))
            finish()
        }

        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ArticuloAdapter(emptyList()) { articulo ->
            val intent = Intent(this, ArtView::class.java)
            intent.putExtra("serial", articulo.serial)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun cargarArticulos() {
        val correoUsuario = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
            .getString("correo_usuario", null)

        if (correoUsuario.isNullOrEmpty()) {
            mostrarError("No se pudo identificar al usuario")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = db.collection("articulos")
                    .whereEqualTo("id_usuario", correoUsuario)
                    .get()
                    .await()

                val listaArticulos = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Articulo::class.java)?.copy(serial = doc.id)
                }

                withContext(Dispatchers.Main) {
                    if (listaArticulos.isEmpty()) {
                        tvMensaje.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        tvMensaje.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateData(listaArticulos)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mostrarError("Error al cargar artículos: ${e.localizedMessage}")
                    Log.e("MisArticulos", "Error al cargar artículos", e)
                }
            } finally {
                withContext(Dispatchers.Main) {
                }
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        tvMensaje.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }
}