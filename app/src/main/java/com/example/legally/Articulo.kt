@file:Suppress("PropertyName")
package com.example.legally

data class Articulo(
    var serial: String = "",
    val nombre_articulo: String = "",
    val descripcion_articulo: String = "",
    val tipo_articulo: String = "",
    val marca_articulo: String = ""
)