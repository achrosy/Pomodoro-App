package com.example.pomodorotimer

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // ── Show notification when a session ends ──────────────────────
    fun showSessionEndNotification(sessionType: PomodoroViewModel.SessionType) {

        // Tapping the notification opens the app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, message) = when (sessionType) {
            PomodoroViewModel.SessionType.WORK ->
                "Work session done! 🎉" to "Time for a break. You earned it!"
            PomodoroViewModel.SessionType.SHORT_BREAK ->
                "Break over!" to "Ready to focus? Let's get back to work."
            PomodoroViewModel.SessionType.LONG_BREAK ->
                "Long break over!" to "Fully recharged? Start a new cycle!"
        }

        val notification = NotificationCompat.Builder(context, PomodoroApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)  // dismiss when tapped
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(PomodoroApp.NOTIFICATION_ID, notification)
    }

    // ── Cancel any active notification ─────────────────────────────
    fun cancelNotification() {
        notificationManager.cancel(PomodoroApp.NOTIFICATION_ID)
    }
}