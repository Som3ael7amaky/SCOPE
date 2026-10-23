package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

object ScopeWidgetManager {
    private const val PREFS_NAME = "scope_live_widget_prefs"
    private const val KEY_STREAK = "key_streak"
    private const val KEY_SPARKS = "key_sparks"
    private const val KEY_TOKENS = "key_tokens"
    private const val KEY_BATTERY = "key_battery"
    private const val KEY_MOOD = "key_mood"

    fun saveMetrics(
        context: Context,
        streak: Int,
        sparks: Int,
        tokens: Int,
        batteryPercent: Int,
        mood: String = "happy"
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_STREAK, streak)
            .putInt(KEY_SPARKS, sparks)
            .putInt(KEY_TOKENS, tokens)
            .putInt(KEY_BATTERY, batteryPercent)
            .putString(KEY_MOOD, mood)
            .apply()

        updateAllWidgets(context)
    }

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val componentName = ComponentName(context, ScopeLiveWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isEmpty()) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val streak = prefs.getInt(KEY_STREAK, 3)
        val sparks = prefs.getInt(KEY_SPARKS, 180)
        val tokens = prefs.getInt(KEY_TOKENS, 25)
        val battery = prefs.getInt(KEY_BATTERY, 85)
        val mood = prefs.getString(KEY_MOOD, "happy") ?: "happy"

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_scope_live)

            // Dynamic Sola Mascot Image
            val mascotResId = when (mood) {
                "thinking" -> R.drawable.sola_thinking
                "idle" -> R.drawable.sola_idle
                "sad" -> R.drawable.sola_sad
                else -> R.drawable.sola_happy
            }
            views.setImageViewResource(R.id.widget_sola_mascot, mascotResId)

            // Metrics
            views.setTextViewText(R.id.widget_streak_text, "🔥 $streak")
            views.setTextViewText(R.id.widget_sparks_text, "⚡ $sparks")
            views.setTextViewText(R.id.widget_tokens_text, "🪙 $tokens")
            views.setTextViewText(R.id.widget_battery_text, "🔋 $battery%")

            // Battery state mood description
            val moodText = when {
                battery >= 80 -> "• ذروة التركيز ⚡"
                battery >= 40 -> "• تدفق مستقر ☀️"
                else -> "• طاقة منخفضة 💤"
            }
            views.setTextViewText(R.id.widget_sola_mood, moodText)

            // One-Tap Quick Start Pomodoro Trigger
            val focusIntent = Intent(context, MainActivity::class.java).apply {
                action = "com.example.ACTION_QUICK_FOCUS"
                putExtra("EXTRA_QUICK_FOCUS", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val focusPendingIntent = PendingIntent.getActivity(
                context,
                1001,
                focusIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_quick_focus, focusPendingIntent)

            // General click opens Home
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                1002,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
