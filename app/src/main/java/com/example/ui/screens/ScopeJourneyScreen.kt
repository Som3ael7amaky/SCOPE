package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudySession
import com.example.data.model.UserProfile
import com.example.ui.audio.ScopeSoundStudio
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeAccent
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber

data class JourneyNode(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val badgeName: String,
    val iconEmoji: String,
    val requiredProgressLabel: String,
    val isUnlocked: Boolean,
    val rewardSparks: Int,
    val colorHex: String = "#38BDF8"
)

@Composable
fun ScopeJourneyScreen(
    userProfile: UserProfile?,
    studySessions: List<StudySession> = emptyList(),
    onBack: () -> Unit,
    onClaimReward: (sparks: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundStudio = remember { ScopeSoundStudio.getInstance(context) }

    val totalFocusMinutes = studySessions.sumOf { it.durationMinutes }
    val totalFocusHours = (totalFocusMinutes / 60.0).toFloat()
    val streakDays = userProfile?.flameCount ?: 3

    val journeyNodes = remember(totalFocusHours, streakDays) {
        listOf(
            JourneyNode(
                id = "node_start",
                titleAr = "محطة الانطلاق والبدايات 🚀",
                subtitleAr = "إنجاز أول ساعة تركيز دراسي وثبات الأداء",
                badgeName = "وسام البداية الشغوفة",
                iconEmoji = "🚀",
                requiredProgressLabel = "1+ ساعات تركيز",
                isUnlocked = totalFocusHours >= 0.5f,
                rewardSparks = 50,
                colorHex = "#38BDF8"
            ),
            JourneyNode(
                id = "node_flame",
                titleAr = "واحة الالتزام والشعلة 🔥",
                subtitleAr = "الحفاظ على الشعلة الأكاديمية لمدة 3 أيام متواصلة",
                badgeName = "وسام حارس الشعلة",
                iconEmoji = "🔥",
                requiredProgressLabel = "شعلة 3 أيام متصلة",
                isUnlocked = streakDays >= 3,
                rewardSparks = 75,
                colorHex = "#F59E0B"
            ),
            JourneyNode(
                id = "node_kanban",
                titleAr = "مختبر المهام ومتابعة المشاريع 💻",
                subtitleAr = "تخطيط وإنجاز المهام التطبيقية في لوحة المتابعة والمشاريع",
                badgeName = "وسام مهندس المهام والمراحل",
                iconEmoji = "💻",
                requiredProgressLabel = "إنجاز مهام المشروع",
                isUnlocked = (userProfile?.completedKanbanTasks ?: 0) >= 1 || totalFocusHours >= 1f,
                rewardSparks = 100,
                colorHex = "#10B981"
            ),
            JourneyNode(
                id = "node_guild",
                titleAr = "فرسان التحالف الموحد 🛡️",
                subtitleAr = "الانضمام أو المساهمة في تحالف دراسي وتجمّع الشرارات",
                badgeName = "وسام فارس التحالف",
                iconEmoji = "🛡️",
                requiredProgressLabel = "عضوية تحالف دراسي",
                isUnlocked = true,
                rewardSparks = 120,
                colorHex = "#8B5CF6"
            ),
            JourneyNode(
                id = "node_master",
                titleAr = "قلعة الماستر الأكاديمي 🏆",
                subtitleAr = "تراكم 20+ ساعة تركيز عميق والوصول للمستوى الأكاديمي 5",
                badgeName = "وسام الماستر الأسطوري",
                iconEmoji = "🏆",
                requiredProgressLabel = "20 ساعة تركيز + مستوى 5",
                isUnlocked = totalFocusHours >= 20f || (userProfile?.level ?: 1) >= 5,
                rewardSparks = 250,
                colorHex = "#EC4899"
            )
        )
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("scope_journey_screen"),
        color = Color(0xFF030712) // Dark Obsidian
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B1329))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        soundStudio.playTap()
                        onBack()
                    },
                    modifier = Modifier.testTag("scope_journey_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "شجرة رحلة سكوب (3D Scope Journey)",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "خارطة النمو والتفوق المعتمدة على ساعات التركيز الحقيقية",
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Journey Hero Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "المستوى الحالي: ${userProfile?.level ?: 1} (${userProfile?.equippedTitle ?: "طالب SCOPE"})",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "إجمالي ساعات التركيز: ${String.format("%.1f", totalFocusHours)} ساعة • شعلة الاستمرارية: ${streakDays}d 🔥",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ScopeFocusGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "نمو حقيقي ⚡",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ScopeFocusGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val unlockedCount = journeyNodes.count { it.isUnlocked }
                            val totalNodes = journeyNodes.size
                            val journeyProgress = unlockedCount.toFloat() / totalNodes.toFloat()

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("تقدم شجرة الإنجازات الأكاديمية", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.Gray)
                                    Text("$unlockedCount من $totalNodes محطات (${(journeyProgress * 100).toInt()}%)", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { journeyProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = Color(0xFF38BDF8),
                                    trackColor = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }

                item {
                    SectionHeader(
                        title = "مسار المحطات والأوسمة الأكاديمية",
                        subtitle = "تتبع نموك الزمني واستلم مكافآت الشرارات"
                    )
                }

                // Milestone Nodes List
                itemsIndexed(journeyNodes) { index, node ->
                    val isLast = index == journeyNodes.size - 1

                    Column {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (node.isUnlocked) Color(0xFF0F172A) else Color(0xFF0B1329).copy(alpha = 0.6f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (node.isUnlocked) ScopeFocusGreen.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Icon / Badge Node Box
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (node.isUnlocked) ScopeFocusGreen.copy(alpha = 0.2f)
                                            else Color(0xFF1E293B)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (node.isUnlocked) {
                                        Text(node.iconEmoji, fontSize = 22.sp)
                                    } else {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = node.titleAr,
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (node.isUnlocked) Color.White else Color.Gray
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = node.subtitleAr,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "المتطلب: ${node.requiredProgressLabel} • المكافأة: +${node.rewardSparks} ⚡",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                if (node.isUnlocked) {
                                    Button(
                                        onClick = {
                                            soundStudio.playComplete()
                                            onClaimReward(node.rewardSparks)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ScopeFocusGreen)
                                    ) {
                                        Text("مطالبة ✨", fontFamily = CairoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1E293B))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("مغلق 🔒", fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        // Connecting Vertical Line
                        if (!isLast) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 38.dp)
                                    .width(2.dp)
                                    .height(16.dp)
                                    .background(
                                        if (node.isUnlocked) ScopeFocusGreen.copy(alpha = 0.5f) else Color.DarkGray
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
