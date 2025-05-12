package com.example.legally

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArticuloAdapter(
    private var lista: List<Articulo>,
    private val onItemClick: (Articulo) -> Unit
) : RecyclerView.Adapter<ArticuloAdapter.ArticuloViewHolder>() {

    inner class ArticuloViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.art_nombre)
        val tvSerial: TextView = itemView.findViewById(R.id.art_serial)
        val tvDetalles: TextView = itemView.findViewById(R.id.art_detalles)
    }

    fun updateData(newData: List<Articulo>) {
        this.lista = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticuloViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_articulo, parent, false)
        return ArticuloViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticuloViewHolder, position: Int) {
        val articulo = lista[position]
        holder.tvNombre.text = "${articulo.marca_articulo} ${articulo.nombre_articulo}"
        holder.tvSerial.text = "S/N: ${articulo.serial}"
        holder.tvDetalles.text = "Detalles:\n${articulo.descripcion_articulo}"

        holder.itemView.setOnClickListener {
            onItemClick(articulo)
        }
    }

    override fun getItemCount(): Int = lista.size
}
