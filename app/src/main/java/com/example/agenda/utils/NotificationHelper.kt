package com.example.agenda.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.agenda.R
import com.example.agenda.data.model.Cours
import com.example.agenda.ui.main.MainActivity
import java.util.concurrent.TimeUnit

/**
 * Classe utilitaire pour gérer les notifications locales
 */
object NotificationHelper {

    private const val CHANNEL_ID = "cours_notifications"
    private const val CHANNEL_NAME = "Rappels de cours"
    private const val CHANNEL_DESCRIPTION = "Notifications pour les cours à venir"

    /**
     * Crée le canal de notification (requis pour Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Planifie une notification pour un cours
     * @param context Le contexte de l'application
     * @param cours Le cours pour lequel planifier la notification
     */
    fun scheduleNotification(context: Context, cours: Cours) {
        if (!cours.notificationActive) return

        // Calcule le délai avant le cours (15 minutes avant)
        val delay = calculateDelay(cours)

        if (delay > 0) {
            val data = Data.Builder()
                .putInt("COURS_ID", cours.id)
                .putString("COURS_NAME", cours.nomCours)
                .putString("COURS_SALLE", cours.salle)
                .putString("COURS_HEURE", cours.heureDebut)
                .build()

            val notificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("cours_${cours.id}")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "notification_${cours.id}",
                ExistingWorkPolicy.REPLACE,
                notificationWork
            )
        }
    }

    /**
     * Annule la notification d'un cours
     */
    fun cancelNotification(context: Context, coursId: Int) {
        WorkManager.getInstance(context).cancelAllWorkByTag("cours_$coursId")
    }

    /**
     * Calcule le délai avant d'envoyer la notification
     * Retourne le délai en millisecondes, ou -1 si le cours est déjà passé
     */
    private fun calculateDelay(cours: Cours): Long {
        // Cette fonction devrait calculer le temps réel jusqu'au prochain cours
        // Pour simplifier, on retourne un délai fixe pour la démonstration
        // Dans une vraie app, il faudrait utiliser Calendar et calculer le temps
        // jusqu'au prochain jour correspondant

        // Exemple simplifié : notification dans 15 minutes (pour test)
        return TimeUnit.MINUTES.toMillis(15)
    }

    /**
     * Affiche une notification immédiate
     */
    fun showNotification(
        context: Context,
        coursId: Int,
        coursName: String,
        salle: String,
        heure: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            coursId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Cours dans 15 minutes")
            .setContentText("$coursName - Salle $salle à $heure")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$coursName\nSalle: $salle\nHeure: $heure"))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.notify(coursId, notification)
    }
}

/**
 * Worker pour afficher les notifications en arrière-plan
 */
class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val coursId = inputData.getInt("COURS_ID", -1)
        val coursName = inputData.getString("COURS_NAME") ?: ""
        val salle = inputData.getString("COURS_SALLE") ?: ""
        val heure = inputData.getString("COURS_HEURE") ?: ""

        if (coursId != -1 && coursName.isNotEmpty()) {
            NotificationHelper.showNotification(
                applicationContext,
                coursId,
                coursName,
                salle,
                heure
            )
        }

        return Result.success()
    }
}