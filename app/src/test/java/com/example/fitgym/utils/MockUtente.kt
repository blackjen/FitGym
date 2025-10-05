package com.example.fitgym.utils

import java.time.LocalDate

// classe che simula una data class utente
data class MockUtente(
    val nome: String,
    val cognome: String,
    val email: String,
    val citta: String,
    val dataNascita: LocalDate,
    var corsiIscritti: MutableList<String> = mutableListOf(),
    var saldo: Int = 0,
    var ingressi: Int = 0,
    var abbonamentoScadenza: LocalDate? = null
) {

    fun iscriviACorso(nomeCorso: String) {
        if (!corsiIscritti.contains(nomeCorso)) {
            corsiIscritti.add(nomeCorso)
        }
    }

    fun disiscriviDaCorso(nomeCorso: String) {
        corsiIscritti.remove(nomeCorso)
    }

    fun haCorso(nomeCorso: String): Boolean {
        return corsiIscritti.contains(nomeCorso)
    }

}