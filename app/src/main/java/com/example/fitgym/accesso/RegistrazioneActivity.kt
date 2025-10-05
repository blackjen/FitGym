package com.example.fitgym.accesso

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitgym.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class RegistrazioneActivity : AppCompatActivity () {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var actvScegliCitta1: AutoCompleteTextView

    private fun mostraCaricamentoDialog(): AlertDialog {
        val builder = AlertDialog.Builder(this)
        builder.setView(layoutInflater.inflate(R.layout.dialog_caricamento, null))
        builder.setCancelable(false)
        return builder.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrazione)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        actvScegliCitta1 = findViewById(R.id.actvScegliCitta1)


        val etNome = findViewById<EditText>(R.id.etNome)
        val etCognome = findViewById<EditText>(R.id.etCognome)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etDataNascita = findViewById<EditText>(R.id.etDataNascita)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfermaPassword = findViewById<EditText>(R.id.etConfermaPassword)
        val btnRegistrazione = findViewById<Button>(R.id.btnRegistrazione)
        val textView = findViewById<TextView>(R.id.tvVaiAlLogin)


        //Crea la lista e la mette nell'AutoCompleteTextView
        val listacitta = listOf("Ancona","Bologna","Firenze","Milano","Napoli","Roma","Torino")
        val adapter = ArrayAdapter(this, R.layout.menu_tendina, listacitta)
        actvScegliCitta1.setAdapter(adapter)

        //Disabilita tastiera e mostra menù a tendina
        actvScegliCitta1.setOnClickListener {
            actvScegliCitta1.showDropDown()
        }


        // Disabilita tastiera e input manuale
        etDataNascita.inputType = android.text.InputType.TYPE_NULL
        etDataNascita.isFocusable = false

        //Permette di scrivere registrare la data di nascita tramite calendario
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

        btnRegistrazione.setOnClickListener{

            btnRegistrazione.isEnabled = false // Disabilita bottone

            val caricamentoDialog = mostraCaricamentoDialog()
            caricamentoDialog.show()

            val cittaselezionata = actvScegliCitta1.text.toString().trim()
            val nome = etNome.text.toString().trim()
            val cognome = etCognome.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val dataNascita = etDataNascita.text.toString().trim()
            val password = etPassword.text.toString()
            val confermaPassword = etConfermaPassword.text.toString()

            if(cittaselezionata.isEmpty()) {
                Toast.makeText(this, "Selezionare Città", Toast.LENGTH_LONG).show()
                caricamentoDialog.dismiss()
                btnRegistrazione.isEnabled = true
                return@setOnClickListener
            }

            //Elimina spazi dalle password
            if (password.trim() != password) {
                Toast.makeText(this, "La password non può iniziare o finire con spazi.",
                    Toast.LENGTH_SHORT).show()
                caricamentoDialog.dismiss()
                btnRegistrazione.isEnabled = true // Abilita bottone
                return@setOnClickListener
            }

            //Elimina spazi dalle password
            if (password.contains(" ")) {
                Toast.makeText(this, "La password non può contenere spazi.",
                    Toast.LENGTH_SHORT).show()
                caricamentoDialog.dismiss()
                btnRegistrazione.isEnabled = true
                return@setOnClickListener
            }

            //Elimina spazi dalla confermaPassword
            if (confermaPassword.trim() != confermaPassword) {
                Toast.makeText(this, "La password non può iniziare o finire con spazi.",
                    Toast.LENGTH_SHORT).show()
                caricamentoDialog.dismiss()
                btnRegistrazione.isEnabled = true
                return@setOnClickListener
            }

            //Elimina spazi dalla confermaPassword
            if (confermaPassword.contains(" ")) {
                Toast.makeText(this, "La password non può contenere spazi.",
                    Toast.LENGTH_SHORT).show()
                caricamentoDialog.dismiss()
                btnRegistrazione.isEnabled = true
                return@setOnClickListener
            }

            // Controlla che nessun campo sia vuoto
            if (nome.isEmpty() || cognome.isEmpty() || email.isEmpty() ||
                dataNascita.isEmpty() || password.isEmpty() || confermaPassword.isEmpty()
            ) {
                Toast.makeText(this, "Compila tutti i campi.", Toast.LENGTH_SHORT).show()
                caricamentoDialog.dismiss()
                btnRegistrazione.isEnabled = true
                return@setOnClickListener
            }

            //Controlla che la password sia lunga almeno 6 caratteri
            if (password.length < 6) {
                Toast.makeText(this, "La password deve essere lunga almeno 6 caratteri.",
                    Toast.LENGTH_SHORT).show()
                caricamentoDialog.dismiss()
                btnRegistrazione.isEnabled = true
                return@setOnClickListener
            }

            //Controlla che la password corrisponda a confermaPassword
            if (password != confermaPassword) {
                Toast.makeText(this, "Le password non coincidono.", Toast.LENGTH_SHORT).show()
                caricamentoDialog.dismiss()
                btnRegistrazione.isEnabled = true
                return@setOnClickListener
            }

            //Controlla validità email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Inserisci una email valida.", Toast.LENGTH_SHORT).show()
                caricamentoDialog.dismiss()
                btnRegistrazione.isEnabled = true
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(this, "Email già registrata.", Toast.LENGTH_SHORT).show()
                        caricamentoDialog.dismiss()
                        btnRegistrazione.isEnabled = true
                        return@addOnCompleteListener
                    }
                    if (task.isSuccessful) {
                        // Registrazione riuscita, salvo i dati in Firestore
                        val userId = auth.currentUser?.uid ?:""
                        val utenteMap = hashMapOf(
                            "città" to cittaselezionata,
                            "nome" to nome,
                            "cognome" to cognome,
                            "email" to email,
                            "dataNascita" to dataNascita,
                            "corsiIscritti" to listOf<String>(),
                            "saldo" to 0,
                            "ingressi" to 0,
                            "abbonamentoScadenza" to null
                        )



                        firestore.collection("utenti")
                            .document(userId)
                            .set(utenteMap)
                            .addOnSuccessListener {
                                caricamentoDialog.dismiss()
                                Toast.makeText(
                                    this, "Registrazione effettuata correttamente.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                //Passa alla schermata di ConfermaRegistrazione
                                val intent = Intent(
                                    this,
                                    ConfermaRegistrazioneActivity::class.java
                                )
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                caricamentoDialog.dismiss()
                                btnRegistrazione.isEnabled = true // Abilita bottone
                                Toast.makeText(
                                    this, "Errore nel salvataggio dati", Toast.LENGTH_LONG
                                ).show()
                            }

                    } else {
                        caricamentoDialog.dismiss()
                        btnRegistrazione.isEnabled = true // Abilita bottone

                        // Registrazione fallita
                        Toast.makeText(
                            this, "Errore registrazione: " +
                                    "${task.exception?.message}", Toast.LENGTH_LONG
                        ).show()
                    }
                }

        }

        //Rende cliccabile la scritta "Vai al Login"
        val spannable = SpannableString("Sei già registrato?  Vai al Login")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@RegistrazioneActivity, LoginActivity::class.java)
                startActivity(intent)
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = Color.BLUE
            }

        }
        spannable.setSpan(clickableSpan, 21, 33, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

}