package com.example.fitgym.notifiche

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.fitgym.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun doWork(): Result {
        val titolo = inputData.getString("titolo") ?: "Promemoria Corso"
        val messaggio = inputData.getString("messaggio") ?: ""

        // Controlla permesso notifiche (solo Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // Non può chiedere permesso da Worker, salta la notifica
                return Result.failure()
            }
        }

        // Mostra notifica locale
        val builder = NotificationCompat.Builder(applicationContext, "reminder_channel")
            .setSmallIcon(R.drawable.ic_notifica)
            .setContentTitle(titolo)
            .setContentText(messaggio)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(applicationContext)
            .notify(System.currentTimeMillis().toInt(), builder.build())

        // Salva la notifica in Firestore
        val utente = auth.currentUser?.uid
        if (utente != null) {
            val nuovaNotifica = hashMapOf(
                "titolo" to titolo,
                "messaggio" to messaggio,
                "data" to Timestamp.now()
            )

            db.collection("utenti").document(utente)
                .update("notifiche", FieldValue.arrayUnion(nuovaNotifica))
                .addOnFailureListener {
                    // Non blocca la notifica locale se fallisce
                }
        }

        return Result.success()
    }
}
