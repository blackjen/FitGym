package com.example.fitgym

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitgym.accesso.LoginActivity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginUtenteTest {

    private val testEmail = "mario.rossi@test.com"
    private val testPassword = "password123"

    @Before
    fun setup() {
        Intents.init()
        ActivityScenario.launch(LoginActivity::class.java)
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun loginUtente() {
        // Inserisci email e password
        onView(withId(R.id.etEmail)).perform(typeText(testEmail), closeSoftKeyboard())
        onView(withId(R.id.etPassword)).perform(typeText(testPassword), closeSoftKeyboard())

        // Click sul bottone login
        onView(withId(R.id.btnLogin)).perform(click())

        // Aspetta che MainActivity si apra
        Thread.sleep(5000)

        Intents.intended(hasComponent(MainActivity::class.java.name))
    }
}
