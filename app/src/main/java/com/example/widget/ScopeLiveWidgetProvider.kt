package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

/**
 * ScopeLiveWidgetProvider: Android Home Screen Widget Provider for SCOPE.
 * Displays real-time 3D Sola Mascot, Streak 🔥, Sparks ⚡, Tokens 🪙, Battery 🔋,
 * and a zero-latency One-Tap Quick Start Pomodoro focus trigger.
 */
class ScopeLiveWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        ScopeWidgetManager.updateAllWidgets(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        ScopeWidgetManager.updateAllWidgets(context)
    }
}
