package com.example.fitgym.notifiche

data class NotificaData(
    val titolo: String = "",
    val messaggio: String = "",
    val data: com.google.firebase.Timestamp? = null
) {
    fun getDataFormattata(): String {
        return data?.toDate()?.let {
            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                .format(it)
        } ?: ""
    }
}


