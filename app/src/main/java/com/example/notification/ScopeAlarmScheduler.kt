package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.ClassOccurrence
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ScopeAlarmScheduler {

    fun scheduleAlarm(context: Context, event: ClassOccurrence) {
        if (!event.hasAlarm) return

        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val triggerMillis = calculateNextTriggerMillis(event) ?: return

            val intent = Intent(context, ScopeAlarmReceiver::class.java).apply {
                putExtra("EXTRA_EVENT_TITLE", event.subjectName)
                putExtra("EXTRA_EVENT_TIME", "${event.startTime} - ${event.endTime}")
                putExtra("EXTRA_EVENT_ROOM", event.room)
                putExtra("EXTRA_EVENT_CATEGORY", event.category)
                putExtra("EXTRA_AUDIO_URI", event.audioUri)
            }

            val requestCode = event.id.hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }

            Log.i("ScopeAlarmScheduler", "Alarm scheduled for '${event.subjectName}' at millis: $triggerMillis")
        } catch (e: Exception) {
            Log.e("ScopeAlarmScheduler", "Failed to schedule alarm for event '${event.subjectName}': ${e.message}", e)
        }
    }

    fun cancelAlarm(context: Context, eventId: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, ScopeAlarmReceiver::class.java)
            val requestCode = eventId.hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.i("ScopeAlarmScheduler", "Alarm cancelled for event ID: $eventId")
            }
        } catch (e: Exception) {
            Log.e("ScopeAlarmScheduler", "Failed to cancel alarm for event ID '$eventId': ${e.message}", e)
        }
    }

    private fun calculateNextTriggerMillis(event: ClassOccurrence): Long? {
        val timeParts = event.startTime.split(":")
        val startHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 9
        val startMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

        val calendar = Calendar.getInstance()

        if (event.eventType == "ONE_TIME" && event.specificDate.isNotBlank()) {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val date = sdf.parse(event.specificDate) ?: return null
                val targetCal = Calendar.getInstance().apply {
                    time = date
                    set(Calendar.HOUR_OF_DAY, startHour)
                    set(Calendar.MINUTE, startMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (targetCal.timeInMillis < System.currentTimeMillis()) {
                    null // Already in past
                } else {
                    targetCal.timeInMillis
                }
            } catch (e: Exception) {
                null
            }
        } else {
            // RECURRING weekly event by dayOfWeek (1 = Sunday, ..., 7 = Saturday)
            val calendarDayOfWeek = when (event.dayOfWeek) {
                1 -> Calendar.SUNDAY
                2 -> Calendar.MONDAY
                3 -> Calendar.TUESDAY
                4 -> Calendar.WEDNESDAY
                5 -> Calendar.THURSDAY
                6 -> Calendar.FRIDAY
                7 -> Calendar.SATURDAY
                else -> Calendar.SUNDAY
            }

            calendar.apply {
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, startMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            var daysUntilTarget = calendarDayOfWeek - calendar.get(Calendar.DAY_OF_WEEK)
            if (daysUntilTarget < 0 || (daysUntilTarget == 0 && calendar.timeInMillis <= System.currentTimeMillis())) {
                daysUntilTarget += 7
            }
            calendar.add(Calendar.DAY_OF_YEAR, daysUntilTarget)
            return calendar.timeInMillis
        }
    }
}
