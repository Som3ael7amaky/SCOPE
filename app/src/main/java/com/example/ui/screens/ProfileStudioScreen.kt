package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.model.Subject
import com.example.data.model.SubjectFile
import com.example.ui.components.CentralizedLibraryContent
import com.example.ui.components.GuidedUploadDialog
import com.example.ui.components.StudentInventoryTab

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.economy.ScopeEconomy
import com.example.data.model.ScopeAvatarsCatalog
import com.example.data.model.ShopCategory
import com.example.data.model.ShopItem
import com.example.data.model.StudySession
import com.example.data.model.Task
import com.example.data.model.UserProfile
import com.example.ui.components.SectionHeader
import com.example.ui.mascot.SolaEmotionState
import com.example.ui.mascot.SolaMascotCanvas
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeAccent
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber
import kotlinx.coroutines.launch

@Composable
fun ProfileStudioScreen(
    userProfile: UserProfile?,
    tasks: List<Task>,
    studySessions: List<StudySession>,
    onPurchaseItem: (ShopItem, () -> Unit, (String) -> Unit) -> Unit,
    onEquipItem: (ShopCategory, ShopItem) -> Unit,
    onUnequipItem: (ShopCategory) -> Unit = {},
    onBack: () -> Unit,
    onOpenDiagnosticSurvey: () -> Unit,
    onUpdateProfile: (displayName: String, bio: String, major: String, year: String, avatarId: String, photoUrl: String, newPassword: String?, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    subjects: List<Subject> = emptyList(),
    allFiles: List<SubjectFile> = emptyList(),
    onAddFile: (subjectId: String, fileName: String, alias: String, fileType: String, category: String, isPinned: Boolean, uriString: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onRenameFile: (subjectId: String, fileId: String, newAlias: String) -> Unit = { _, _, _ -> },
    onTogglePinFile: (subjectId: String, fileId: String) -> Unit = { _, _ -> },
    onDeleteFile: (subjectId: String, fileId: String) -> Unit = { _, _ -> },
    onMoveFile: (fromSubjectId: String, toSubjectId: String, fileId: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedProfileTab by remember { mutableStateOf(0) }
    var showStoreDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Native file upload launcher for library tab
    var pendingUploadUri by remember { mutableStateOf<Uri?>(null) }
    var pendingUploadFileName by remember { mutableStateOf("") }
    var showGuidedUploadDialog by remember { mutableStateOf(false) }

    val libraryFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingUploadUri = uri
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "مستند_جديد.pdf"
            pendingUploadFileName = name
            showGuidedUploadDialog = true
        }
    }

    val completedTasksCount = tasks.count { it.isCompleted }
    val totalFocusMinutes = studySessions.sumOf { it.durationMinutes }
    val totalSessionsCount = studySessions.size

    val userLevel = userProfile?.level ?: 1
    val currentXp = userProfile?.xp ?: 0
    val nextLevelThreshold = (userLevel * 250)
    val xpProgress = (currentXp % 250) / 250f

    // Current equipped colors/frames
    val equippedFrameColor = when (userProfile?.equippedFrame) {
        "frame_celestial_gold" -> Color(0xFFF59E0B)
        "frame_holo_prism" -> Color(0xFF8B5CF6)
        "frame_emerald_scholar" -> Color(0xFF10B981)
        "frame_obsidian_elite" -> Color(0xFF312E81)
        else -> Color(0xFF06B6D4) // Cyber neon
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("profile_studio_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Text(
                        text = "استوديو الطالب والألعاب الأكاديمية",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Store Quick Trigger Button
                    Button(
                        onClick = { showStoreDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("open_store_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "متجر التخصيص",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "المتجر 🛒",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // 3-Tab Selector Row (الملف والإنجازات, الحقيبة والمقتنيات, مكتبتي)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        "الملف والإنجازات 🎓",
                        "الحقيبة والمقتنيات 🎒",
                        "مكتبتي 📂"
                    )
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedProfileTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ScopePrimary else Color.Transparent)
                                .clickable { selectedProfileTab = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontFamily = CairoFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Tab 0: الملف والإنجازات
            if (selectedProfileTab == 0) {
                // Dual Currency Balances Card
                item {
                    Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("currency_balances_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Focus Sparks
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(ScopePrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "⚡", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${userProfile?.sparks ?: 0}",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = ScopePrimary
                                )
                                Text(
                                    text = "شرارات التركيز (Sparks)",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        )

                        // Sola Tokens
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(ScopeWarningAmber.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🪙", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${userProfile?.tokens ?: 0}",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = ScopeWarningAmber
                                )
                                Text(
                                    text = "رموز صولا (Tokens)",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Student Identity Hero Card (Avatar with 3D Border, Title, SCOPE ID)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_identity_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar inside equipped 3D Animated Border
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .border(4.dp, equippedFrameColor, CircleShape)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.scope_logo),
                                contentDescription = "Student Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Equipped Title Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ScopePrimary.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🏷️ ${userProfile?.equippedTitle ?: "طالب باحث"}",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = ScopePrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Student Name
                        Text(
                            text = userProfile?.displayName ?: "طالب SCOPE",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Unique SCOPE ID
                        Text(
                            text = userProfile?.scopeId ?: "@student_scope",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${userProfile?.academicMajor ?: "هندسة البرمجيات والذكاء الاصطناعي"} • ${userProfile?.academicYear ?: "السنة الثالثة"}",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!userProfile?.customStatus.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "💬 \"${userProfile?.customStatus}\"",
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Level & XP Bar
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "المستوى $userLevel (رتبة دراسية)",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ScopePrimary
                                )
                                Text(
                                    text = "$currentXp XP (إجمالي الطاقة)",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { xpProgress.coerceIn(0.05f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = ScopePrimary,
                                trackColor = ScopePrimary.copy(alpha = 0.15f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Edit Profile CTA Button
                        OutlinedButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("edit_profile_btn"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ScopePrimary.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = ScopePrimary.copy(alpha = 0.08f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = ScopePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تعديل الملف الشخصي ✏️",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = ScopePrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Academic Stats Showcase (Focus Minutes, Streak, Tasks, Efficiency)
            item {
                SectionHeader(title = "إحصائيات الإنجاز والنشاط الأكاديمي")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Flame Streak
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ScopeWarningAmber.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = ScopeWarningAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "سلسلة الحضور", fontFamily = CairoFontFamily, fontSize = 11.sp, color = ScopeWarningAmber)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "${userProfile?.flameCount ?: 1} أيام", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = ScopeWarningAmber)
                        }
                    }

                    // Total Focus Minutes
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ScopePrimary.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = ScopePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "دقائق التركيز", fontFamily = CairoFontFamily, fontSize = 11.sp, color = ScopePrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "$totalFocusMinutes دقيقة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = ScopePrimary)
                        }
                    }

                    // Completed Tasks
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ScopeFocusGreen.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = ScopeFocusGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "المهام المنجزة", fontFamily = CairoFontFamily, fontSize = 11.sp, color = ScopeFocusGreen)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "$completedTasksCount مهام", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = ScopeFocusGreen)
                        }
                    }
                }
            }

            // Badges Shelf (Unlocked & Locked)
            item {
                SectionHeader(
                    title = "رف الشارات والأوسمة (Showcase Shelf)",
                    subtitle = "تُمنح عبر استمرارية التعلم وإنجاز المهام"
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(ScopeEconomy.allBadges) { badge ->
                        val isUnlocked = userProfile?.unlockedBadgeIds?.contains(badge.id) == true ||
                                badge.id == "badge_first_spark" ||
                                (badge.id == "badge_deep_dive" && totalSessionsCount > 0) ||
                                (badge.id == "badge_sola_aligned" && userProfile?.onboardingCompleted == true)

                        Card(
                            modifier = Modifier
                                .width(135.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 2.dp else 0.dp),
                            border = if (isUnlocked) borderStroke(1.dp, ScopePrimary.copy(alpha = 0.3f)) else null
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(if (isUnlocked) ScopePrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isUnlocked) {
                                        Text(text = badge.iconEmoji, fontSize = 22.sp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "قفل",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = badge.title,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = badge.criteria,
                                    fontFamily = CairoFontFamily,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Diagnostic Preferences Summary with Re-Take Action
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ScopePrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تفضيلات شخصية صولا الحالية",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            TextButton(onClick = onOpenDiagnosticSurvey) {
                                Text(
                                    text = "تعديل / إعادة ✏️",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ScopePrimary
                                )
                            }
                        }

                        val prefs = userProfile?.diagnosticPreferences
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "• الهدف الأكاديمي: ${prefs?.academicGoal ?: "رفع المعدل التراكمي"}", fontFamily = CairoFontFamily, fontSize = 12.sp)
                        Text(text = "• فترة ذروة النشاط: ${prefs?.peakWindow ?: "الصباح الباكر"}", fontFamily = CairoFontFamily, fontSize = 12.sp)
                        Text(text = "• أسلوب الدراسة المعتمد: ${prefs?.studyStyle ?: "بومودورو 25 دقيقة"}", fontFamily = CairoFontFamily, fontSize = 12.sp)
                        Text(text = "• نبرة توجيه صولا: ${prefs?.solaTone ?: "متوازنة وتحليلية"}", fontFamily = CairoFontFamily, fontSize = 12.sp)
                    }
                }
            }
            } // End of selectedProfileTab == 0

            // Tab 1: الحقيبة والمقتنيات
            if (selectedProfileTab == 1) {
                item {
                    StudentInventoryTab(
                        userProfile = userProfile,
                        onEquipItem = onEquipItem,
                        onUnequipItem = onUnequipItem,
                        onOpenStore = { showStoreDialog = true }
                    )
                }
            }

            // Tab 2: مكتبتي الأكاديمية
            if (selectedProfileTab == 2) {
                item {
                    CentralizedLibraryContent(
                        allFiles = allFiles,
                        subjects = subjects,
                        onAddDocumentClick = { libraryFilePicker.launch("*/*") },
                        onRenameFile = onRenameFile,
                        onTogglePinFile = onTogglePinFile,
                        onDeleteFile = onDeleteFile,
                        onMoveFile = onMoveFile
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Guided Upload Wizard Dialog
    if (showGuidedUploadDialog) {
        GuidedUploadDialog(
            fileUri = pendingUploadUri,
            suggestedFileName = pendingUploadFileName,
            subjects = subjects,
            onDismiss = { showGuidedUploadDialog = false },
            onConfirmUpload = { alias, subId, cat, isPinned, fType, uStr ->
                onAddFile(subId, pendingUploadFileName, alias, fType, cat, isPinned, uStr)
                showGuidedUploadDialog = false
            }
        )
    }

    // Customization Store Dialog
    if (showStoreDialog) {
        CustomizationStoreDialog(
            userProfile = userProfile,
            onPurchaseItem = { item ->
                onPurchaseItem(
                    item,
                    {
                        scope.launch { snackbarHostState.showSnackbar("تم شراء ${item.name} وتجهيزه بنجاح! 🎉") }
                    },
                    { errorMsg ->
                        scope.launch { snackbarHostState.showSnackbar(errorMsg) }
                    }
                )
            },
            onEquipItem = onEquipItem,
            onDismiss = { showStoreDialog = false }
        )
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            userProfile = userProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, bio, major, year, avatar, photo, pass, onSuccess, onError ->
                onUpdateProfile(
                    name, bio, major, year, avatar, photo, pass,
                    {
                        onSuccess()
                        scope.launch { snackbarHostState.showSnackbar("تم حفظ وتحديث الملف الشخصي بنجاح 🚀") }
                    },
                    { err ->
                        onError(err)
                        scope.launch { snackbarHostState.showSnackbar(err) }
                    }
                )
            }
        )
    }
}

@Composable
fun CustomizationStoreDialog(
    userProfile: UserProfile?,
    onPurchaseItem: (ShopItem) -> Unit,
    onEquipItem: (ShopCategory, ShopItem) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(ShopCategory.FRAME) }
    val categories = ShopCategory.entries

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(12.dp)
                .testTag("customization_store_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "متجر SCOPE للتخصيص 🎨",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "خصص إطاراتك، هالات صولا، وألقابك الأكاديمية",
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Current Wallet Display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "رصيدك المتاح:",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚡ ${userProfile?.sparks ?: 0}",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ScopePrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "🪙 ${userProfile?.tokens ?: 0}",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ScopeWarningAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Tabs
                TabRow(
                    selectedTabIndex = categories.indexOf(selectedCategory),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = ScopePrimary
                ) {
                    categories.forEach { cat ->
                        Tab(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            text = {
                                Text(
                                    text = cat.titleAr,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Items List
                val categoryItems = ScopeEconomy.storeCatalog.filter { it.category == selectedCategory }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categoryItems) { item ->
                        val isOwned = userProfile?.unlockedItemIds?.contains(item.id) == true ||
                                item.sparksPrice == 0 && item.tokensPrice == 0

                        val isEquipped = when (item.category) {
                            ShopCategory.FRAME -> userProfile?.equippedFrame == item.id
                            ShopCategory.AURA -> userProfile?.equippedAura == item.id
                            ShopCategory.TITLE -> userProfile?.equippedTitle == item.name
                            ShopCategory.GLASS_FX -> userProfile?.equippedGlassFx == item.id
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isEquipped) ScopePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Color / Icon preview circle
                                val previewColor = try {
                                    Color(android.graphics.Color.parseColor(item.previewColorHex))
                                } catch (e: Exception) {
                                    ScopePrimary
                                }

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(previewColor.copy(alpha = 0.2f))
                                        .border(2.dp, previewColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (item.category) {
                                            ShopCategory.FRAME -> Icons.Default.Palette
                                            ShopCategory.AURA -> Icons.Default.AutoAwesome
                                            ShopCategory.TITLE -> Icons.Default.MilitaryTech
                                            ShopCategory.GLASS_FX -> Icons.Default.ColorLens
                                        },
                                        contentDescription = null,
                                        tint = previewColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.name,
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (item.rarity == "أسطوري") ScopeWarningAmber.copy(alpha = 0.2f)
                                                    else ScopePrimary.copy(alpha = 0.15f)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = item.rarity,
                                                fontFamily = CairoFontFamily,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.rarity == "أسطوري") ScopeWarningAmber else ScopePrimary
                                            )
                                        }
                                    }
                                    Text(
                                        text = item.description,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Action Button: Equip / Equipped / Purchase
                                if (isEquipped) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(ScopeFocusGreen.copy(alpha = 0.15f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "مجهز ✓",
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = ScopeFocusGreen
                                        )
                                    }
                                } else if (isOwned) {
                                    OutlinedButton(
                                        onClick = { onEquipItem(item.category, item) },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "تجهيز",
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = ScopePrimary
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = { onPurchaseItem(item) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        val priceLabel = if (item.tokensPrice > 0) "${item.tokensPrice} 🪙" else "${item.sparksPrice} ⚡"
                                        Text(
                                            text = "شراء $priceLabel",
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White
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

private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)

@Composable
fun EditProfileDialog(
    userProfile: UserProfile?,
    onDismiss: () -> Unit,
    onSave: (displayName: String, bio: String, major: String, year: String, avatarId: String, photoUrl: String, newPassword: String?, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit
) {
    var displayName by remember { mutableStateOf(userProfile?.displayName ?: "") }
    var bio by remember { mutableStateOf(userProfile?.customStatus ?: "") }
    var academicMajor by remember { mutableStateOf(userProfile?.academicMajor ?: "") }
    var academicYear by remember { mutableStateOf(userProfile?.academicYear ?: "") }
    var selectedAvatarId by remember { mutableStateOf(userProfile?.avatarId ?: "avatar_scholar_owl") }
    var photoUrl by remember { mutableStateOf(userProfile?.photoUrl ?: "") }
    var newPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
                .testTag("edit_profile_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = borderStroke(1.2.dp, ScopePrimary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تعديل الملف الشخصي ✏️",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Text(text = "✕", color = Color(0xFF94A3B8), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Avatar Selector
                Text(
                    text = "اختر الأفاتار الأكاديمي 3D:",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ScopePrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ScopeAvatarsCatalog.ALL_AVATARS) { avatar ->
                        val isSelected = selectedAvatarId == avatar.id
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) ScopePrimary.copy(alpha = 0.25f) else Color(0xFF1E293B))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) ScopePrimary else Color(0xFF334155),
                                    shape = CircleShape
                                )
                                .clickable { selectedAvatarId = avatar.id },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = avatar.emoji, fontSize = 24.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Display Name
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("الاسم الظاهر", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ScopePrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Bio / Status
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("النبذة والحالة الأكاديمية (Bio)", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ScopePrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Major
                OutlinedTextField(
                    value = academicMajor,
                    onValueChange = { academicMajor = it },
                    label = { Text("التخصص الأكاديمي", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ScopePrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Academic Year
                OutlinedTextField(
                    value = academicYear,
                    onValueChange = { academicYear = it },
                    label = { Text("السنة / المرحلة الدراسية", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ScopePrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Photo URL (Optional)
                OutlinedTextField(
                    value = photoUrl,
                    onValueChange = { photoUrl = it },
                    label = { Text("رابط الصورة الرمزية (اختياري)", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ScopePrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // New Password (Optional)
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("كلمة المرور الجديدة (اختياري للتعيين)", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                    singleLine = true,
                    visualTransformation = if (!passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ScopePrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء", fontFamily = CairoFontFamily, color = Color(0xFF94A3B8))
                    }

                    Button(
                        onClick = {
                            isSaving = true
                            errorMessage = null
                            onSave(
                                displayName,
                                bio,
                                academicMajor,
                                academicYear,
                                selectedAvatarId,
                                photoUrl,
                                newPassword.ifBlank { null },
                                {
                                    isSaving = false
                                    onDismiss()
                                },
                                { err ->
                                    isSaving = false
                                    errorMessage = err
                                }
                            )
                        },
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f).testTag("save_profile_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                    ) {
                        Text(
                            text = if (isSaving) "جاري الحفظ..." else "حفظ التعديلات 💾",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
