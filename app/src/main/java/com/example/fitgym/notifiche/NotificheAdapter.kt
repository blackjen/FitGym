package com.example.fitgym.notifiche

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitgym.R

class NotificheAdapter(
    private val listaTemp: MutableList<NotificaData>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<NotificheAdapter.NotificaViewHolder>() {

    class NotificaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitolo: TextView = itemView.findViewById(R.id.tvTitoloNotifica)
        val tvMessaggio: TextView = itemView.findViewById(R.id.tvMessaggioNotifica)
        val tvData: TextView = itemView.findViewById(R.id.tvDataNotifica)
        val ibElimina: ImageButton = itemView.findViewById(R.id.ibElimina)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notifica, parent, false)
        return NotificaViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificaViewHolder, position: Int) {
        val notifica = listaTemp[position]
        holder.tvTitolo.text = notifica.titolo
        holder.tvMessaggio.text = notifica.messaggio
        holder.tvData.text = notifica.getDataFormattata()

        holder.ibElimina.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Conferma Rimozione")
                .setMessage("Sei sicuro di voler eliminare la notifica?")
                .setPositiveButton("Conferma") { _, _ ->
                    onDeleteClick(position)
                }
                .setNegativeButton("Annulla", null)
                .show()

        }
    }

    fun updateData(nuoveNotifiche: List<NotificaData>) {
        listaTemp.clear()
        listaTemp.addAll(nuoveNotifiche)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): NotificaData {
        return listaTemp[position]
    }

    fun removeItem(position: Int) {
        listaTemp.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listaTemp.size)
    }

    override fun getItemCount(): Int = listaTemp.size
}
