package com.example.fitgym.accesso

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.fitgym.R

class SchermataBenvenutoActivity : AppCompatActivity () {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schermatabenvenuto)

        //Passa alla schermata di Login
        val btnSchermataLogin = findViewById<Button>(R.id.btnSchermataLogin)
        btnSchermataLogin.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        //Passa alla schermata di Registrazione
        val btnSchermataRegistrazione = findViewById<Button>(R.id.btnSchermataRegistrazione)
        btnSchermataRegistrazione.setOnClickListener{
            val intent = Intent(this, RegistrazioneActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}