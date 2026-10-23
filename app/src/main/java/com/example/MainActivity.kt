package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.audio.ScopeSoundStudio
import com.example.ui.audio.ScopeSoundStudioBottomSheet
import com.example.ui.components.ScopeHeader
import com.example.ui.mascot.FloatingSolaMascot
import com.example.ui.screens.AvatarStudioDialog
import com.example.ui.screens.BurnoutShieldDialog
import com.example.ui.components.ScopeChangelogDialog
import com.example.ui.components.ScopeUpdateModal
import com.example.ui.screens.CommunityHubScreen
import com.example.ui.screens.ControlCenterDialog
import com.example.ui.screens.DeveloperControlPanelDialog
import com.example.ui.screens.ExamSimulatorDialog
import com.example.ui.screens.GpaTimeMachineDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileStudioScreen
import com.example.ui.screens.ProjectDetailScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.ScopeNotificationOverlay
import com.example.ui.screens.ScopeListensDialog
import com.example.ui.screens.ScopeJourneyScreen
import com.example.ui.screens.ScopeSearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.AuthFlowNavigator
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.SolaOnboardingWizard
import com.example.ui.screens.SolaPersonalityDialog
import com.example.ui.screens.SolaScreen
import com.example.ui.screens.StudyGuildsDialog
import com.example.ui.screens.StudyScreen
import com.example.ui.screens.UniversalSearchModal
import com.example.ui.theme.AdaptiveBackgroundContainer
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeTheme
import com.example.ui.theme.ScopeThemeEngine
import com.example.ui.viewmodel.ScopeViewModel

sealed class Screen(val title: String, val filledIcon: ImageVector, val outlinedIcon: ImageVector, val tag: String) {
    object Home : Screen("الرئيسية", Icons.Filled.Home, Icons.Outlined.Home, "tab_home")
    object Study : Screen("الدراسة", Icons.Filled.School, Icons.Outlined.School, "tab_study")
    object Schedule : Screen("الجدول", Icons.Filled.DateRange, Icons.Outlined.DateRange, "tab_schedule")
    object Projects : Screen("المشاريع", Icons.Filled.Folder, Icons.Outlined.Folder, "tab_projects")
    object Community : Screen("المجتمع", Icons.Filled.Group, Icons.Outlined.Group, "tab_community")
    object Sola : Screen("صولا", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "tab_sola")
}

class MainActivity : ComponentActivity() {
    private val viewModel: ScopeViewModel by viewModels()
    private val quickFocusTrigger = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            ScopeTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                    val isGuestMode by viewModel.isGuestMode.collectAsStateWithLifecycle()
                    val isSigningIn by viewModel.isSigningIn.collectAsStateWithLifecycle()
                    val signInError by viewModel.signInError.collectAsStateWithLifecycle()
                    val context = LocalContext.current

                    val startupError by viewModel.startupError.collectAsStateWithLifecycle()

                    if (currentUser == null && !isGuestMode) {
                        AuthFlowNavigator(
                            isSigningIn = isSigningIn,
                            errorMessage = signInError,
                            context = context,
                            onSignInWithGoogle = { viewModel.signInWithGoogle(context) },
                            onSignInWithEmail = { email, pass -> viewModel.signInWithEmail(email, pass) },
                            onRegisterWithEmail = { email, pass, name, scopeId, uni, major, year, phone ->
                                viewModel.signUpWithEmail(email, pass, name, scopeId, uni, major, year, phone)
                            },
                            onSendPasswordReset = { email -> viewModel.sendPasswordReset(email) },
                            onContinueAsGuest = { viewModel.continueAsGuest() }
                        )
                    } else {
                        ScopeMainApp(
                            viewModel = viewModel,
                            quickFocusTrigger = quickFocusTrigger.value,
                            onQuickFocusHandled = { quickFocusTrigger.value = false }
                        )
                    }

                    if (startupError != null) {
                        androidx.compose.ui.window.Dialog(onDismissRequest = { viewModel.dismissStartupError() }) {
                            androidx.compose.material3.Card(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF121826)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFEF4444))
                            ) {
                                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("تنبيه نظام التشغيل ⚠️", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = startupError!!,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 12.sp,
                                        color = Color(0xFFFECDD3),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = { viewModel.dismissStartupError() },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                    ) {
                                        Text("متابعة العمل (وضع الزائر / المحلي) 🚀", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        if (intent?.getBooleanExtra("EXTRA_QUICK_FOCUS", false) == true) {
            quickFocusTrigger.value = true
        }
    }
}

@Composable
fun ScopeMainApp(
    viewModel: ScopeViewModel,
    quickFocusTrigger: Boolean = false,
    onQuickFocusHandled: () -> Unit = {}
) {
    val context = LocalContext.current
    val soundStudio = remember { ScopeSoundStudio.getInstance(context) }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    var showPermissionsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initUpdateCheck(context)
        val prefs = context.getSharedPreferences("scope_prefs", android.content.Context.MODE_PRIVATE)
        val hasShownPermissions = prefs.getBoolean("has_shown_permissions_rationale", false)
        if (!hasShownPermissions) {
            showPermissionsDialog = true
            prefs.edit().putBoolean("has_shown_permissions_rationale", true).apply()
        }
    }

    val showUpdateModal by viewModel.showUpdateModal.collectAsStateWithLifecycle()
    val showChangelog by viewModel.showChangelog.collectAsStateWithLifecycle()

    LaunchedEffect(quickFocusTrigger) {
        if (quickFocusTrigger) {
            currentScreen = Screen.Study
            viewModel.startTimer()
            onQuickFocusHandled()
        }
    }

    // Screen Overlays / Navigation destinations
    var showControlCenter by remember { mutableStateOf(false) }
    var showSearchScreen by remember { mutableStateOf(false) }
    var showProfileStudio by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showDiagnosticSurvey by remember { mutableStateOf(false) }
    var showSoundStudioSheet by remember { mutableStateOf(false) }
    var showAvatarStudio by remember { mutableStateOf(false) }
    var showDevPanel by remember { mutableStateOf(false) }

    // Data from ViewModel
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val exams by viewModel.exams.collectAsStateWithLifecycle()
    val studySessions by viewModel.studySessions.collectAsStateWithLifecycle()
    val occurrences by viewModel.occurrences.collectAsStateWithLifecycle()
    val solaMessages by viewModel.solaMessages.collectAsStateWithLifecycle()
    val isSolaThinking by viewModel.isSolaThinking.collectAsStateWithLifecycle()
    val isHighThinkingMode by viewModel.isHighThinkingMode.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.timerSeconds.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.selectedFocusSubject.collectAsStateWithLifecycle()
    val showReflectionDialog by viewModel.showReflectionDialog.collectAsStateWithLifecycle()
    val currentGuidePrompt by viewModel.currentGuidePrompt.collectAsStateWithLifecycle()
    val circadianBattery by viewModel.circadianBattery.collectAsStateWithLifecycle()
    val flashcards by viewModel.flashcards.collectAsStateWithLifecycle()
    val conceptMaps by viewModel.conceptMaps.collectAsStateWithLifecycle()
    val showGpaTimeMachine by viewModel.showGpaTimeMachine.collectAsStateWithLifecycle()
    val showBurnoutShield by viewModel.showBurnoutShield.collectAsStateWithLifecycle()
    val activeExamSimulation by viewModel.activeExamSimulation.collectAsStateWithLifecycle()
    val burnoutRadarStatus by viewModel.burnoutRadarStatus.collectAsStateWithLifecycle()
    val activeGuild by viewModel.activeGuild.collectAsStateWithLifecycle()
    val guildLeaderboard by viewModel.guildLeaderboard.collectAsStateWithLifecycle()
    val guildActivityFeed by viewModel.guildActivityFeed.collectAsStateWithLifecycle()
    val showStudyGuildsDialog by viewModel.showStudyGuildsDialog.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val showProjectDetailModal by viewModel.showProjectDetailModal.collectAsStateWithLifecycle()
    val solaSpeechStyle by viewModel.solaSpeechStyle.collectAsStateWithLifecycle()
    val isHapticsEnabled by viewModel.isHapticsEnabled.collectAsStateWithLifecycle()
    val isNotificationsSilenced by viewModel.isNotificationsSilenced.collectAsStateWithLifecycle()
    val isRestModeActive by viewModel.isRestModeActive.collectAsStateWithLifecycle()
    val showSolaPersonalityModal by viewModel.showSolaPersonalityModal.collectAsStateWithLifecycle()
    val showScopeJourneyModal by viewModel.showScopeJourneyModal.collectAsStateWithLifecycle()

    // Batch 3 State Flows
    val feedbacks by viewModel.feedbacks.collectAsStateWithLifecycle()
    val devCommunityToolsEnabled by viewModel.devCommunityToolsEnabled.collectAsStateWithLifecycle()
    val showUniversalSearchModal by viewModel.showUniversalSearchModal.collectAsStateWithLifecycle()
    val showNotificationCenter by viewModel.showNotificationCenter.collectAsStateWithLifecycle()
    val showScopeListensModal by viewModel.showScopeListensModal.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val mutedChannelIds by viewModel.mutedChannelIds.collectAsStateWithLifecycle()
    val audioSummaries by viewModel.audioSummaries.collectAsStateWithLifecycle()
    val allLibraryFiles by viewModel.allLibraryFiles.collectAsStateWithLifecycle()

    LaunchedEffect(userProfile, circadianBattery) {
        viewModel.syncLiveWidget(context)
    }

    // 3D Backgrounds & Theme System
    val enable3dBackgrounds by viewModel.enable3dBackgrounds.collectAsStateWithLifecycle()
    val selectedThemePackId by viewModel.selectedThemePackId.collectAsStateWithLifecycle()
    val activeThemePack = remember(selectedThemePackId) { ScopeThemeEngine.getThemePack(selectedThemePackId) }

    // Global Search State
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchCategory by viewModel.searchCategory.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    val screens = listOf(
        Screen.Home,
        Screen.Study,
        Screen.Schedule,
        Screen.Projects,
        Screen.Community,
        Screen.Sola
    )

    // Handle Back Press when in full overlays
    if (showSearchScreen) {
        BackHandler { showSearchScreen = false }
    } else if (showProfileStudio) {
        BackHandler { showProfileStudio = false }
    } else if (showSettingsScreen) {
        BackHandler { showSettingsScreen = false }
    }

    // Determine screen-specific background drawable
    val currentScreenBgRes = when {
        showSearchScreen || showProfileStudio || showSettingsScreen -> activeThemePack.homeBgRes
        currentScreen == Screen.Home -> activeThemePack.homeBgRes
        currentScreen == Screen.Study -> activeThemePack.studyBgRes
        currentScreen == Screen.Schedule -> activeThemePack.scheduleBgRes
        currentScreen == Screen.Projects -> activeThemePack.projectsBgRes
        currentScreen == Screen.Community -> activeThemePack.campusBgRes
        currentScreen == Screen.Sola -> activeThemePack.homeBgRes
        else -> activeThemePack.homeBgRes
    }

    AdaptiveBackgroundContainer(
        bgResId = currentScreenBgRes,
        enable3dBackgrounds = enable3dBackgrounds
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                if (!showSearchScreen && !showProfileStudio && !showSettingsScreen) {
                    ScopeHeader(
                        userProfile = userProfile,
                        onOpenControlCenter = {
                            soundStudio.playTap()
                            showControlCenter = true
                        },
                        onOpenSearch = {
                            soundStudio.playTap()
                            viewModel.openUniversalSearchModal()
                        },
                        onOpenProfileStudio = {
                            soundStudio.playTap()
                            showProfileStudio = true
                        },
                        onOpenNotifications = {
                            soundStudio.playTap()
                            viewModel.openNotificationCenter()
                        },
                        onOpenFeedback = {
                            soundStudio.playTap()
                            viewModel.openScopeListensModal()
                        },
                        unreadNotificationsCount = notifications.count { !it.isRead }
                    )
                }
            },

            bottomBar = {
                if (!showSearchScreen && !showProfileStudio && !showSettingsScreen) {
                    NavigationBar(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .testTag("bottom_nav_bar"),
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (enable3dBackgrounds) 0.92f else 1f),
                        tonalElevation = 6.dp
                    ) {
                        screens.forEach { screen ->
                            val isSelected = currentScreen == screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    soundStudio.playTap()
                                    currentScreen = screen
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) screen.filledIcon else screen.outlinedIcon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontFamily = CairoFontFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 10.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ScopePrimary,
                                    selectedTextColor = ScopePrimary,
                                    indicatorColor = ScopePrimary.copy(alpha = 0.15f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag(screen.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    showSearchScreen -> {
                        ScopeSearchScreen(
                            searchQuery = searchQuery,
                            selectedCategory = searchCategory,
                            searchResults = searchResults,
                            onQueryChange = { viewModel.updateSearchQuery(it) },
                            onCategorySelect = { viewModel.updateSearchCategory(it) },
                            onResultClick = { item ->
                                showSearchScreen = false
                                when (item.targetTab) {
                                    "STUDY" -> currentScreen = Screen.Study
                                    "SCHEDULE" -> currentScreen = Screen.Schedule
                                    "PROJECTS" -> currentScreen = Screen.Projects
                                    "CAMPUS" -> currentScreen = Screen.Community
                                    "SOLA" -> currentScreen = Screen.Sola
                                    else -> currentScreen = Screen.Home
                                }
                            },
                            onBack = { showSearchScreen = false }
                        )
                    }

                    showProfileStudio -> {
                        ProfileStudioScreen(
                            userProfile = userProfile,
                            tasks = tasks,
                            studySessions = studySessions,
                            subjects = subjects,
                            allFiles = allLibraryFiles,
                            onPurchaseItem = { item, onSuccess, onError ->
                                viewModel.purchaseStoreItem(item, onSuccess, onError)
                            },
                            onEquipItem = { cat, item ->
                                viewModel.equipItem(cat, item)
                            },
                            onUnequipItem = { cat ->
                                viewModel.unequipItem(cat)
                            },
                            onAddFile = { subId, fn, al, ft, cat, isP, uStr ->
                                viewModel.addDocumentFile(subId, fn, al, ft, cat, isP, uStr)
                            },
                            onRenameFile = { subId, fid, al ->
                                viewModel.renameSubjectFile(subId, fid, al)
                            },
                            onTogglePinFile = { subId, fid ->
                                viewModel.togglePinFile(subId, fid)
                            },
                            onDeleteFile = { subId, fid ->
                                viewModel.deleteSubjectFile(subId, fid)
                            },
                            onMoveFile = { fromSubId, toSubId, fid ->
                                viewModel.moveSubjectFile(fromSubId, toSubId, fid)
                            },
                            onBack = { showProfileStudio = false },
                            onOpenDiagnosticSurvey = { showDiagnosticSurvey = true },
                            onUpdateProfile = { name, bio, major, year, avatar, photo, pass, onSuccess, onError ->
                                viewModel.updateProfile(name, bio, major, year, avatar, photo, pass, onSuccess, onError)
                            }
                        )
                    }

                    showSettingsScreen -> {
                        SettingsScreen(
                            userProfile = userProfile,
                            onBack = { showSettingsScreen = false },
                            onSignOut = {
                                showSettingsScreen = false
                                viewModel.signOut()
                            },
                            onRetakeDiagnosticSurvey = {
                                showDiagnosticSurvey = true
                            },
                            enable3dBackgrounds = enable3dBackgrounds,
                            onToggle3dBackgrounds = { viewModel.toggle3dBackgrounds(it) },
                            selectedThemePackId = selectedThemePackId,
                            onSelectThemePack = { viewModel.selectThemePack(it) },
                            isMasterAudioMuted = soundStudio.isMasterMuted,
                            onToggleMasterAudioMute = { soundStudio.setMasterMute(it) },
                            onOpenSoundStudio = { showSoundStudioSheet = true },
                            onOpenDevPanel = { showDevPanel = true },
                            onWipeData = { viewModel.wipeData() }
                        )
                    }

                    showDevPanel -> {
                        DeveloperControlPanelDialog(
                            userProfile = userProfile,
                            feedbacks = feedbacks,
                            onRewardFeedback = { id, s, t -> viewModel.rewardAndResolveFeedback(id, s, t) },
                            isDevCommunityToolsEnabled = devCommunityToolsEnabled,
                            onToggleDevCommunityTools = { viewModel.toggleDevCommunityTools(it) },
                            onGiftEconomy = { s, t, p -> viewModel.grantDevSparksAndTokens(s, t, p) },
                            onGiftToRecipient = { recipient, sparks, tokens, grantPro, onResult ->
                                viewModel.transferGiftingToRecipient(recipient, sparks, tokens, grantPro, onResult)
                            },
                            onCreateChallenge = { viewModel.addCommunityChallenge(it) },
                            onUpdateAnalytics = { viewModel.updateDevAnalytics(it) },
                            onDismiss = { showDevPanel = false }
                        )
                    }

                    else -> {
                        when (currentScreen) {
                            Screen.Home -> HomeScreen(
                                userProfile = userProfile,
                                recommendation = viewModel.getActionableRecommendation(),
                                tasks = tasks,
                                exams = exams,
                                subjects = subjects,
                                batteryStatus = circadianBattery,
                                onReorderTasksByEnergy = { viewModel.reorderTasksByEnergy() },
                                onToggleTask = { id, status -> viewModel.toggleTask(id, status) },
                                onAddTask = { title, subj, prio, effort, isPersonal, personalCat ->
                                    viewModel.addNewTask(title, subj, prio, effort, isPersonal, personalCat)
                                },
                                onNavigateToStudy = { currentScreen = Screen.Study },
                                onNavigateToSola = { currentScreen = Screen.Sola },
                                onNavigateToSchedule = { currentScreen = Screen.Schedule },
                                onNavigateToSearch = { showSearchScreen = true },
                                onNavigateToProfile = { showProfileStudio = true },
                                onOpenGpaTimeMachine = { viewModel.openGpaTimeMachine() },
                                onOpenBurnoutShield = { viewModel.openBurnoutShield() },
                                onStartExamSimulation = { exam -> viewModel.startExamSimulation(exam) },
                                onOpenDevPanel = { showDevPanel = true }
                            )

                            Screen.Study -> StudyScreen(
                                subjects = subjects,
                                studySessions = studySessions,
                                flashcards = flashcards,
                                conceptMaps = conceptMaps,
                                allFiles = allLibraryFiles,
                                timerSeconds = timerSeconds,
                                isTimerRunning = isTimerRunning,
                                selectedSubject = selectedSubject,
                                showReflectionDialog = showReflectionDialog,
                                onStartTimer = { viewModel.startTimer() },
                                onPauseTimer = { viewModel.pauseTimer() },
                                onResetTimer = { viewModel.resetTimer(it) },
                                onSelectSubject = { viewModel.selectFocusSubject(it) },
                                onSaveReflection = { sig, notes -> viewModel.saveSessionReflection(sig, notes) },
                                onDismissReflection = { viewModel.dismissReflectionDialog() },
                                onAddSubject = { name, code, hex, inst -> viewModel.addNewSubject(name, code, hex, inst) },
                                onSaveVisionDeck = { f, c, xp -> viewModel.saveVisionDeck(f, c, xp) },
                                onAddSubjectFile = { sid, fn, al, ft, cat -> viewModel.addSubjectFile(sid, fn, al, ft, cat) },
                                onAddDocumentFile = { subId, fn, al, ft, cat, isP, uStr -> viewModel.addDocumentFile(subId, fn, al, ft, cat, isP, uStr) },
                                onRenameSubjectFile = { sid, fid, al -> viewModel.renameSubjectFile(sid, fid, al) },
                                onTogglePinFile = { sid, fid -> viewModel.togglePinFile(sid, fid) },
                                onDeleteSubjectFile = { sid, fid -> viewModel.deleteSubjectFile(sid, fid) },
                                onMoveSubjectFile = { fromSubId, toSubId, fid -> viewModel.moveSubjectFile(fromSubId, toSubId, fid) },
                                recommendation = viewModel.getActionableRecommendation().explanation
                            )

                            Screen.Schedule -> ScheduleScreen(
                                occurrences = occurrences,
                                subjects = subjects,
                                availableWindows = viewModel.calculateAvailableWindows(),
                                onAddOccurrence = { subj, day, s, e, r -> viewModel.addNewClassOccurrence(subj, day, s, e, r) }
                            )

                            Screen.Projects -> ProjectsScreen(
                                tasks = tasks,
                                subjects = subjects,
                                projects = projects,
                                onOpenProjectDetail = { proj -> viewModel.openProjectDetail(proj) },
                                onToggleTask = { id, status -> viewModel.toggleTask(id, status) },
                                onAddTask = { title, subj, prio, effort -> viewModel.addNewTask(title, subj, prio, effort) }
                            )

                            Screen.Community -> CommunityHubScreen(
                                userProfile = userProfile,
                                friends = viewModel.friends.collectAsStateWithLifecycle().value,
                                friendRequests = viewModel.friendRequests.collectAsStateWithLifecycle().value,
                                onAddFriend = { uid -> viewModel.addFriend(uid) },
                                onAcceptFriendRequest = { req -> viewModel.acceptFriendRequest(req) },
                                onDeclineFriendRequest = { id -> viewModel.declineFriendRequest(id) },
                                activeGuild = activeGuild,
                                onOpenGuilds = { viewModel.openStudyGuilds() }
                            )

                            Screen.Sola -> SolaScreen(
                                messages = solaMessages,
                                isThinking = isSolaThinking,
                                isHighThinkingMode = isHighThinkingMode,
                                onToggleHighThinking = { viewModel.toggleHighThinkingMode() },
                                onSendMessage = { viewModel.sendSolaQuery(it) }
                            )
                        }

                        // Transparent Floating Sola Companion Layer (Border-free Overlay)
                        if (currentScreen != Screen.Sola) {
                            FloatingSolaMascot(
                                prompt = currentGuidePrompt,
                                onActionClick = { targetTab ->
                                    soundStudio.playSolaChirp()
                                    when (targetTab) {
                                        "HOME" -> currentScreen = Screen.Home
                                        "STUDY" -> currentScreen = Screen.Study
                                        "SCHEDULE" -> currentScreen = Screen.Schedule
                                        "PROJECTS" -> currentScreen = Screen.Projects
                                        "CAMPUS" -> currentScreen = Screen.Community
                                        "SOLA" -> currentScreen = Screen.Sola
                                        "PROFILE" -> showProfileStudio = true
                                        "EXAM_SIMULATOR" -> viewModel.startExamSimulation()
                                        "BURNOUT_SHIELD" -> viewModel.openBurnoutShield()
                                        "GPA_MACHINE" -> viewModel.openGpaTimeMachine()
                                        else -> currentScreen = Screen.Sola
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }

            // Control Center Dialog
            if (showControlCenter) {
                ControlCenterDialog(
                    userProfile = userProfile,
                    solaSpeechStyle = solaSpeechStyle,
                    isHapticsEnabled = isHapticsEnabled,
                    isNotificationsSilenced = isNotificationsSilenced,
                    isRestModeActive = isRestModeActive,
                    onDismiss = { showControlCenter = false },
                    onToggleFocusShield = { viewModel.toggleFocusShield(it) },
                    onToggleHaptics = { viewModel.toggleHaptics(it) },
                    onToggleNotificationsSilencer = { viewModel.toggleNotificationsSilencer(it) },
                    onToggleRestMode = { viewModel.toggleRestMode(it) },
                    onNavigateToProfileStudio = { showProfileStudio = true },
                    onNavigateToSettings = { showSettingsScreen = true },
                    onRetakeDiagnostic = { showDiagnosticSurvey = true },
                    onOpenSolaPersonality = { viewModel.openSolaPersonalityModal() },
                    onOpenScopeJourney = { viewModel.openScopeJourneyModal() },
                    onSignOut = { viewModel.signOut() },
                    onOpenSoundStudio = { showSoundStudioSheet = true },
                    onOpenDevPanel = { showDevPanel = true }
                )
            }

            // Sola Personality & Context Studio Modal
            if (showSolaPersonalityModal) {
                SolaPersonalityDialog(
                    currentStyle = solaSpeechStyle,
                    userProfile = userProfile,
                    onStyleChange = { style -> viewModel.setSolaSpeechStyle(style) },
                    onClearMemoryCache = { viewModel.clearSolaMemoryCache() },
                    onDismiss = { viewModel.closeSolaPersonalityModal() }
                )
            }

            // 3D Scope Journey Screen Overlay
            if (showScopeJourneyModal) {
                ScopeJourneyScreen(
                    userProfile = userProfile,
                    studySessions = studySessions,
                    onBack = { viewModel.closeScopeJourneyModal() },
                    onClaimReward = { sparks -> viewModel.grantDevSparksAndTokens(sparks, 0, false) }
                )
            }

            // Avatar Studio Dialog
            if (showAvatarStudio) {
                AvatarStudioDialog(
                    userProfile = userProfile,
                    onEquipAvatar = { avatarId -> viewModel.updateAvatar(avatarId) },
                    onUploadPhoto = { url -> viewModel.updateAvatar(userProfile?.avatarId ?: "avatar_scholar_owl", url) },
                    onDismiss = { showAvatarStudio = false }
                )
            }

            // Super Developer Control Panel Dialog
            if (showDevPanel) {
                DeveloperControlPanelDialog(
                    userProfile = userProfile,
                    feedbacks = feedbacks,
                    onRewardFeedback = { fbId, sparks, tokens -> viewModel.rewardAndResolveFeedback(fbId, sparks, tokens) },
                    isDevCommunityToolsEnabled = devCommunityToolsEnabled,
                    onToggleDevCommunityTools = { enabled -> viewModel.toggleDevCommunityTools(enabled) },
                    onGiftEconomy = { sparks, tokens, makePro -> viewModel.grantDevSparksAndTokens(sparks, tokens, makePro) },
                    onGiftToRecipient = { recipient, sparks, tokens, grantPro, onResult ->
                        viewModel.transferGiftingToRecipient(recipient, sparks, tokens, grantPro, onResult)
                    },
                    onCreateChallenge = { challenge -> viewModel.addCommunityChallenge(challenge) },
                    onUpdateAnalytics = { analytics -> viewModel.updateDevAnalytics(analytics) },
                    onDismiss = { showDevPanel = false }
                )
            }

            // Universal Glass Search Modal
            if (showUniversalSearchModal) {
                UniversalSearchModal(
                    subjects = subjects,
                    tasks = tasks,
                    audioSummaries = audioSummaries,
                    projects = projects,
                    onAskSola = { prompt ->
                        viewModel.sendSolaQuery(prompt)
                        currentScreen = Screen.Sola
                    },
                    onNavigateToItem = { cat, _ ->
                        when (cat) {
                            "SUBJECT" -> currentScreen = Screen.Study
                            "TASK" -> currentScreen = Screen.Home
                            "AUDIO" -> currentScreen = Screen.Study
                            "PROJECT" -> currentScreen = Screen.Projects
                            "PEER" -> currentScreen = Screen.Community
                        }
                    },
                    onDismiss = { viewModel.closeUniversalSearchModal() }
                )
            }

            // Floating Glass Notification Center Overlay
            if (showNotificationCenter) {
                ScopeNotificationOverlay(
                    notifications = notifications,
                    isFocusShieldActive = userProfile?.focusShieldEnabled ?: false,
                    mutedChannelIds = mutedChannelIds,
                    onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
                    onToggleChannelMute = { chId -> viewModel.toggleNotificationChannelMute(chId) },
                    onDismiss = { viewModel.closeNotificationCenter() }
                )
            }

            // "SCOPE Listens" Live Student Feedback Dialog
            if (showScopeListensModal) {
                ScopeListensDialog(
                    userFeedbacks = feedbacks,
                    onSubmitFeedback = { type, content, cat -> viewModel.submitFeedback(type, content, cat) },
                    onDismiss = { viewModel.closeScopeListensModal() }
                )
            }


            // Scope Multi-Track Sound Studio Bottom Sheet
            if (showSoundStudioSheet) {
                ScopeSoundStudioBottomSheet(
                    onDismiss = { showSoundStudioSheet = false }
                )
            }

            // GPA Time Machine Dialog
            if (showGpaTimeMachine) {
                GpaTimeMachineDialog(
                    subjects = subjects,
                    onSaveTarget = { target -> viewModel.saveGpaTarget(target) },
                    onDismiss = { viewModel.closeGpaTimeMachine() }
                )
            }

            // Sola Exam Simulator Dialog
            activeExamSimulation?.let { simSession ->
                ExamSimulatorDialog(
                    session = simSession,
                    onCompleteSimulation = { score, total, xp, sparks ->
                        viewModel.completeExamSimulation(score, total, xp, sparks)
                    },
                    onDismiss = { viewModel.closeExamSimulation() }
                )
            }

            // Burnout Radar & Rest Protector Shield Dialog
            if (showBurnoutShield) {
                BurnoutShieldDialog(
                    radarStatus = burnoutRadarStatus,
                    onClaimRestReward = { sparks, xp ->
                        viewModel.claimBurnoutRestReward(sparks, xp)
                    },
                    onDismiss = { viewModel.closeBurnoutShield() }
                )
            }

            // Study Guilds & Co-Op Battles Dialog
            if (showStudyGuildsDialog) {
                StudyGuildsDialog(
                    activeGuild = activeGuild,
                    userProfile = userProfile,
                    leaderboardGuilds = guildLeaderboard,
                    activityFeed = guildActivityFeed,
                    onCreateGuild = { name, tag, desc, emblem -> viewModel.createGuild(name, tag, desc, emblem) },
                    onJoinGuild = { inviteCode -> viewModel.joinGuildByCode(inviteCode) },
                    onContributeSparks = { sparks -> viewModel.contributeGuildSparks(sparks) },
                    onDismiss = { viewModel.closeStudyGuilds() }
                )
            }

            // Upgraded Project & Team Hub Screen Overlay
            if (showProjectDetailModal && activeProject != null) {
                ProjectDetailScreen(
                    project = activeProject!!,
                    userProfile = userProfile,
                    onBack = { viewModel.closeProjectDetail() },
                    onDeleteProject = { pid -> viewModel.deleteProject(pid) },
                    onAddRegisteredMember = { idOrEmail, roleTitle -> viewModel.addRegisteredProjectMember(idOrEmail, roleTitle) },
                    onAddExternalMember = { name, roleTitle -> viewModel.addExternalProjectMember(name, roleTitle) },
                    onAssignMemberRole = { memId, role, roleTitle -> viewModel.assignProjectMemberRole(memId, role, roleTitle) },
                    onStartElection = { title, candidates -> viewModel.startProjectElection(title, candidates) },
                    onCastVote = { candidateId -> viewModel.castProjectVote(candidateId) },
                    onAddTransaction = { title, amt, cat, isExp -> viewModel.addProjectFinancialTransaction(title, amt, cat, isExp) },
                    onPoolSparks = { sparks, tokens -> viewModel.poolProjectSparks(sparks, tokens) },
                    onAddNote = { title, content -> viewModel.addProjectNote(title, content) },
                    onToggleTask = { taskId, done -> viewModel.toggleProjectTask(taskId, done) },
                    onAddTask = { title -> viewModel.addProjectTask(title) }
                )
            }
            // Sola Smart Interactive Onboarding Survey
            val needsInitialOnboarding = userProfile != null && !userProfile!!.onboardingCompleted
            if (needsInitialOnboarding || showDiagnosticSurvey) {
                SolaOnboardingWizard(
                    initialPreferences = userProfile?.diagnosticPreferences ?: com.example.data.model.DiagnosticPreferences(),
                    onComplete = { preferences ->
                        viewModel.saveDiagnosticSurvey(preferences)
                        showDiagnosticSurvey = false
                    },
                    onSkip = {
                        viewModel.skipDiagnosticSurvey()
                        showDiagnosticSurvey = false
                    },
                    onDismiss = {
                        viewModel.skipDiagnosticSurvey()
                        showDiagnosticSurvey = false
                    }
                )
            }

            // In-App Auto-Update Manager & Changelog Engine
            showUpdateModal?.let { config ->
                ScopeUpdateModal(
                    versionName = config.latestVersionName,
                    updateUrl = config.updateUrl,
                    onDismiss = { viewModel.dismissUpdateModal() }
                )
            }

            showChangelog?.let { versionName ->
                ScopeChangelogDialog(
                    versionName = versionName,
                    onDismiss = { viewModel.dismissChangelog() }
                )
            }

            if (showPermissionsDialog) {
                com.example.ui.components.SystemPermissionsDialog(
                    onPermissionsResult = { showPermissionsDialog = false },
                    onDismiss = { showPermissionsDialog = false }
                )
            }
        }
    }
}
