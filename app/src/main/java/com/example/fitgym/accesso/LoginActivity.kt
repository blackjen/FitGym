package com.example.fitgym.accesso

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
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitgym.MainActivity
import com.example.fitgym.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity () {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Funzione che genera la schermata di caricamento
    private fun mostraCaricamentoDialog(): AlertDialog {
        val builder = AlertDialog.Builder(this)
        builder.setView(layoutInflater.inflate(R.layout.dialog_caricamento, null))
        builder.setCancelable(false)
        return builder.create()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val tvVaiAllaRegistrazione = findViewById<TextView>(R.id.tvVaiAllaRegistrazione)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Compila tutti i campi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false // Disabilita bottone

            // Mostra il caricamento
            val caricamentoDialog = mostraCaricamentoDialog()
            caricamentoDialog.show()



            // Login con Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    btnLogin.isEnabled = true // Abilita bottone
                    if (task.isSuccessful) {
                        // Login riuscito, recupera dati utente da Firestore
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            firestore.collection("utenti").document(userId).get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        val nome = document.getString("nome") ?: ""
                                        val cognome = document.getString("cognome") ?: ""

                                        caricamentoDialog.dismiss()

                                        Toast.makeText(this, "Benvenuto, $nome $cognome!",
                                            Toast.LENGTH_SHORT).show()

                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        caricamentoDialog.dismiss()
                                        Toast.makeText(this, "Dati utente non trovati.",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    caricamentoDialog.dismiss()
                                    Toast.makeText(this, "Errore caricamento dati",
                                        Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        caricamentoDialog.dismiss()
                        Toast.makeText(this, "Login fallito, email o password errate",
                            Toast.LENGTH_SHORT).show()
                    }
                }

        }

        //Rende cliccabile la scritta "Registrati!"
        val spannable = SpannableString("Non hai un account?  Registrati!")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, RegistrazioneActivity::class.java)
                startActivity(intent)
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = Color.BLUE
            }

        }
        spannable.setSpan(clickableSpan, 21, 32, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvVaiAllaRegistrazione.text = spannable
        tvVaiAllaRegistrazione.movementMethod = LinkMovementMethod.getInstance()
    }
}