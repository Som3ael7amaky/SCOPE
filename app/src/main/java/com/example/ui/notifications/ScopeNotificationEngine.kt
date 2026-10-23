package com.example.ui.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.AppNotification
import com.example.data.model.DndPreferences
import com.example.data.model.NotificationChannelType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

/**
 * SCOPE Smart Notification Engine:
 * Manages 4 dedicated system channels, Do Not Disturb (DND) scheduling,
 * and in-app Notification Center history.
 */
object ScopeNotificationEngine {

    private val _notifications = MutableStateFlow<List<AppNotification>>(
        listOf(
            AppNotification(
                title = "حارس الشعلة 🔥",
                message = "لا تفوت شعلتك الدراسية! تبقى 4 ساعات قبل منتصف الليل لتسجيل نشاطك اليومي.",
                channel = NotificationChannelType.STREAK_GUARD,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 45
            ),
            AppNotification(
                title = "محاضرة الخوارزميات بعد 30 دقيقة 📅",
                message = "قاعة المدرج 3B — د. طارق محمود. تم تجهيز ملخص AVL Trees للمراجعة السريعة.",
                channel = NotificationChannelType.SCHEDULE_GUARD,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 120
            ),
            AppNotification(
                title = "همسة صولا الأكاديمية 🤖",
                message = "دخلت الآن نافذة إنتاجيتك الذهبية (الصباح الباكر)! ما رأيك في جلسة تركيز 25 دقيقة؟",
                channel = NotificationChannelType.SOLA_NUDGES,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 240
            )
        )
    )
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _dndPreferences = MutableStateFlow(DndPreferences(isEnabled = true, startHour = 23, startMinute = 0, endHour = 7, endMinute = 0))
    val dndPreferences: StateFlow<DndPreferences> = _dndPreferences.asStateFlow()

    private var channelsCreated = false

    fun initChannels(context: Context) {
        if (channelsCreated || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        NotificationChannelType.values().forEach { channelType ->
            val importance = when (channelType) {
                NotificationChannelType.STREAK_GUARD,
                NotificationChannelType.SCHEDULE_GUARD -> NotificationManager.IMPORTANCE_HIGH
                NotificationChannelType.SOLA_NUDGES,
                NotificationChannelType.CAMPUS_PINGS -> NotificationManager.IMPORTANCE_DEFAULT
            }
            val channel = NotificationChannel(channelType.channelId, channelType.titleAr, importance).apply {
                description = channelType.descriptionAr
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        channelsCreated = true
    }

    fun isDndActive(): Boolean {
        val dnd = _dndPreferences.value
        if (!dnd.isEnabled) return false

        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMin = now.get(Calendar.MINUTE)
        val currentTimeMinutes = currentHour * 60 + currentMin

        val startTimeMinutes = dnd.startHour * 60 + dnd.startMinute
        val endTimeMinutes = dnd.endHour * 60 + dnd.endMinute

        return if (startTimeMinutes > endTimeMinutes) {
            // Over midnight (e.g. 23:00 to 07:00)
            currentTimeMinutes >= startTimeMinutes || currentTimeMinutes < endTimeMinutes
        } else {
            currentTimeMinutes in startTimeMinutes until endTimeMinutes
        }
    }

    fun setDndEnabled(enabled: Boolean) {
        _dndPreferences.value = _dndPreferences.value.copy(isEnabled = enabled)
    }

    fun setDndHours(startHour: Int, startMin: Int, endHour: Int, endMin: Int) {
        _dndPreferences.value = DndPreferences(isEnabled = true, startHour = startHour, startMinute = startMin, endHour = endHour, endMinute = endMin)
    }

    fun postNotification(
        context: Context,
        channelType: NotificationChannelType,
        title: String,
        message: String,
        bypassDnd: Boolean = false
    ) {
        initChannels(context)

        // Record in in-app notification center
        val notificationItem = AppNotification(
            title = title,
            message = message,
            channel = channelType
        )
        _notifications.value = listOf(notificationItem) + _notifications.value

        if (!bypassDnd && isDndActive()) {
            // DND active, suppressed from system status bar
            return
        }

        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                channelType.ordinal,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, channelType.channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (_: SecurityException) {
            // Handled safely
        }
    }

    // Convenient trigger helpers
    fun triggerStreakGuard(context: Context, streak: Int) {
        postNotification(
            context = context,
            channelType = NotificationChannelType.STREAK_GUARD,
            title = "حافظ على شعلتك 🔥 ($streak أيام متتالية)",
            message = "تبقى ساعات قليلة قبل انتهاء اليوم! أكمل جلسة بومودورو واحدة لحماية شعلتك ومضاعفة شراراتك."
        )
    }

    fun triggerScheduleGuard(context: Context, className: String, timeRange: String, room: String) {
        postNotification(
            context = context,
            channelType = NotificationChannelType.SCHEDULE_GUARD,
            title = "موعد المحاضرة القادمة: $className 📅",
            message = "تبدأ المحاضرة خلال 30 دقيقة ($timeRange) في $room. تم تفعيل درع التركيز الأكاديمي."
        )
    }

    fun triggerSolaNudge(context: Context, peakWindow: String) {
        postNotification(
            context = context,
            channelType = NotificationChannelType.SOLA_NUDGES,
            title = "همسة صولا الذكية 🤖✨",
            message = "أهلاً بك! لقد حان وقتك المثالي للإنجاز ($peakWindow). صولا جهزت لك أهم مهمة دراسية للمقرر اليوم."
        )
    }

    fun triggerCampusPing(context: Context, friendName: String, roomName: String) {
        postNotification(
            context = context,
            channelType = NotificationChannelType.CAMPUS_PINGS,
            title = "دعوة مذاكرة جماعية من $friendName 👥",
            message = "انضم الآن إلى غرفة '$roomName' — 5 طلاب يدرسون بتركيز في الغرفة الهادئة."
        )
    }

    fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }
}
