package com.ivantrykosh.app.budgettracker.client

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class
 */
@HiltAndroidApp
class BudgetTrackerApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        AppPreferences.setup(applicationContext)

        createNotificationChannel()
    }

    /**
     * Create notification channel to send notifications
     */
    private fun createNotificationChannel() {
        val name: CharSequence = Constants.Notification.CHANNEL_NAME
        val description = Constants.Notification.CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(Constants.Notification.CHANNEL_ID, name, importance)
        channel.description = description

        // Register the channel with the system
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
    }
}