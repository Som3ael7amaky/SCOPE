package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ClassOccurrence
import com.example.data.model.Subject
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber
import com.example.ui.viewmodel.ScheduleWindow

data class EventCategoryInfo(
    val code: String,
    val titleAr: String,
    val iconEmoji: String,
    val colorHex: String
)

val EVENT_CATEGORIES = listOf(
    EventCategoryInfo("ACADEMIC", "دراسي/جامعي", "🎓", "#38BDF8"),
    EventCategoryInfo("WORK", "عمل/دوام", "💼", "#F97316"),
    EventCategoryInfo("SELF_DEV", "تطوير ذاتي", "⚡", "#10B981"),
    EventCategoryInfo("PERSONAL", "شخصي/صحة", "🧘", "#A855F7")
)

fun parseColor(hex: String, fallback: Color = Color(0xFF38BDF8)): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}

@Composable
fun ScheduleScreen(
    occurrences: List<ClassOccurrence>,
    subjects: List<Subject>,
    availableWindows: List<ScheduleWindow>,
    onAddOccurrence: (ClassOccurrence) -> Unit,
    onDeleteOccurrence: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf(1) } // 1 = الأحد ... 7 = السبت
    var showAddDialog by remember { mutableStateOf(false) }

    val daysOfWeek = listOf(
        1 to "الأحد",
        2 to "الاثنين",
        3 to "الثلاثاء",
        4 to "الأربعاء",
        5 to "الخميس",
        6 to "الجمعة",
        7 to "السبت"
    )

    // Filter occurrences for the selected day & sort by start time
    val currentDayOccurrences = remember(occurrences, selectedDay) {
        occurrences
            .filter { it.dayOfWeek == selectedDay }
            .sortedBy { it.startTime }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("schedule_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. 7-Day Weekday Selector Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.92f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.7f))
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(daysOfWeek) { (dayNum, dayName) ->
                        val isSelected = selectedDay == dayNum
                        val dayEventsCount = occurrences.count { it.dayOfWeek == dayNum }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.linearGradient(
                                            colors = listOf(ScopePrimary, Color(0xFF0284C7))
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(Color.Transparent, Color.Transparent)
                                        )
                                    }
                                )
                                .border(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = if (isSelected) Color(0xFF38BDF8) else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedDay = dayNum }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayName,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                )
                                if (dayEventsCount > 0) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.White else ScopePrimary)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Schedule Intelligence: Focus Windows
        if (availableWindows.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "نوافذ التركيز الذكية ⚡",
                    subtitle = "فرص زمنية خالية بين التزاماتك لاستغلالها"
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    availableWindows.forEach { window ->
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (window.isFocusWindow) Color(0xFF0C4A6E).copy(alpha = 0.5f) else Color(0xFF1E293B).copy(alpha = 0.6f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (window.isFocusWindow) ScopePrimary.copy(alpha = 0.5f) else Color(0xFF334155)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (window.isFocusWindow) Icons.Default.Star else Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = if (window.isFocusWindow) ScopePrimary else Color(0xFF94A3B8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${window.durationMinutes} دقيقة",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (window.isFocusWindow) ScopePrimary else Color(0xFF94A3B8)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = window.title,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = window.timeRange,
                                    fontFamily = CairoFontFamily,
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Section Header for Daily Timeline
        item {
            SectionHeader(
                title = "الجدول الزمني للأنشطة 🗓️",
                subtitle = "${daysOfWeek.firstOrNull { it.first == selectedDay }?.second ?: ""} - ${currentDayOccurrences.size} مواعيد مجدولة",
                action = {
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ScopePrimary,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة موعد", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
        }

        // 4. Empty Slate vs. Vertical Timeline Cards
        if (currentDayOccurrences.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.85f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.sola_happy),
                            contentDescription = "Sola Companion",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "يوم هادئ بدون مواعيد!",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "لا توجد مواعيد أو محاضرات مجدولة لهذا اليوم. يمديك تشحن طاقتك أو تضيف موعد جديد لروتينك الأكاديمي.",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إضافة موعد جديد للجدول", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        } else {
            // Vertical Timeline Items
            items(currentDayOccurrences) { occ ->
                val categoryInfo = EVENT_CATEGORIES.firstOrNull { it.code == occ.category } ?: EVENT_CATEGORIES[0]
                val categoryColor = parseColor(occ.categoryColorHex.ifBlank { categoryInfo.colorHex })

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Left Timeline Column
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(64.dp)
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            text = occ.startTime,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = occ.endTime,
                            fontFamily = CairoFontFamily,
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(categoryColor)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Card Content
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .border(
                                1.dp,
                                categoryColor.copy(alpha = 0.4f),
                                RoundedCornerShape(18.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.9f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category Tag
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(categoryColor.copy(alpha = 0.18f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${categoryInfo.iconEmoji} ${categoryInfo.titleAr}",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = categoryColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Routine / One-Time Tag
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1E293B))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (occ.eventType == "ONE_TIME") "📅 حدث لمرة واحدة" else "🔁 روتين أسبوعي",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                // Delete Event Option
                                IconButton(
                                    onClick = { onDeleteOccurrence(occ.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف الموعد",
                                        tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Event Title
                            Text(
                                text = occ.subjectName,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Time & Room Details
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${occ.startTime} - ${occ.endTime}",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = occ.room.ifBlank { "غير محدد" },
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            // Custom Audio Indicator Tag if present
                            if (occ.audioUri.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F2942))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = ScopePrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "تنبيه صوتي خاص مخصص 🎵",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 10.sp,
                                        color = ScopePrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 5. Add Schedule Event Dialog / Modal
    if (showAddDialog) {
        var eventTitle by remember { mutableStateOf("") }
        var selectedSubject by remember { mutableStateOf<Subject?>(subjects.firstOrNull()) }
        var eventType by remember { mutableStateOf("RECURRING") } // "RECURRING" or "ONE_TIME"
        var selectedCategory by remember { mutableStateOf(EVENT_CATEGORIES[0]) }
        var eventDayNum by remember { mutableStateOf(selectedDay) }
        var specificDateInput by remember { mutableStateOf("") }
        var startTimeInput by remember { mutableStateOf("09:00") }
        var endTimeInput by remember { mutableStateOf("10:30") }
        var roomInput by remember { mutableStateOf("القاعة 101") }
        var hasAlarm by remember { mutableStateOf(true) }
        var customAudioUri by remember { mutableStateOf("") }
        var customAudioFileName by remember { mutableStateOf("") }

        // Custom Audio File Picker Launcher
        val audioPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                customAudioUri = uri.toString()
                customAudioFileName = uri.lastPathSegment ?: "ملف صوتي خاص"
            }
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "إضافة موعد أو روتين جديد ⏰",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Event Title
                    OutlinedTextField(
                        value = eventTitle,
                        onValueChange = { eventTitle = it },
                        label = { Text("عنوان الموعد / النشاط", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                        placeholder = { Text("مثال: محاضرة الخوارزميات / دوام رسمي", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ScopePrimary,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Subject Selector (Optional)
                    if (subjects.isNotEmpty()) {
                        Text("ربط بـ مقرر أكاديمي (اختياري):", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color(0xFF94A3B8))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(subjects) { s ->
                                val isSel = selectedSubject?.id == s.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) ScopePrimary else Color(0xFF1E293B))
                                        .clickable {
                                            selectedSubject = s
                                            if (eventTitle.isBlank()) eventTitle = s.name
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = s.name,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = if (isSel) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Event Type Toggle (RECURRING vs ONE_TIME)
                    Text("نوع الموعد:", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { eventType = "RECURRING" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (eventType == "RECURRING") ScopePrimary.copy(alpha = 0.2f) else Color.Transparent,
                                contentColor = if (eventType == "RECURRING") ScopePrimary else Color(0xFF94A3B8)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (eventType == "RECURRING") ScopePrimary else Color(0xFF334155))
                        ) {
                            Text("🔁 روتين أسبوعي", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { eventType = "ONE_TIME" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (eventType == "ONE_TIME") ScopePrimary.copy(alpha = 0.2f) else Color.Transparent,
                                contentColor = if (eventType == "ONE_TIME") ScopePrimary else Color(0xFF94A3B8)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (eventType == "ONE_TIME") ScopePrimary else Color(0xFF334155))
                        ) {
                            Text("📅 حدث لمرة واحدة", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Category Selector
                    Text("التصنيف:", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color(0xFF94A3B8))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(EVENT_CATEGORIES) { cat ->
                            val isSel = selectedCategory.code == cat.code
                            val catColor = parseColor(cat.colorHex)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) catColor.copy(alpha = 0.25f) else Color(0xFF1E293B))
                                    .border(1.dp, if (isSel) catColor else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${cat.iconEmoji} ${cat.titleAr}",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    color = if (isSel) catColor else Color.White,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Day / Date Selector
                    if (eventType == "RECURRING") {
                        Text("اليوم الأسبوعي للروتين:", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color(0xFF94A3B8))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(daysOfWeek) { (dNum, dName) ->
                                val isSel = eventDayNum == dNum
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) ScopePrimary else Color(0xFF1E293B))
                                        .clickable { eventDayNum = dNum }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = dName,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = if (isSel) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = specificDateInput,
                            onValueChange = { specificDateInput = it },
                            label = { Text("التاريخ المحدد (YYYY-MM-DD)", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                            placeholder = { Text("2026-10-25", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ScopePrimary,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    // Times & Room Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = startTimeInput,
                            onValueChange = { startTimeInput = it },
                            label = { Text("وقت البدء", fontFamily = CairoFontFamily, fontSize = 10.sp) },
                            placeholder = { Text("09:00", fontFamily = CairoFontFamily, fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ScopePrimary,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = endTimeInput,
                            onValueChange = { endTimeInput = it },
                            label = { Text("وقت الانتهاء", fontFamily = CairoFontFamily, fontSize = 10.sp) },
                            placeholder = { Text("10:30", fontFamily = CairoFontFamily, fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ScopePrimary,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    OutlinedTextField(
                        value = roomInput,
                        onValueChange = { roomInput = it },
                        label = { Text("المكان / القاعة / المعمل", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                        placeholder = { Text("مثال: القاعة 101 / المعمل 3", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ScopePrimary,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Custom Audio Picker Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                            .padding(12.dp)
                    ) {
                        Text("النغمة / الصوت المخصص للتنبيه 🎵:", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedButton(
                            onClick = { audioPickerLauncher.launch("audio/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ScopePrimary.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.AudioFile, contentDescription = null, tint = ScopePrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (customAudioFileName.isNotBlank()) customAudioFileName else "اختيار نغمة خاصة (MP3/WAV)",
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = ScopePrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Enable Alarm Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تفعيل التنبيه والمنبه الذكي ⏰", fontFamily = CairoFontFamily, fontSize = 12.sp, color = Color.White)
                        Switch(
                            checked = hasAlarm,
                            onCheckedChange = { hasAlarm = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ScopePrimary,
                                checkedTrackColor = ScopePrimary.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalTitle = eventTitle.ifBlank { selectedSubject?.name ?: "موعد جديد" }
                        val newOcc = ClassOccurrence(
                            id = java.util.UUID.randomUUID().toString(),
                            subjectId = selectedSubject?.id ?: "",
                            subjectName = finalTitle,
                            dayOfWeek = eventDayNum,
                            startTime = startTimeInput.ifBlank { "09:00" },
                            endTime = endTimeInput.ifBlank { "10:30" },
                            room = roomInput.ifBlank { "غير محدد" },
                            eventType = eventType,
                            category = selectedCategory.code,
                            categoryColorHex = selectedCategory.colorHex,
                            specificDate = specificDateInput,
                            audioUri = customAudioUri,
                            hasAlarm = hasAlarm
                        )
                        onAddOccurrence(newOcc)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                ) {
                    Text("حفظ الموعد في الجدول 🚀", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily, color = Color.Gray)
                }
            },
            containerColor = Color(0xFF121826),
            shape = RoundedCornerShape(22.dp)
        )
    }
}
