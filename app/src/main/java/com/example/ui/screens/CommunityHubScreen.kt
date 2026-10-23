package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.guilds.StudyGuild
import com.example.data.model.CampusMessage
import com.example.data.model.SharedNote
import com.example.data.model.StudyBuddy
import com.example.data.model.StudyRoom
import com.example.data.model.UserProfile
import com.example.ui.audio.ScopeSoundStudio
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeAccent
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber

enum class CommunityTab(val titleAr: String, val icon: ImageVector) {
    FRIENDS("قائمة الأصدقاء", Icons.Default.Group),
    FRIEND_REQUESTS("طلبات الصداقة", Icons.Default.Favorite),
    STUDY_ROOMS("غرف المذاكرة", Icons.Default.Headset),
    GUILDS("تحالفات المذاكرة 🛡️", Icons.Default.Shield),
    CHAT("دردشة الدفعة", Icons.AutoMirrored.Filled.Chat),
    SHARED_NOTES("الملاحظات التشاركية", Icons.Default.Description)
}

@Composable
fun CommunityHubScreen(
    userProfile: UserProfile?,
    friends: List<com.example.data.model.Friend> = emptyList(),
    friendRequests: List<com.example.data.model.FriendRequest> = emptyList(),
    onAddFriend: (String) -> Unit = {},
    onAcceptFriendRequest: (com.example.data.model.FriendRequest) -> Unit = {},
    onDeclineFriendRequest: (String) -> Unit = {},
    activeGuild: StudyGuild? = null,
    onOpenGuilds: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundStudio = remember { ScopeSoundStudio.getInstance(context) }

    var selectedTab by remember { mutableStateOf(CommunityTab.FRIENDS) }
    var activeStudyRoomId by remember { mutableStateOf<String?>(null) }
    var isMicMuted by remember { mutableStateOf(true) }


    // Live study rooms
    val studyRooms = remember {
        mutableStateListOf(
            StudyRoom("r1", "مختبر الخوارزميات الصامت 🤫", "مراجعة هياكل الأشجار والرسوم البيانية", 4, 10, true, "تركيز هادئ"),
            StudyRoom("r2", "غرفة مناقشة مشاريع التخرج 🚀", "تبادل الأفكار ومعمارية النظم", 6, 8, false, "نقاش تفاعلي"),
            StudyRoom("r3", "كافيه البرمجة الليلي ☕", "كود ومذاكرة حرة مع موسيقى لو-فاي", 8, 12, true, "جلسة مفتوحة")
        )
    }

    // Direct peer chat messages
    val chatMessages = remember {
        mutableStateListOf(
            CampusMessage(senderName = "سارة القحطاني", senderScopeId = "@sara_se", content = "يا شباب، هل تم تحديد موعد تسليم مسودة المشروع الأسبوع القادم؟", timeLabel = "10:14 ص", isMe = false),
            CampusMessage(senderName = "عبدالرحمن الشمري", senderScopeId = "@abdul_cs", content = "نعم، الدكتور أكد أن التسليم الأحد القادم عبر البوابة الأكاديمية.", timeLabel = "10:16 ص", isMe = false),
            CampusMessage(senderName = userProfile?.displayName ?: "أنت", senderScopeId = userProfile?.scopeId ?: "@me", content = "ممتاز، سأقوم برفع مخطط الـ ERD اليوم للمراجعة المشتركة 👍", timeLabel = "10:18 ص", isMe = true)
        )
    }

    // Collaborative shared notes
    val sharedNotes = remember {
        mutableStateListOf(
            SharedNote("n1", "ملخص قوانين وتطبيقات التعقيد الزمني Big-O", "الخوارزميات CS301", "أحمد السبيعي", 34, "شرح مبسط مع جداول مقارنة بين QuickSort و MergeSort وكيفية تحليل الشفرة."),
            SharedNote("n2", "مذكرة نماذج أسئلة فاينل الذكاء الاصطناعي", "الذكاء الاصطناعي CS440", "ريم المالكي", 52, "تجميعة أسئلة السنوات السابقة مع الحلول النموذجية لموضوعات Backprop و CNN."),
            SharedNote("n3", "خارطة مفاهيم قواعد البيانات الموزعة NoSQL", "قواعد البيانات CS320", "خالد الحربي", 28, "توضيح شامل لمبادئ Sharding و Replication و Consistency Models.")
        )
    }

    var chatInputText by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("campus_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campus Hub Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(ScopeAccent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = ScopeAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "المجتمع الطلابي",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Campus Social Hub & Study Rooms",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ScopeFocusGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "مباشر 🟢",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ScopeFocusGreen
                                )
                            }
                        }

                        // Active Voice Room Participation Banner if joined
                        AnimatedVisibility(visible = activeStudyRoomId != null) {
                            Column {
                                Spacer(modifier = Modifier.height(14.dp))
                                val joinedRoom = studyRooms.firstOrNull { it.id == activeStudyRoomId }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = ScopePrimary.copy(alpha = 0.12f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Headset, contentDescription = null, tint = ScopePrimary, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "أنت متصل الآن: ${joinedRoom?.name}",
                                                    fontFamily = CairoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "الميكروفون: ${if (isMicMuted) "مكتوم (للحفاظ على هدوء الغرفة)" else "نشط"}",
                                                    fontFamily = CairoFontFamily,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Row {
                                            IconButton(onClick = {
                                                soundStudio.playTap()
                                                isMicMuted = !isMicMuted
                                            }) {
                                                Icon(
                                                    imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                                    contentDescription = null,
                                                    tint = if (isMicMuted) ScopeWarningAmber else ScopeFocusGreen
                                                )
                                            }
                                            IconButton(onClick = {
                                                soundStudio.playTap()
                                                activeStudyRoomId = null
                                            }) {
                                                Text("مغادرة", fontFamily = CairoFontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Campus Category Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CommunityTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ScopePrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                                .clickable {
                                    soundStudio.playTap()
                                    selectedTab = tab
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) ScopePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tab.titleAr,
                                    fontFamily = CairoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ScopePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // TAB 1: Friends List
            if (selectedTab == CommunityTab.FRIENDS) {
                item {
                    var friendCode by remember { mutableStateOf("") }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = friendCode,
                            onValueChange = { friendCode = it },
                            placeholder = { Text("أدخل كود الصديق (مثال: SC-XXXXXX)") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { onAddFriend(friendCode) }) {
                            Text("إضافة")
                        }
                    }
                }
                item {
                    SectionHeader(
                        title = "أصدقائي (${friends.size})",
                        subtitle = "تواصل مع أصدقائك وتابع حالة التركيز لديهم"
                    )
                }

                items(friends) { friend ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(ScopePrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = friend.displayName.firstOrNull()?.toString() ?: "U",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = ScopePrimary
                                )
                                if (friend.isOnline) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(ScopeFocusGreen)
                                            .align(Alignment.BottomEnd)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = friend.displayName,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (friend.isOnline) "متاح 🟢" else "في جلسة تركيز 🛡️",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    color = if (friend.isOnline) ScopeFocusGreen else ScopeAccent
                                )
                            }

                            Button(
                                onClick = {
                                    soundStudio.playSolaChirp()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary.copy(alpha = 0.15f)),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = "تحفيز ⚡",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ScopePrimary
                                )
                            }
                        }
                    }
                }
            }

            // TAB 2: Friend Requests
            if (selectedTab == CommunityTab.FRIEND_REQUESTS) {
                item {
                    SectionHeader(
                        title = "طلبات الصداقة (${friendRequests.size})",
                        subtitle = "وافق على طلبات الانضمام لقائمة أصدقائك"
                    )
                }

                items(friendRequests) { request ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = request.senderName,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = request.senderScopeId,
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Row {
                                IconButton(onClick = { onAcceptFriendRequest(request) }) {
                                    Icon(Icons.Default.Favorite, contentDescription = "قبول", tint = ScopeFocusGreen)
                                }
                                IconButton(onClick = { onDeclineFriendRequest(request.id) }) {
                                    Icon(Icons.Default.Shield, contentDescription = "رفض", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            // TAB 3: Live Study Rooms
            if (selectedTab == CommunityTab.STUDY_ROOMS) {
                item {
                    SectionHeader(
                        title = "غرف المذاكرة الصوتية والمرئية",
                        subtitle = "انضم إلى غرف التركيز الجماعية لتحفيز الالتزام"
                    )
                }

                items(studyRooms) { room ->
                    val isJoined = activeStudyRoomId == room.id
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isJoined) ScopePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = room.name,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ScopeFocusGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${room.activeCount} / ${room.maxParticipants} طالب",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ScopeFocusGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = room.topic,
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "نوع الغرفة: ${room.tag}",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    color = ScopeAccent
                                )

                                Button(
                                    onClick = {
                                        if (isJoined) {
                                            soundStudio.playTap()
                                            activeStudyRoomId = null
                                        } else {
                                            soundStudio.playComplete()
                                            activeStudyRoomId = room.id
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isJoined) MaterialTheme.colorScheme.error else ScopePrimary
                                    ),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        text = if (isJoined) "مغادرة الغرفة" else "انضمام للمذاكرة 🎙️",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // TAB 3: Study Guilds & Co-Op Battles
            if (selectedTab == CommunityTab.GUILDS) {
                item {
                    SectionHeader(
                        title = "تحالفات المذاكرة (Study Guilds)",
                        subtitle = "كوّن تحالفاً مع أصدقائك للتعاون في تجميع شرارات التركيز ⚡"
                    )
                }

                item {
                    if (activeGuild != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = activeGuild.emblemIcon,
                                            fontSize = 28.sp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f, fill = false)) {
                                            Text(
                                                text = activeGuild.name,
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "كود التحالف: ${activeGuild.inviteCode} • ${activeGuild.members.size}/5 أعضاء",
                                                fontFamily = CairoFontFamily,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            soundStudio.playTap()
                                            onOpenGuilds()
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                                    ) {
                                        Text(
                                            text = "غرفة التحالف 🛡️",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                val progress = (activeGuild.weeklySparkPool.toFloat() / activeGuild.weeklyGoalSparks.toFloat()).coerceIn(0f, 1f)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "هدف الشرارات الأسبوعي ⚡",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${activeGuild.weeklySparkPool} / ${activeGuild.weeklyGoalSparks}",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ScopeAccent
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = ScopeAccent,
                                    trackColor = ScopeAccent.copy(alpha = 0.2f)
                                )
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🛡️",
                                    fontSize = 40.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "انضم لفرسان التحالفات الدراسية",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "أنشئ تحالفاً خاصاً بك أو انضم بكود دعوة لتجميع الشرارات وتفعيل درع شبح التركيز Ghost Focus 🛡️",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        soundStudio.playTap()
                                        onOpenGuilds()
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                                ) {
                                    Text(
                                        text = "استكشاف وإنشاء التحالفات 🛡️",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // TAB 4: Peer Academic Chat
            if (selectedTab == CommunityTab.CHAT) {
                item {
                    SectionHeader(
                        title = "دردشة الدفعة والأقران",
                        subtitle = "تبادل الاستفسارات الأكاديمية والمواعيد مع الطلاب"
                    )
                }

                items(chatMessages) { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (msg.isMe) ScopePrimary else MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = msg.senderName,
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (msg.isMe) Color.White.copy(alpha = 0.9f) else ScopePrimary
                                    )
                                    Text(
                                        text = msg.timeLabel,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 9.sp,
                                        color = if (msg.isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.content,
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = if (msg.isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }

                // Chat Input Field
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = { Text("اكتب استفسارك أو رسالتك للدفعة...", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("campus_chat_input"),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (chatInputText.isNotBlank()) {
                                    soundStudio.playTap()
                                    chatMessages.add(
                                        CampusMessage(
                                            senderName = userProfile?.displayName ?: "أنت",
                                            senderScopeId = userProfile?.scopeId ?: "@me",
                                            content = chatInputText,
                                            timeLabel = "الآن",
                                            isMe = true
                                        )
                                    )
                                    chatInputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(ScopePrimary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال", tint = Color.White)
                        }
                    }
                }
            }

            // TAB 5: Collaborative Shared Notes
            if (selectedTab == CommunityTab.SHARED_NOTES) {
                item {
                    SectionHeader(
                        title = "الملخصات والمذكرات التشاركية",
                        subtitle = "أفضل ملخصات الطلاب والمذكرات المعتمدة"
                    )
                }

                items(sharedNotes) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = note.title,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ScopePrimary.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = note.subject,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ScopePrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = note.summary,
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "إعداد: ${note.author}",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ScopeFocusGreen.copy(alpha = 0.12f))
                                        .clickable {
                                            soundStudio.playTap()
                                            val idx = sharedNotes.indexOf(note)
                                            if (idx != -1) {
                                                sharedNotes[idx] = note.copy(likesCount = note.likesCount + 1)
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = ScopeFocusGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${note.likesCount} إعجاب",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ScopeFocusGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
