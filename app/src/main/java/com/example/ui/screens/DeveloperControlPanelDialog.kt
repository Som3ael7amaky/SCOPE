package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CommunityChallenge
import com.example.data.model.DevLiveAnalytics
import com.example.data.model.UserProfile
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber

/**
 * Super Developer In-App Control Center for ismailhamaky@gmail.com
 */
@Composable
fun DeveloperControlPanelDialog(
    userProfile: UserProfile?,
    feedbacks: List<com.example.data.model.FeedbackItem> = emptyList(),
    onRewardFeedback: (feedbackId: String, sparks: Int, tokens: Int) -> Unit = { _, _, _ -> },
    isDevCommunityToolsEnabled: Boolean = true,
    onToggleDevCommunityTools: (Boolean) -> Unit = {},
    onGiftEconomy: (Int, Int, Boolean) -> Unit,
    onGiftToRecipient: (recipient: String, sparks: Int, tokens: Int, grantPro: Boolean, onResult: (Boolean, String) -> Unit) -> Unit = { _, _, _, _, _ -> },
    onCreateChallenge: (CommunityChallenge) -> Unit,
    onUpdateAnalytics: (DevLiveAnalytics) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    // Gifting state
    var recipientQuery by remember { mutableStateOf("") }
    var giftSparksAmount by remember { mutableStateOf("500") }
    var giftTokensAmount by remember { mutableStateOf("50") }
    var giftProStatus by remember { mutableStateOf(true) }
    var giftSuccessMessage by remember { mutableStateOf<String?>(null) }
    var giftErrorMessage by remember { mutableStateOf<String?>(null) }

    // Challenge creator state
    var challengeTitle by remember { mutableStateOf("تحدي الـ 50 ساعة تركيز الأكاديمي 🏆") }
    var challengeDesc by remember { mutableStateOf("ماراثون دراسي تنافسي لكافة طلاب الجامعات قبل موسم الاختبارات") }
    var challengeHours by remember { mutableStateOf("50") }
    var challengeSparks by remember { mutableStateOf("250") }
    var challengeTokens by remember { mutableStateOf("25") }
    var challengeSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Algorithm & Sola announcement state
    var xpMultiplier by remember { mutableFloatStateOf(2.0f) }
    var solaGlobalAnnouncement by remember { mutableStateOf("🚀 تنبيه المطور: تم مضاعفة نقاط الخبرة (2.0x XP) لجميع جلسات البومودورو بمناسبة أسبوع الاختبارات!") }

    val infiniteTransition = rememberInfiniteTransition(label = "DevCrownSheen")
    val goldenSheen by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sheen"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(28.dp))
                .border(
                    width = 2.5.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = goldenSheen),
                            Color(0xFFFF8C00),
                            Color(0xFF38BDF8),
                            Color(0xFFFFD700).copy(alpha = goldenSheen)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .testTag("developer_control_panel_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Top Dev Banner
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
                                .background(Color(0x33FFD700)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👑", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "لوحة تحكم المطور الأسمى (Super Dev)",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFFFFD700)
                                )
                            }
                            Text(
                                text = "ismailhamaky@gmail.com • الصلاحيات الفائقة مفعلة",
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_dev_panel")) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DevNavTab(
                        title = "الإهداء والترقية",
                        icon = Icons.Default.CardGiftcard,
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    DevNavTab(
                        title = "التحديات",
                        icon = Icons.Default.EmojiEvents,
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                    DevNavTab(
                        title = "الخوارزمية",
                        icon = Icons.Default.Speed,
                        isSelected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                    DevNavTab(
                        title = "الملاحظات 📢",
                        icon = Icons.Default.Psychology,
                        isSelected = selectedTab == 4,
                        onClick = { selectedTab = 4 }
                    )
                    DevNavTab(
                        title = "الرادار 📡",
                        icon = Icons.Default.GraphicEq,
                        isSelected = selectedTab == 3,
                        onClick = { selectedTab = 3 }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))


                // Tab Content Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        0 -> {
                            // User Management & Economy Gifting
                            DevSectionCard(title = "إهداء العملات وترقية SCOPE Pro ⚡") {
                                Text(
                                    text = "إرسال شحنة تكريم أو مكافأة بشرارات التركيز ⚡ ورموز صولا 🪙 إلى أي طالب أو نفسك:",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Mandatory/Optional Recipient Lookup Field
                                OutlinedTextField(
                                    value = recipientQuery,
                                    onValueChange = {
                                        recipientQuery = it
                                        giftSuccessMessage = null
                                        giftErrorMessage = null
                                    },
                                    label = { Text("كود SCOPE أو البريد الإلكتروني للمستلم (مثال: SC-ZXVC37)", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                                    placeholder = { Text("اتركه فارغاً لشحن حسابك الحالي الذاتي", fontFamily = CairoFontFamily, fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("gifting_recipient_input"),
                                    singleLine = true
                                )

                                val isSelfGiftingAttempt = remember(recipientQuery, userProfile) {
                                    val q = recipientQuery.trim().lowercase()
                                    q.isNotEmpty() && (
                                        q == userProfile?.email?.lowercase() ||
                                        q == userProfile?.scopeId?.lowercase() ||
                                        q == "@${userProfile?.scopeId?.removePrefix("@")?.lowercase()}"
                                    )
                                }

                                if (isSelfGiftingAttempt) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "لا يمكنك إهداء نفسك! اختر زعيماً أو صديقاً آخر 🚫",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = giftSparksAmount,
                                        onValueChange = { giftSparksAmount = it },
                                        label = { Text("الشرارات ⚡", fontFamily = CairoFontFamily, fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = giftTokensAmount,
                                        onValueChange = { giftTokensAmount = it },
                                        label = { Text("الرموز 🪙", fontFamily = CairoFontFamily, fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            giftSuccessMessage = null
                                            giftErrorMessage = null
                                            if (isSelfGiftingAttempt) {
                                                giftErrorMessage = "لا يمكنك إهداء نفسك! اختر زعيماً أو صديقاً آخر 🚫"
                                                return@Button
                                            }

                                            val sp = giftSparksAmount.toIntOrNull() ?: 500
                                            val tk = giftTokensAmount.toIntOrNull() ?: 50

                                            if (recipientQuery.isNotBlank()) {
                                                onGiftToRecipient(recipientQuery.trim(), sp, tk, giftProStatus) { success, msg ->
                                                    if (success) giftSuccessMessage = msg else giftErrorMessage = msg
                                                }
                                            } else {
                                                onGiftEconomy(sp, tk, true)
                                                giftSuccessMessage = "تم شحن حسابك الذاتي بـ $sp ⚡ و $tk 🪙 وترقية SCOPE Pro بنجاح!"
                                            }
                                        },
                                        enabled = !isSelfGiftingAttempt,
                                        modifier = Modifier.weight(1f).height(44.dp).testTag("gift_economy_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ScopeFocusGreen)
                                    ) {
                                        Text(
                                            text = if (recipientQuery.isNotBlank()) "إهداء الطالب المستهدف 🎁" else "شحن حسابك وتفعيل Pro 👑",
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }
                                }

                                if (giftSuccessMessage != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = giftSuccessMessage ?: "",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = ScopeFocusGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (giftErrorMessage != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = giftErrorMessage ?: "",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            DevSectionCard(title = "بيانات المطور الحالية في النظام") {
                                DevInfoRow(label = "الاسم الظاهر:", value = userProfile?.displayName ?: "المطور إسماعيل")
                                DevInfoRow(label = "المعرف الرسمي:", value = userProfile?.scopeId ?: "@ismail_dev")
                                DevInfoRow(label = "رتبة النظام:", value = "System Creator & Lead Architect")
                                DevInfoRow(label = "شعلة الاستمرارية:", value = "${userProfile?.flameCount ?: 99} يوم 🔥")
                                DevInfoRow(label = "رصيد الشرارات:", value = "${userProfile?.sparks ?: 9999} ⚡")
                            }
                        }

                        1 -> {
                            // Community Challenge Creator
                            DevSectionCard(title = "إنشاء تحدي أكاديمي عام لكافة الطلاب 🏆") {
                                OutlinedTextField(
                                    value = challengeTitle,
                                    onValueChange = { challengeTitle = it },
                                    label = { Text("عنوان التحدي", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = challengeDesc,
                                    onValueChange = { challengeDesc = it },
                                    label = { Text("وصف التحدي وقواعد المشاركة", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = challengeHours,
                                        onValueChange = { challengeHours = it },
                                        label = { Text("الساعات المطلوبة", fontFamily = CairoFontFamily, fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = challengeSparks,
                                        onValueChange = { challengeSparks = it },
                                        label = { Text("جائزة ⚡", fontFamily = CairoFontFamily, fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = challengeTokens,
                                        onValueChange = { challengeTokens = it },
                                        label = { Text("جائزة 🪙", fontFamily = CairoFontFamily, fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val newChallenge = CommunityChallenge(
                                            title = challengeTitle,
                                            description = challengeDesc,
                                            targetHours = challengeHours.toIntOrNull() ?: 50,
                                            rewardSparks = challengeSparks.toIntOrNull() ?: 250,
                                            rewardTokens = challengeTokens.toIntOrNull() ?: 25,
                                            participantCount = 1,
                                            daysRemaining = 7
                                        )
                                        onCreateChallenge(newChallenge)
                                        challengeSuccessMessage = "تم نشر تحدي '${newChallenge.title}' لكافة الطلاب بنجاح!"
                                    },
                                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("publish_challenge_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C00))
                                ) {
                                    Text(
                                        text = "نشر التحدي الأكاديمي العام 🚀",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }

                                if (challengeSuccessMessage != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = challengeSuccessMessage ?: "",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = ScopeFocusGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        2 -> {
                            // Algorithm & Announcements
                            DevSectionCard(title = "مضاعف خبرة المذاكرة (Exam XP Multiplier) ⚡") {
                                Text(
                                    text = "مضاعف الخبرة الحالي: ${String.format("%.1f", xpMultiplier)}x",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ScopePrimary
                                )
                                Slider(
                                    value = xpMultiplier,
                                    onValueChange = { xpMultiplier = it },
                                    valueRange = 1.0f..4.0f,
                                    steps = 5,
                                    colors = SliderDefaults.colors(thumbColor = ScopePrimary, activeTrackColor = ScopePrimary)
                                )
                                Text(
                                    text = "يضاعف نقاط الخبرة المكتسبة من جلسات البومودورو وإنجاز المهام في فترات الضغط الأكاديمي.",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            DevSectionCard(title = "تعديل إعلان صولا العام (Sola Announcement)") {
                                OutlinedTextField(
                                    value = solaGlobalAnnouncement,
                                    onValueChange = { solaGlobalAnnouncement = it },
                                    label = { Text("نص الإعلان الشامل لجميع الشاشات", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        onUpdateAnalytics(
                                            DevLiveAnalytics(
                                                xpMultiplier = xpMultiplier,
                                                activeAnnouncement = solaGlobalAnnouncement
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                                ) {
                                    Text(
                                        text = "حفظ وتحديث الإعلان للجميع ✓",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        3 -> {
                            // Live Analytics Radar
                            DevSectionCard(title = "رادار النشاط الحي لبيئة SCOPE 📡") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    DevRadarStat(number = "428", label = "طالب نشط الآن", color = ScopeFocusGreen)
                                    DevRadarStat(number = "3,840", label = "ساعات مذاكرة مسجلة", color = ScopePrimary)
                                    DevRadarStat(number = "94%", label = "التزام بجلسات التركيز", color = ScopeWarningAmber)
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "أكثر المواد تفاعلاً اليوم:",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                 listOf(
                                    "1. الخوارزميات وهياكل البيانات (1,240 ساعة)",
                                    "2. أنظمة قواعد البيانات الموزعة (980 ساعة)",
                                    "3. الذكاء الاصطناعي وتعلم الآلة (890 ساعة)"
                                ).forEach {
                                    Text(
                                        text = it,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        4 -> {
                            // SCOPE Listens Feedback Admin Portal
                            DevSectionCard(title = "إدارة ملاحظات الطلاب وإهداء المكافآت 📢 (SCOPE Listens)") {
                                if (feedbacks.isEmpty()) {
                                    Text(
                                        text = "لا توجد ملاحظات جديدة من الطلاب حالياً.",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    feedbacks.forEach { fb ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "${fb.senderName} (${fb.senderScopeId})",
                                                        fontFamily = CairoFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = ScopePrimary
                                                    )
                                                    Text(
                                                        text = fb.category,
                                                        fontFamily = CairoFontFamily,
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = fb.content,
                                                    fontFamily = CairoFontFamily,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                if (fb.status == "PENDING") {
                                                    Button(
                                                        onClick = { onRewardFeedback(fb.id, 100, 10) },
                                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = ScopeFocusGreen)
                                                    ) {
                                                        Text(
                                                            text = "إهداء +100⚡ و +10🪙 وإغلاق الملاحظة 🎁",
                                                            fontFamily = CairoFontFamily,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.sp,
                                                            color = Color.White
                                                        )
                                                    }
                                                } else {
                                                    Text(
                                                        text = "تمت مكافأة الطالب بنجاح (+${fb.rewardedSparks}⚡)",
                                                        fontFamily = CairoFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp,
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
            }
        }
    }
}


@Composable
private fun DevNavTab(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFFFFD700).copy(alpha = 0.2f) else Color.Transparent
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontFamily = CairoFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.sp,
                color = if (isSelected) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DevSectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, ScopePrimary.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun DevInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontFamily = CairoFontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun DevRadarStat(number: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = number, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        Text(text = label, fontFamily = CairoFontFamily, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
