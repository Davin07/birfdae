package com.birthdayreminder.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.birthdayreminder.MainActivity
import com.birthdayreminder.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        const val BIRTHDAY_CHANNEL_ID = "birthday_reminders"
        const val BIRTHDAY_CHANNEL_NAME = "Birthday Reminders"
        const val BIRTHDAY_CHANNEL_DESCRIPTION = "Notifications for upcoming birthdays"
        
        private const val NOTIFICATION_ID_BASE = 1000
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                BIRTHDAY_CHANNEL_ID,
                BIRTHDAY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = BIRTHDAY_CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showBirthdayNotification(
        birthdayId: Long,
        personName: String,
        age: Int? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("birthday_id", birthdayId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            birthdayId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = "🎉 Birthday Today!"
        val message = if (age != null) {
            "$personName is turning $age today!"
        } else {
            "It's $personName's birthday today!"
        }
        
        val notification = NotificationCompat.Builder(context, BIRTHDAY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // We'll need to create this icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(
                NOTIFICATION_ID_BASE + birthdayId.toInt(),
                notification
            )
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
            // This will be handled by the permission request in the UI
        }
    }
    
    fun showAdvanceBirthdayNotification(
        birthdayId: Long,
        personName: String,
        daysUntil: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("birthday_id", birthdayId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            birthdayId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = "🎂 Birthday Reminder"
        val message = "$personName's birthday is in $daysUntil day${if (daysUntil != 1) "s" else ""}!"
        
        val notification = NotificationCompat.Builder(context, BIRTHDAY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(
                NOTIFICATION_ID_BASE + birthdayId.toInt() + daysUntil * 10000,
                notification
            )
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
        }
    }
    
    fun cancelBirthdayNotification(birthdayId: Long) {
        val notificationManager = NotificationManagerCompat.from(context)
        // Cancel both today and advance notifications
        notificationManager.cancel(NOTIFICATION_ID_BASE + birthdayId.toInt())
        // Cancel advance notifications (1, 3, 7 days)
        listOf(1, 3, 7).forEach { days ->
            notificationManager.cancel(NOTIFICATION_ID_BASE + birthdayId.toInt() + days * 10000)
        }
    }
    
    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}