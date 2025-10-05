package com.example.fitgym.corsi

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitgym.R
import java.time.format.DateTimeFormatter

class CorsiAdapter(
    private val listaTemp: MutableList<CorsoData>,
    private val onIscrizioneClick: (Int) -> Unit
) : RecyclerView.Adapter<CorsiAdapter.CorsoViewHolder>() {

    class CorsoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nome = view.findViewById<TextView>(R.id.tvNomeCorso)
        val orario = view.findViewById<TextView>(R.id.tvOrarioCorso)
        val data = view.findViewById<TextView>(R.id.tvDataCorso)
        val btnIscriviti = view.findViewById<Button>(R.id.btnIscriviti)
        val btnVisualizza = view.findViewById<Button>(R.id.btnVisualizza)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CorsoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_corso, parent, false)
        return CorsoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CorsoViewHolder, position: Int) {
        val corso = listaTemp[position]

        holder.nome.text = corso.nomeCorso
        holder.orario.text = corso.orario
        holder.data.text = corso.data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

        holder.btnIscriviti.text = if (corso.iscritto) "Disdici" else "Iscriviti"

        holder.btnVisualizza.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle(corso.nomeCorso)
                .setMessage(corso.descrizione)
                .setPositiveButton("Chiudi", null)
                .show()
        }

        holder.btnIscriviti.setOnClickListener {

            if (holder.btnIscriviti.text == "Iscriviti"){

                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Conferma iscrizione")
                    .setMessage("Sei sicuro di volerti iscrivere al corso ${corso.nomeCorso}?\n")
                    .setPositiveButton("Conferma") { _, _ ->
                        onIscrizioneClick(position)
                    }
                    .setNegativeButton("Annulla", null)
                    .show()
            } else{

                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Conferma Disdetta")
                    .setMessage("Sei sicuro di voler disdire la prenotazione al corso ${corso.nomeCorso}?\n")
                    .setPositiveButton("Conferma") { _, _ ->
                        onIscrizioneClick(position)
                    }
                    .setNegativeButton("Annulla", null)
                    .show()
            }


        }
    }

    fun updateData(nuovaLista: List<CorsoData>) {
        listaTemp.clear()
        listaTemp.addAll(nuovaLista)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = listaTemp.size
}
