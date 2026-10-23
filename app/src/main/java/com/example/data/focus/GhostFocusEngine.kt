package com.example.data.focus

import com.example.data.guilds.GuildActivityFeedItem

data class GhostFocusStatus(
    val isGhostFocusActive: Boolean = false,
    val subjectName: String = "",
    val durationMinutes: Int = 25,
    val startTime: Long = System.currentTimeMillis(),
    val mutesCampusPings: Boolean = true,
    val mutesDirectChats: Boolean = true,
    val mutesRoomInvites: Boolean = true,
    val broadcastStatusText: String = "🛡️ في درع التركيز العميق (Ghost Focus Mode)"
)

object GhostFocusEngine {

    fun activateGhostFocus(subjectName: String, durationMinutes: Int): GhostFocusStatus {
        return GhostFocusStatus(
            isGhostFocusActive = true,
            subjectName = subjectName,
            durationMinutes = durationMinutes,
            startTime = System.currentTimeMillis(),
            mutesCampusPings = true,
            mutesDirectChats = true,
            mutesRoomInvites = true,
            broadcastStatusText = "🛡️ في درع التركيز العميق ($subjectName)"
        )
    }

    fun deactivateGhostFocus(): GhostFocusStatus {
        return GhostFocusStatus(
            isGhostFocusActive = false,
            subjectName = "",
            durationMinutes = 0,
            startTime = 0L,
            mutesCampusPings = false,
            mutesDirectChats = false,
            mutesRoomInvites = false,
            broadcastStatusText = "متصل وغير مشغول"
        )
    }

    fun generatePostFocusBroadcast(
        studentName: String,
        guildId: String,
        durationMinutes: Int,
        sparksEarned: Int
    ): GuildActivityFeedItem {
        return GuildActivityFeedItem(
            guildId = guildId,
            studentName = studentName,
            actionText = "أتم $durationMinutes دقيقة تركيز عميق بنجاح! +$sparksEarned ⚡ للتحالف (درع Flow Shield)",
            sparksEarned = sparksEarned,
            timestamp = System.currentTimeMillis()
        )
    }
}
