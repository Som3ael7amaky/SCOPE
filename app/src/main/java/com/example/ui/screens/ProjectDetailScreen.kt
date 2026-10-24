package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ProjectMilestone
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import com.example.data.model.UserProfile
import com.example.data.projects.ProjectMember
import com.example.data.projects.ProjectRole
import com.example.data.projects.ScopeProject
import com.example.ui.audio.ScopeSoundStudio
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeAccent
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber

enum class ProjectTab(val tag: String, val titleAr: String, val icon: ImageVector) {
    OVERVIEW("projects.overview", "النظرة العامة", Icons.Default.Folder),
    TASKS("tasks", "المهام", Icons.Default.ViewKanban),
    MEMBERS("members", "الأعضاء", Icons.Default.Group),
    AUDIT_LOG("audit_log", "السجل", Icons.Default.Timeline),
    FINANCIALS("financials", "المالية", Icons.Default.MonetizationOn),
    NOTES("notes", "ملاحظات", Icons.Default.Description),
    LEADERSHIP("leadership", "القيادة", Icons.Default.HowToVote)
}

@Composable
fun ProjectDetailScreen(
    project: ScopeProject,
    userProfile: UserProfile?,
    onBack: () -> Unit,
    onDeleteProject: (projectId: String) -> Unit = {},
    onAddRegisteredMember: (scopeIdOrEmail: String, customRoleTitle: String) -> Unit = { _, _ -> },
    onAddExternalMember: (displayName: String, customRoleTitle: String) -> Unit = { _, _ -> },
    onAssignMemberRole: (memberId: String, newRole: ProjectRole, customRoleTitle: String) -> Unit = { _, _, _ -> },
    onStartElection: (title: String, candidateMemberIds: List<String>) -> Unit = { _, _ -> },
    onCastVote: (candidateMemberId: String) -> Unit = {},
    onAddTransaction: (title: String, amountFiat: Double, category: String, isExpense: Boolean) -> Unit = { _, _, _, _ -> },
    onPoolSparks: (sparks: Int, tokens: Int) -> Unit = { _, _ -> },
    onAddNote: (title: String, content: String) -> Unit = { _, _ -> },
    onToggleTask: (taskId: String, isCompleted: Boolean) -> Unit = { _, _ -> },
    onAddTask: (title: String) -> Unit = {},
    onAddMilestone: (title: String, dueDate: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundStudio = remember { ScopeSoundStudio.getInstance(context) }

    var selectedTab by remember { mutableStateOf(ProjectTab.OVERVIEW) }

    // Dialog Modals
    var showStartElectionModal by remember { mutableStateOf(false) }
    var showAddTransactionModal by remember { mutableStateOf(false) }
    var showPoolSparksModal by remember { mutableStateOf(false) }
    var showAddNoteModal by remember { mutableStateOf(false) }
    var showAddTaskModal by remember { mutableStateOf(false) }
    var showAddMilestoneModal by remember { mutableStateOf(false) }
    var showAddMemberModal by remember { mutableStateOf(false) }
    var showReportModal by remember { mutableStateOf(false) }
    var selectedMemberForRole by remember { mutableStateOf<ProjectMember?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("project_detail_screen"),
        containerColor = Color(0xFF030712), // Dark Obsidian
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B1329))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        soundStudio.playTap()
                        onBack()
                    },
                    modifier = Modifier.testTag("project_detail_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                var showDeleteConfirm by remember { mutableStateOf(false) }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = project.name,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = project.code,
                                fontFamily = CairoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                    Text(
                        text = "مشروع أكاديمي • ${project.members.size} أعضاء مشاركين",
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ScopeFocusGreen.copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${(project.progressPercentage * 100).toInt()}% مكتمل",
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ScopeFocusGreen
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        soundStudio.playTap()
                        showDeleteConfirm = true
                    },
                    modifier = Modifier.testTag("delete_project_btn")
                ) {
                    Text(text = "🗑️", fontSize = 18.sp)
                }

                if (showDeleteConfirm) {
                    Dialog(onDismissRequest = { showDeleteConfirm = false }) {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("حذف المشروع؟ ⚠️", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "هل أنت متأكد من رغبتك في حذف مشروع '${project.name}'؟ لا يمكن التراجع عن هذا الإجراء وسيتم مسح كافة البيانات المالية والمهام المرتبطة به.",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedButton(
                                        onClick = { showDeleteConfirm = false },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("إلغاء", fontFamily = CairoFontFamily)
                                    }
                                    Button(
                                        onClick = {
                                            soundStudio.playComplete()
                                            showDeleteConfirm = false
                                            onDeleteProject(project.id)
                                            onBack()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("حذف الآن 🗑️", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Glassmorphic Horizontal Multi-Tab Selector (7 Tabs)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ProjectTab.values()) { tab ->
                    val isSelected = selectedTab == tab
                    val tabColor = if (isSelected) Color(0xFF38BDF8) else Color.Gray

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.22f)
                                else Color(0xFF1E293B).copy(alpha = 0.6f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF38BDF8) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                soundStudio.playTap()
                                selectedTab = tab
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("tab_${tab.tag}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.titleAr,
                                tint = tabColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.titleAr,
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color.LightGray
                            )
                        }
                    }
                }
            }

            // Tab Content Body
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    ProjectTab.OVERVIEW -> ProjectOverviewTab(
                        project = project,
                        onOpenTab = { selectedTab = it },
                        onStartElectionClick = { showStartElectionModal = true }
                    )

                    ProjectTab.TASKS -> ProjectTasksTab(
                        tasks = project.tasks,
                        onToggleTask = onToggleTask,
                        onAddTaskClick = { showAddTaskModal = true }
                    )

                    ProjectTab.MEMBERS -> ProjectMembersTab(
                        members = project.members,
                        onStartElectionClick = { showStartElectionModal = true }
                    )

                    ProjectTab.AUDIT_LOG -> ProjectAuditLogTab(
                        auditLog = project.auditLog
                    )

                    ProjectTab.FINANCIALS -> ProjectFinancialsTab(
                        financials = project.financials,
                        onAddTransactionClick = { showAddTransactionModal = true },
                        onPoolSparksClick = { showPoolSparksModal = true }
                    )

                    ProjectTab.NOTES -> ProjectNotesTab(
                        notes = project.notes,
                        onAddNoteClick = { showAddNoteModal = true }
                    )

                    ProjectTab.LEADERSHIP -> ProjectLeadershipTab(
                        project = project,
                        currentUserId = userProfile?.uid ?: "",
                        onStartElectionClick = { showStartElectionModal = true },
                        onCastVote = onCastVote
                    )
                }
            }
        }

        // --- MODAL DIALOGS ---

        // 1. Start Election Modal
        if (showStartElectionModal) {
            var electionTitle by remember { mutableStateOf("انتخابات قائد الفريق التقني لمشروع ${project.code}") }
            val selectedCandidateIds = remember { mutableStateOf(project.members.map { it.id }.toSet()) }

            Dialog(
                onDismissRequest = { showStartElectionModal = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HowToVote, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "بدء انتخاب جديد (Leadership Election)",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                            IconButton(onClick = { showStartElectionModal = false }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = electionTitle,
                            onValueChange = { electionTitle = it },
                            label = { Text("عنوان الانتخابات والهدف", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "الأعضاء المرشحون لقيادة المشروع:",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            project.members.forEach { member ->
                                val isChecked = selectedCandidateIds.value.contains(member.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1E293B))
                                        .clickable {
                                            val current = selectedCandidateIds.value.toMutableSet()
                                            if (isChecked) current.remove(member.id) else current.add(member.id)
                                            selectedCandidateIds.value = current
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isChecked) ScopeFocusGreen else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "${member.displayName} (${member.role.titleAr})",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (electionTitle.isNotBlank() && selectedCandidateIds.value.isNotEmpty()) {
                                    soundStudio.playComplete()
                                    onStartElection(electionTitle, selectedCandidateIds.value.toList())
                                    showStartElectionModal = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                        ) {
                            Text("إطلاق التصويت الديمقراطي 🗳️", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }

        // 2. Add Financial Transaction Modal (Income or Expense)
        if (showAddTransactionModal) {
            var txTitle by remember { mutableStateOf("") }
            var txAmountStr by remember { mutableStateOf("") }
            var isExpense by remember { mutableStateOf(true) }
            var category by remember { mutableStateOf("طباعة ونسخ") }

            Dialog(onDismissRequest = { showAddTransactionModal = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.92f).padding(16.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ScopeAccent)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = if (isExpense) "إضافة مصروف مشروع جديد -" else "إضافة مساهمة / دخل للمشروع +",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Expense / Income Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isExpense) MaterialTheme.colorScheme.error.copy(alpha = 0.3f) else Color(0xFF1E293B))
                                    .clickable { isExpense = true }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("مصروف (-)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isExpense) MaterialTheme.colorScheme.error else Color.Gray)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (!isExpense) ScopeFocusGreen.copy(alpha = 0.3f) else Color(0xFF1E293B))
                                    .clickable { isExpense = false }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("مساهمة / دخل (+)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (!isExpense) ScopeFocusGreen else Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = txTitle,
                            onValueChange = { txTitle = it },
                            label = { Text("بيان المعاملة (مثل: رسوم طباعة البوستر)", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = txAmountStr,
                            onValueChange = { txAmountStr = it },
                            label = { Text("المبلغ (ر.س)", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val amt = txAmountStr.toDoubleOrNull() ?: 0.0
                                if (txTitle.isNotBlank() && amt > 0) {
                                    soundStudio.playComplete()
                                    onAddTransaction(txTitle, amt, category, isExpense)
                                    showAddTransactionModal = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ScopeAccent)
                        ) {
                            Text("حفظ المعاملة المالية", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }

        // 3. Pool Sparks Modal
        if (showPoolSparksModal) {
            var sparksInput by remember { mutableStateOf("50") }
            var tokensInput by remember { mutableStateOf("5") }

            Dialog(onDismissRequest = { showPoolSparksModal = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "إيداع شرارات ⚡ ورموز صولا 🪙 بالمشروع",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = sparksInput,
                            onValueChange = { sparksInput = it },
                            label = { Text("عدد الشرارات ⚡", fontFamily = CairoFontFamily) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = tokensInput,
                            onValueChange = { tokensInput = it },
                            label = { Text("عدد رموز صولا 🪙", fontFamily = CairoFontFamily) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val s = sparksInput.toIntOrNull() ?: 0
                                val t = tokensInput.toIntOrNull() ?: 0
                                if (s > 0 || t > 0) {
                                    soundStudio.playComplete()
                                    onPoolSparks(s, t)
                                    showPoolSparksModal = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                        ) {
                            Text("إيداع الشرارات والرموز 🚀", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Add Note Modal
        if (showAddNoteModal) {
            var noteTitle by remember { mutableStateOf("") }
            var noteContent by remember { mutableStateOf("") }

            Dialog(onDismissRequest = { showAddNoteModal = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.92f).padding(16.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("إضافة ملاحظة توثيقية جديدة 📝", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            label = { Text("عنوان الملاحظة / قرار الاجتماع", fontFamily = CairoFontFamily) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = noteContent,
                            onValueChange = { noteContent = it },
                            label = { Text("محتوى وتفاصيل الملاحظة الأكاديمية", fontFamily = CairoFontFamily) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (noteTitle.isNotBlank()) {
                                    soundStudio.playTap()
                                    onAddNote(noteTitle, noteContent)
                                    showAddNoteModal = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                        ) {
                            Text("حفظ الملاحظة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }

        // 5. Add Task Modal
        if (showAddTaskModal) {
            var taskTitle by remember { mutableStateOf("") }

            Dialog(onDismissRequest = { showAddTaskModal = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("إضافة مهمة جديدة لمشروع ${project.code}", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = taskTitle,
                            onValueChange = { taskTitle = it },
                            label = { Text("عنوان المهمة البرمجية / الفنية", fontFamily = CairoFontFamily) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (taskTitle.isNotBlank()) {
                                    soundStudio.playTap()
                                    onAddTask(taskTitle)
                                    showAddTaskModal = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                        ) {
                            Text("إضافة المهمة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- TAB COMPONENTS ---

@Composable
private fun ProjectOverviewTab(
    project: ScopeProject,
    onOpenTab: (ProjectTab) -> Unit,
    onStartElectionClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Progress Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = project.description,
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val progress = project.progressPercentage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("معدل إنجاز المشروع الإجمالي", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.Gray)
                        Text("${(progress * 100).toInt()}%", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ScopeFocusGreen)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = ScopeFocusGreen,
                        trackColor = Color(0xFF1E293B)
                    )
                }
            }
        }

        // Quick Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OverviewStatCard(title = "الأعضاء", value = "${project.members.size}", sub = "طالب", icon = Icons.Default.Group, color = Color(0xFF38BDF8), modifier = Modifier.weight(1f))
                OverviewStatCard(title = "الرصيد", value = "${project.financials.balanceFiat.toInt()}", sub = "ر.س", icon = Icons.Default.MonetizationOn, color = ScopeAccent, modifier = Modifier.weight(1f))
                OverviewStatCard(title = "الشرارات ⚡", value = "${project.financials.pooledSparks}", sub = "شرارة", icon = Icons.Default.AutoAwesome, color = ScopeFocusGreen, modifier = Modifier.weight(1f))
            }
        }

        // Leadership Banner
        item {
            val owner = project.members.firstOrNull { it.role == ProjectRole.OWNER }
            val leader = project.members.firstOrNull { it.role == ProjectRole.LEADER }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("قيادة المشروع الحالية 👑⭐️", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text("المالك: ${owner?.displayName ?: "أنت"} • القائد: ${leader?.displayName ?: "قيد الانتخاب"}", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.Gray)
                    }

                    Button(
                        onClick = { onOpenTab(ProjectTab.LEADERSHIP) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8).copy(alpha = 0.2f))
                    ) {
                        Text("إدارة القيادة 🗳️", fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Milestones Section
        item {
            SectionHeader(title = "المراحل الرئيسية للمشروع (Milestones)", subtitle = "جدول التسليمات الأكاديمية")
        }

        items(project.milestones) { m ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (m.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (m.isCompleted) ScopeFocusGreen else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(m.title, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        Text("تاريخ التسليم: ${m.dueDate}", fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewStatCard(
    title: String,
    value: String,
    sub: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(sub, fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color.Gray)
            }
            Text(title, fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color.LightGray)
        }
    }
}

@Composable
private fun ProjectTasksTab(
    tasks: List<Task>,
    onToggleTask: (String, Boolean) -> Unit,
    onAddTaskClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = "متابعة المهام ومراحل العمل بالمشروع (${tasks.size})", subtitle = "قائمة المهام البرمجية والتطبيقية")
                Button(
                    onClick = onAddTaskClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة مهمة", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (tasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد مهام مسجلة حالياً بالمشروع.", fontFamily = CairoFontFamily, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        } else {
            items(tasks) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onToggleTask(task.id, !task.isCompleted) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (task.isCompleted) ScopeFocusGreen else ScopePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("${task.effortMinutes} دقيقة جهد متوقع", fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color.Gray)
                        }
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (task.isCompleted) ScopeFocusGreen.copy(alpha = 0.2f) else ScopeWarningAmber.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(if (task.isCompleted) "مكتملة" else "قيد العمل", fontFamily = CairoFontFamily, fontSize = 10.sp, color = if (task.isCompleted) ScopeFocusGreen else ScopeWarningAmber)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectMembersTab(
    members: List<ProjectMember>,
    onStartElectionClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = "أعضاء فريق المشروع (${members.size})", subtitle = "الأدوار، كفاءة التركيز، والشعلة")
                Button(
                    onClick = onStartElectionClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                ) {
                    Icon(Icons.Default.HowToVote, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("بدء انتخابات 🗳️", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }

        items(members) { member ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(42.dp).clip(CircleShape).background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(member.displayName.firstOrNull()?.toString() ?: "U", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF38BDF8))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(member.displayName, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${member.role.badgeEmoji} ${member.role.titleAr}", fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                        }
                        Text("${member.scopeId} • كفاءة التركيز: ${member.focusEfficiency}%", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.Gray)
                    }

                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(ScopeWarningAmber.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("🔥 ${member.streakDays}d", fontFamily = CairoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ScopeWarningAmber)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectAuditLogTab(
    auditLog: List<com.example.data.projects.ProjectActivityItem>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionHeader(title = "سجل النشاط الشفاف (Transparent Audit Log)", subtitle = "تسجيل التغييرات والانتخابات والمعاملات المالية")
        }

        if (auditLog.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("لا يوجد سجل أحداث حتى الآن.", fontFamily = CairoFontFamily, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        } else {
            items(auditLog) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(log.title, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text(log.actorName, fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color(0xFF38BDF8))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(log.description, fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.LightGray, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectFinancialsTab(
    financials: com.example.data.projects.ProjectFinancials,
    onAddTransactionClick: () -> Unit,
    onPoolSparksClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(title = "مالية وميزانية المشروع (Project Budgeting)", subtitle = "تتبع الدخل والمصروفات بالريال + صندوق الشرارات ⚡")
        }

        // 3 Metric Overview Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard("الرصيد", "${financials.balanceFiat.toInt()} ${financials.fiatCurrency}", Color(0xFF38BDF8), Modifier.weight(1f))
                MetricCard("الدخل (+)", "${financials.totalIncome.toInt()} ${financials.fiatCurrency}", ScopeFocusGreen, Modifier.weight(1f))
                MetricCard("المصروفات (-)", "${financials.totalExpenses.toInt()} ${financials.fiatCurrency}", MaterialTheme.colorScheme.error, Modifier.weight(1f))
            }
        }

        // Pooled Sparks & Tokens Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, ScopeFocusGreen.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("صندوق الشرارات والرموز التعاوني ⚡🪙", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text("الرصيد المجمع: ${financials.pooledSparks} شرارة ⚡ • ${financials.pooledTokens} رمز صولا 🪙", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.Gray)
                    }

                    Button(
                        onClick = onPoolSparksClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ScopeFocusGreen)
                    ) {
                        Text("إيداع ⚡", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddTransactionClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ScopeAccent)
                ) {
                    Text("إضافة معاملة مالية +", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                }
            }
        }

        // Transactions History
        item {
            Text("سجل المعاملات المالية الأخيرة:", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
        }

        items(financials.transactions) { tx ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(tx.title, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        Text("بواسطة: ${tx.loggedByName} • بند: ${tx.category}", fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color.Gray)
                    }

                    Text(
                        text = "${if (tx.isExpense) "-" else "+"}${tx.amountFiat.toInt()} ${financials.fiatCurrency}",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (tx.isExpense) MaterialTheme.colorScheme.error else ScopeFocusGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
        }
    }
}

@Composable
private fun ProjectNotesTab(
    notes: List<com.example.data.projects.ProjectNote>,
    onAddNoteClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = "ملاحظات وتوثيق المشروع (${notes.size})", subtitle = "قرارات الاجتماعات والمعمارية التقنية")
                Button(onClick = onAddNoteClick, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))) {
                    Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة ملاحظة +", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }

        items(notes) { note ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(note.title, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text(note.authorName, fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color(0xFF38BDF8))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(note.content, fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.LightGray, lineHeight = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun ProjectLeadershipTab(
    project: ScopeProject,
    currentUserId: String,
    onStartElectionClick: () -> Unit,
    onCastVote: (candidateMemberId: String) -> Unit
) {
    val election = project.activeElection

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(title = "وحدة القيادة والانتخابات الديمقراطية 🗳️", subtitle = "اختيار قائد الفريق عبر التصويت المباشر وتقييم كفاءة التركيز")
        }

        // Action Button to Start Election
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("بدء عملية انتخاب جديدة 🗳️", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text("يحق لمالك وقائد المشروع فتح باب التصويت لاختيار القيادة", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.Gray)
                    }

                    Button(
                        onClick = onStartElectionClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                    ) {
                        Text("بدء انتخاب جديد", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 11.sp)
                    }
                }
            }
        }

        // Active Election View
        if (election != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, if (election.isOpen) ScopeFocusGreen else Color.Gray)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(election.title, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (election.isOpen) ScopeFocusGreen.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(if (election.isOpen) "التصويت مفتوح 🟢" else "اكتمل الانتخاب 🔒", fontFamily = CairoFontFamily, fontSize = 10.sp, color = if (election.isOpen) ScopeFocusGreen else Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("المرشحون للقيادة وأصوات الأعضاء:", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))

                        val totalVotes = election.votes.size.coerceAtLeast(1)

                        election.candidates.forEach { candidate ->
                            val candidateVotes = election.votes.values.count { it == candidate.id }
                            val voteRatio = candidateVotes.toFloat() / totalVotes.toFloat()
                            val hasVotedForThis = election.votes[currentUserId] == candidate.id

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("${candidate.displayName} (${candidate.role.titleAr})", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                            Text("كفاءة التركيز: ${candidate.focusEfficiency}% • الأصوات: $candidateVotes صوتاً", fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color.Gray)
                                        }

                                        if (election.isOpen) {
                                            Button(
                                                onClick = { onCastVote(candidate.id) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = if (hasVotedForThis) ScopeFocusGreen else Color(0xFF38BDF8))
                                            ) {
                                                Text(if (hasVotedForThis) "صوّت له ✅" else "تصويت 🗳️", fontFamily = CairoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { voteRatio.coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = ScopeFocusGreen,
                                        trackColor = Color(0xFF0F172A)
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
