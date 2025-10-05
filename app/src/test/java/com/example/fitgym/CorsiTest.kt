package com.example.fitgym

import com.example.fitgym.corsi.CorsoData
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CorsiTest {

    private lateinit var corsiPrenotati: MutableList<CorsoData>
    private lateinit var corsoZumba: CorsoData

    @Before
    fun setUp() {
        corsiPrenotati = mutableListOf()

        corsoZumba = CorsoData(
            nomeCorso = "Zumba",
            giorno = 3,
            orario = "19:00",
            data = LocalDate.of(2025, 9, 25),
            citta = "Ancona",
            iscritto = true,
            descrizione = "Corso di Zumba per tutti i livelli"
        )

        corsiPrenotati.add(corsoZumba)
    }

    @Test
    fun testCorsoAggiuntoAllaLista() {
        assertTrue(corsiPrenotati.contains(corsoZumba))
    }

    @Test
    fun testRecuperoCorsoDaLista() {
        val corso = corsiPrenotati.firstOrNull { it.nomeCorso == "Zumba" }
        assertNotNull(corso)
        assertEquals("Ancona", corso?.citta)
        assertEquals(LocalDate.of(2025, 9, 25), corso?.data)
        assertEquals(3, corso?.giorno)
        assertTrue(corso?.iscritto == true)
    }

    @Test
    fun testDisiscrizioneCorso() {
        corsoZumba.iscritto = false
        assertFalse(corsoZumba.iscritto)
    }
}



