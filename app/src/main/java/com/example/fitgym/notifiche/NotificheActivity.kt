package com.example.fitgym.notifiche

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitgym.MainActivity
import com.example.fitgym.NegozioActivity
import com.example.fitgym.R
import com.example.fitgym.corsi.CorsiActivity
import com.example.fitgym.utente.ProfiloActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificheActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var ibHome: ImageButton
    private lateinit var ibCorsi: ImageButton
    private lateinit var ibProfilo: ImageButton
    private lateinit var ibNegozio: ImageButton
    private lateinit var adapter: NotificheAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val listaNotifiche = mutableListOf<NotificaData>()
    private val listaTemp = mutableListOf<NotificaData>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifiche)

        ibHome = findViewById(R.id.ibHome)
        ibProfilo = findViewById(R.id.ibProfilo)
        ibNegozio = findViewById(R.id.ibNegozio)
        ibCorsi = findViewById(R.id.ibCorsi)
        recyclerView = findViewById(R.id.recyclerNotifiche)
        adapter = NotificheAdapter(listaTemp) { position ->
            eliminaNotifica(position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        caricaNotifiche()

        ibHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        ibNegozio.setOnClickListener {
            val intent = Intent(this, NegozioActivity::class.java)
            startActivity(intent)
        }

        ibProfilo.setOnClickListener {
            startActivity(Intent(this, ProfiloActivity::class.java))
        }

        ibCorsi.setOnClickListener {
            startActivity(Intent(this, CorsiActivity::class.java))
        }

    }

    private fun caricaNotifiche() {
        val utente = auth.currentUser?.uid ?: return
        db.collection("utenti").document(utente)
            .get()
            .addOnSuccessListener { snapshot ->
                val notificheMap = snapshot.get("notifiche") as? List<Map<String, Any>> ?: emptyList()
                val notifiche = notificheMap.map {
                    NotificaData(
                        titolo = it["titolo"] as? String ?: "",
                        messaggio = it["messaggio"] as? String ?: "",
                        data = it["data"] as? com.google.firebase.Timestamp
                    )
                }
                listaNotifiche.clear()
                listaNotifiche.addAll(notifiche.sortedByDescending { it.data })
                adapter.updateData(listaNotifiche)
            }
    }


    private fun eliminaNotifica(position: Int) {
        val utente = auth.currentUser?.uid ?: return
        val notifica = adapter.getItem(position)

        val docRef = db.collection("utenti").document(utente)
        docRef.get().addOnSuccessListener { snapshot ->
            val listaNotificheMap = (snapshot.get("notifiche") as? List<Map<String, Any>>)?.toMutableList()
                ?: mutableListOf()

            val daRimuovere = listaNotificheMap.find { map ->
                (map["titolo"] as? String) == notifica.titolo &&
                        (map["messaggio"] as? String) == notifica.messaggio &&
                        ((map["data"] as? com.google.firebase.Timestamp)?.seconds == notifica.data?.seconds)
            }

            if (daRimuovere != null) {
                listaNotificheMap.remove(daRimuovere)
                docRef.update("notifiche", listaNotificheMap)
                    .addOnSuccessListener {
                        adapter.removeItem(position)
                    }
            }
        }
    }

}