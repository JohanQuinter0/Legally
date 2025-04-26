package com.example.legally

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MensajeAdapter(
    private val mensajes: List<Mensaje>,
    private val miCorreo: String
) : RecyclerView.Adapter<MensajeAdapter.MensajeViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (mensajes[position].emisor == miCorreo) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajeViewHolder {
        val layoutId = if (viewType == 1) R.layout.item_mensaje_enviado else R.layout.item_mensaje_recibido
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MensajeViewHolder(view)
    }

    override fun onBindViewHolder(holder: MensajeViewHolder, position: Int) {
        holder.bind(mensajes[position])
    }

    override fun getItemCount(): Int = mensajes.size

    class MensajeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textoMensaje: TextView = itemView.findViewById(R.id.textoMensaje)

        fun bind(mensaje: Mensaje) {
            textoMensaje.text = mensaje.texto
        }
    }
}