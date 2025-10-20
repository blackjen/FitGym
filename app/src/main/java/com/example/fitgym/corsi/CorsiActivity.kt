package com.example.fitgym.corsi

import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitgym.MainActivity
import com.example.fitgym.NegozioActivity
import com.example.fitgym.R
import com.example.fitgym.notifiche.NotificheActivity
import com.example.fitgym.notifiche.ReminderWorker
import com.example.fitgym.utente.ProfiloActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class CorsiActivity : AppCompatActivity() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var ibHome: ImageButton
    private lateinit var ibProfilo: ImageButton
    private lateinit var ibNegozio: ImageButton
    private lateinit var ibNotifiche: ImageButton
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val listaCorsi = mutableListOf<CorsoData>()
    private val listaTemp = mutableListOf<CorsoData>()
    private var corsiBase: List<CorsoBase> = emptyList()
    private var cittaUtente: String = "Ancona" // default
    private val adapter = CorsiAdapter(listaTemp) { position ->
        val corso = listaCorsi[position]
        aggiornaIscrizione(listaCorsi, position)
        aggiornaNotifiche(corso, corso.iscritto)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_corsi)

        // Gestione permessi notifiche (chiede il permesso per mostrare notifiche se android è 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        ibHome = findViewById(R.id.ibHome)
        ibProfilo = findViewById(R.id.ibProfilo)
        ibNegozio = findViewById(R.id.ibNegozio)
        ibNotifiche = findViewById(R.id.ibNotifiche)
        recyclerView = findViewById(R.id.recyclerCorsi)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        ibHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        ibNegozio.setOnClickListener {
            val intent = Intent(this, NegozioActivity::class.java)
            startActivity(intent)
        }


        ibNotifiche.setOnClickListener {
            val intent = Intent(this, NotificheActivity::class.java)
            startActivity(intent)
        }


        ibProfilo.setOnClickListener {
            startActivity(Intent(this, ProfiloActivity::class.java))
        }

        pulisciIscrizioniScadute()

        creaCanaleNotifiche()

        // Ottiene città utente da Firestore
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("utenti").document(userId)
                .get()
                .addOnSuccessListener { document->
                    cittaUtente = document.getString("città") ?: "Ancona"
                    caricaCorsiFiltrati()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Errore nel recupero città", Toast.LENGTH_SHORT).show()
                    caricaCorsiFiltrati() // default
                }
        }

        caricaCorsiBaseDaFirestore { corsi ->
            corsiBase = corsi
            val corsiGenerati = generaCorsiDaBase(corsiBase, 3)
            val corsiFiltrati = filtraCorsiValidi(corsiGenerati)
            val corsiDaAggiungere = corsiGenerati.size - corsiFiltrati.size
            val nuoviCorsi = generaCorsiInCoda(corsiBase, corsiDaAggiungere)

            listaCorsi.clear()
            listaCorsi.addAll(corsiFiltrati + nuoviCorsi)

            caricaIscrizioni(listaCorsi)
            ordinaCorsi(listaCorsi)
            adapter.updateData(listaCorsi)

        }
    }

    // Genera la lista effettiva dei corsi, mostrandola nell'interfaccia (chiamando tutte le altre funzioni)
    private fun caricaCorsiFiltrati() {
        caricaCorsiBaseDaFirestore { corsi ->
            corsiBase = corsi
            val corsiGenerati = generaCorsiDaBase(corsiBase, 3)
            val corsiFiltrati = filtraCorsiValidi(corsiGenerati)

            listaCorsi.clear()
            listaCorsi.addAll(corsiFiltrati)

            caricaIscrizioni(listaCorsi)
            ordinaCorsi(listaCorsi)
            adapter.updateData(listaCorsi)
        }
    }

    // Crea una lista di oggetti CorsoBase, leggendoli da firebase
    private fun caricaCorsiBaseDaFirestore(onComplete: (List<CorsoBase>) -> Unit) {
        db.collection("corsi_base").get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.documents.mapNotNull { doc ->
                    val nome = doc.getString("nome") ?: return@mapNotNull null
                    val giornoStr = doc.getString("giorno") ?: return@mapNotNull null
                    val orario = doc.getString("orario") ?: return@mapNotNull null
                    val descrizione = doc.getString("descrizione") ?: return@mapNotNull null

                    val giorno = when (giornoStr.lowercase()) {
                        "lunedì" -> DayOfWeek.MONDAY
                        "martedì" -> DayOfWeek.TUESDAY
                        "mercoledì" -> DayOfWeek.WEDNESDAY
                        "giovedì" -> DayOfWeek.THURSDAY
                        "venerdì" -> DayOfWeek.FRIDAY
                        "sabato" -> DayOfWeek.SATURDAY
                        "domenica" -> DayOfWeek.SUNDAY
                        else -> null
                    } ?: return@mapNotNull null

                    CorsoBase(nome, giorno, orario, descrizione)
                }
                onComplete(lista)
            }
            .addOnFailureListener {
                onComplete(emptyList())
            }
    }

    // Restituisce una lista di oggetti CorsoData (corsi completi) da oggi a 3 settimane
    private fun generaCorsiDaBase(corsiBase: List<CorsoBase>, settimane: Int): List<CorsoData> {

        val oggi = LocalDate.now()
        val lista = mutableListOf<CorsoData>()

        for (settimana in 0 until settimane) {
            for (cb in corsiBase) {
                val dataCorso = oggi
                    .plusWeeks(settimana.toLong())
                    .with(TemporalAdjusters.nextOrSame(cb.giorno))

                lista.add(
                    CorsoData(
                        nomeCorso = cb.nomeCorso,
                        giorno = cb.giorno.value,
                        orario = cb.orario,
                        data = dataCorso,
                        citta = cittaUtente,
                        iscritto = false,
                        descrizione = cb.descrizione
                    )
                )
            }
        }
        return lista
    }

    // Elimina dalla lista i corsi con data passata
    private fun filtraCorsiValidi(lista: List<CorsoData>): List<CorsoData> {
        val oggi = LocalDate.now()
        val oraCorrente = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        val filtrati = lista.filter { corso ->
            when {
                corso.data.isAfter(oggi) -> true
                corso.data.isEqual(oggi) -> {
                    try {
                        val oraCorso = LocalTime.parse(corso.orario, formatter)
                        oraCorso.isAfter(oraCorrente)
                    } catch (e: Exception) {
                        false
                    }
                }
                else -> false
            }
        }
        return filtrati
    }


    // Se sono stati filtrati alcuni corsi, ne genera altrettanti per arrivare alle 3 settimane successive
    private fun generaCorsiInCoda(corsiBase: List<CorsoBase>, quanti: Int): List<CorsoData> {
        val oggi = LocalDate.now()
        val lista = mutableListOf<CorsoData>()
        var settimaneAggiunte = 3 // partiamo dopo le prime 3 settimane già generate

        while (lista.size < quanti) {
            for (cb in corsiBase) {
                val dataCorso = oggi
                    .plusWeeks(settimaneAggiunte.toLong())
                    .with(TemporalAdjusters.nextOrSame(cb.giorno))

                lista.add(
                    CorsoData(
                        nomeCorso = cb.nomeCorso,
                        giorno = cb.giorno.value,
                        orario = cb.orario,
                        data = dataCorso,
                        citta = cittaUtente,
                        iscritto = false,
                        descrizione = cb.descrizione
                    )
                )

                if (lista.size == quanti) break
            }
            settimaneAggiunte++
        }

        return lista
    }

    // Ordina i corsi per data crescente, poi per orario.
    private fun ordinaCorsi(corsi: MutableList<CorsoData>) {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")

        corsi.sortWith(compareBy<CorsoData> { it.data }
            .thenBy { corso ->
                try {
                    LocalTime.parse(corso.orario, formatter)
                } catch (e: Exception) {
                    LocalTime.MIDNIGHT // Se l'orario non è valido, lo considera 00:00
                }
            })
    }

    // Legge da firebase la lista dei corsi a cui è iscritto l'utente, aggiorna l'interfaccia e
    // programma i reminder per i corsi iscritti
    private fun caricaIscrizioni(corsi: MutableList<CorsoData>) {
        val utente = auth.currentUser?.uid ?: return
        db.collection("utenti").document(utente).get()
            .addOnSuccessListener { snapshot ->
                val iscritti = snapshot.get("corsiIscritti") as? List<String> ?: emptyList()
                corsi.forEach { corso ->
                    val idUnico = "${corso.nomeCorso}_${corso.data}_${corso.citta}"
                    corso.iscritto = iscritti.contains(idUnico)
                }
                adapter.notifyDataSetChanged()

                // Programma i reminder per tutti i corsi a cui l'utente è iscritto
                corsi.filter { it.iscritto }.forEach { corso ->
                    programmaReminder(corso)
                }
            }
    }

    // Aggiunge/Elimina i corsi dalla lista corsiIscritti
    private fun aggiornaIscrizione(corsi: MutableList<CorsoData>, position: Int) {
        val utente = auth.currentUser?.uid ?: return
        val corso = corsi[position]
        val idUnico = "${corso.nomeCorso}_${corso.data}_${corso.citta}"

        db.collection("utenti").document(utente).get()
            .addOnSuccessListener { snapshot ->
                val lista = (snapshot.get("corsiIscritti") as? MutableList<String>)?.toMutableList()
                    ?: mutableListOf()

                if (corso.iscritto) {
                    lista.remove(idUnico)
                } else {
                    lista.add(idUnico)
                }

                db.collection("utenti").document(utente)
                    .update("corsiIscritti", lista)
                    .addOnSuccessListener {
                        corso.iscritto = !corso.iscritto
                        adapter.notifyItemChanged(position)

                        if (corso.iscritto) {
                            // Programma il reminder solo se nuovo iscritto
                            programmaReminder(corso)
                        }
                    }
            }
    }

    // Crea una notifica all'iscrizione/disiscrizione di un corso
    private fun aggiornaNotifiche(corso: CorsoData, isIscritto: Boolean) {
        val utente = auth.currentUser?.uid ?: return
        val titolo = if (isIscritto) "DISISCRIZIONE CORSO" else "ISCRIZIONE CORSO"
        val messaggio = if (isIscritto) {
            "Hai annullato l'iscrizione a ${corso.nomeCorso} del ${corso.data} alle ${corso.orario}"
        } else {
            "Ti sei iscritto a ${corso.nomeCorso} il ${corso.data} alle ${corso.orario}"
        }

        val nuovaNotifica = hashMapOf(
            "titolo" to titolo,
            "messaggio" to messaggio,
            "data" to com.google.firebase.Timestamp.now()
        )

        db.collection("utenti").document(utente)
            .update("notifiche", com.google.firebase.firestore.FieldValue.arrayUnion(nuovaNotifica))
    }

    // Elimina dalla lista corsiIscritti, i corsi che sono passati
    private fun pulisciIscrizioniScadute() {
        val utente = auth.currentUser?.uid ?: return
        val oggi = LocalDate.now()
        val oraCorrente = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        db.collection("utenti").document(utente).get()
            .addOnSuccessListener { snapshot ->
                val lista = (snapshot.get("corsiIscritti") as? List<String>)?.toMutableList()
                    ?: mutableListOf()

                // Filtra i corsi, i corsi passati vengono eliminati (false), quelli validi vengono tenuti (true)
                val listaAggiornata = lista.filter { idCorso ->
                    val parti = idCorso.split("_")
                    if (parti.size < 3) return@filter true

                    return@filter try {
                        val dataCorso = LocalDate.parse(parti[1])
                        if (dataCorso.isAfter(oggi)) {
                            true
                        } else if (dataCorso.isEqual(oggi)) {
                            val corso = listaCorsi.find { it.nomeCorso == parti[0] && it.data.toString() == parti[1] }
                            if (corso != null) {
                                val oraCorso = LocalTime.parse(corso.orario, formatter)
                                oraCorso.isAfter(oraCorrente)
                            } else {
                                true
                            }
                        } else {
                            false
                        }
                    } catch (e: Exception) {
                        true // Se fallisce, non elimina
                    }
                }

                if (listaAggiornata.size != lista.size) {
                    db.collection("utenti").document(utente)
                        .update("corsiIscritti", listaAggiornata)
                }
            }
    }

    // Crea il canale per Android NotificationManager
    private fun creaCanaleNotifiche() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Promemoria Corsi"
            val descriptionText = "Notifiche per corsi a cui sei iscritto"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel("reminder_channel", name, importance)
            channel.description = descriptionText
            val notificationManager: android.app.NotificationManager =
                getSystemService(android.app.NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Genera una notifica quando manca 1 ora dall'inizio di un corso a cui si è iscritti
    // Usa WorkManager per programmare il lavoro in background.
    fun programmaReminder(corso: CorsoData) {
        if (!corso.iscritto) return

        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val oraCorso = LocalTime.parse(corso.orario, formatter)
        val dataCorso = LocalDateTime.of(corso.data, oraCorso).minusHours(1) // 1h prima

        val delayMillis = dataCorso.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis()
        if (delayMillis <= 0) return // Se manca meno di un'ora non schedulare

        val data = Data.Builder()
            .putString("titolo", "Promemoria Corso: ${corso.nomeCorso}")
            .putString("messaggio", "Il corso inizia alle ${corso.orario} a ${corso.citta}")
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("${corso.nomeCorso}_${corso.data}_${corso.citta}") // tag unico per il corso
            .build()

        // enqueueUniqueWork assicura che ci sia al massimo un reminder per corso
        WorkManager.getInstance(this).enqueueUniqueWork(
            "${corso.nomeCorso}_${corso.data}_${corso.citta}",
            androidx.work.ExistingWorkPolicy.REPLACE, // se esiste già, lo sostituisce
            workRequest
        )
    }


}