package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Subject
import com.example.data.model.SubjectFile
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Universal Academic Library ("مكتبتي 📂") & Guided Native Upload Wizard
 */

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuidedUploadDialog(
    fileUri: Uri?,
    suggestedFileName: String,
    subjects: List<Subject>,
    defaultSubjectId: String = "",
    onDismiss: () -> Unit,
    onConfirmUpload: (alias: String, subjectId: String, category: String, isPinned: Boolean, fileType: String, uriString: String) -> Unit
) {
    val detectedType = remember(suggestedFileName) {
        val lower = suggestedFileName.lowercase()
        when {
            lower.endsWith(".pdf") -> "PDF"
            lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "IMAGE"
            lower.endsWith(".doc") || lower.endsWith(".docx") -> "DOC"
            else -> "DOC"
        }
    }

    var alias by remember {
        mutableStateOf(suggestedFileName.substringBeforeLast("."))
    }
    var selectedSubjectId by remember {
        mutableStateOf(
            if (defaultSubjectId.isNotBlank()) defaultSubjectId
            else subjects.firstOrNull()?.id ?: ""
        )
    }
    var selectedCategory by remember { mutableStateOf("محاضرات") }
    var isPinned by remember { mutableStateOf(false) }

    val categories = listOf("محاضرات", "ملخصات", "امتحانات", "ملاحظات")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp)
                .testTag("guided_upload_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ScopePrimary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ScopePrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (detectedType == "PDF") Icons.Default.PictureAsPdf else Icons.Default.Image,
                                contentDescription = null,
                                tint = ScopePrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "معالج رفع المستندات 📂",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = suggestedFileName,
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Display Name / Alias
                Text(
                    text = "اسم العرض المخصص (الاسم الأكاديمي المستعار):",
                    fontFamily = CairoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    placeholder = { Text("مثال: ملزمة المنتصف - نظم كهرومغناطيسية", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("upload_alias_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ScopePrimary,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Subject Association
                Text(
                    text = "ربط المقرر الدراسي:",
                    fontFamily = CairoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Option for General
                    FilterChip(
                        selected = selectedSubjectId.isEmpty(),
                        onClick = { selectedSubjectId = "" },
                        label = { Text("عام / بدون مقرر 📁", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ScopePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    subjects.forEach { subj ->
                        val isSel = selectedSubjectId == subj.id
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedSubjectId = subj.id },
                            label = { Text(subj.name, fontFamily = CairoFontFamily, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ScopePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Tag
                Text(
                    text = "تصنيف المستند:",
                    fontFamily = CairoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSel = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) ScopePrimary else Color(0xFF1E293B))
                                .clickable { selectedCategory = cat }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else Color.LightGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Priority Pin Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = null,
                            tint = if (isPinned) ScopeWarningAmber else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تثبيت في المفضلة الأكاديمية ⭐",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                    Switch(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ScopeWarningAmber
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
                    ) {
                        Text("إلغاء", fontFamily = CairoFontFamily)
                    }

                    Button(
                        onClick = {
                            val finalAlias = if (alias.isNotBlank()) alias else suggestedFileName
                            onConfirmUpload(
                                finalAlias,
                                selectedSubjectId,
                                selectedCategory,
                                isPinned,
                                detectedType,
                                fileUri?.toString() ?: ""
                            )
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.5f).testTag("confirm_upload_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                    ) {
                        Text("حفظ المستند 🚀", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Universal Document Library Component: Grid & List views, Filters, Search, 1-Tap View & Share, Full CRUD
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CentralizedLibraryContent(
    allFiles: List<SubjectFile>,
    subjects: List<Subject>,
    onAddDocumentClick: () -> Unit,
    onRenameFile: (subjectId: String, fileId: String, newAlias: String) -> Unit,
    onTogglePinFile: (subjectId: String, fileId: String) -> Unit,
    onDeleteFile: (subjectId: String, fileId: String) -> Unit,
    onMoveFile: (fromSubjectId: String, toSubjectId: String, fileId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isGridView by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("الكل") }
    var selectedSubjectFilter by remember { mutableStateOf("الكل") }
    var onlyPinned by remember { mutableStateOf(false) }

    // Dialog state for viewing document
    var previewFile by remember { mutableStateOf<SubjectFile?>(null) }
    // Dialog state for renaming
    var renamingFile by remember { mutableStateOf<SubjectFile?>(null) }
    // Dialog state for moving
    var movingFile by remember { mutableStateOf<SubjectFile?>(null) }

    // Filter files
    val filteredFiles = remember(allFiles, searchQuery, selectedCategoryFilter, selectedSubjectFilter, onlyPinned) {
        allFiles.filter { file ->
            val matchesQuery = searchQuery.isBlank() ||
                file.customAlias.contains(searchQuery, ignoreCase = true) ||
                file.fileName.contains(searchQuery, ignoreCase = true) ||
                file.subjectName.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == "الكل" || file.category == selectedCategoryFilter
            val matchesSubject = selectedSubjectFilter == "الكل" || file.subjectId == selectedSubjectFilter
            val matchesPinned = !onlyPinned || file.isPinned

            matchesQuery && matchesCategory && matchesSubject && matchesPinned
        }.sortedWith(compareByDescending<SubjectFile> { it.isPinned }.thenByDescending { it.timestamp })
    }

    Column(modifier = modifier.fillMaxWidth().testTag("centralized_library_view")) {
        // Top Action Header with Search and View Toggle
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "مكتبتي الأكاديمية 📂",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ScopePrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${allFiles.size} ملف",
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = ScopePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { isGridView = !isGridView }) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "تبديل العرض",
                        tint = ScopePrimary
                    )
                }

                Button(
                    onClick = onAddDocumentClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("library_upload_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("رفع ملف 📤", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث في أسماء الملازم، المقررات، أو التصنيفات...", fontFamily = CairoFontFamily, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "مسح", tint = Color.Gray)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ScopePrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Bar (Category Chips + Pinned Toggle)
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("الكل", "محاضرات", "ملخصات", "امتحانات", "ملاحظات").forEach { cat ->
                FilterChip(
                    selected = selectedCategoryFilter == cat,
                    onClick = { selectedCategoryFilter = cat },
                    label = { Text(cat, fontFamily = CairoFontFamily, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ScopePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }

            FilterChip(
                selected = onlyPinned,
                onClick = { onlyPinned = !onlyPinned },
                leadingIcon = {
                    Icon(
                        imageVector = if (onlyPinned) Icons.Default.Star else Icons.Outlined.StarOutline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (onlyPinned) ScopeWarningAmber else Color.Gray
                    )
                },
                label = { Text("المثبتة فقط", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ScopeWarningAmber.copy(alpha = 0.2f),
                    selectedLabelColor = ScopeWarningAmber
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Files Display
        if (filteredFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📂", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (allFiles.isEmpty()) "مكتبتك الأكاديمية فارغة حالياً" else "لا توجد نتائج تطابق بحثك",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (allFiles.isEmpty()) "ارفع أول ملف من جهازك لتنظيم ملازمك ومحاضراتك بسهولة!" else "جرّب تغيير كلمات البحث أو المرشحات.",
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    if (allFiles.isEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onAddDocumentClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                        ) {
                            Text("رفع أول مستند ➕", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().height(440.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredFiles, key = { it.id }) { file ->
                    DocumentGridCard(
                        file = file,
                        onViewClick = { previewFile = file },
                        onShareClick = { shareDocument(context, file) },
                        onTogglePin = { onTogglePinFile(file.subjectId, file.id) },
                        onRenameClick = { renamingFile = file },
                        onMoveClick = { movingFile = file },
                        onDeleteClick = { onDeleteFile(file.subjectId, file.id) }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(440.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredFiles, key = { it.id }) { file ->
                    DocumentListRow(
                        file = file,
                        onViewClick = { previewFile = file },
                        onShareClick = { shareDocument(context, file) },
                        onTogglePin = { onTogglePinFile(file.subjectId, file.id) },
                        onRenameClick = { renamingFile = file },
                        onMoveClick = { movingFile = file },
                        onDeleteClick = { onDeleteFile(file.subjectId, file.id) }
                    )
                }
            }
        }
    }

    // Preview Dialog
    previewFile?.let { file ->
        DocumentViewerPreviewDialog(
            file = file,
            onDismiss = { previewFile = null },
            onShare = { shareDocument(context, file) },
            onOpenExternal = { openExternalDocument(context, file) }
        )
    }

    // Rename Dialog
    renamingFile?.let { file ->
        var newAlias by remember { mutableStateOf(file.customAlias) }
        Dialog(onDismissRequest = { renamingFile = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth(0.92f).padding(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ScopePrimary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "تعديل اسم المستند ✏️",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newAlias,
                        onValueChange = { newAlias = it },
                        label = { Text("الاسم المخصص", fontFamily = CairoFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ScopePrimary,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { renamingFile = null }, modifier = Modifier.weight(1f)) {
                            Text("إلغاء", fontFamily = CairoFontFamily)
                        }
                        Button(
                            onClick = {
                                if (newAlias.isNotBlank()) {
                                    onRenameFile(file.subjectId, file.id, newAlias)
                                }
                                renamingFile = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                        ) {
                            Text("حفظ", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Move Subject Dialog
    movingFile?.let { file ->
        Dialog(onDismissRequest = { movingFile = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth(0.92f).padding(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ScopePrimary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "نقل المستند إلى مقرر آخر 🔄",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "المستند: ${file.customAlias}",
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(subjects) { targetSub ->
                            val isCurrent = targetSub.id == file.subjectId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isCurrent) ScopePrimary.copy(alpha = 0.2f) else Color(0xFF1E293B))
                                    .clickable(enabled = !isCurrent) {
                                        onMoveFile(file.subjectId, targetSub.id, file.id)
                                        movingFile = null
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = targetSub.name,
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = if (isCurrent) ScopePrimary else Color.White,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isCurrent) {
                                    Text("المقرر الحالي", fontFamily = CairoFontFamily, fontSize = 10.sp, color = ScopePrimary)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(onClick = { movingFile = null }, modifier = Modifier.fillMaxWidth()) {
                        Text("إغلاق", fontFamily = CairoFontFamily)
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentListRow(
    file: SubjectFile,
    onViewClick: () -> Unit,
    onShareClick: () -> Unit,
    onTogglePin: () -> Unit,
    onRenameClick: () -> Unit,
    onMoveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewClick() }
            .testTag("doc_row_${file.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = if (file.isPinned) androidx.compose.foundation.BorderStroke(1.dp, ScopeWarningAmber.copy(alpha = 0.6f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File Type Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (file.fileType) {
                            "PDF" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                            "IMAGE" -> Color(0xFF10B981).copy(alpha = 0.15f)
                            else -> ScopePrimary.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (file.fileType) {
                        "PDF" -> Icons.Default.PictureAsPdf
                        "IMAGE" -> Icons.Default.Image
                        else -> Icons.Default.Book
                    },
                    contentDescription = null,
                    tint = when (file.fileType) {
                        "PDF" -> Color(0xFFEF4444)
                        "IMAGE" -> Color(0xFF10B981)
                        else -> ScopePrimary
                    },
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (file.isPinned) {
                        Text(text = "⭐ ", fontSize = 11.sp)
                    }
                    Text(
                        text = file.customAlias,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ScopePrimary.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = file.category,
                            fontFamily = CairoFontFamily,
                            fontSize = 9.sp,
                            color = ScopePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = if (file.subjectName.isNotBlank()) file.subjectName else "عام",
                        fontFamily = CairoFontFamily,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action Buttons
            IconButton(onClick = onViewClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.OpenInNew, contentDescription = "عرض", tint = ScopePrimary, modifier = Modifier.size(18.dp))
            }

            IconButton(onClick = onShareClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }

            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "المزيد", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(if (file.isPinned) "إلغاء التثبيت" else "تثبيت في المفضلة", fontFamily = CairoFontFamily) },
                        leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) },
                        onClick = { menuExpanded = false; onTogglePin() }
                    )
                    DropdownMenuItem(
                        text = { Text("تعديل الاسم المستعار", fontFamily = CairoFontFamily) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = { menuExpanded = false; onRenameClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("نقل إلى مقرر آخر", fontFamily = CairoFontFamily) },
                        leadingIcon = { Icon(Icons.Default.DriveFileMove, contentDescription = null) },
                        onClick = { menuExpanded = false; onMoveClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("حذف المستند", fontFamily = CairoFontFamily, color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { menuExpanded = false; onDeleteClick() }
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentGridCard(
    file: SubjectFile,
    onViewClick: () -> Unit,
    onShareClick: () -> Unit,
    onTogglePin: () -> Unit,
    onRenameClick: () -> Unit,
    onMoveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewClick() }
            .testTag("doc_grid_${file.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = if (file.isPinned) androidx.compose.foundation.BorderStroke(1.dp, ScopeWarningAmber) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (file.fileType) {
                                "PDF" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                "IMAGE" -> Color(0xFF10B981).copy(alpha = 0.15f)
                                else -> ScopePrimary.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (file.fileType) {
                            "PDF" -> Icons.Default.PictureAsPdf
                            "IMAGE" -> Icons.Default.Image
                            else -> Icons.Default.Book
                        },
                        contentDescription = null,
                        tint = when (file.fileType) {
                            "PDF" -> Color(0xFFEF4444)
                            "IMAGE" -> Color(0xFF10B981)
                            else -> ScopePrimary
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(if (file.isPinned) "إلغاء التثبيت" else "تثبيت في المفضلة", fontFamily = CairoFontFamily) },
                            onClick = { menuExpanded = false; onTogglePin() }
                        )
                        DropdownMenuItem(
                            text = { Text("تعديل الاسم", fontFamily = CairoFontFamily) },
                            onClick = { menuExpanded = false; onRenameClick() }
                        )
                        DropdownMenuItem(
                            text = { Text("نقل إلى مقرر", fontFamily = CairoFontFamily) },
                            onClick = { menuExpanded = false; onMoveClick() }
                        )
                        DropdownMenuItem(
                            text = { Text("حذف", fontFamily = CairoFontFamily, color = MaterialTheme.colorScheme.error) },
                            onClick = { menuExpanded = false; onDeleteClick() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = file.customAlias,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (file.subjectName.isNotBlank()) file.subjectName else "مستند عام",
                fontFamily = CairoFontFamily,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ScopePrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = file.category, fontFamily = CairoFontFamily, fontSize = 9.sp, color = ScopePrimary, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onShareClick, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = ScopePrimary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * Rich Preview Dialog with 1-tap direct opening & sharing
 */
@Composable
fun DocumentViewerPreviewDialog(
    file: SubjectFile,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onOpenExternal: () -> Unit
) {
    val dateStr = remember(file.timestamp) {
        val sdf = SimpleDateFormat("dd MMMM yyyy - hh:mm a", Locale("ar"))
        sdf.format(Date(file.timestamp))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ScopePrimary.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ScopePrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(text = file.fileType, fontFamily = CairoFontFamily, fontSize = 11.sp, color = ScopePrimary, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(ScopePrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (file.fileType == "PDF") Icons.Default.PictureAsPdf else Icons.Default.Book,
                        contentDescription = null,
                        tint = ScopePrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = file.customAlias,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "اسم الملف الأصلي: ${file.fileName}",
                    fontFamily = CairoFontFamily,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E293B))
                        .padding(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المقرر الدراسي:", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.Gray)
                        Text(if (file.subjectName.isNotBlank()) file.subjectName else "مستند عام", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("التصنيف:", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.Gray)
                        Text(file.category, fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ScopePrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("تاريخ الإضافة:", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.Gray)
                        Text(dateStr, fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة", fontFamily = CairoFontFamily)
                    }

                    Button(
                        onClick = onOpenExternal,
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("فتح المستند", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Helpers for opening and sharing
private fun openExternalDocument(context: Context, file: SubjectFile) {
    if (file.uriString.isNotBlank()) {
        try {
            val uri = Uri.parse(file.uriString)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                val mime = when (file.fileType) {
                    "PDF" -> "application/pdf"
                    "IMAGE" -> "image/*"
                    else -> "*/*"
                }
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "فتح مستند ${file.customAlias}"))
            return
        } catch (e: Exception) {
            // fallback
        }
    }
    Toast.makeText(context, "معاينة المستند: ${file.customAlias} (${file.category})", Toast.LENGTH_SHORT).show()
}

private fun shareDocument(context: Context, file: SubjectFile) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            if (file.uriString.isNotBlank()) {
                putExtra(Intent.EXTRA_STREAM, Uri.parse(file.uriString))
                type = when (file.fileType) {
                    "PDF" -> "application/pdf"
                    "IMAGE" -> "image/*"
                    else -> "*/*"
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
            }
            putExtra(Intent.EXTRA_TEXT, "مستند دراسي من منظومة SCOPE: ${file.customAlias} (${file.subjectName} - ${file.category})")
            putExtra(Intent.EXTRA_TITLE, file.customAlias)
        }
        context.startActivity(Intent.createChooser(shareIntent, "مشاركة المستند مع زملائك"))
    } catch (e: Exception) {
        Toast.makeText(context, "تعذر مشاركة المستند", Toast.LENGTH_SHORT).show()
    }
}
