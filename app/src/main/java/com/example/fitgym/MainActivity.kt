package com.example.fitgym

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.fitgym.corsi.CorsiActivity
import com.example.fitgym.notifiche.NotificheActivity
import com.example.fitgym.utente.ProfiloActivity
import com.google.firebase.firestore.FirebaseFirestore
import org.checkerframework.common.subtyping.qual.Bottom
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var actvScegliCitta: AutoCompleteTextView
    private val db = FirebaseFirestore.getInstance()

    private lateinit var btnCodice: Button
    private lateinit var tvCodice: TextView
    private lateinit var ibCopiaCodice: ImageButton
    private lateinit var ibProfilo: ImageButton
    private lateinit var ibCorsi: ImageButton
    private lateinit var ibNegozio: ImageButton
    private lateinit var ibNotifiche: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)



        actvScegliCitta = findViewById(R.id.actvScegliCitta)
        btnCodice = findViewById(R.id.btnCodice)
        tvCodice = findViewById(R.id.tvCodice)
        ibCopiaCodice = findViewById(R.id.ibCopiaCodice)
        ibProfilo = findViewById(R.id.ibProfilo)
        ibCorsi = findViewById(R.id.ibCorsi)
        ibNegozio = findViewById(R.id.ibNegozio)
        ibNotifiche = findViewById(R.id.ibNotifiche)



        //Crea la lista e la mette nell'AutoCompleteTextView
        val listacitta = listOf("Ancona","Bologna","Firenze","Milano","Napoli","Roma","Torino")
        val adapter = ArrayAdapter(this, R.layout.menu_tendina, listacitta)
        actvScegliCitta.setAdapter(adapter)

        //Disabilita tastiera e mostra menù a tendina
        actvScegliCitta.setOnClickListener {
            actvScegliCitta.showDropDown()
        }

        // Permette di avere la città di registrazione come città di Default
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("utenti").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val cittaRegistrata = document.getString("città")
                        if (!cittaRegistrata.isNullOrEmpty()) {
                            actvScegliCitta.setText(cittaRegistrata, false)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Errore nel recupero città", Toast.LENGTH_SHORT).show()
                }
        }

        // Listener che salva subito la città scelta
        actvScegliCitta.setOnItemClickListener { parent, _, position, _ ->
            val cittaselezionata = parent.getItemAtPosition(position).toString()
            if (userId != null) {
                db.collection("utenti").document(userId)
                    .update("città", cittaselezionata)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Città aggiornata: $cittaselezionata", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Errore aggiornamento città", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        btnCodice.setOnClickListener {

            val cittaselezionata = actvScegliCitta.text.toString().trim()

            if (cittaselezionata.isEmpty()) {
                Toast.makeText(this, "Seleziona città per generare il codice", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefisso = generaprefisso(cittaselezionata)
            val codicegenerato = generacodice()
            tvCodice.text = "$prefisso: $codicegenerato"
        }

        ibCopiaCodice.setOnClickListener {
            val codiceDaCopiare = tvCodice.text.toString()
            if (codiceDaCopiare.isNotEmpty()) {
                copianegliappunti(codiceDaCopiare)
                Toast.makeText(this, "Codice copiato negli appunti!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Nessun codice da copiare, genera il codice!", Toast.LENGTH_SHORT).show()
            }
        }

        ibCorsi.setOnClickListener {
            val intent = Intent(this, CorsiActivity::class.java)
            startActivity(intent)
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
            val intent = Intent(this, ProfiloActivity::class.java)
            startActivity(intent)
        }

    }

    //Funzione che genera il codice
    private fun generacodice(): String {
        val caratteri = "abcdefghijklmnopqrstuvwxyz0123456789"
        return (1..10)
            .map { caratteri.random() }
            .joinToString("")
    }

    //Funzione che copia il codice
    private fun copianegliappunti(testo: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE)
        if (clipboard is ClipboardManager) {
            val clip = ClipData.newPlainText("codice", testo)
            clipboard.setPrimaryClip(clip)
        } else {
            Toast.makeText(this, "Impossibile accedere agli appunti", Toast.LENGTH_SHORT).show()
        }
    }

    //Funzione che genera il prefisso
    private fun generaprefisso (citta: String) : String{
        return when(citta){
            "Ancona" -> "AN"
            "Bologna" -> "BO"
            "Firenze" -> "FI"
            "Milano" -> "MI"
            "Napoli" -> "NA"
            "Roma" -> "RM"
            "Torino" -> "TO"
            else -> ""
        }

    }

}