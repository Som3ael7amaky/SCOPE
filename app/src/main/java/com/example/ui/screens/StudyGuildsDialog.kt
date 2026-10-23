package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.guilds.GuildActivityFeedItem
import com.example.data.guilds.StudyGuild
import com.example.data.guilds.StudyGuildsEngine
import com.example.data.model.UserProfile
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeAccent
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber

@Composable
fun StudyGuildsDialog(
    activeGuild: StudyGuild?,
    userProfile: UserProfile?,
    leaderboardGuilds: List<StudyGuild>,
    activityFeed: List<GuildActivityFeedItem>,
    onCreateGuild: (name: String, tag: String, desc: String, emblem: String) -> Unit,
    onJoinGuild: (inviteCode: String) -> Unit,
    onContributeSparks: (sparks: Int) -> Unit,
    onDismiss: () -> Unit
) {
    // 0: My Guild, 1: Leaderboard, 2: Rewards
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateModal by remember { mutableStateOf(false) }
    var showJoinModal by remember { mutableStateOf(false) }

    var newGuildName by remember { mutableStateOf("") }
    var newGuildTag by remember { mutableStateOf("") }
    var newGuildDesc by remember { mutableStateOf("") }
    var newGuildEmblem by remember { mutableStateOf("🛡️") }

    var inviteCodeInput by remember { mutableStateOf("") }

    val currentGuild = activeGuild ?: leaderboardGuilds.firstOrNull() ?: StudyGuildsEngine.getSampleGuilds().first()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("study_guilds_dialog"),
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF030712).copy(alpha = 0.97f),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                Brush.verticalGradient(listOf(ScopePrimary, ScopeAccent))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(ScopePrimary.copy(alpha = 0.25f))
                                .border(1.dp, ScopePrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = ScopePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "تحالفات المذاكرة الجماعية 🛡️",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Study Guilds & Co-Op Battles (حد أقصى 5 أعضاء)",
                                fontFamily = CairoFontFamily,
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_guilds_dialog_btn")) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GuildTabButton("تحالفي 🛡️", selectedTab == 0) { selectedTab = 0 }
                    GuildTabButton("لائحة المتصدرين 🏆", selectedTab == 1) { selectedTab = 1 }
                    GuildTabButton("مكافآت التحالف 🎁", selectedTab == 2) { selectedTab = 2 }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (selectedTab) {
                    0 -> {
                        // --- MY GUILD TAB ---
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Guild Card Header
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    border = androidx.compose.foundation.BorderStroke(1.2.dp, ScopePrimary.copy(alpha = 0.6f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = currentGuild.emblemIcon, fontSize = 28.sp)
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = "${currentGuild.name} ${currentGuild.tag}",
                                                        fontFamily = CairoFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = "كود الدعوة: ${currentGuild.inviteCode} • المركز #${currentGuild.guildRank}",
                                                        fontFamily = CairoFontFamily,
                                                        fontSize = 11.sp,
                                                        color = ScopeAccent
                                                    )
                                                }
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                IconButton(onClick = { showCreateModal = true }, modifier = Modifier.size(32.dp)) {
                                                    Icon(Icons.Default.Add, contentDescription = "إنشاء", tint = Color.White)
                                                }
                                                IconButton(onClick = { showJoinModal = true }, modifier = Modifier.size(32.dp)) {
                                                    Icon(Icons.Default.Key, contentDescription = "انضمام", tint = ScopePrimary)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = currentGuild.description,
                                            fontFamily = CairoFontFamily,
                                            fontSize = 12.sp,
                                            color = Color(0xFFCBD5E1)
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Co-Op Spark Pool Bar
                                        val poolProgress = (currentGuild.weeklySparkPool.toFloat() / currentGuild.weeklyGoalSparks).coerceIn(0f, 1f)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "صندوق شرارات التحالف الأسبوعي (Co-Op Pool) ⚡",
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = ScopeWarningAmber
                                            )
                                            Text(
                                                text = "${currentGuild.weeklySparkPool} / ${currentGuild.weeklyGoalSparks} ⚡",
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        LinearProgressIndicator(
                                            progress = { poolProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = ScopeWarningAmber,
                                            trackColor = Color(0xFF1E293B)
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = { onContributeSparks(25) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(40.dp)
                                                .testTag("contribute_guild_sparks_btn"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = ScopeWarningAmber)
                                        ) {
                                            Text(
                                                text = "المساهمة بـ +25 ⚡ في الصندوق الأسبوعي للتحالف",
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                        }
                                    }
                                }
                            }

                            // Members List (Up to 5)
                            item {
                                Text(
                                    text = "أعضاء التحالف النشطون (${currentGuild.members.size} / ${currentGuild.maxMembers}):",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }

                            items(currentGuild.members) { member ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.7f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(ScopePrimary.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (member.role == "LEADER") "👑" else "🛡️",
                                                fontSize = 18.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = member.displayName,
                                                    fontFamily = CairoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color.White
                                                )
                                                if (member.role == "LEADER") {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(ScopeWarningAmber.copy(alpha = 0.2f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "القائد",
                                                            fontFamily = CairoFontFamily,
                                                            fontSize = 9.sp,
                                                            color = ScopeWarningAmber,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = member.scopeId,
                                                fontFamily = CairoFontFamily,
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }

                                        Text(
                                            text = "+${member.sparksContributedThisWeek} ⚡",
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = ScopeFocusGreen
                                        )
                                    }
                                }
                            }

                            // Guild Live Feed
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "السجل المباشر لإنجازات التحالف 📡",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }

                            items(activityFeed) { feed ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "⚡", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = feed.studentName,
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = ScopeAccent
                                            )
                                            Text(
                                                text = feed.actionText,
                                                fontFamily = CairoFontFamily,
                                                fontSize = 11.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // --- LEADERBOARD TAB ---
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(leaderboardGuilds.indices.toList()) { idx ->
                                val g = leaderboardGuilds[idx]
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (idx == 0) ScopeWarningAmber else Color(0xFF1E293B)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = when (idx) {
                                                0 -> "🥇"
                                                1 -> "🥈"
                                                2 -> "🥉"
                                                else -> "#${idx + 1}"
                                            },
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.width(32.dp)
                                        )

                                        Text(text = g.emblemIcon, fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${g.name} ${g.tag}",
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "${g.members.size}/5 أعضاء • ${g.totalStudyHours} ساعة دراسة",
                                                fontFamily = CairoFontFamily,
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }

                                        Text(
                                            text = "${g.weeklySparkPool} ⚡",
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = ScopeWarningAmber
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // --- REWARDS TAB ---
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(StudyGuildsEngine.getGuildRewards()) { reward ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = reward.iconEmoji, fontSize = 26.sp)
                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = reward.title,
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = reward.description,
                                                fontFamily = CairoFontFamily,
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (reward.isUnlocked) ScopeFocusGreen.copy(alpha = 0.2f) else Color(0xFF1E293B)
                                                )
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (reward.isUnlocked) "مفعل 🔓" else "${reward.requiredSparkPool} ⚡",
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (reward.isUnlocked) ScopeFocusGreen else Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Create Guild Modal Popup
                if (showCreateModal) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "تأسيس تحالف خاص جديد (حتى 5 أعضاء) 🛡️",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newGuildName,
                                onValueChange = { newGuildName = it },
                                label = { Text("اسم التحالف", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newGuildTag,
                                onValueChange = { newGuildTag = it },
                                label = { Text("رمز التاق (مثال: ALGO)", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showCreateModal = false }) {
                                    Text("إلغاء", fontFamily = CairoFontFamily, color = Color.White)
                                }
                                Button(
                                    onClick = {
                                        if (newGuildName.isNotBlank()) {
                                            onCreateGuild(newGuildName, newGuildTag, newGuildDesc, newGuildEmblem)
                                            showCreateModal = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                                ) {
                                    Text("تأسيس", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Join Guild Modal Popup
                if (showJoinModal) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "الانضمام لتحالف بكود الدعوة 🔑",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = inviteCodeInput,
                                onValueChange = { inviteCodeInput = it },
                                label = { Text("أدخل كود الدعوة (مثال: SCOPE-9921)", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showJoinModal = false }) {
                                    Text("إلغاء", fontFamily = CairoFontFamily, color = Color.White)
                                }
                                Button(
                                    onClick = {
                                        if (inviteCodeInput.isNotBlank()) {
                                            onJoinGuild(inviteCodeInput)
                                            showJoinModal = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                                ) {
                                    Text("انضمام", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuildTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) ScopePrimary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontFamily = CairoFontFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
            color = if (isSelected) Color.White else Color(0xFF94A3B8)
        )
    }
}
