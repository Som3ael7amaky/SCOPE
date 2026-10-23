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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserProfile
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeAccent
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber

@Composable
fun SettingsScreen(
    userProfile: UserProfile?,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onRetakeDiagnosticSurvey: () -> Unit,
    modifier: Modifier = Modifier,
    enable3dBackgrounds: Boolean = true,
    onToggle3dBackgrounds: (Boolean) -> Unit = {},
    selectedThemePackId: String = "cyber_academia",
    onSelectThemePack: (String) -> Unit = {},
    isMasterAudioMuted: Boolean = false,
    onToggleMasterAudioMute: (Boolean) -> Unit = {},
    onOpenSoundStudio: () -> Unit = {},
    onOpenDevPanel: () -> Unit = {},
    onWipeData: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val soundStudio = remember { com.example.ui.audio.ScopeSoundStudio.getInstance(context) }

    // Toggles state
    var lectureAlerts by remember { mutableStateOf(true) }
    var streakProtectionAlerts by remember { mutableStateOf(true) }
    var hapticFeedback by remember { mutableStateOf(true) }
    var floatingMascotEnabled by remember { mutableStateOf(true) }
    var auraPulseEffects by remember { mutableStateOf(true) }
    var glassmorphismFx by remember { mutableStateOf(true) }

    var showPlansDialog by remember { mutableStateOf(false) }
    var showLegalDialog by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("settings_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "مركز الإعدادات الشامل (Settings Hub)",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // 1. Account & Security
            item {
                SectionHeader(title = "1. الحساب والأمان (Account & Security)")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(ScopePrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = ScopePrimary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userProfile?.displayName ?: "طالب SCOPE",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = userProfile?.email ?: "student@scope.edu",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = userProfile?.scopeId ?: "@student_scope",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    color = ScopePrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = ScopeFocusGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "المصادقة السحابية الثنائية",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "مفعلة عبر Google ✓",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = ScopeFocusGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onSignOut,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_sign_out_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "تسجيل الخروج من الحساب", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        if (userProfile?.isDeveloper == true || com.example.data.model.isSuperDeveloperEmail(userProfile?.email)) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onOpenDevPanel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_dev_panel_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                            ) {
                                Text(text = "لوحة التحكم الإدارية (Dev Panel) 👑", color = Color.Black, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. Subscriptions & Billing
            item {
                SectionHeader(title = "2. الاشتراكات والفوترة (Subscriptions & Billing)")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "الخطة الحالية: SCOPE Free Starter",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "تشمل محرك صولا الذكي والجدول الأكاديمي السحابي",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ScopeFocusGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "نشط",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ScopeFocusGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showPlansDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("view_plans_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "عرض خطط SCOPE Pro والمميزات المتقدمة ✨",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 3. Sola Diagnostic Preferences
            item {
                SectionHeader(title = "3. تفضيلات تشخيص صولا (Sola Diagnostic Preferences)")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val prefs = userProfile?.diagnosticPreferences
                        Text(
                            text = "تم تخصيص استجابات صولا بناءً على تشخيصك الأكاديمي:",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        PreferenceItemRow("الهدف:", prefs?.academicGoal ?: "رفع المعدل التراكمي")
                        PreferenceItemRow("ذروة الإنتاجية:", prefs?.peakWindow ?: "الصباح الباكر")
                        PreferenceItemRow("التحدي الدراسي:", prefs?.primaryChallenge ?: "صعوبة البدء ومقاومة الانطلاق")
                        PreferenceItemRow("أسلوب الدراسة:", prefs?.studyStyle ?: "بومودورو 25 دقيقة")
                        PreferenceItemRow("نبرة صولا:", prefs?.solaTone ?: "متوازنة وتحليلية")

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onRetakeDiagnosticSurvey,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("retake_survey_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ScopeAccent)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إعادة إجراء الاستبيان التشخيصي لصولا 🤖",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 4. Notifications & Haptic Feedback
            item {
                SectionHeader(title = "4. الإشعارات والاهتزاز اللمسي (Notifications & Haptics)")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SettingSwitchRow(
                            icon = Icons.Default.Notifications,
                            title = "تنبيهات المحاضرات والمهام الذكية",
                            subtitle = "إشعار قبل 15 دقيقة من بداية المحاضرة أو موعد التسليم",
                            checked = lectureAlerts,
                            onCheckedChange = { lectureAlerts = it }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SettingSwitchRow(
                            icon = Icons.Default.Shield,
                            title = "حماية شعلة الالتزام (Streak Protection)",
                            subtitle = "تذكير ذكي إذا اقترب اليوم من الانتهاء دون جلسة دراسية",
                            checked = streakProtectionAlerts,
                            onCheckedChange = { streakProtectionAlerts = it }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SettingSwitchRow(
                            icon = Icons.Default.Vibration,
                            title = "الاهتزاز اللمسي (Haptic Feedback)",
                            subtitle = "ردود فعل لمسية لطيفة عند إكمال المهام ومؤقت التركيز",
                            checked = hapticFeedback,
                            onCheckedChange = { hapticFeedback = it }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SettingSwitchRow(
                            icon = Icons.AutoMirrored.Filled.VolumeOff,
                            title = "كتم كافة الأصوات (Master Audio Mute)",
                            subtitle = "تعطيل المؤثرات الصوتية وأجواء الاستوديو الخلفية",
                            checked = isMasterAudioMuted,
                            onCheckedChange = {
                                soundStudio.playTap()
                                onToggleMasterAudioMute(it)
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                soundStudio.playTap()
                                onOpenSoundStudio()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_sound_studio_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "فتح استوديو الأجواء الصوتية (Sound Studio) 🎧",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 5. Appearance & 3D Visual Effects
            item {
                SectionHeader(title = "5. المظهر والتأثيرات ثلاثية الأبعاد (Appearance & 3D FX)")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // 3D Screen Backgrounds Toggle
                        SettingSwitchRow(
                            icon = Icons.Default.Palette,
                            title = "تفعيل الخلفيات ثلاثية الأبعاد (Enable 3D Screen Backgrounds)",
                            subtitle = "خلفيات ثلاثية الأبعاد خاصة بكل محطة مغشاة بطبقة زجاجية (Depth Blur) لضمان وضوح 100%. عند التعطيل، يتم تفعيل النمط الداكن البسيط (Dark AMOLED).",
                            checked = enable3dBackgrounds,
                            onCheckedChange = {
                                soundStudio.playTap()
                                onToggle3dBackgrounds(it)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Theme Pack Selector
                        Text(
                            text = "حزمة الخلفيات النشطة (Active Theme Pack):",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            com.example.ui.theme.ScopeThemeEngine.AVAILABLE_PACKS.forEach { pack ->
                                val isSelected = pack.id == selectedThemePackId
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) ScopePrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable {
                                            soundStudio.playTap()
                                            onSelectThemePack(pack.id)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = pack.name,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) ScopePrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SettingSwitchRow(
                            icon = Icons.Default.Visibility,
                            title = "طبقة مجسم صولا ثلاثي الأبعاد العائم",
                            subtitle = "ظهور صولا الشفاف المرافق على زوايا الشاشات المختلفة",
                            checked = floatingMascotEnabled,
                            onCheckedChange = { floatingMascotEnabled = it }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SettingSwitchRow(
                            icon = Icons.Default.Palette,
                            title = "نبضات الهالة الضوئية وتأثيرات المدار",
                            subtitle = "توهج وحلقات إشعاع تتنفس حول صولا حسب حالته المزاجية",
                            checked = auraPulseEffects,
                            onCheckedChange = { auraPulseEffects = it }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SettingSwitchRow(
                            icon = Icons.Default.AutoAwesome,
                            title = "تأثير الزجاج الهولوغرافي المتقدم",
                            subtitle = "شفافية بطاقات واجهة المستخدم وانعكاسات زجاجية",
                            checked = glassmorphismFx,
                            onCheckedChange = { glassmorphismFx = it }
                        )
                    }
                }
            }

            // 6. Legal & Privacy
            item {
                SectionHeader(title = "6. الخصوصية والشروط القانونية (Legal & Privacy)")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        LegalLinkRow("سياسة الخصوصية الأكاديمية (Privacy Policy)") {
                            showLegalDialog = "نحن نحترم خصوصية الطلاب بأعلى المعايير. جميع بياناتك الأكاديمية ومحادثاتك مع صولا مشفرة ومحفوظة ضمن حسابك الآمن ولا يتم مشاركتها أو بيعها لأي طرف ثالث."
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LegalLinkRow("شروط الاستخدام والخدمة (Terms of Service)") {
                            showLegalDialog = "منصة SCOPE مخصصة لدعم المسيرة التعليمية للطلاب الجامعيين وتسهيل المذاكرة والتخطيط السليم، وجميع حقوق العلامة محفوظة."
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LegalLinkRow("تراخيص البرمجيات والمصادر المفتوحة (Open Source)") {
                            showLegalDialog = "تطبيق SCOPE مبني باستخدام Jetpack Compose و Material Design 3 و Firebase ومحرك الذكاء الاصطناعي من Google تحت تراخيص المشاع البرمجي."
                        }
                    }
                }
            }

            // 7. About SCOPE & App Version
            item {
                SectionHeader(title = "7. حول التطبيق والإصدار (About SCOPE)")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SCOPE OS (Student Operating System)",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ScopePrimary
                        )
                        Text(
                            text = "الإصدار v1.1.0-gold (بناء مستقر)",
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "محرك صولا ثلاثي الأبعاد: جاهز ونشط 🤖",
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = ScopeFocusGreen
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        var showCleanSlateConfirm by remember { mutableStateOf(false) }

                        Button(
                            onClick = { showCleanSlateConfirm = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("بدء بحساب نظيف (حذف البيانات التجريبية) 🧹", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                        }

                        if (showCleanSlateConfirm) {
                            Dialog(onDismissRequest = { showCleanSlateConfirm = false }) {
                                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("تصفير الحساب؟ 🧹", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("سيتم حذف كافة البيانات التجريبية والمقررات والمهام المسجلة لتبدأ بصفحة بيضاء حقيقية. هل أنت متأكد؟", fontFamily = CairoFontFamily, fontSize = 13.sp, textAlign = TextAlign.Center)
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedButton(onClick = { showCleanSlateConfirm = false }, modifier = Modifier.weight(1f)) {
                                                Text("إلغاء", fontFamily = CairoFontFamily)
                                            }
                                            Button(
                                                onClick = {
                                                    soundStudio.playComplete()
                                                    onWipeData()
                                                    showCleanSlateConfirm = false
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("تأكيد الحذف", fontFamily = CairoFontFamily)
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

    // Plans Dialog
    if (showPlansDialog) {
        Dialog(onDismissRequest = { showPlansDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("subscription_plans_dialog"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "خطط SCOPE المميزة ⭐",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ScopePrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ارتقِ بتجربتك الأكاديمية مع قدرات صولا الفائقة",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    PlanFeatureRow("تفكير صولا التحليلي العميق (High Thinking Mode) غير محدود")
                    PlanFeatureRow("توليف ذكي للملخصات الصوتية مع ميزة الاستماع بدون إنترنت")
                    PlanFeatureRow("مزامنة فورية متعددة الأجهزة لكل الجداول والمذكرات")
                    PlanFeatureRow("فتح مجاني لجميع الإطارات ثلاثية الأبعاد والهالات الأسطورية")

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showPlansDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                    ) {
                        Text(
                            text = "تجربة SCOPE Pro مجاناً لمدة 14 يوماً",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(onClick = { showPlansDialog = false }) {
                        Text(text = "إغلاق", fontFamily = CairoFontFamily, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Legal Content Dialog
    if (showLegalDialog != null) {
        Dialog(onDismissRequest = { showLegalDialog = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "المعلومات القانونية والشروط",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ScopePrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = showLegalDialog ?: "",
                        fontFamily = CairoFontFamily,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showLegalDialog = null },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "فهمت ذلك", fontFamily = CairoFontFamily, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontFamily = CairoFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ScopePrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = ScopePrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = subtitle, fontFamily = CairoFontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ScopePrimary
            )
        )
    }
}

@Composable
private fun LegalLinkRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontFamily = CairoFontFamily, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun PlanFeatureRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(ScopeFocusGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = ScopeFocusGreen, modifier = Modifier.size(12.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontFamily = CairoFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
