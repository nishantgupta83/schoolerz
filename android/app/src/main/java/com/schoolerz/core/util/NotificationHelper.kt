package com.schoolerz.core.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.schoolerz.MainActivity
import com.schoolerz.R

/**
 * Helper object for managing local notifications in the Schoolerz app.
 * Handles notification channel creation, permission checking, and notification display.
 */
object NotificationHelper {
    const val CHANNEL_ID = "schoolerz_default"
    const val CHANNEL_NAME = "Schoolerz Notifications"
    const val CHANNEL_DESCRIPTION = "General notifications for Schoolerz app"

    private const val NOTIFICATION_ID_BASE = 1000
    private var notificationIdCounter = NOTIFICATION_ID_BASE

    /**
     * Creates the default notification channel for the app.
     * Must be called before showing any notifications on Android O and above.
     * Safe to call multiple times - only creates the channel once.
     *
     * @param context Application context
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Checks if notification permission is granted.
     * On Android 13+ (API 33+), this requires POST_NOTIFICATIONS permission.
     * On older versions, notifications are enabled by default.
     *
     * @param context Application context
     * @return true if notifications are enabled, false otherwise
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * Shows an immediate local notification.
     *
     * @param context Application context
     * @param title Notification title
     * @param body Notification body text
     * @param notificationId Optional custom notification ID. If not provided, auto-increments.
     */
    fun showNotification(
        context: Context,
        title: String,
        body: String,
        notificationId: Int = getNextNotificationId()
    ) {
        if (!areNotificationsEnabled(context)) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    /**
     * Schedules a notification to be shown after a delay.
     * Uses a Handler to post delayed execution on the main thread.
     *
     * @param context Application context
     * @param title Notification title
     * @param body Notification body text
     * @param delayMs Delay in milliseconds before showing the notification
     */
    fun scheduleNotification(
        context: Context,
        title: String,
        body: String,
        delayMs: Long
    ) {
        Handler(Looper.getMainLooper()).postDelayed({
            showNotification(context, title, body)
        }, delayMs)
    }

    /**
     * Generates a unique notification ID.
     *
     * @return Next available notification ID
     */
    private fun getNextNotificationId(): Int {
        return notificationIdCounter++
    }

    /**
     * Cancels a specific notification by ID.
     *
     * @param context Application context
     * @param notificationId ID of the notification to cancel
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    /**
     * Cancels all notifications from this app.
     *
     * @param context Application context
     */
    fun cancelAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
