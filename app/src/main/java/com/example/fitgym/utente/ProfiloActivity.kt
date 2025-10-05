package com.example.fitgym.utente

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import com.example.fitgym.R
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitgym.MainActivity
import com.example.fitgym.NegozioActivity
import com.example.fitgym.accesso.LoginActivity
import com.example.fitgym.accesso.RegistrazioneActivity
import com.example.fitgym.corsi.CorsiActivity
import com.example.fitgym.notifiche.NotificheActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class ProfiloActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var ibHome: ImageButton
    private lateinit var ibCorsi: ImageButton
    private lateinit var ibNegozio: ImageButton
    private lateinit var ibNotifiche: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profilo)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        ibHome = findViewById(R.id.ibHome)
        ibCorsi = findViewById(R.id.ibCorsi)
        ibNegozio = findViewById(R.id.ibNegozio)
        ibNotifiche = findViewById(R.id.ibNotifiche)

        val etNome = findViewById<EditText>(R.id.etNome)
        val etCognome = findViewById<EditText>(R.id.etCognome)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etDataNascita = findViewById<EditText>(R.id.etDataNascita)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        val btnModificaPassword = findViewById<Button>(R.id.btnModificaPassword)
        val btnAggiornaProfilo = findViewById<Button>(R.id.btnModifica)
        val btnEliminaProfilo = findViewById<Button>(R.id.btnElimina)
        val btnEsci = findViewById<Button>(R.id.btnEsci)

        // Disabilita tastiera e input manuale
        etDataNascita.inputType = android.text.InputType.TYPE_NULL
        etDataNascita.isFocusable = false

        //Permette di scrivere la data di nascita tramite calendario
        etDataNascita.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, y, m, d ->
                val selectedDate = String.format("%02d/%02d/%04d", d, m+1, y)
                etDataNascita.setText(selectedDate)
            }, year, month, day)

            datePicker.datePicker.maxDate = System.currentTimeMillis() //Data max selezionabile: Oggi
            datePicker.show()
        }

        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Utente non autenticato", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Recupera dati da Firestore
        firestore.collection("utenti").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    etNome.setText(document.getString("nome"))
                    etCognome.setText(document.getString("cognome"))
                    etEmail.setText(document.getString("email"))
                    etDataNascita.setText(document.getString("dataNascita"))
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Errore nel caricamento dati", Toast.LENGTH_SHORT).show()
            }

        etPassword.setText("*******")
        etPassword.isEnabled = false

        btnModificaPassword.setOnClickListener {

            // Abilita modifica password
            if (!etPassword.isEnabled) {
                etPassword.setText("")
                etPassword.isEnabled = true
                etPassword.requestFocus()
                btnModificaPassword.isEnabled = false
            }
        }

        btnAggiornaProfilo.setOnClickListener {
            val nuovonome = etNome.text.toString().trim()
            val nuovocognome = etCognome.text.toString().trim()
            val nuovaemail = etEmail.text.toString().trim()
            val nuovadata = etDataNascita.text.toString().trim()
            val nuovapassword = etPassword.text.toString()

            if (nuovonome.isEmpty() || nuovocognome.isEmpty() || nuovaemail.isEmpty() || nuovadata.isEmpty()) {
                Toast.makeText(this, "Completa tutti i campi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Aggiorna Email se cambiata
            if (nuovaemail != auth.currentUser?.email) {
                auth.currentUser?.updateEmail(nuovaemail)
                    ?.addOnFailureListener {
                        Toast.makeText(this, "Errore nell'aggiornamento email", Toast.LENGTH_SHORT).show()
                    }
            }

            // Aggiorna Password se cambiata
            if (etPassword.isEnabled) {
                if (nuovapassword.length < 6) {
                    Toast.makeText(this, "La nuova password deve avere almeno 6 caratteri", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                auth.currentUser?.updatePassword(nuovapassword)
                    ?.addOnSuccessListener {
                        Toast.makeText(this, "Password aggiornata", Toast.LENGTH_SHORT).show()
                    }
                    ?.addOnFailureListener {
                        Toast.makeText(this, "Errore nell'aggiornamento della password", Toast.LENGTH_SHORT).show()
                    }
            }

            etPassword.setText("*******")
            etPassword.isEnabled = false
            btnModificaPassword.isEnabled = true

            val datiAggiornati = mapOf(
                "nome" to nuovonome,
                "cognome" to nuovocognome,
                "email" to nuovaemail,
                "dataNascita" to nuovadata
            )

            firestore.collection("utenti").document(userId)
                .update(datiAggiornati)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profilo aggiornato", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Errore aggiornamento Firestore", Toast.LENGTH_SHORT).show()
                }

        }

        btnEliminaProfilo.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Conferma eliminazione")
                .setMessage(
                    "Vuoi davvero eliminare il profilo? Una volta eliminato, non potrai più recuperarlo." +
                            "sarai reindirizzato alla schermata di registrazione."
                )
                .setPositiveButton("Elimina") { _, _ ->

                    val userId = auth.currentUser?.uid

                    if (userId == null) {
                        Toast.makeText(this, "Utente non autenticato", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton

                    }

                    // 1. Elimina da Firestore
                    firestore.collection("utenti").document(userId)
                        .delete()
                        .addOnSuccessListener {
                            // 2. Elimina da FirebaseAuth
                            auth.currentUser?.delete()
                                ?.addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Profilo eliminato correttamente",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // 3. Torna alla RegistrazioneActivity
                                    val intent = Intent(this, RegistrazioneActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                                ?.addOnFailureListener {
                                    Toast.makeText(this, "Errore nell'eliminazione dell'account",
                                        Toast.LENGTH_SHORT).show()
                                }

                        }

                }
                .setNegativeButton("Annulla", null)
                .show()
        }

        btnEsci.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Conferma uscita")
                .setMessage("Uscire dall'account?")
                .setPositiveButton("Sì") { _, _ ->
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //L'utente non può tornare indietro con il tasto back
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        ibHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
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
    }
}