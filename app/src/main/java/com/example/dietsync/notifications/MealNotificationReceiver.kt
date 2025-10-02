package com.example.dietsync.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.dietsync.R
import java.util.Calendar

class MealNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "meal_reminder_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val mealName = intent.getStringExtra("meal") ?: "Meal Time!"
        val day = intent.getStringExtra("day") ?: "MONDAY"
        val hour = intent.getIntExtra("hour", 0)
        val minute = intent.getIntExtra("minute", 0)

        // Create channel (with custom sound if present)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Meal Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you about your scheduled meals"
                // If you have raw/alarm_tone.mp3, set it as the channel sound
                try {
                    val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.alarm_tone}")
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    setSound(soundUri, audioAttributes)
                } catch (ignored: Exception) { /* no custom sound or file missing */ }
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

        // Build notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Meal Reminder 🍽️")
            .setContentText("It’s time to eat: $mealName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Safe notify (Android 13+ requires POST_NOTIFICATIONS)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(makeRequestCode(day, hour, minute, mealName), builder.build())
        }

        // Reschedule this meal for next week (weekly repeat)
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val rescheduleIntent = Intent(context, MealNotificationReceiver::class.java).apply {
                putExtra("meal", mealName)
                putExtra("day", day)
                putExtra("hour", hour)
                putExtra("minute", minute)
            }
            val requestCode = makeRequestCode(day, hour, minute, mealName)
            val pending = PendingIntent.getBroadcast(
                context,
                requestCode,
                rescheduleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val cal = Calendar.getInstance()
            // map DayOfWeek(1..7 where 1=MONDAY) to Calendar constants
            val calendarDay = when (day.uppercase()) {
                "MONDAY" -> Calendar.MONDAY
                "TUESDAY" -> Calendar.TUESDAY
                "WEDNESDAY" -> Calendar.WEDNESDAY
                "THURSDAY" -> Calendar.THURSDAY
                "FRIDAY" -> Calendar.FRIDAY
                "SATURDAY" -> Calendar.SATURDAY
                "SUNDAY" -> Calendar.SUNDAY
                else -> Calendar.MONDAY
            }
            cal.set(Calendar.DAY_OF_WEEK, calendarDay)
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)

            // Move to next week (we just fired this week's)
            cal.add(Calendar.WEEK_OF_YEAR, 1)

            // schedule exact for next week
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pending)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pending)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun makeRequestCode(day: String, hour: Int, minute: Int, mealName: String): Int {
        return ("$day|$hour:$minute|$mealName").hashCode()
    }
}