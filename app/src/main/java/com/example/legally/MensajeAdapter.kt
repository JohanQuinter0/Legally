package com.example.legally

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

sealed class MensajeItem {
    data class MensajeNormal(val mensaje: Mensaje) : MensajeItem()
    data class SeparadorFecha(val fecha: String) : MensajeItem()
}

class MensajeAdapter(
    mensajesOriginales: List<Mensaje>,
    private val miCorreo: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mensajesConFecha: List<MensajeItem>

    init {
        mensajesConFecha = insertarSeparadoresDeFecha(mensajesOriginales)
    }

    override fun getItemViewType(position: Int): Int {
        return when (mensajesConFecha[position]) {
            is MensajeItem.SeparadorFecha -> 2
            is MensajeItem.MensajeNormal -> {
                val mensajeNormal = mensajesConFecha[position] as MensajeItem.MensajeNormal
                if (mensajeNormal.mensaje.emisor == miCorreo) 1 else 0
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> MensajeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_mensaje_recibido, parent, false))
            1 -> MensajeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_mensaje_enviado, parent, false))
            2 -> SeparadorFechaViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_fecha, parent, false))
            else -> throw IllegalArgumentException("Tipo de vista desconocido")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = mensajesConFecha[position]) {
            is MensajeItem.MensajeNormal -> (holder as MensajeViewHolder).bind(item.mensaje)
            is MensajeItem.SeparadorFecha -> (holder as SeparadorFechaViewHolder).bind(item.fecha)
        }
    }

    override fun getItemCount(): Int = mensajesConFecha.size

    class MensajeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textoMensaje: TextView = itemView.findViewById(R.id.textoMensaje)
        private val horaMensaje: TextView = itemView.findViewById(R.id.horaMensaje)

        fun bind(mensaje: Mensaje) {
            textoMensaje.text = mensaje.texto
            mensaje.timestamp?.let {
                val date = it.toDate()
                val sdfHora = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                horaMensaje.text = sdfHora.format(date)
            }
        }
    }

    class SeparadorFechaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textoFecha: TextView = itemView.findViewById(R.id.textoFecha)

        fun bind(fecha: String) {
            textoFecha.text = fecha
        }
    }

    private fun insertarSeparadoresDeFecha(mensajes: List<Mensaje>): List<MensajeItem> {
        val resultado = mutableListOf<MensajeItem>()
        var fechaAnterior: String? = null

        for (mensaje in mensajes) {
            val date = mensaje.timestamp?.toDate() ?: continue
            val sdfFecha = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val fechaActual = sdfFecha.format(date)

            if (fechaActual != fechaAnterior) {
                resultado.add(MensajeItem.SeparadorFecha(formatearFecha(date)))
                fechaAnterior = fechaActual
            }

            resultado.add(MensajeItem.MensajeNormal(mensaje))
        }

        return resultado
    }

    private fun formatearFecha(date: java.util.Date): String {
        val hoy = java.util.Calendar.getInstance()
        val fechaMensaje = java.util.Calendar.getInstance()
        fechaMensaje.time = date

        return when {
            hoy.get(java.util.Calendar.YEAR) == fechaMensaje.get(java.util.Calendar.YEAR) &&
                    hoy.get(java.util.Calendar.DAY_OF_YEAR) == fechaMensaje.get(java.util.Calendar.DAY_OF_YEAR) -> "Hoy"
            hoy.get(java.util.Calendar.YEAR) == fechaMensaje.get(java.util.Calendar.YEAR) &&
                    hoy.get(java.util.Calendar.DAY_OF_YEAR) - 1 == fechaMensaje.get(java.util.Calendar.DAY_OF_YEAR) -> "Ayer"
            else -> {
                val sdf = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault())
                sdf.format(date)
            }
        }
    }
}
