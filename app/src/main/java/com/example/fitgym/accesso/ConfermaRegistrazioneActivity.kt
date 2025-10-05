package com.example.fitgym.accesso

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.fitgym.R

class ConfermaRegistrazioneActivity : AppCompatActivity () {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confermaregistrazione)

        //Passa alla schermata di Login
        val btnSchermataLogin = findViewById<Button>(R.id.btnSchermataLogin)
        btnSchermataLogin.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

}