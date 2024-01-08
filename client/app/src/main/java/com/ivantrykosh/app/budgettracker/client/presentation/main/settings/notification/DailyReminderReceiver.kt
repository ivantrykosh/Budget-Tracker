package com.ivantrykosh.app.budgettracker.client.presentation.main.settings.notification

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.presentation.splash_screen.SplashScreenActivity

/**
 * Daily reminder receiver
 */
class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("DailyReminderReceiver", "Received broadcast")
        context?.let {
            showNotification(
                context,
                context.getString(R.string.daily_reminder_title),
                context.getString(R.string.daily_reminder_message)
            )
        }
    }

    private fun showNotification(context: Context, title: String, content: String) {
        Log.d("DailyReminderReceiver", "Start show notification")
        // Create an explicit intent for the SplashScreenActivity
        val mainIntent = Intent(context, SplashScreenActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE)

        // Build the notification
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, Constants.Notification.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Show the notification
        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(1, builder.build())
        }
    }
}