package com.example.fitgym.corsi

import java.time.DayOfWeek

data class CorsoBase (
    val nomeCorso: String,
    val giorno: DayOfWeek,
    val orario: String,
    val descrizione: String
    )