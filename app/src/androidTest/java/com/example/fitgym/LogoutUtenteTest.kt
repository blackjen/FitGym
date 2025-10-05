package com.example.fitgym

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitgym.accesso.LoginActivity
import com.example.fitgym.utente.ProfiloActivity
import com.google.firebase.auth.FirebaseAuth
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogoutUtenteTest {

    private val testEmail = "mario.rossi@test.com"
    private val testPassword = "password123"

    @Before
    fun setup() {
        // Logga l'utente prima del test
        val auth = FirebaseAuth.getInstance()
        val task = auth.signInWithEmailAndPassword(testEmail, testPassword)
        com.google.android.gms.tasks.Tasks.await(task) // Non va avanti finchè non viene completata la task

        Intents.init()
        ActivityScenario.launch(ProfiloActivity::class.java)

    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun logoutUtente() {

        onView(withId(R.id.btnEsci))
            .perform(scrollTo(), click())


        onView(withText("Sì"))
            .inRoot(isDialog())
            .perform(click())


        Thread.sleep(2000)

        Intents.intended(hasComponent(LoginActivity::class.java.name))
    }
}
