package com.example.fitgym

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.fitgym.corsi.CorsiActivity
import com.example.fitgym.notifiche.NotificheActivity
import com.example.fitgym.utente.ProfiloActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class NegozioActivity : AppCompatActivity() {

    private lateinit var actvAbbonamento: AutoCompleteTextView
    private lateinit var btnAbbonamento: Button
    private lateinit var tvSaldo: TextView
    private lateinit var tvIngressi: TextView
    private lateinit var tvScadenza: TextView
    private lateinit var tvRicaricaSaldo: TextView
    private lateinit var ibAcquistaIngressi: ImageButton
    private lateinit var ibProfilo: ImageButton
    private lateinit var ibCorsi: ImageButton
    private lateinit var ibHome: ImageButton
    private lateinit var ibNotifiche: ImageButton
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Chiave -> Valore
    private val prezziAbbonamenti = mapOf(
        "Abbonamento Mensile (40€)" to 40,
        "Abbonamento Trimestrale (100€)" to 100,
        "Abbonamento Semestrale (180€)" to 180,
        "Abbonamento Annuale (300€)" to 300
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_negozio)

        actvAbbonamento = findViewById(R.id.actvAbbonamento)
        btnAbbonamento = findViewById(R.id.btnAbbonamento)
        ibAcquistaIngressi = findViewById(R.id.ibAcquistaIngressi)
        tvSaldo = findViewById(R.id.tvSaldo)
        tvIngressi = findViewById(R.id.tvIngressi)
        tvScadenza = findViewById(R.id.tvScadenza)
        tvRicaricaSaldo = findViewById(R.id.tvRicaricaSaldo)
        ibProfilo = findViewById(R.id.ibProfilo)
        ibCorsi = findViewById(R.id.ibCorsi)
        ibHome = findViewById<ImageButton>(R.id.ibHome)
        ibNotifiche = findViewById(R.id.ibNotifiche)

        // Lista opzioni abbonamenti
        val abbonamenti = listOf(
            "Abbonamento Mensile (40€)",
            "Abbonamento Trimestrale (100€)",
            "Abbonamento Semestrale (180€)",
            "Abbonamento Annuale (300€)"
        )


        val adapter = ArrayAdapter(this, R.layout.menu_tendina, abbonamenti)
        actvAbbonamento.setAdapter(adapter)

        //Disabilita tastiera e mostra menù a tendina
        actvAbbonamento.setOnClickListener {
            actvAbbonamento.showDropDown()
        }

        caricaDatiUtente()

        btnAbbonamento.setOnClickListener {
            val scelta = actvAbbonamento.text.toString()

            if (scelta.isEmpty() || !prezziAbbonamenti.containsKey(scelta)) {
                Toast.makeText(this, "Seleziona un abbonamento valido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prezzo = prezziAbbonamenti[scelta] ?: return@setOnClickListener

            // Mostra popup di conferma
            AlertDialog.Builder(this)
                .setTitle("Conferma acquisto")
                .setMessage("Sei sicuro di voler acquistare $scelta?\n" +
                        "Verranno scalati $prezzo€ dal tuo saldo.")
                .setPositiveButton("Conferma") { _, _ ->
                    aggiornaAbbonamento(scelta, prezzo)
                }
                .setNegativeButton("Annulla", null)
                .show()
        }

        ibAcquistaIngressi.setOnClickListener {
            mostraPopupIngressi()
        }

        //Rende cliccabile la scritta "Ricarica Saldo"
        val spannable = SpannableString("Ricarica Saldo")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                mostraDialogRicarica()
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = Color.BLUE
            }

        }
        spannable.setSpan(clickableSpan, 0, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvRicaricaSaldo.text = spannable
        tvRicaricaSaldo.movementMethod = LinkMovementMethod.getInstance()

        ibHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        ibCorsi.setOnClickListener {
            val intent = Intent(this, CorsiActivity::class.java)
            startActivity(intent)
        }


        ibNotifiche.setOnClickListener {
            val intent = Intent(this, NotificheActivity::class.java)
            startActivity(intent)
        }


        ibProfilo.setOnClickListener {
            val intent = Intent(this, ProfiloActivity::class.java)
            startActivity(intent)
        }



    }


    private fun caricaDatiUtente() {
        db.collection("utenti").document(userId).get()
            .addOnSuccessListener { document ->

                // (?:) -> Se il campo non esiste o è "Null" allora verrà assegnato il valore di default seguente
                val saldo = document.getLong("saldo") ?: 0
                val ingressi = document.getLong("ingressi") ?: 0
                val scadenza = document.getString("abbonamentoScadenza") ?: "Nessun abbonamento"

                tvSaldo.text = "Il Tuo Saldo: ${saldo}€"
                tvIngressi.text = "Ingressi: $ingressi"
                tvScadenza.text = "Scadenza Abbonamento:\n $scadenza"
            }
            .addOnFailureListener {
                Toast.makeText(this, "Errore nel caricamento dati utente", Toast.LENGTH_SHORT).show()
            }
    }

    private val prezzoIngresso = 10

    private fun mostraPopupIngressi() {
        AlertDialog.Builder(this)
            .setTitle("Conferma acquisto")
            .setMessage("Sicuro di comprare un ingresso?\nVerranno scalati $prezzoIngresso€ dal tuo saldo.")
            .setPositiveButton("Conferma") { _, _ ->
                acquistaIngresso()
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun acquistaIngresso() {
        db.collection("utenti").document(userId).get()
            .addOnSuccessListener { document ->
                val saldoAttuale = document.getLong("saldo") ?: 0
                val ingressiAttuali = document.getLong("ingressi") ?: 0

                if (saldoAttuale < prezzoIngresso) {
                    Toast.makeText(this, "Saldo insufficiente!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val nuovoSaldo = saldoAttuale - prezzoIngresso
                val nuoviIngressi = ingressiAttuali + 1

                db.collection("utenti").document(userId)
                    .update(
                        mapOf(
                            "saldo" to nuovoSaldo,
                            "ingressi" to nuoviIngressi
                        )
                    )
                    .addOnSuccessListener {
                        tvSaldo.text = "Il tuo saldo: ${nuovoSaldo}€"
                        tvIngressi.text = "Ingressi: $nuoviIngressi"
                        Toast.makeText(this, "Ingresso acquistato con successo!", Toast.LENGTH_SHORT).show()

                        aggiornaNotifiche(
                            "ACQUISTO INGRESSO",
                            "Hai acquistato un ingresso singolo"
                        )
                    }
            }
    }
    // Calcola nuova scadenza in base al tipo di abbonamento
    private fun calcolaNuovaScadenza(scelta: String, scadenzaAttuale: String?): String {
        val cal = Calendar.getInstance()
        val formato = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        if (!scadenzaAttuale.isNullOrEmpty()) {
            val dataAttuale = formato.parse(scadenzaAttuale)
            if (dataAttuale != null && dataAttuale.after(Date())) {
                cal.time = dataAttuale
            }
        }

        when (scelta) {
            "Abbonamento Mensile (40€)" -> cal.add(Calendar.MONTH, 1)
            "Abbonamento Trimestrale (100€)" -> cal.add(Calendar.MONTH, 3)
            "Abbonamento Semestrale (180€)" -> cal.add(Calendar.MONTH, 6)
            "Abbonamento Annuale (300€)" -> cal.add(Calendar.YEAR, 1)
        }

        return formato.format(cal.time)
    }

    // Aggiorna Firestore con la nuova scadenza
    private fun aggiornaAbbonamento(scelta: String, prezzo: Int) {
        db.collection("utenti").document(userId).get()
            .addOnSuccessListener { document ->
                val saldoAttuale = document.getLong("saldo") ?: 0
                if (saldoAttuale < prezzo) {
                    Toast.makeText(this, "Saldo insufficiente!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val scadenzaAttuale = document.getString("abbonamentoScadenza")
                val nuovaScadenza = calcolaNuovaScadenza(scelta, scadenzaAttuale)
                val nuovoSaldo = saldoAttuale - prezzo

                db.collection("utenti").document(userId)
                    .update(
                        mapOf(
                            "saldo" to nuovoSaldo,
                            "abbonamentoScadenza" to nuovaScadenza
                        )
                    )
                    .addOnSuccessListener {
                        tvSaldo.text = "Il tuo saldo: ${nuovoSaldo}€"
                        tvScadenza.text = "Scadenza: $nuovaScadenza"
                        Toast.makeText(this, "Abbonamento attivo fino al $nuovaScadenza", Toast.LENGTH_LONG).show()

                        aggiornaNotifiche(
                            "ACQUISTO ABBONAMENTO",
                            "Hai acquistato $scelta valido fino al $nuovaScadenza"
                        )
                    }
            }
    }

    private fun ricaricaSaldo(importo: Int) {
        db.collection("utenti").document(userId).get()
            .addOnSuccessListener { document ->
                val saldoAttuale = document.getLong("saldo") ?: 0
                val nuovoSaldo = saldoAttuale + importo

                db.collection("utenti").document(userId)
                    .update("saldo", nuovoSaldo)
                    .addOnSuccessListener {
                        tvSaldo.text = "Il tuo saldo: ${nuovoSaldo}€"
                        Toast.makeText(
                            this,
                            "Saldo ricaricato di $importo€!",
                            Toast.LENGTH_SHORT
                        ).show()

                        aggiornaNotifiche(
                            "RICARICA SALDO",
                            "Hai ricaricato il saldo di $importo€"
                        )
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Errore nella ricarica del saldo", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Errore nel caricamento dei dati utente", Toast.LENGTH_SHORT).show()
            }
    }


    // Mostra una piccola schermata per la ricarica
    private fun mostraDialogRicarica() {

        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_ricarica, null)

        val btn10 = dialogView.findViewById<Button>(R.id.btn10)
        val btn20 = dialogView.findViewById<Button>(R.id.btn20)
        val btn50 = dialogView.findViewById<Button>(R.id.btn50)
        val btn100 = dialogView.findViewById<Button>(R.id.btn100)
        val btnConferma = dialogView.findViewById<Button>(R.id.btnConfermaRicarica)

        var cifraSelezionata = 0

        val onClickCifra = { valore: Int ->
            cifraSelezionata = valore
            Toast.makeText(this, "Selezionato: $valore€", Toast.LENGTH_SHORT).show()
        }

        btn10.setOnClickListener { onClickCifra(10) }
        btn20.setOnClickListener { onClickCifra(20) }
        btn50.setOnClickListener { onClickCifra(50) }
        btn100.setOnClickListener { onClickCifra(100) }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnConferma.setOnClickListener {
            if (cifraSelezionata > 0) {
                ricaricaSaldo(cifraSelezionata)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Seleziona prima un importo", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun aggiornaNotifiche(titolo: String, messaggio: String) {
        val utente = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val nuovaNotifica = hashMapOf(
            "titolo" to titolo,
            "messaggio" to messaggio,
            "data" to com.google.firebase.Timestamp.now()
        )

        db.collection("utenti").document(utente)
            .update("notifiche", com.google.firebase.firestore.FieldValue.arrayUnion(nuovaNotifica))
    }
}


