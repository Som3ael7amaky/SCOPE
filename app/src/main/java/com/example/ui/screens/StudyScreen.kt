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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConceptMap
import com.example.data.model.Flashcard
import com.example.data.model.LearningSignal
import com.example.data.model.StudySession
import com.example.data.model.Subject
import com.example.ui.audio.ScopeSoundStudio
import com.example.ui.audio.SolaPodcastSynthesizer
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeAccent
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.window.Dialog
import com.example.data.model.SubjectFile
import com.example.ui.components.CentralizedLibraryContent
import com.example.ui.components.GuidedUploadDialog

@Composable
fun StudyScreen(
    subjects: List<Subject>,
    studySessions: List<StudySession>,
    flashcards: List<Flashcard> = emptyList(),
    conceptMaps: List<ConceptMap> = emptyList(),
    allFiles: List<SubjectFile> = emptyList(),
    timerSeconds: Int,
    isTimerRunning: Boolean,
    selectedSubject: Subject?,
    showReflectionDialog: Boolean,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: (Int) -> Unit,
    onSelectSubject: (Subject) -> Unit,
    onSaveReflection: (LearningSignal, String) -> Unit,
    onDismissReflection: () -> Unit,
    onAddSubject: (String, String, String, String) -> Unit,
    onSaveVisionDeck: (List<Flashcard>, ConceptMap, Int) -> Unit = { _, _, _ -> },
    onAddSubjectFile: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> },
    onAddDocumentFile: (subjectId: String, fileName: String, alias: String, fileType: String, category: String, isPinned: Boolean, uriString: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onRenameSubjectFile: (String, String, String) -> Unit = { _, _, _ -> },
    onTogglePinFile: (String, String) -> Unit = { _, _ -> },
    onDeleteSubjectFile: (String, String) -> Unit = { _, _ -> },
    onMoveSubjectFile: (fromSubjectId: String, toSubjectId: String, fileId: String) -> Unit = { _, _, _ -> },
    recommendation: String = "اختر مقرراً لبدء رحلة التفوق اليومية",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundStudio = remember { ScopeSoundStudio.getInstance(context) }
    val podcastSynthesizer = remember { SolaPodcastSynthesizer(context, soundStudio) }

    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showSoundStudioSheet by remember { mutableStateOf(false) }
    var showVisionDialog by remember { mutableStateOf(false) }
    var showPodcastPlayer by remember { mutableStateOf(false) }
    var showGpaTimeMachineDialog by remember { mutableStateOf(false) }

    var showCentralizedLibraryModal by remember { mutableStateOf(false) }
    var pendingUploadUri by remember { mutableStateOf<Uri?>(null) }
    var pendingUploadFileName by remember { mutableStateOf("") }
    var pendingTargetSubjectId by remember { mutableStateOf("") }
    var showGuidedUploadDialog by remember { mutableStateOf(false) }

    val studyFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingUploadUri = uri
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "مستند_جديد.pdf"
            pendingUploadFileName = fileName
            showGuidedUploadDialog = true
        }
    }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("study_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Smart Sola Recommendation Pill
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ScopePrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "مرشد التفوق الأكاديمي 🚀",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = recommendation,
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // STEP 1: المقررات والمواد الأكاديمية
        item {
            SectionHeader(
                title = "1. المقررات والمواد الأكاديمية 📚",
                subtitle = "إدارة ملفات المحاضرات وتقدم المنهج",
                action = {
                    Button(
                        onClick = { showAddSubjectDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary.copy(alpha = 0.15f), contentColor = ScopePrimary),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("مقرر +", fontFamily = CairoFontFamily, fontSize = 11.sp)
                    }
                }
            )
        }

        // Central Library Quick Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showCentralizedLibraryModal = true },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, ScopePrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ScopePrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📂", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "مكتبتي الأكاديمية الشاملة 📂",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${allFiles.size} مستندات عبر جميع المقررات",
                                fontFamily = CairoFontFamily,
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Button(
                        onClick = { studyFilePicker.launch("*/*") },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("رفع 📤", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (subjects.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد مقررات. ابدأ بإضافة أول مقرر لك!", fontFamily = CairoFontFamily, color = Color.Gray)
                    }
                }
            }
        } else {
            items(subjects) { subj ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onSelectSubject(subj) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selectedSubject?.id == subj.id) ScopePrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface),
                    border = if (selectedSubject?.id == subj.id) androidx.compose.foundation.BorderStroke(1.dp, ScopePrimary) else null
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(ScopePrimary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Book, contentDescription = null, tint = ScopePrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = subj.name,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "د. ${subj.instructor} • ${subj.files.size} ملفات",
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { /* Open files */ }) {
                            Text(text = "📁", fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        // STEP 2: خطة التفوق اليومية
        item {
            SectionHeader(
                title = "2. خطة التفوق اليومية ⚡",
                subtitle = "سجل ساعات التركيز وحقق هدفك اليومي"
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val totalFocusToday = studySessions.sumOf { it.durationMinutes }
                    val targetMinutes = 240 // 4 hours target
                    val progress = (totalFocusToday.toFloat() / targetMinutes.toFloat()).coerceIn(0f, 1f)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مؤشر الانجاز اليومي", fontFamily = CairoFontFamily, fontSize = 12.sp)
                        Text("$totalFocusToday / $targetMinutes دقيقة", fontFamily = CairoFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ScopePrimary)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                        color = ScopeFocusGreen,
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Timer Trigger
                    Button(
                        onClick = { if (isTimerRunning) onPauseTimer() else onStartTimer() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isTimerRunning) ScopeWarningAmber else ScopePrimary)
                    ) {
                        Icon(if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isTimerRunning) "إيقاف مؤقت للتركيز" else "بدء جلسة تركيز ذكية", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // STEP 3: الملخصات والبطاقات 3D
        item {
            SectionHeader(
                title = "3. الملخصات والبطاقات 3D ✨",
                subtitle = "توليد ذكي للملخصات الصوتية والبطاقات"
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    modifier = Modifier.weight(1f).clickable { showVisionDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ScopeAccent.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("صولا فيجن 📷", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("مسح السلايدات", fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color.Gray)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f).clickable { /* Review cards */ },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ScopePrimary.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("مراجعة البطاقات 🎴", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${flashcards.size} بطاقة جاهزة", fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        // STEP 4: حارس الامتحانات والدرجات
        item {
            SectionHeader(
                title = "4. حارس الامتحانات والدرجات 🛡️",
                subtitle = "تتبع مواعيد الاختبارات وحاسبة المعدل"
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎯 هدف الفصل الحالي:", fontFamily = CairoFontFamily, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("4.75 / 5.00", fontFamily = CairoFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = ScopePrimary)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showGpaTimeMachineDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Text("فتح حاسبة المعدل المتوقع 🚀", fontFamily = CairoFontFamily, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { showSoundStudioSheet = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                        ) {
                            Text("استوديو الصوت 🎧", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Subject File Center Section
        if (selectedSubject != null) {
            item { SectionHeader(title = "مركز ملفات المقرر (${selectedSubject.name})") }
            items(selectedSubject.files) { file ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Book, contentDescription = null, tint = ScopePrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = file.customAlias, fontWeight = FontWeight.Bold, fontFamily = CairoFontFamily, fontSize = 13.sp)
                            Text(text = file.fileName, fontFamily = CairoFontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onTogglePinFile(selectedSubject.id, file.id) }) {
                            Icon(Icons.Default.Star, contentDescription = "تثبيت", tint = if (file.isPinned) ScopeWarningAmber else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onDeleteSubjectFile(selectedSubject.id, file.id) }) {
                            Icon(Icons.Default.Close, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        pendingTargetSubjectId = selectedSubject.id
                        studyFilePicker.launch("*/*")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                ) {
                    Text("إضافة ملف محلي للمقرر ➕", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Centralized Library Modal
    if (showCentralizedLibraryModal) {
        Dialog(
            onDismissRequest = { showCentralizedLibraryModal = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.96f).padding(12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("مكتبتي الأكاديمية 📂", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(onClick = { showCentralizedLibraryModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }
                    CentralizedLibraryContent(
                        allFiles = allFiles,
                        subjects = subjects,
                        onAddDocumentClick = { studyFilePicker.launch("*/*") },
                        onRenameFile = onRenameSubjectFile,
                        onTogglePinFile = onTogglePinFile,
                        onDeleteFile = onDeleteSubjectFile,
                        onMoveFile = onMoveSubjectFile
                    )
                }
            }
        }
    }

    // Guided Upload Dialog
    if (showGuidedUploadDialog) {
        GuidedUploadDialog(
            fileUri = pendingUploadUri,
            suggestedFileName = pendingUploadFileName,
            subjects = subjects,
            defaultSubjectId = pendingTargetSubjectId,
            onDismiss = {
                showGuidedUploadDialog = false
                pendingTargetSubjectId = ""
            },
            onConfirmUpload = { alias, subId, cat, isPinned, fType, uStr ->
                onAddDocumentFile(subId, pendingUploadFileName, alias, fType, cat, isPinned, uStr)
                showGuidedUploadDialog = false
                pendingTargetSubjectId = ""
            }
        )
    }

    // Reflection Dialog upon timer completion
    if (showReflectionDialog) {
        var selectedSignal by remember { mutableStateOf(LearningSignal.UNDERSTOOD) }
        var reflectionNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = onDismissReflection,
            title = {
                Text(
                    text = "اكتملت جلسة التركيز! سجل تأملك",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "كيف كان استيعابك للموضوعات في هذه الجلسة؟",
                        fontFamily = CairoFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        LearningSignal.values().forEach { sig ->
                            val isSel = selectedSignal == sig
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) ScopePrimary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedSignal = sig }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = when (sig) {
                                        LearningSignal.UNDERSTOOD -> "مفهوم"
                                        LearningSignal.NEEDS_REVIEW -> "يحتاج مراجعة"
                                        LearningSignal.UNCLEAR -> "غير واضح"
                                        LearningSignal.IMPORTANT -> "مهم جداً"
                                    },
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = reflectionNotes,
                        onValueChange = { reflectionNotes = it },
                        label = { Text("ملاحظاتك السريعة / نقاط تحتاج تعزيز", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onSaveReflection(selectedSignal, reflectionNotes) },
                    colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                ) {
                    Text("حفظ التأمل", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissReflection) {
                    Text("تخطي", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    // Add Subject Dialog
    if (showAddSubjectDialog) {
        var name by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("") }
        var instructor by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text("إضافة مقرر أكاديمي جديد", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم المقرر (مثال: شبكات الحاسوب)", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("رمز المقرر (مثال: CS305)", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = instructor,
                        onValueChange = { instructor = it },
                        label = { Text("اسم المحاضر", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAddSubject(name, code, "#2563EB", instructor)
                            showAddSubjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                ) {
                    Text("إضافة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    if (showSoundStudioSheet) {
        com.example.ui.audio.ScopeSoundStudioBottomSheet(
            onDismiss = { showSoundStudioSheet = false }
        )
    }

    if (showVisionDialog) {
        SolaVisionDialog(
            subjects = subjects,
            onSaveDeck = onSaveVisionDeck,
            onDismiss = { showVisionDialog = false }
        )
    }

    if (showPodcastPlayer) {
        SolaAudioMiniPlayer(
            synthesizer = podcastSynthesizer,
            onDismiss = { showPodcastPlayer = false }
        )
    }

    if (showGpaTimeMachineDialog) {
        GpaTimeMachineDialog(
            subjects = subjects,
            onDismiss = { showGpaTimeMachineDialog = false }
        )
    }
}
