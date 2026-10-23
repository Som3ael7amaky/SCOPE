package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.KanbanColumn
import com.example.data.model.ProjectAttachment
import com.example.data.model.ProjectMilestone
import com.example.data.model.Subject
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import com.example.ui.audio.ScopeSoundStudio
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeAccent
import com.example.ui.theme.ScopeFocusGreen
import com.example.data.projects.ScopeProject
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber

@Composable
fun ProjectsScreen(
    tasks: List<Task>,
    subjects: List<Subject> = emptyList(),
    projects: List<ScopeProject> = emptyList(),
    onOpenProjectDetail: (ScopeProject) -> Unit = {},
    onToggleTask: (String, Boolean) -> Unit = { _, _ -> },
    onAddTask: (String, Subject, TaskPriority, Int) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundStudio = remember { ScopeSoundStudio.getInstance(context) }

    // Kanban state
    var selectedColumn by remember { mutableStateOf<KanbanColumn?>(null) } // null = All columns view

    // In-memory tracked Milestones
    val milestones = remember {
        mutableStateListOf(
            ProjectMilestone(title = "مواصفات المعمارية التقنية (Architecture Spec)", dueDate = "بعد 3 أيام", isCompleted = true),
            ProjectMilestone(title = "تكامل نماذج الذكاء الاصطناعي مع الواجهة", dueDate = "بعد 6 أيام", isCompleted = false),
            ProjectMilestone(title = "العرض التقديمي التجريبي واختبار المستخدمين", dueDate = "بعد 12 يوماً", isCompleted = false)
        )
    }

    // In-memory tracked Attachments
    val attachments = remember {
        mutableStateListOf(
            ProjectAttachment(name = "Scope_System_Design.pdf", sizeLabel = "2.4 MB", fileType = "PDF"),
            ProjectAttachment(name = "Dataset_Analytics_v3.csv", sizeLabel = "850 KB", fileType = "DATA"),
            ProjectAttachment(name = "Sprint_Backlog_Specs.docx", sizeLabel = "1.2 MB", fileType = "DOC")
        )
    }

    var showAddMilestoneDialog by remember { mutableStateOf(false) }
    var showAddAttachmentDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showCreateProjectDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("projects_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Workspace Hero Banner
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
                                        .background(ScopePrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ViewKanban,
                                        contentDescription = null,
                                        tint = ScopePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "مساحة المشاريع ولوحة متابعة المهام",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Projects & Agile Kanban Hub",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ScopePrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "إنتاجية بحتة",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ScopePrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Sprint Progress Bar
                        val completedCount = tasks.count { it.isCompleted }
                        val totalCount = tasks.size.coerceAtLeast(1)
                        val progress = completedCount.toFloat() / totalCount

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "إنجاز مهام المشروع الأكاديمي",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$completedCount من $totalCount (${(progress * 100).toInt()}%)",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ScopePrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = ScopeFocusGreen,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            // Active Projects Hub Cards
            if (projects.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "المشاريع الأكاديمية النشطة (${projects.size})",
                        subtitle = "انقر لفتح لوحة التحكم الديمقراطية والمالية الكاملة للمشروع",
                        action = {
                            Button(
                                onClick = { showCreateProjectDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ScopeAccent)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("مشروع جديد +", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        projects.forEach { proj ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        soundStudio.playTap()
                                        onOpenProjectDetail(proj)
                                    }
                                    .testTag("project_card_${proj.id}"),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ScopePrimary.copy(alpha = 0.25f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(ScopePrimary.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Folder, contentDescription = null, tint = ScopePrimary, modifier = Modifier.size(20.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                                Text(
                                                    text = proj.name,
                                                    fontFamily = CairoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "كود: ${proj.code} • ${proj.members.size} أعضاء • الرصيد: ${proj.financials.balanceFiat.toInt()} ${proj.financials.fiatCurrency}",
                                                    fontFamily = CairoFontFamily,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = {
                                                soundStudio.playTap()
                                                onOpenProjectDetail(proj)
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                                        ) {
                                            Text(
                                                text = "لوحة التحكم 🚀",
                                                fontFamily = CairoFontFamily,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    val prog = proj.progressPercentage
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "نسبة الإنجاز",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${(prog * 100).toInt()}%",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ScopeFocusGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { prog },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = ScopeFocusGreen,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Kanban Column Filter Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KanbanFilterChip(
                        title = "الكل (${tasks.size})",
                        isSelected = selectedColumn == null,
                        color = ScopePrimary,
                        onClick = {
                            soundStudio.playTap()
                            selectedColumn = null
                        }
                    )
                    KanbanFilterChip(
                        title = "📋 قيد الانتظار (${tasks.count { !it.isCompleted }})",
                        isSelected = selectedColumn == KanbanColumn.TODO,
                        color = ScopeWarningAmber,
                        onClick = {
                            soundStudio.playTap()
                            selectedColumn = KanbanColumn.TODO
                        }
                    )
                    KanbanFilterChip(
                        title = "✅ منجز (${tasks.count { it.isCompleted }})",
                        isSelected = selectedColumn == KanbanColumn.DONE,
                        color = ScopeFocusGreen,
                        onClick = {
                            soundStudio.playTap()
                            selectedColumn = KanbanColumn.DONE
                        }
                    )
                }
            }

            // Kanban Tasks List
            val displayedTasks = when (selectedColumn) {
                KanbanColumn.TODO -> tasks.filter { !it.isCompleted }
                KanbanColumn.DONE -> tasks.filter { it.isCompleted }
                else -> tasks
            }

            item {
                SectionHeader(
                    title = "لوحة متابعة المهام ومراحل العمل (${displayedTasks.size})",
                    subtitle = "انقر لنقل المهمة بين مراحل التنفيذ والإنجاز"
                )
            }

            if (displayedTasks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد مهام في هذا العمود حالياً.",
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(displayedTasks) { task ->
                    KanbanTaskCard(
                        task = task,
                        onToggle = {
                            if (!task.isCompleted) {
                                soundStudio.playComplete()
                            } else {
                                soundStudio.playTap()
                            }
                            onToggleTask(task.id, !task.isCompleted)
                        }
                    )
                }
            }

            // Project Milestones Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "المراحل الأكاديمية الرئيسية (Milestones)",
                        subtitle = "محطات التسليم والتقييمات الكبرى"
                    )
                    TextButton(onClick = {
                        soundStudio.playTap()
                        showAddMilestoneDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة مرحلة", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(milestones) { milestone ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (milestone.isCompleted) ScopeFocusGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                soundStudio.playTap()
                                val idx = milestones.indexOf(milestone)
                                if (idx != -1) {
                                    milestones[idx] = milestone.copy(isCompleted = !milestone.isCompleted)
                                    if (!milestone.isCompleted) soundStudio.playComplete()
                                }
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (milestone.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (milestone.isCompleted) ScopeFocusGreen else ScopePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = milestone.title,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تاريخ الاستحقاق: ${milestone.dueDate}",
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (milestone.isCompleted) ScopeFocusGreen.copy(alpha = 0.15f) else ScopeWarningAmber.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (milestone.isCompleted) "منجز" else "مجدول",
                                fontFamily = CairoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (milestone.isCompleted) ScopeFocusGreen else ScopeWarningAmber
                            )
                        }
                    }
                }
            }

            // Project File Attachments Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "الملحقات والمستندات الأكاديمية (Attachments)",
                        subtitle = "وثائق المشروع، الأكواد، وملفات البيانات المرجعية"
                    )
                    TextButton(onClick = {
                        soundStudio.playTap()
                        showAddAttachmentDialog = true
                    }) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إرفاق ملف", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(attachments) { att ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ScopePrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = ScopePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = att.name,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${att.fileType} • ${att.sizeLabel}",
                                fontFamily = CairoFontFamily,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ScopeAccent.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "مُرفق",
                                fontFamily = CairoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ScopeAccent
                            )
                        }
                    }
                }
            }
        }

        // Add Task FAB
        FloatingActionButton(
            onClick = {
                soundStudio.playTap()
                showAddTaskDialog = true
            },
            containerColor = ScopePrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 80.dp)
                .testTag("add_project_task_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة مهمة جديدة")
        }

        // Add Milestone Dialog
        if (showAddMilestoneDialog) {
            var newTitle by remember { mutableStateOf("") }
            var newDate by remember { mutableStateOf("خلال أسبوع") }

            Dialog(onDismissRequest = { showAddMilestoneDialog = false }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "إضافة مرحلة رئيسية (Milestone)",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("عنوان المرحلة (مثل: تسليم مسودة البحث)", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newDate,
                            onValueChange = { newDate = it },
                            label = { Text("موعد التسليم المتوقع", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddMilestoneDialog = false }) {
                                Text("إلغاء", fontFamily = CairoFontFamily)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newTitle.isNotBlank()) {
                                        soundStudio.playTap()
                                        milestones.add(ProjectMilestone(title = newTitle, dueDate = newDate))
                                        showAddMilestoneDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                            ) {
                                Text("إضافة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Add Attachment Dialog
        if (showAddAttachmentDialog) {
            var newFileName by remember { mutableStateOf("") }
            var newFileType by remember { mutableStateOf("PDF") }

            Dialog(onDismissRequest = { showAddAttachmentDialog = false }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "إرفاق مستند جديد بالمشروع",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newFileName,
                            onValueChange = { newFileName = it },
                            label = { Text("اسم الملف (مثل: Final_Project_Report.pdf)", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddAttachmentDialog = false }) {
                                Text("إلغاء", fontFamily = CairoFontFamily)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newFileName.isNotBlank()) {
                                        soundStudio.playTap()
                                        attachments.add(
                                            ProjectAttachment(
                                                name = newFileName,
                                                sizeLabel = "1.8 MB",
                                                fileType = newFileType
                                            )
                                        )
                                        showAddAttachmentDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                            ) {
                                Text("حفظ المرفق", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Add Task Dialog
        if (showAddTaskDialog) {
            var taskTitle by remember { mutableStateOf("") }
            var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()) }
            var effortMinutes by remember { mutableStateOf(45) }

            Dialog(onDismissRequest = { showAddTaskDialog = false }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "إضافة مهمة جديدة لمتابعة العمل",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = taskTitle,
                            onValueChange = { taskTitle = it },
                            label = { Text("عنوان المهمة الأكاديمية", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddTaskDialog = false }) {
                                Text("إلغاء", fontFamily = CairoFontFamily)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (taskTitle.isNotBlank()) {
                                        soundStudio.playTap()
                                        val subj = selectedSubject ?: Subject(name = "مشروع التخرج", code = "CS499")
                                        onAddTask(taskTitle, subj, TaskPriority.HIGH, effortMinutes)
                                        showAddTaskDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                            ) {
                                Text("إضافة للوحة المتابعة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Create Project Dialog
        if (showCreateProjectDialog) {
            var newProjName by remember { mutableStateOf("") }
            var newProjGoal by remember { mutableStateOf("") }
            var initialBudget by remember { mutableStateOf("0") }

            Dialog(onDismissRequest = { showCreateProjectDialog = false }) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)), // Obsidian Dark
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ScopePrimary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "إنشاء مشروع جديد 🚀",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "ابدأ رحلة تعاونية جديدة مع زملائك",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = newProjName,
                            onValueChange = { newProjName = it },
                            label = { Text("اسم المشروع", fontFamily = CairoFontFamily, color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        OutlinedTextField(
                            value = newProjGoal,
                            onValueChange = { newProjGoal = it },
                            label = { Text("هدف المشروع (اختصاري)", fontFamily = CairoFontFamily, color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        OutlinedTextField(
                            value = initialBudget,
                            onValueChange = { if (it.all { char -> char.isDigit() }) initialBudget = it },
                            label = { Text("الميزانية الأولية (ر.س)", fontFamily = CairoFontFamily, color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextButton(
                                onClick = { showCreateProjectDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("إلغاء", fontFamily = CairoFontFamily, color = Color.Gray)
                            }
                            Button(
                                onClick = {
                                    if (newProjName.isNotBlank()) {
                                        soundStudio.playComplete()
                                        showCreateProjectDialog = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("إنشاء المشروع", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KanbanFilterChip(
    title: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontFamily = CairoFontFamily,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun KanbanTaskCard(
    task: Task,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) ScopeFocusGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
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
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (task.isCompleted) ScopeFocusGreen else Color.Transparent)
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = ScopePrimary, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${task.subjectName} • ${task.effortMinutes} دقيقة",
                    fontFamily = CairoFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (task.isCompleted) ScopeFocusGreen.copy(alpha = 0.15f) else ScopeWarningAmber.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (task.isCompleted) "منجز ✅" else "قيد العمل ⏳",
                    fontFamily = CairoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) ScopeFocusGreen else ScopeWarningAmber
                )
            }
        }
    }
}
