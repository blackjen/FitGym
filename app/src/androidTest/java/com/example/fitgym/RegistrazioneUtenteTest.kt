package com.example.fitgym

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitgym.accesso.ConfermaRegistrazioneActivity
import com.example.fitgym.accesso.RegistrazioneActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrazioneUtenteTest {

    @Before
    fun setup() {
        Intents.init()
        ActivityScenario.launch(RegistrazioneActivity::class.java)
    }

    @After
    fun tearDown() {
        Intents.release()

        //Elimina l'utente appena registrato
        FirebaseAuth.getInstance().currentUser?.delete()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("utenti").document(userId).delete()
        }

    }

    @Test
    fun registrazioneUtente() {
        // Compila i campi validi
        onView(withId(R.id.actvScegliCitta1)).perform(replaceText("Roma"), closeSoftKeyboard())
        onView(withId(R.id.etNome)).perform(typeText("Luigi"), closeSoftKeyboard())
        onView(withId(R.id.etCognome)).perform(typeText("Bianchi"), closeSoftKeyboard())
        onView(withId(R.id.etEmail)).perform(typeText("luigi.bianchi@test.com"), closeSoftKeyboard())
        onView(withId(R.id.etDataNascita)).perform(replaceText("01/01/2000"), closeSoftKeyboard())
        onView(withId(R.id.etPassword)).perform(typeText("password123"), closeSoftKeyboard())
        onView(withId(R.id.etConfermaPassword)).perform(typeText("password123"), closeSoftKeyboard())

        // Click registrazione
        onView(withId(R.id.btnRegistrazione)).perform(click())

        Thread.sleep(5000)

        // Verifica che parta l'Intent per ConfermaRegistrazioneActivity
        Intents.intended(hasComponent(ConfermaRegistrazioneActivity::class.java.name))
    }
}
