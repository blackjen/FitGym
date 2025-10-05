package com.example.fitgym.corsi
import java.time.LocalDate

data class CorsoData(
    val nomeCorso: String,
    val giorno: Int,           // DayOfWeek in numero (1 = Lunedì)
    val orario: String,
    val data: LocalDate,
    val citta: String,
    var iscritto: Boolean = false,
    val descrizione: String
)
