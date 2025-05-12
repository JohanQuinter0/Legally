package com.example.legally

import android.content.Intent
import android.graphics.Outline
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.legally.databinding.ActivityMapaBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.Locale

class Mapa : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapaBinding
    private lateinit var googleMap: GoogleMap
    private var isSelectingLocation = false
    private var selectedLatLng: LatLng? = null
    private var selectedArticleId: String? = null
    private lateinit var overlayView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedArticleId = intent.getStringExtra("ARTICULO_ID")
        Log.d("Mapa", "ID recibido: $selectedArticleId")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.back.setOnClickListener { finish() }

        binding.btnSeleccionarUbicacion.setOnClickListener {
            startActivity(Intent(this, EscogerArticuloMapa::class.java))
        }

        isSelectingLocation = intent.getBooleanExtra("SELECT_LOCATION", false)
        if (isSelectingLocation) {
            binding.btnSeleccionarUbicacion.visibility = View.GONE
        }

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }

        window.setBackgroundDrawableResource(R.color.white)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mapa)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isScrollGesturesEnabled = true

        val mapView = (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).view
        mapView?.let { view ->
            val cornerRadiusInDp = 50f
            val cornerRadiusInPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                cornerRadiusInDp,
                resources.displayMetrics
            ).toInt()

            view.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, cornerRadiusInPx.toFloat())
                }
            }
            view.clipToOutline = true
        }

        val defaultLocation = LatLng(4.7110, -74.0721)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

        cargarArticulosPerdidos()

        if (isSelectingLocation) {
            googleMap.setOnMapClickListener { latLng ->
                selectedLatLng = latLng
                googleMap.clear()
                googleMap.addMarker(MarkerOptions().position(latLng).title("Nueva ubicación"))
                mostrarOverlayConfirmacion()
            }
        }
    }

    private fun mostrarOverlayConfirmacion() {
        val rootLayout = findViewById<FrameLayout>(android.R.id.content)
        overlayView = LayoutInflater.from(this).inflate(R.layout.confirmacion_ubicacion, rootLayout, false)
        rootLayout.addView(overlayView)

        val btnSi = overlayView.findViewById<Button>(R.id.btnSiubicacion)
        val btnNo = overlayView.findViewById<Button>(R.id.btnNoUbicacion)

        btnSi.setOnClickListener {
            confirmarUbicacion()
        }

        btnNo.setOnClickListener {
            cancelarSeleccion()
        }
    }

    private fun confirmarUbicacion() {
        selectedLatLng?.let { latLng ->
            selectedArticleId?.let { articleId ->
                obtenerDireccion(latLng) { direccion ->
                    guardarUbicacionEnFirebase(articleId, latLng, direccion) {
                        cerrarOverlay()

                        setResult(RESULT_OK)

                        val intent = Intent(this, ArtView::class.java).apply {
                            putExtra("serial", articleId)
                            putExtra("ubicacion_aproximada", direccion ?: "Ubicación desconocida")
                            putExtra("modo_verificacion", true)
                            putExtra("es_perdido", true)
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun cancelarSeleccion() {
        selectedLatLng = null
        googleMap.clear()
        cerrarOverlay()
    }

    private fun cerrarOverlay() {
        val rootLayout = findViewById<FrameLayout>(android.R.id.content)
        rootLayout.removeView(overlayView)
    }

    private fun obtenerDireccion(latLng: LatLng, callback: (String?) -> Unit) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            @Suppress("DEPRECATION") val direcciones = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!direcciones.isNullOrEmpty()) {
                callback(direcciones[0].getAddressLine(0))
            } else {
                callback(null)
            }
        } catch (_: Exception) {
            callback(null)
        }
    }

    private fun guardarUbicacionEnFirebase(articleId: String, latLng: LatLng, direccion: String?, onComplete: () -> Unit) {
        val db = Firebase.firestore
        val ubicacion = hashMapOf(
            "latitud" to latLng.latitude,
            "longitud" to latLng.longitude,
            "direccion" to direccion
        )

        db.collection("articulos").document(articleId)
            .update(
                mapOf(
                    "ubicacionPerdida" to ubicacion,
                    "estado" to "perdido"
                )
            )
            .addOnSuccessListener {
                Toast.makeText(this, "Ubicación guardada", Toast.LENGTH_SHORT).show()
                onComplete()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarArticulosPerdidos() {
        val db = Firebase.firestore
        db.collection("articulos")
            .whereEqualTo("estado", "perdido")
            .whereNotEqualTo("ubicacionPerdida", null)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val ubicacion = document.get("ubicacionPerdida") as? Map<*, *>
                    ubicacion?.let {
                        val lat = it["latitud"] as? Double ?: return@let
                        val lng = it["longitud"] as? Double ?: return@let
                        val nombre = document.getString("nombre_articulo") ?: "Artículo perdido"

                        googleMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat, lng))
                                .title(nombre)
                        )
                    }
                }
            }
    }
}
