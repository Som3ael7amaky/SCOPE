package com.example.ui.mascot

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.R

enum class SolaEmotionState(
    val primaryGlow: Color,
    val secondaryGlow: Color,
    val accentColor: Color,
    val emotionLabelAr: String,
    @get:DrawableRes val drawableRes: Int
) {
    IDLE(
        primaryGlow = Color(0xFF2563EB), // Royal Blue
        secondaryGlow = Color(0xFF06B6D4), // Cyan
        accentColor = Color(0xFF38BDF8),
        emotionLabelAr = "هادئ ومستعد",
        drawableRes = R.drawable.sola_idle
    ),
    HAPPY_CELEBRATION(
        primaryGlow = Color(0xFF10B981), // Emerald
        secondaryGlow = Color(0xFFF59E0B), // Amber / Gold
        accentColor = Color(0xFF34D399),
        emotionLabelAr = "محتفل ومتحمس",
        drawableRes = R.drawable.sola_happy
    ),
    EMPATHETIC_SAD(
        primaryGlow = Color(0xFF8B5CF6), // Purple
        secondaryGlow = Color(0xFFA78BFA), // Lavender
        accentColor = Color(0xFFC084FC),
        emotionLabelAr = "متعاطف وداعم",
        drawableRes = R.drawable.sola_sad
    ),
    THINKING_ANALYTICAL(
        primaryGlow = Color(0xFF0284C7), // Sky Blue
        secondaryGlow = Color(0xFF6366F1), // Indigo
        accentColor = Color(0xFF38BDF8),
        emotionLabelAr = "تفكير تحليلي عميق",
        drawableRes = R.drawable.sola_thinking
    ),
    ASSERTIVE_GUIDE(
        primaryGlow = Color(0xFFF97316), // Vivid Orange
        secondaryGlow = Color(0xFFEF4444), // Coral Red
        accentColor = Color(0xFFFB923C),
        emotionLabelAr = "تنبيه وإرشاد حازم",
        drawableRes = R.drawable.sola_assertive
    )
}

data class SolaGuidePrompt(
    val id: String = java.util.UUID.randomUUID().toString(),
    val emotion: SolaEmotionState,
    val title: String,
    val message: String,
    val actionLabel: String? = null,
    val actionTargetTab: String? = null, // "HOME", "STUDY", "SCHEDULE", "PROJECTS", "SOLA"
    val timestamp: Long = System.currentTimeMillis()
)
