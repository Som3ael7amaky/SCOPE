package com.example.data.guilds

import com.example.data.model.UserProfile
import kotlin.random.Random

data class GuildRewardItem(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val requiredSparkPool: Int,
    val isUnlocked: Boolean
)

data class GuildActivityFeedItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val guildId: String,
    val studentName: String,
    val actionText: String, // e.g. "أتم 45 دقيقة تركيز بنجاح! +15 ⚡ للتحالف"
    val sparksEarned: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class StudyGuildMember(
    val uid: String,
    val displayName: String,
    val scopeId: String,
    val role: String, // "LEADER", "MEMBER"
    val sparksContributedThisWeek: Int,
    val avatarId: String = "avatar_scholar_owl"
)

data class StudyGuild(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val tag: String, // e.g. "[AI-GUILD]"
    val description: String,
    val inviteCode: String,
    val emblemIcon: String = "🛡️", // Emoji or Badge symbol
    val colorHex: String = "#2563EB",
    val members: List<StudyGuildMember> = emptyList(),
    val maxMembers: Int = 5,
    val weeklySparkPool: Int = 740,
    val weeklyGoalSparks: Int = 1000,
    val totalStudyHours: Float = 142.5f,
    val guildRank: Int = 1,
    val unlockedRewards: List<String> = listOf("reward_guild_frame", "reward_master_title")
)

object StudyGuildsEngine {

    fun createGuild(
        name: String,
        tag: String,
        description: String,
        emblem: String,
        colorHex: String,
        leaderProfile: UserProfile?
    ): StudyGuild {
        val code = "GUILD-" + (1000..9999).random()
        val formattedTag = if (tag.startsWith("[")) tag else "[$tag]"
        val leader = StudyGuildMember(
            uid = leaderProfile?.uid ?: "leader_uid",
            displayName = leaderProfile?.displayName ?: "قائد التحالف",
            scopeId = leaderProfile?.scopeId ?: "@leader",
            role = "LEADER",
            sparksContributedThisWeek = 150,
            avatarId = leaderProfile?.avatarId ?: "avatar_scholar_owl"
        )
        return StudyGuild(
            name = name,
            tag = formattedTag,
            description = description,
            inviteCode = code,
            emblemIcon = emblem,
            colorHex = colorHex,
            members = listOf(leader),
            weeklySparkPool = 150,
            weeklyGoalSparks = 1000,
            totalStudyHours = 12.0f,
            guildRank = (1..5).random()
        )
    }

    fun joinGuildByCode(
        inviteCode: String,
        existingGuilds: List<StudyGuild>,
        userProfile: UserProfile?
    ): StudyGuild? {
        val matched = existingGuilds.firstOrNull { it.inviteCode.equals(inviteCode.trim(), ignoreCase = true) }
            ?: getSampleGuilds().firstOrNull { it.inviteCode.equals(inviteCode.trim(), ignoreCase = true) }
            ?: getSampleGuilds().first()

        if (matched.members.size >= matched.maxMembers) {
            return null // Guild is full
        }

        val newMember = StudyGuildMember(
            uid = userProfile?.uid ?: "user_${System.currentTimeMillis()}",
            displayName = userProfile?.displayName ?: "طالب SCOPE",
            scopeId = userProfile?.scopeId ?: "@student",
            role = "MEMBER",
            sparksContributedThisWeek = 20,
            avatarId = userProfile?.avatarId ?: "avatar_scholar_owl"
        )

        return matched.copy(
            members = matched.members + newMember,
            weeklySparkPool = matched.weeklySparkPool + 20
        )
    }

    fun contributeSparks(
        guild: StudyGuild,
        studentName: String,
        sparks: Int
    ): Pair<StudyGuild, GuildActivityFeedItem> {
        val newPool = guild.weeklySparkPool + sparks
        val updatedGuild = guild.copy(weeklySparkPool = newPool)
        val feedItem = GuildActivityFeedItem(
            guildId = guild.id,
            studentName = studentName,
            actionText = "ساهم بـ +$sparks ⚡ في صندوق تحالف ${guild.name}!",
            sparksEarned = sparks
        )
        return Pair(updatedGuild, feedItem)
    }

    fun getSampleGuilds(): List<StudyGuild> {
        return listOf(
            StudyGuild(
                id = "g1",
                name = "فرسان الخوارزميات",
                tag = "[ALGO-HEROES]",
                description = "نخبة طلاب الحاسب والذكاء الاصطناعي لحل مسائل LeetCode وأبنية البيانات.",
                inviteCode = "SCOPE-9921",
                emblemIcon = "⚔️",
                colorHex = "#2563EB",
                members = listOf(
                    StudyGuildMember("m1", "إسماعيل حماقي", "@ismail", "LEADER", 280, "avatar_dev_crown"),
                    StudyGuildMember("m2", "عبدالرحمن الشمري", "@abdul", "MEMBER", 210),
                    StudyGuildMember("m3", "سارة القحطاني", "@sara", "MEMBER", 150),
                    StudyGuildMember("m4", "فيصل العتيبي", "@faisal", "MEMBER", 100)
                ),
                weeklySparkPool = 740,
                weeklyGoalSparks = 1000,
                totalStudyHours = 184.5f,
                guildRank = 1
            ),
            StudyGuild(
                id = "g2",
                name = "تحالف صانعي النظم",
                tag = "[SYS-ARCHS]",
                description = "مجموعة التركيز العميق لهندسة البرمجيات وقواعد البيانات الموزعة.",
                inviteCode = "SCOPE-4412",
                emblemIcon = "🛡️",
                colorHex = "#8B5CF6",
                members = listOf(
                    StudyGuildMember("m5", "نورة الدوسري", "@noura", "LEADER", 320),
                    StudyGuildMember("m6", "خالد المطيري", "@khalid", "MEMBER", 190),
                    StudyGuildMember("m7", "ريم الغامدي", "@reem", "MEMBER", 110)
                ),
                weeklySparkPool = 620,
                weeklyGoalSparks = 1000,
                totalStudyHours = 142.0f,
                guildRank = 2
            ),
            StudyGuild(
                id = "g3",
                name = "كتيبة الذكاء الاصطناعي",
                tag = "[NEURAL-AI]",
                description = "التركيز على نماذج التعلم العميق والرياضيات المتقدمة.",
                inviteCode = "SCOPE-7788",
                emblemIcon = "🧠",
                colorHex = "#10B981",
                members = listOf(
                    StudyGuildMember("m8", "طارق الزهراني", "@tariq", "LEADER", 240),
                    StudyGuildMember("m9", "منى الحربي", "@mona", "MEMBER", 210)
                ),
                weeklySparkPool = 450,
                weeklyGoalSparks = 1000,
                totalStudyHours = 98.0f,
                guildRank = 3
            )
        )
    }

    fun getSampleFeed(guildId: String): List<GuildActivityFeedItem> {
        val now = System.currentTimeMillis()
        return listOf(
            GuildActivityFeedItem(
                guildId = guildId,
                studentName = "إسماعيل حماقي 👑",
                actionText = "أتم جلسة بومودورو مركزة لمدة 45 دقيقة! +20 ⚡ للتحالف",
                sparksEarned = 20,
                timestamp = now - 1200000L
            ),
            GuildActivityFeedItem(
                guildId = guildId,
                studentName = "سارة القحطاني",
                actionText = "حلت محاكاة اختبار الخوارزميات بنجاح! +15 ⚡ للتحالف",
                sparksEarned = 15,
                timestamp = now - 3600000L
            ),
            GuildActivityFeedItem(
                guildId = guildId,
                studentName = "عبدالرحمن الشمري",
                actionText = "استخرج بطاقات ذكية عبر Sola Vision! +15 ⚡ للتحالف",
                sparksEarned = 15,
                timestamp = now - 7200000L
            )
        )
    }

    fun getGuildRewards(): List<GuildRewardItem> {
        return listOf(
            GuildRewardItem(
                id = "reward_guild_frame",
                title = "إطار التحالف الذهبي الأسطوري 🏆",
                description = "إطار زجاجي متوهج بشعار التحالف لكافة الأعضاء عند الوصول لـ 1000 ⚡",
                iconEmoji = "🖼️",
                requiredSparkPool = 1000,
                isUnlocked = false
            ),
            GuildRewardItem(
                id = "reward_master_title",
                title = "لقب Guild Master 👑",
                description = "عنوان حاطف للأنظار يعرض في بطاقات التعريف وملفات الطلاب",
                iconEmoji = "🎖️",
                requiredSparkPool = 800,
                isUnlocked = true
            ),
            GuildRewardItem(
                id = "reward_ambient_palette",
                title = "نغمة ألوان الغرفة الخضراء 🟢",
                description = "لوحة نيون ساحرة لغرف المذاكرة الصوتية الخاصة بالتحالف",
                iconEmoji = "🎨",
                requiredSparkPool = 500,
                isUnlocked = true
            )
        )
    }
}
