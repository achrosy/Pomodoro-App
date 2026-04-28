package com.example.pomodorotimer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class PomodoroApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Pomodoro Timer",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Pomodoro session alerts"
            enableVibration(true)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID      = "pomodoro_channel"
        const val NOTIFICATION_ID = 1
    }
}