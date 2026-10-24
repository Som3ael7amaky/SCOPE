package com.example.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class LearningSignal {
    UNDERSTOOD,
    NEEDS_REVIEW,
    UNCLEAR,
    IMPORTANT
}

enum class TaskPriority {
    HIGH,
    MEDIUM,
    LOW
}

enum class OccurrenceStatus {
    SCHEDULED,
    ATTENDED,
    CANCELLED
}

data class DiagnosticPreferences(
    val academicGoal: String = "رفع المعدل التراكمي",
    val peakWindow: String = "الصباح الباكر (06:00 - 11:00)",
    val primaryChallenge: String = "صعوبة البدء ومقاومة الانطلاق",
    val studyStyle: String = "بومودورو 25 دقيقة",
    val solaTone: String = "متوازنة وتحليلية",
    val completedAt: Long = System.currentTimeMillis()
)

data class UserProfile(
    @DocumentId val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "student",
    val scopeId: String = "",
    val academicMajor: String = "",
    val university: String = "جامعة الملك سعود",
    val academicYear: String = "السنة الثالثة",
    val phoneNumber: String = "",
    val photoUrl: String = "",
    val avatarId: String = "avatar_scholar_owl",
    val language: String = "ar",
    val focusShieldEnabled: Boolean = false,
    val flameCount: Int = 3,
    val sparks: Int = 150, // Focus Sparks ⚡
    val tokens: Int = 20,  // Sola Tokens 🪙
    val xp: Int = 620,
    val level: Int = 4,
    val isPro: Boolean = false,
    val isDeveloper: Boolean = false,
    val customStatus: String = "طالب SCOPE طموح 🚀",
    val completedNightSessions: Int = 0,
    val completedKanbanTasks: Int = 0,
    val onboardingCompleted: Boolean = false,
    val diagnosticPreferences: DiagnosticPreferences = DiagnosticPreferences(),
    val equippedFrame: String = "frame_cyber_neon",
    val equippedAura: String = "aura_solar_amber",
    val equippedTitle: String = "مهندس الخوارزميات",
    val equippedGlassFx: String = "glass_holographic",
    val unlockedItemIds: List<String> = listOf(
        "frame_cyber_neon",
        "frame_default",
        "aura_solar_amber",
        "aura_default",
        "title_default",
        "glass_holographic",
        "glass_default"
    ),
    val unlockedBadgeIds: List<String> = listOf("badge_first_spark", "badge_streak_3", "badge_deep_dive"),
    @ServerTimestamp val createdAt: Date? = null
)

data class SubjectFile(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fileName: String = "document.pdf",
    val customAlias: String = "ملزمة المقرر",
    val fileType: String = "PDF", // "PDF", "IMAGE", "NOTES"
    val isPinned: Boolean = false,
    val category: String = "محاضرات",
    val subjectId: String = "",
    val subjectName: String = "",
    val uriString: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Subject(
    @DocumentId val id: String = "",
    val name: String = "",
    val code: String = "",
    val colorHex: String = "#2563EB",
    val instructor: String = "",
    val creditHours: Int = 3,
    val notesCount: Int = 0,
    val files: List<SubjectFile> = emptyList(),
    @ServerTimestamp val createdAt: Date? = null
)

data class Task(
    @DocumentId val id: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val title: String = "",
    val dueTimestamp: Long = System.currentTimeMillis() + 86400000L * 2,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val effortMinutes: Int = 45,
    val isCompleted: Boolean = false,
    val isPersonal: Boolean = false,
    val personalCategory: String = "شخصي",
    val signal: LearningSignal = LearningSignal.UNDERSTOOD,
    @ServerTimestamp val createdAt: Date? = null
)

data class Exam(
    @DocumentId val id: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val title: String = "",
    val examTimestamp: Long = System.currentTimeMillis() + 86400000L * 7,
    val weightPercentage: Int = 20,
    val topics: String = "",
    @ServerTimestamp val createdAt: Date? = null
)

data class StudySession(
    @DocumentId val id: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val durationMinutes: Int = 25,
    val reflectionSignal: LearningSignal = LearningSignal.UNDERSTOOD,
    val reflectionNotes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ClassOccurrence(
    @DocumentId val id: String = "",
    val subjectId: String = "",
    val subjectName: String = "", // Used as Event Title
    val dayOfWeek: Int = 1, // 1 = الأحد (Sunday) ... 7 = السبت (Saturday)
    val startTime: String = "09:00",
    val endTime: String = "10:30",
    val room: String = "القاعة 101",
    val eventType: String = "RECURRING", // "RECURRING" or "ONE_TIME"
    val category: String = "ACADEMIC", // "ACADEMIC", "WORK", "SELF_DEV", "PERSONAL"
    val categoryColorHex: String = "#38BDF8", // Default Academic Blue
    val specificDate: String = "", // e.g. "2026-10-24" if ONE_TIME
    val audioUri: String = "", // Custom Audio MP3/WAV file URI
    val hasAlarm: Boolean = true,
    val notes: String = "",
    val status: OccurrenceStatus = OccurrenceStatus.SCHEDULED
)

data class SolaMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String = "sola", // "user" or "sola"
    val text: String = "",
    val thinkingContent: String = "",
    val isHighThinking: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ShopCategory(val titleAr: String) {
    FRAME("إطارات 3D"),
    AURA("هالات صولا"),
    TITLE("الألقاب والشارات"),
    GLASS_FX("تأثيرات الزجاج")
}

data class ShopItem(
    val id: String,
    val name: String,
    val category: ShopCategory,
    val description: String,
    val sparksPrice: Int = 0,
    val tokensPrice: Int = 0,
    val previewColorHex: String = "#2563EB",
    val rarity: String = "نادر" // شائع، نادر، أسطوري
)

data class StudentBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean = false,
    val criteria: String = ""
)

enum class SearchCategory(val titleAr: String) {
    ALL("الكل"),
    SUBJECTS("المقررات"),
    TASKS("المهام"),
    AUDIO("الملخصات الصوتية"),
    FLASHCARDS("البطاقات"),
    SOLA_CHAT("محادثات صولا")
}

data class SearchResultItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: SearchCategory,
    val tag: String,
    val colorHex: String = "#2563EB",
    val targetTab: String = "HOME"
)

data class AudioSummary(
    val id: String,
    val subjectName: String,
    val title: String,
    val durationMinutes: Int,
    val keyConcepts: String
)

data class Flashcard(
    val id: String,
    val subjectName: String,
    val front: String,
    val back: String,
    val masteryLevel: Int = 1
)

data class ThemePack(
    val id: String,
    val name: String,
    val description: String,
    val previewColorHex: String = "#3B82F6",
    val homeBgRes: Int,
    val studyBgRes: Int,
    val scheduleBgRes: Int,
    val projectsBgRes: Int,
    val campusBgRes: Int,
    val isUnlocked: Boolean = true,
    val tokenPrice: Int = 0
)

enum class AmbientTrack(val id: String, val titleAr: String, val descriptionAr: String, val iconEmoji: String) {
    RAIN_ON_GLASS("rain", "مطر على الزجاج", "صوت قطرات المطر اللطيفة لتهدئة الذهن", "🌧️"),
    ACADEMIC_CAFE("cafe", "كافيه أكاديمي", "أجواء مقهى دراسي ومحادثات هادئة بعيدة", "☕"),
    DEEP_BROWN_NOISE("brown", "ضوضاء بنية عميقة", "ترددات عميقة لحجب المشتتات والتركيز الحاد", "🎧"),
    ALPHA_WAVES("alpha", "موجات ألفا (10Hz)", "ذبذبات ثنائية الأذن لتحفيز صفاء الذهن", "🌊"),
    MECHANICAL_TYPING("typing", "كتابة ميكانيكية", "نقرات لوحة مفاتيح إيقاعية تعزز الإنتاجية", "⌨️")
}

enum class KanbanColumn(val titleAr: String) {
    TODO("قيد الانتظار"),
    IN_PROGRESS("جاري التنفيذ"),
    DONE("منجز")
}

data class ProjectMilestone(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val dueDate: String,
    val isCompleted: Boolean = false
)

data class ProjectAttachment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val sizeLabel: String,
    val fileType: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class StudyBuddy(
    val id: String,
    val name: String,
    val major: String,
    val status: String,
    val isOnline: Boolean,
    val streak: Int,
    val currentActivity: String
)

data class StudyRoom(
    val id: String,
    val name: String,
    val topic: String,
    val activeCount: Int,
    val maxParticipants: Int = 10,
    val isSilent: Boolean = true,
    val tag: String
)

data class CampusMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderName: String,
    val senderScopeId: String,
    val content: String,
    val timeLabel: String,
    val isMe: Boolean = false
)

data class SharedNote(
    val id: String,
    val title: String,
    val subject: String,
    val author: String,
    val likesCount: Int,
    val summary: String
)

// Super Developer Recognition Helper
fun isSuperDeveloperEmail(email: String?): Boolean {
    if (email.isNullOrBlank()) return false
    val clean = email.trim().lowercase()
    return clean == "ismailhamaky@gmail.com"
}

// 3D Avatar System
data class AvatarItem(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val category: String, // "أساسي", "إنجاز", "مطور"
    val unlockCriteria: String = "",
    val isDevExclusive: Boolean = false,
    val requiredLevel: Int = 0,
    val requiredNightSessions: Int = 0,
    val requiredKanbanTasks: Int = 0,
    val requiredStreak: Int = 0
)

object ScopeAvatarsCatalog {
    val ALL_AVATARS = listOf(
        AvatarItem(
            id = "avatar_scholar_owl",
            title = "البومة الحكيمة 🦉",
            description = "مفكر أكاديمي متزن وباحث شغوف بالمعرفة والتركيز العميق",
            emoji = "🦉",
            category = "أساسي"
        ),
        AvatarItem(
            id = "avatar_cyber_scholar",
            title = "طالب السايبر 💻",
            description = "مبرمج شغوف بخوارزميات المستقبل والأنظمة والذكاء الاصطناعي",
            emoji = "💻",
            category = "أساسي"
        ),
        AvatarItem(
            id = "avatar_quantum_thinker",
            title = "المفكر الكمّي ⚛️",
            description = "عقلية هندسية تحلل المعادلات والمعضلات الرياضية المعقدة",
            emoji = "⚛️",
            category = "أساسي"
        ),
        AvatarItem(
            id = "avatar_solar_prodigy",
            title = "نابغة صولا ☀️",
            description = "محب للإنتاجية العالية وإشراقة الصباح الباكر وتصفية الذهن",
            emoji = "☀️",
            category = "أساسي"
        ),
        AvatarItem(
            id = "avatar_night_owl",
            title = "ساهر الليل الأسطوري 🌙",
            description = "أكمل 10 جلسات دراسية ليلية هادئة بعد منتصف الليل",
            emoji = "🌙",
            category = "إنجاز",
            unlockCriteria = "10 جلسات ليلية",
            requiredNightSessions = 10
        ),
        AvatarItem(
            id = "avatar_solar_ambassador",
            title = "سفير صولا الذهبي ☀️",
            description = "بلغ المستوى الأكاديمي 15 وافتح هالة الإلهام الكوزمية",
            emoji = "✨",
            category = "إنجاز",
            unlockCriteria = "المستوى 15",
            requiredLevel = 15
        ),
        AvatarItem(
            id = "avatar_task_master",
            title = "قاهر المهام 📋",
            description = "أنجز 50 مهمة بنجاح في لوحة كانبان وقسم المشاريع",
            emoji = "📋",
            category = "إنجاز",
            unlockCriteria = "50 مهمة في لوحة متابعة المهام",
            requiredKanbanTasks = 50
        ),
        AvatarItem(
            id = "avatar_streak_flame",
            title = "لهب الاستمرارية 🔥",
            description = "حافظ على شعلة دراسية مستمرة لمدة 30 يوماً متواصلة",
            emoji = "🔥",
            category = "إنجاز",
            unlockCriteria = "شعلة 30 يوماً",
            requiredStreak = 30
        ),
        AvatarItem(
            id = "avatar_dev_crown",
            title = "تاج المطور الأسمى 👑",
            description = "حكر حصري لمهندس ومؤسس المنصة ismailhamaky@gmail.com",
            emoji = "👑",
            category = "مطور",
            unlockCriteria = "حساب المطور الرسمي",
            isDevExclusive = true
        )
    )

    fun getAvatar(avatarId: String): AvatarItem {
        return ALL_AVATARS.firstOrNull { it.id == avatarId } ?: ALL_AVATARS.first()
    }

    fun isAvatarUnlocked(avatar: AvatarItem, user: UserProfile?): Boolean {
        if (user == null) return avatar.category == "أساسي"
        if (avatar.isDevExclusive) {
            return isSuperDeveloperEmail(user.email) || user.isDeveloper
        }
        if (avatar.category == "أساسي") return true
        if (avatar.requiredLevel > 0 && user.level >= avatar.requiredLevel) return true
        if (avatar.requiredNightSessions > 0 && user.completedNightSessions >= avatar.requiredNightSessions) return true
        if (avatar.requiredKanbanTasks > 0 && user.completedKanbanTasks >= avatar.requiredKanbanTasks) return true
        if (avatar.requiredStreak > 0 && user.flameCount >= avatar.requiredStreak) return true
        return false
    }
}

// Super Developer Control Center Models
data class CommunityChallenge(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val rewardSparks: Int = 200,
    val rewardTokens: Int = 15,
    val targetHours: Int = 50,
    val participantCount: Int = 124,
    val daysRemaining: Int = 5,
    val isActive: Boolean = true
)

data class DevLiveAnalytics(
    val activeStudentsCount: Int = 428,
    val totalStudyHoursLogged: Int = 3840,
    val topSubjects: List<String> = listOf("الخوارزميات وهياكل البيانات", "قواعد البيانات الموزعة", "الذكاء الاصطناعي"),
    val xpMultiplier: Float = 1.0f,
    val activeAnnouncement: String = "مرحباً بجميع الطلاب! تم تفعيل مضاعف التركيز ⚡ لجميع الجلسات الدراسية."
)

// Smart Notification Engine Models
enum class NotificationChannelType(
    val channelId: String,
    val titleAr: String,
    val descriptionAr: String,
    val iconEmoji: String
) {
    STREAK_GUARD("channel_streak_guard", "حارس الشعلة 🔥 (Streak Guard)", "تنبيهات مسائية لحماية الشعلة الأكاديمية قبل نهاية اليوم", "🔥"),
    SCHEDULE_GUARD("channel_schedule_guard", "حارس الجدول 📅 (Schedule Guard)", "تنبيهات المحاضرات وتسليم المهام ومواعيد الامتحانات", "📅"),
    SOLA_NUDGES("channel_sola_nudges", "همسات صولا 🤖 (Sola Nudges)", "تذكيرات تحفيزية ذكية خلال نافذة إنتاجيتك المفضلة", "🤖"),
    CAMPUS_PINGS("channel_campus_pings", "نداءات الحرم 👥 (Campus Pings)", "دعوات غرف المذاكرة ورسائل زملاء الدفعة الأكاديمية", "👥")
}

data class AppNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val channel: NotificationChannelType,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class DndPreferences(
    val isEnabled: Boolean = true,
    val startHour: Int = 23,
    val startMinute: Int = 0,
    val endHour: Int = 7,
    val endMinute: Int = 0
)

// Public Profile for Search & Campus Inspector Preview Card
data class PublicStudentProfile(
    val uid: String,
    val displayName: String,
    val scopeId: String,
    val academicMajor: String,
    val university: String,
    val flameCount: Int,
    val level: Int,
    val sparks: Int,
    val equippedTitle: String,
    val equippedFrame: String,
    val equippedAura: String,
    val avatarId: String,
    val photoUrl: String = "",
    val isDeveloper: Boolean = false,
    val isPro: Boolean = false,
    val customStatus: String = "طالب SCOPE طموح 🚀",
    val activeFocusStatus: String = "في جلسة تركيز وبناء ⚡",
    val showcaseBadges: List<String> = listOf("badge_first_spark", "badge_streak_3")
)

// Sola Vision Engine Models: Node-based Concept Maps
data class ConceptMapNode(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val summary: String,
    val category: String = "CORE", // "CORE", "SUB_TOPIC", "APPLICATION"
    val colorHex: String = "#3B82F6",
    val connections: List<String> = emptyList() // target node IDs
)

data class ConceptMap(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val subjectName: String,
    val nodes: List<ConceptMapNode> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

// Sola 60s Audio Podcast Synthesizer Models
data class PodcastSegment(
    val timeRange: String, // e.g. "00:00 - 00:15"
    val phaseTitle: String, // e.g. "الخطاف الذهني والمقدمة"
    val text: String
)

data class SolaPodcastEpisode(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val subjectName: String,
    val durationSeconds: Int = 60,
    val segments: List<PodcastSegment> = emptyList(),
    val fullScriptText: String = "",
    val isSavedOffline: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// SCOPE Listens Feedback Model
data class FeedbackItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderUid: String = "",
    val senderName: String = "",
    val senderScopeId: String = "",
    val feedbackType: String = "BUG", // "BUG", "SUGGESTION", "COMPLIMENT"
    val content: String = "",
    val category: String = "عام",
    val status: String = "PENDING", // "PENDING", "REWARDED", "RESOLVED"
    val rewardedSparks: Int = 0,
    val rewardedTokens: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class Friend(
    val uid: String = "",
    val displayName: String = "",
    val scopeId: String = "",
    val isOnline: Boolean = false,
    val currentActivity: String = "متاح 🟢"
)

data class FriendRequest(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderUid: String = "",
    val senderName: String = "",
    val senderScopeId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class StudyNote(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

