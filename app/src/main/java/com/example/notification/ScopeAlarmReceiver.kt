package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class ScopeAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EXTRA_EVENT_TITLE") ?: "تنبيه موعد مجدول ⏰"
        val timeRange = intent.getStringExtra("EXTRA_EVENT_TIME") ?: ""
        val room = intent.getStringExtra("EXTRA_EVENT_ROOM") ?: ""
        val category = intent.getStringExtra("EXTRA_EVENT_CATEGORY") ?: "ACADEMIC"
        val audioUriString = intent.getStringExtra("EXTRA_AUDIO_URI") ?: ""

        Log.i("ScopeAlarmReceiver", "Alarm triggered for event: '$title' ($timeRange)")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "scope_routine_alarms"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = if (audioUriString.isNotBlank()) {
                try { Uri.parse(audioUriString) } catch (e: Exception) { RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) }
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                "تنبيهات الجدول والروتين الأكاديمي ⏰",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة إشعارات مواعيد المحاضرات والروتين الأكاديمي بـ SCOPE"
                enableVibration(true)
                setSound(soundUri, attributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            title.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val categoryEmoji = when (category) {
            "WORK" -> "💼"
            "SELF_DEV" -> "⚡"
            "PERSONAL" -> "🧘"
            else -> "🎓"
        }

        val bodyText = buildString {
            if (timeRange.isNotBlank()) append("الوقت: $timeRange")
            if (room.isNotBlank()) append(" | المكان: $room")
        }.ifBlank { "حين الموعد المجدول في خطتك اليومية" }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.scope_logo)
            .setContentTitle("$categoryEmoji $title")
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Custom audio play for custom URI on all Android versions if provided
        if (audioUriString.isNotBlank()) {
            try {
                val ringtone = RingtoneManager.getRingtone(context, Uri.parse(audioUriString))
                ringtone?.play()
            } catch (e: Exception) {
                Log.e("ScopeAlarmReceiver", "Failed to play custom ringtone: ${e.message}", e)
            }
        }

        val notificationId = (System.currentTimeMillis() % 10000).toInt()
        notificationManager.notify(notificationId, builder.build())
    }
}
