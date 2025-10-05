package com.example.fitgym

import com.example.fitgym.utils.MockUtente
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class MockUtenteTest {

    private lateinit var utente: MockUtente
    private val corso = "CrossFit"

    @Before
    fun setUp() {
        utente = MockUtente(
            nome = "Mario",
            cognome = "Rossi",
            email = "mariorossi@example.com",
            citta = "Ancona",
            dataNascita = LocalDate.of(2000, 5, 10)
        )
    }

    @Test
    fun testCreazioneUtente() {
        assertEquals("Mario", utente.nome)
        assertEquals("Rossi", utente.cognome)
        assertEquals("mariorossi@example.com", utente.email)
        assertEquals("Ancona", utente.citta)
        assertEquals(LocalDate.of(2000, 5, 10), utente.dataNascita)
        assertTrue(utente.corsiIscritti.isEmpty())
    }

    @Test
    fun testPrenotazioneCorso() {
        utente.iscriviACorso(corso)
        assertTrue(utente.haCorso(corso))
        assertEquals(1, utente.corsiIscritti.size)
    }

    @Test
    fun testDisiscrizioneCorso() {
        utente.iscriviACorso(corso)
        utente.disiscriviDaCorso(corso)
        assertFalse(utente.haCorso(corso))
        assertEquals(0, utente.corsiIscritti.size)
    }
}
