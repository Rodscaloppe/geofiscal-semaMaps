package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object NotificationHelper {

    private const val CHANNEL_CRITICAL_ID = "sema_critical_alerts"
    private const val CHANNEL_SYNC_ID = "sema_sync_alerts"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val criticalChannel = NotificationChannel(
                CHANNEL_CRITICAL_ID,
                "Alertas Críticos SEMA-MT (Irregularidades)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações urgentes de infrações ambientais, embargos e desmatamento"
                enableVibration(true)
                setShowBadge(true)
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC_ID,
                "Sincronização e Fila Offline",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alertas de sincronização de relatórios de vistoria e restauração de rede"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(criticalChannel)
            notificationManager.createNotificationChannel(syncChannel)
        }
    }

    /**
     * Sends an immediate push notification alerting about an environmental irregularity detected by SEMA-MT.
     */
    fun showImmediateIrregularityAlert(
        context: Context,
        municipality: String,
        irregularityType: String,
        details: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_CRITICAL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("ALERTA CRÍTICO SEMA-MT: $irregularityType")
            .setContentText("Localidade: $municipality - $details")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Irregularidade identificada durante vistoria em $municipality:\n$details\n\nAbra o app para gerar laudo oficial com QR Code e registrar auto de fiscalização.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            // Missing POST_NOTIFICATIONS runtime permission on Android 13+
            e.printStackTrace()
        }
    }

    /**
     * Sends notification when network is restored and offline records are queued or synced.
     */
    fun showSyncStatusNotification(
        context: Context,
        title: String,
        message: String,
        isSuccess: Boolean = true
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val icon = if (isSuccess) android.R.drawable.stat_sys_upload_done else android.R.drawable.stat_sys_warning

        val notification = NotificationCompat.Builder(context, CHANNEL_SYNC_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(2001, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
