package com.example.ui.mascot

import com.example.data.model.ClassOccurrence
import com.example.data.model.Exam
import com.example.data.model.StudySession
import com.example.data.model.Task
import com.example.data.model.TaskPriority

object SolaGuideEngine {

    fun evaluateAcademicState(
        tasks: List<Task>,
        occurrences: List<ClassOccurrence>,
        recentSessions: List<StudySession>,
        exams: List<Exam> = emptyList(),
        isSolaThinking: Boolean,
        isTimerRunning: Boolean,
        justCompletedSession: Boolean = false
    ): SolaGuidePrompt {
        // Priority 0.5: Sola Personal Context Respect / Life Tasks Suppressor
        val activePersonalTask = tasks.firstOrNull { !it.isCompleted && it.isPersonal }
        if (activePersonalTask != null) {
            return SolaGuidePrompt(
                emotion = SolaEmotionState.IDLE,
                title = "وضع احترام الوقت الشخصي ☕",
                message = "أنت الآن في وقت مخصص لحياتك الشخصية (${activePersonalTask.title} • ${activePersonalTask.personalCategory}). تمت تهدئة التنبيهات الدراسية لحين انتهائك!",
                actionLabel = "عرض الوقت الشخصي",
                actionTargetTab = "HOME"
            )
        }

        // Priority 1: Sola is currently thinking (High Thinking Mode)
        if (isSolaThinking) {
            return SolaGuidePrompt(
                emotion = SolaEmotionState.THINKING_ANALYTICAL,
                title = "صولا تفكر بعمق...",
                message = "أقوم الآن بتحليل سياقك الأكاديمي، ومراجعة أوزان المقررات والاختبارات لتوليد أدق مسار.",
                actionLabel = "عرض التفاصيل",
                actionTargetTab = "SOLA"
            )
        }

        // Priority 1.5: Burnout Protection Shield (High Focus Overload >= 8h)
        val now = System.currentTimeMillis()
        val focusMinutesToday = recentSessions.filter { now - it.timestamp < 86400000L }.sumOf { it.durationMinutes }
        if (focusMinutesToday >= 480) {
            return SolaGuidePrompt(
                emotion = SolaEmotionState.ASSERTIVE_GUIDE,
                title = "درع حماية الإجهاد الذهني مفعل 🛡️",
                message = "سجلت أكثر من 8 ساعات تركيز مكثف اليوم! ذاكرتك بحاجة إلى استراحة إلزامية 20 دقيقة لحماية كفاءة الدماغ.",
                actionLabel = "تفعيل استراحة الدرع",
                actionTargetTab = "BURNOUT_SHIELD"
            )
        }

        // Priority 1.8: 24-48h Pre-Exam Simulator Trigger
        val upcomingExam48h = exams.firstOrNull { it.examTimestamp - now in 0..(48 * 3600 * 1000L) }
        if (upcomingExam48h != null) {
            return SolaGuidePrompt(
                emotion = SolaEmotionState.ASSERTIVE_GUIDE,
                title = "محاكاة الامتحان جاهزة! 🎯",
                message = "امتحان '${upcomingExam48h.title}' خلال أقل من 48 ساعة. خض محاكاة الـ 5 أسئلة لاختبار استيعابك وكشف الثغرات مبكراً!",
                actionLabel = "بدء محاكاة الامتحان",
                actionTargetTab = "EXAM_SIMULATOR"
            )
        }

        // Priority 2: Focus Accomplishment Celebration
        if (justCompletedSession || (recentSessions.isNotEmpty() && System.currentTimeMillis() - recentSessions.first().timestamp < 300000L)) {
            val lastSession = recentSessions.firstOrNull()
            return SolaGuidePrompt(
                emotion = SolaEmotionState.HAPPY_CELEBRATION,
                title = "إنجاز رائع! أتممت جلسة التركيز 🎉",
                message = "تهانينا! أكملت بنجاح جلسة دراسية لمقرر ${lastSession?.subjectName ?: "الدراسة"}. هذا هو الزخم الحقيقي نحو التفوق!",
                actionLabel = "تسجيل التأمل",
                actionTargetTab = "STUDY"
            )
        }

        // Priority 3: Schedule Overlap & Conflict Detection
        val conflict = detectScheduleConflict(occurrences)
        if (conflict != null) {
            return SolaGuidePrompt(
                emotion = SolaEmotionState.ASSERTIVE_GUIDE,
                title = "تنبيه تعارض في الجدول ⚠️",
                message = "رصدت تداخلاً زمنياً بين محاضرة '${conflict.first.subjectName}' و'${conflict.second.subjectName}' في نفس اليوم. دعنا نعيد ترتيب الوقت!",
                actionLabel = "فحص الجدول",
                actionTargetTab = "SCHEDULE"
            )
        }

        // Priority 4: Empathetic Support for High Workload / Overdue
        val highPriorityPending = tasks.filter { !it.isCompleted && it.priority == TaskPriority.HIGH }
        if (highPriorityPending.size >= 2) {
            return SolaGuidePrompt(
                emotion = SolaEmotionState.EMPATHETIC_SAD,
                title = "أنا معك، لا تدع الضغط يربكك 💜",
                message = "لديك مهام متراكمة ذات أولوية، والضغط الأكاديمي أمر طبيعي يمر به الجميع. ما رأيك أن نبدأ بمهمة واحدة بسيطة لمدة 15 دقيقة فقط؟",
                actionLabel = "بدء مهمة خفيفة",
                actionTargetTab = "HOME"
            )
        }

        // Priority 5: Active Focus Timer Mode
        if (isTimerRunning) {
            return SolaGuidePrompt(
                emotion = SolaEmotionState.IDLE,
                title = "درع التركيز مفعل 🛡️",
                message = "أنا أحرس وقتك الآن وأحجب المشتتات. تنفس بعمق وركز في الصفحة التي أمامك فقط.",
                actionLabel = "عرض المؤقت",
                actionTargetTab = "STUDY"
            )
        }

        // Priority 6: First Launch / Onboarding Tour
        if (tasks.isEmpty() && occurrences.isEmpty() && recentSessions.isEmpty()) {
            return SolaGuidePrompt(
                emotion = SolaEmotionState.HAPPY_CELEBRATION,
                title = "أهلاً بك في SCOPE! أنا رفيقتك صولا 🌟",
                message = "أنا هنا لفهم سياقك الأكاديمي، وتنظيم جدول محاضراتك وجلسات تركيزك. لنبدأ بإضافة موادك الدراسية الآن!",
                actionLabel = "إضافة مادة للجدول",
                actionTargetTab = "SCHEDULE"
            )
        }

        // Priority 7: Default Encouraging Companion
        val pendingCount = tasks.count { !it.isCompleted }
        return SolaGuidePrompt(
            emotion = SolaEmotionState.IDLE,
            title = "مرحباً بك! رفيقتك صولا معك ✨",
            message = if (pendingCount > 0) {
                "لديك $pendingCount مهام اليوم، ونوافذ تركيز ممتازة متاحة لاستغلالها. أنا هنا متى ما أردت مشورة أكاديمية!"
            } else {
                "أنت في حالة توازن ممتازة اليوم! كل المهام الرئيسية منجزة، استغل الوقت في المراجعة الذاتية أو استراحة مستحقة."
            },
            actionLabel = "اسأل صولا",
            actionTargetTab = "SOLA"
        )
    }

    private fun detectScheduleConflict(occurrences: List<ClassOccurrence>): Pair<ClassOccurrence, ClassOccurrence>? {
        val groupedByDay = occurrences.groupBy { it.dayOfWeek }
        for ((_, dayOccurrences) in groupedByDay) {
            if (dayOccurrences.size < 2) continue
            for (i in 0 until dayOccurrences.size) {
                for (j in i + 1 until dayOccurrences.size) {
                    val o1 = dayOccurrences[i]
                    val o2 = dayOccurrences[j]
                    if (isTimeOverlapping(o1.startTime, o1.endTime, o2.startTime, o2.endTime)) {
                        return Pair(o1, o2)
                    }
                }
            }
        }
        return null
    }

    private fun isTimeOverlapping(start1: String, end1: String, start2: String, end2: String): Boolean {
        fun toMinutes(time: String): Int {
            val parts = time.split(":")
            if (parts.size < 2) return 0
            val h = parts[0].trim().toIntOrNull() ?: 0
            val m = parts[1].trim().toIntOrNull() ?: 0
            return h * 60 + m
        }
        val s1 = toMinutes(start1)
        val e1 = toMinutes(end1)
        val s2 = toMinutes(start2)
        val e2 = toMinutes(end2)
        return s1 < e2 && s2 < e1
    }
}
