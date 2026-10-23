package com.example.data.circadian

import com.example.data.model.DiagnosticPreferences
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import java.util.Calendar

enum class CircadianEnergyState(
    val titleAr: String,
    val subtitleAr: String,
    val emoji: String,
    val colorHex: String,
    val adviceAr: String
) {
    HIGH_PEAK(
        titleAr = "ذروة اليقظة والتركيز الفائق ⚡",
        subtitleAr = "أعلى مستويات الاستيعاب وحل المسائل الصعبة",
        emoji = "⚡",
        colorHex = "#10B981", // Glowing Emerald Green
        adviceAr = "طاقتك الذهنية في أوجها! صولا تقترح إنجاز المسائل الصعبة والبرمجة والفيزياء الآن."
    ),
    STEADY_FOCUS(
        titleAr = "تركيز مستقر ومنتظم ☀️",
        subtitleAr = "تدفق معرفي سلس ومثالي للمهام الاعتيادية",
        emoji = "☀️",
        colorHex = "#F59E0B", // Solar Amber
        adviceAr = "طاقتك متزنة ومثالية للمراجعة المنهجية وحل الواجبات ومتابعة قنوات المذاكرة."
    ),
    LOW_FATIGUE(
        titleAr = "طاقة منخفضة — بحاجة إلى استعادة النشاط 💤",
        subtitleAr = "إجهاد ذهني متوقع في المنحنى اليومي",
        emoji = "💤",
        colorHex = "#EF4444", // Crimson Red
        adviceAr = "البطارية الذهنية منخفضة. خذ استراحة قصيرة، راجع بطاقات التكرار، أو استمع لملخص صوتي خفيف."
    )
}

data class CircadianBatteryStatus(
    val percentage: Int, // 0 - 100
    val state: CircadianEnergyState,
    val currentHour: Int,
    val peakWindowLabel: String,
    val nextPeakDescription: String
)

object CircadianEnergyEngine {

    /**
     * Calculates the student's dynamic mental battery (0% - 100%)
     * based on time-of-day circadian rhythm, reported peak focus window,
     * and cognitive sessions completed today.
     */
    fun calculateEnergy(
        preferences: DiagnosticPreferences?,
        sessionsCompletedToday: Int = 0,
        calendar: Calendar = Calendar.getInstance()
    ): CircadianBatteryStatus {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val timeFloat = hour + (minute / 60f)

        // Baseline biological curve (human circadian alertness cycle):
        // 00:00 - 05:00: deep trough 15% - 25% (melatonin secretion)
        // 06:00 - 08:30: cortisol awakening response rising 35% -> 75%
        // 08:30 - 11:30: primary circadian peak 85% - 95%
        // 12:30 - 14:30: natural postprandial dip 50% - 60%
        // 15:30 - 19:30: secondary evening wave 75% - 88%
        // 20:30 - 23:30: decline toward sleep 55% -> 25%
        var baseEnergy = when {
            timeFloat < 5.0f -> 18f + (timeFloat / 5.0f) * 12f
            timeFloat < 8.5f -> 30f + ((timeFloat - 5.0f) / 3.5f) * 45f // 30 -> 75
            timeFloat < 12.0f -> 85f + ((timeFloat - 8.5f) / 3.5f) * 10f // 85 -> 95
            timeFloat < 15.0f -> 82f - ((timeFloat - 12.0f) / 3.0f) * 28f // 82 -> 54
            timeFloat < 19.5f -> 55f + ((timeFloat - 15.0f) / 4.5f) * 30f // 55 -> 85
            timeFloat < 22.0f -> 82f - ((timeFloat - 19.5f) / 2.5f) * 37f // 82 -> 45
            else -> 45f - ((timeFloat - 22.0f) / 2.0f) * 25f // 45 -> 20
        }

        // Student's custom self-reported peak productivity window boost
        val peakWindow = preferences?.peakWindow ?: "الصباح الباكر (06:00 - 11:00)"
        val isInStudentPeak = when {
            peakWindow.contains("06:00") && (hour in 6..11) -> true
            peakWindow.contains("12:00") && (hour in 12..16) -> true
            peakWindow.contains("17:00") && (hour in 17..22) -> true
            peakWindow.contains("23:00") && (hour in 23..24 || hour in 0..4) -> true
            else -> false
        }

        if (isInStudentPeak) {
            baseEnergy = (baseEnergy + 16f).coerceAtMost(98f)
        }

        // Subtract cognitive depletion from intensive sessions today (max 20% drain)
        val fatigueDrain = (sessionsCompletedToday * 4f).coerceAtMost(20f)
        val finalPercentage = (baseEnergy - fatigueDrain).toInt().coerceIn(10, 100)

        val state = when {
            finalPercentage >= 80 -> CircadianEnergyState.HIGH_PEAK
            finalPercentage >= 40 -> CircadianEnergyState.STEADY_FOCUS
            else -> CircadianEnergyState.LOW_FATIGUE
        }

        val nextPeakDesc = when {
            hour in 0..7 -> "الذروة القادمة: الصباح الباكر (09:00 ص)"
            hour in 8..11 -> "أنت في نافذة الذروة القصوى حالياً ⚡"
            hour in 12..14 -> "الذروة القادمة: موجة المساء الذهبية (05:00 م)"
            hour in 15..19 -> "أنت في موجة التركيز المسائية ☀️"
            else -> "الذروة القادمة: صباح الغد (09:00 ص)"
        }

        return CircadianBatteryStatus(
            percentage = finalPercentage,
            state = state,
            currentHour = hour,
            peakWindowLabel = peakWindow,
            nextPeakDescription = nextPeakDesc
        )
    }

    /**
     * Smart Sola Task Re-Ordering:
     * - High Energy (>80%): Sola automatically surfaces heavy cognitive tasks (hard math, coding, physics, exam prep) to top.
     * - Low Energy (<40%): Sola filters for light tasks (flashcard reviews, low effort, audio summaries) and suggests rest.
     * - Steady (40-79%): Balanced reordering.
     */
    fun reorderTasks(tasks: List<Task>, energyLevel: Int): List<Task> {
        val uncompleted = tasks.filter { !it.isCompleted }
        val completed = tasks.filter { it.isCompleted }

        val sortedUncompleted = when {
            energyLevel >= 80 -> {
                // High Energy: Surface heavy cognitive tasks (High Priority, Hard subjects, High Effort > 40m)
                uncompleted.sortedWith(
                    compareByDescending<Task> { it.priority == TaskPriority.HIGH }
                        .thenByDescending { isCognitivelyHeavy(it) }
                        .thenByDescending { it.effortMinutes }
                        .thenBy { it.dueTimestamp }
                )
            }
            energyLevel < 40 -> {
                // Low Battery: Surface light tasks (low effort, quick review, organization)
                uncompleted.sortedWith(
                    compareBy<Task> { it.effortMinutes }
                        .thenBy { isCognitivelyHeavy(it) }
                        .thenBy { it.priority == TaskPriority.HIGH }
                        .thenBy { it.dueTimestamp }
                )
            }
            else -> {
                // Steady Focus: Balanced prioritization
                uncompleted.sortedWith(
                    compareByDescending<Task> { it.priority }
                        .thenBy { it.dueTimestamp }
                )
            }
        }

        return sortedUncompleted + completed
    }

    private fun isCognitivelyHeavy(task: Task): Boolean {
        val title = task.title.lowercase()
        val subject = task.subjectName.lowercase()
        val heavyKeywords = listOf("خوارزم", "برمج", "رياضي", "فيزياء", "ذكاء", "كود", "معادلات", "امتحان", "مصفوف", "شجرة", "bst", "algorithm", "database")
        return heavyKeywords.any { title.contains(it) || subject.contains(it) } || task.effortMinutes >= 45
    }
}
