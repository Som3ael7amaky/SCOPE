package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AudioSummary
import com.example.data.model.Subject
import com.example.data.model.Task
import com.example.data.projects.ScopeProject
import com.example.data.model.StudyBuddy
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber

/**
 * Universal Glass Search Modal overlay with Cross-App Query Engine & Sola AI Search Prompt
 */
@Composable
fun UniversalSearchModal(
    subjects: List<Subject>,
    tasks: List<Task>,
    audioSummaries: List<AudioSummary>,
    projects: List<ScopeProject>,
    buddies: List<StudyBuddy> = emptyList(),
    onAskSola: (String) -> Unit,
    onNavigateToItem: (category: String, id: String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") } // "ALL", "SUBJECTS", "TASKS", "AUDIO", "PROJECTS", "PEERS"

    val filteredSubjects = remember(searchQuery) {
        if (searchQuery.isBlank()) subjects
        else subjects.filter { it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true) }
    }

    val filteredTasks = remember(searchQuery) {
        if (searchQuery.isBlank()) tasks
        else tasks.filter { it.title.contains(searchQuery, ignoreCase = true) || it.subjectName.contains(searchQuery, ignoreCase = true) }
    }

    val filteredAudio = remember(searchQuery) {
        if (searchQuery.isBlank()) audioSummaries
        else audioSummaries.filter { it.title.contains(searchQuery, ignoreCase = true) || it.keyConcepts.contains(searchQuery, ignoreCase = true) }
    }

    val filteredProjects = remember(searchQuery) {
        if (searchQuery.isBlank()) projects
        else projects.filter { it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true) }
    }

    val sampleBuddies = remember(searchQuery) {
        val list = if (buddies.isNotEmpty()) buddies else listOf(
            StudyBuddy("b1", "عبدالرحمن الشمري", "هندسة الحاسب", "متصل", true, 12, "يذاكر الخوارزميات"),
            StudyBuddy("b2", "سارة خالد", "الذكاء الاصطناعي", "مشغول", true, 8, "تطور مشروع التخرج"),
            StudyBuddy("b3", "محمد العمري", "نظم المعلومات", "في درع التركيز 🛡️", true, 15, "مراجعة قواعد البيانات")
        )
        if (searchQuery.isBlank()) list
        else list.filter { it.name.contains(searchQuery, ignoreCase = true) || it.major.contains(searchQuery, ignoreCase = true) }
    }

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
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF38BDF8),
                            Color(0xFF818CF8),
                            Color(0xFF38BDF8).copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .testTag("universal_search_modal"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Search Input Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ابحث في المقررات، المهام، الملاحظات، المشاريع، والزملاء...", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "بحث", tint = ScopePrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "مسح", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("universal_search_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_universal_search")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Integrated Sola AI Search Prompt Banner
                if (searchQuery.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                onAskSola(searchQuery)
                                onDismiss()
                            }
                            .testTag("sola_ai_search_prompt"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = ScopePrimary.copy(alpha = 0.15f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ScopePrimary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(ScopePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "أسأل صولا الذكية عن '$searchQuery'...",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = ScopePrimary
                                    )
                                    Text(
                                        text = "تحليل المنهج وإجابة سريعة مدعومة بـ Gemini AI",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ScopePrimary)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = "اسأل الآن 🤖", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Category Filter Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SearchCategoryPill(title = "الكل", isSelected = selectedCategory == "ALL", onClick = { selectedCategory = "ALL" })
                    SearchCategoryPill(title = "المقررات 📚", isSelected = selectedCategory == "SUBJECTS", onClick = { selectedCategory = "SUBJECTS" })
                    SearchCategoryPill(title = "المهام 📋", isSelected = selectedCategory == "TASKS", onClick = { selectedCategory = "TASKS" })
                    SearchCategoryPill(title = "الصوتيات 🎧", isSelected = selectedCategory == "AUDIO", onClick = { selectedCategory = "AUDIO" })
                    SearchCategoryPill(title = "المشاريع 📂", isSelected = selectedCategory == "PROJECTS", onClick = { selectedCategory = "PROJECTS" })
                    SearchCategoryPill(title = "الزملاء 👥", isSelected = selectedCategory == "PEERS", onClick = { selectedCategory = "PEERS" })
                }

                // Search Results List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (selectedCategory == "ALL" || selectedCategory == "SUBJECTS") {
                        if (filteredSubjects.isNotEmpty()) {
                            item { SearchSectionHeader(title = "المقررات الأكاديمية", icon = Icons.Default.School) }
                            items(filteredSubjects) { subj ->
                                SearchResultCard(
                                    title = subj.name,
                                    subtitle = "رمز المقرر: ${subj.code} • المحاضر: ${subj.instructor}",
                                    tag = "مقرر",
                                    color = Color(android.graphics.Color.parseColor(subj.colorHex.ifBlank { "#2563EB" })),
                                    onClick = {
                                        onNavigateToItem("SUBJECT", subj.id)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }

                    if (selectedCategory == "ALL" || selectedCategory == "TASKS") {
                        if (filteredTasks.isNotEmpty()) {
                            item { SearchSectionHeader(title = "المهام الدراسية", icon = Icons.Default.Task) }
                            items(filteredTasks) { task ->
                                SearchResultCard(
                                    title = task.title,
                                    subtitle = "مقرر: ${task.subjectName} • الجهد: ${task.effortMinutes} دقيقة",
                                    tag = "مهمة",
                                    color = ScopeWarningAmber,
                                    onClick = {
                                        onNavigateToItem("TASK", task.id)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }

                    if (selectedCategory == "ALL" || selectedCategory == "AUDIO") {
                        if (filteredAudio.isNotEmpty()) {
                            item { SearchSectionHeader(title = "الملخصات والصوتيات", icon = Icons.Default.GraphicEq) }
                            items(filteredAudio) { audio ->
                                SearchResultCard(
                                    title = audio.title,
                                    subtitle = "مقرر: ${audio.subjectName} • المدة: ${audio.durationMinutes} دقيقة",
                                    tag = "ملخص صوتي",
                                    color = ScopeFocusGreen,
                                    onClick = {
                                        onNavigateToItem("AUDIO", audio.id)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }

                    if (selectedCategory == "ALL" || selectedCategory == "PROJECTS") {
                        if (filteredProjects.isNotEmpty()) {
                            item { SearchSectionHeader(title = "المشاريع الأكاديمية", icon = Icons.Default.Folder) }
                            items(filteredProjects) { proj ->
                                SearchResultCard(
                                    title = proj.name,
                                    subtitle = "كود: ${proj.code} • الأعضاء: ${proj.members.size}",
                                    tag = "مشروع",
                                    color = Color(0xFF818CF8),
                                    onClick = {
                                        onNavigateToItem("PROJECT", proj.id)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }

                    if (selectedCategory == "ALL" || selectedCategory == "PEERS") {
                        if (sampleBuddies.isNotEmpty()) {
                            item { SearchSectionHeader(title = "زملاء الدفعة والحرم", icon = Icons.Default.Group) }
                            items(sampleBuddies) { buddy ->
                                SearchResultCard(
                                    title = buddy.name,
                                    subtitle = "التخصص: ${buddy.major} • النشاط: ${buddy.currentActivity}",
                                    tag = "زميل",
                                    color = Color(0xFFEC4899),
                                    onClick = {
                                        onNavigateToItem("PEER", buddy.id)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchCategoryPill(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) ScopePrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            fontFamily = CairoFontFamily,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SearchSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = ScopePrimary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = ScopePrimary
        )
    }
}

@Composable
private fun SearchResultCard(
    title: String,
    subtitle: String,
    tag: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = CairoFontFamily,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = tag,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = color
                )
            }
        }
    }
}
