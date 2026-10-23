package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.circadian.CircadianBatteryStatus
import com.example.data.circadian.CircadianEnergyEngine
import com.example.data.economy.ScopeEconomy
import com.example.data.exam.BurnoutRadarStatus
import com.example.data.exam.ExamSimulationSession
import com.example.data.exam.SolaExamAndBurnoutEngine
import com.example.data.focus.GhostFocusEngine
import com.example.data.focus.GhostFocusStatus
import com.example.data.gpa.GpaTimeMachineEngine
import com.example.data.guilds.GuildActivityFeedItem
import com.example.data.guilds.StudyGuild
import com.example.data.guilds.StudyGuildsEngine
import com.example.data.projects.ProjectsHubEngine
import com.example.data.projects.ScopeProject
import com.example.data.model.*
import com.example.data.remote.AppVersionConfig
import com.example.data.remote.SolaAiService
import com.example.data.remote.ScopeUpdateManager
import com.example.data.repository.AuthRepository
import com.example.data.repository.FirestoreRepository
import com.example.data.preferences.UserPreferencesRepository
import com.example.ui.mascot.SolaEmotionState
import com.example.ui.mascot.SolaGuideEngine
import com.example.ui.mascot.SolaGuidePrompt
import com.example.widget.ScopeWidgetManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class ScheduleWindow(
    val title: String,
    val timeRange: String,
    val durationMinutes: Int,
    val isFocusWindow: Boolean
)

data class Recommendation(
    val title: String,
    val explanation: String,
    val targetTaskId: String? = null,
    val targetSubjectId: String? = null,
    val urgency: String = "عالي"
)

class ScopeViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository(),
    private val solaAiService: SolaAiService = SolaAiService()
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.authState
        .stateIn(viewModelScope, SharingStarted.Eagerly, authRepository.currentUser)

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    val allLibraryFiles: StateFlow<List<SubjectFile>> = _subjects.map { list ->
        list.flatMap { s ->
            s.files.map { f ->
                f.copy(
                    subjectId = if (f.subjectId.isBlank()) s.id else f.subjectId,
                    subjectName = if (f.subjectName.isBlank()) s.name else f.subjectName
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _exams = MutableStateFlow<List<Exam>>(emptyList())
    val exams: StateFlow<List<Exam>> = _exams.asStateFlow()

    private val _studySessions = MutableStateFlow<List<StudySession>>(emptyList())
    val studySessions: StateFlow<List<StudySession>> = _studySessions.asStateFlow()

    private val _occurrences = MutableStateFlow<List<ClassOccurrence>>(emptyList())
    val occurrences: StateFlow<List<ClassOccurrence>> = _occurrences.asStateFlow()

    // Update & Changelog State
    private val _showUpdateModal = MutableStateFlow<AppVersionConfig?>(null)
    val showUpdateModal: StateFlow<AppVersionConfig?> = _showUpdateModal.asStateFlow()

    private val _showChangelog = MutableStateFlow<String?>(null)
    val showChangelog: StateFlow<String?> = _showChangelog.asStateFlow()

    private var userPreferencesRepository: UserPreferencesRepository? = null
    private val updateManager = ScopeUpdateManager()

    // Friends Management State
    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    private val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequest>> = _friendRequests.asStateFlow()

    private val _startupError = MutableStateFlow<String?>(null)
    val startupError: StateFlow<String?> = _startupError.asStateFlow()

    fun dismissStartupError() {
        _startupError.value = null
    }

    init {
        viewModelScope.launch {
            try {
                currentUser.collect { user ->
                    if (user != null) {
                        try {
                            launch {
                                try {
                                    firestoreRepository.getUserProfile(user.uid).collect { profile ->
                                        if (profile != null) {
                                            val isDev = com.example.data.model.isSuperDeveloperEmail(profile.email)
                                            val resolved = if (isDev) {
                                                profile.copy(
                                                    isDeveloper = true,
                                                    equippedTitle = "المطور الأسمى 👑"
                                                )
                                            } else profile
                                            _userProfile.value = resolved
                                        } else {
                                            _userProfile.value = null
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("ScopeViewModel", "User profile stream error: ${e.message}", e)
                                }
                            }
                            launch { try { firestoreRepository.getSubjects(user.uid).collect { _subjects.value = it } } catch (e: Exception) {} }
                            launch { try { firestoreRepository.getTasks(user.uid).collect { _tasks.value = it } } catch (e: Exception) {} }
                            launch { try { firestoreRepository.getExams(user.uid).collect { _exams.value = it } } catch (e: Exception) {} }
                            launch { try { firestoreRepository.getStudySessions(user.uid).collect { _studySessions.value = it } } catch (e: Exception) {} }
                            launch { try { firestoreRepository.getClassOccurrences(user.uid).collect { _occurrences.value = it } } catch (e: Exception) {} }
                            launch { try { firestoreRepository.getFriends(user.uid).collect { _friends.value = it } } catch (e: Exception) {} }
                            launch { try { firestoreRepository.getFriendRequests(user.uid).collect { _friendRequests.value = it } } catch (e: Exception) {} }
                            
                            try {
                                firestoreRepository.seedInitialAcademicDataIfEmpty(user.uid, user.displayName ?: "", user.email ?: "")
                            } catch (e: Exception) {
                                android.util.Log.e("ScopeViewModel", "Seed initial data error: ${e.message}", e)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ScopeViewModel", "Firebase user session sync error: ${e.message}", e)
                            _startupError.value = "تنبيه: تعذر الاتصال بالخادم، يعمل التطبيق في وضع الاتصال المحلي والزائر."
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ScopeViewModel", "Startup fatal error: ${e.message}", e)
                _startupError.value = "خطأ في بدء التشغيل: ${e.localizedMessage ?: "تحقق من اتصالك بالإنترنت"}"
            }
        }
    }

    fun initUpdateCheck(context: Context) {
        if (userPreferencesRepository == null) {
            userPreferencesRepository = UserPreferencesRepository(context)
        }
        
        viewModelScope.launch {
            val lastSeen = userPreferencesRepository?.lastSeenVersionCode?.first() ?: 0
            if (BuildConfig.VERSION_CODE > lastSeen) {
                _showChangelog.value = BuildConfig.VERSION_NAME
            }

            val latest = updateManager.checkLatestVersion()
            if (latest != null && updateManager.isUpdateAvailable(latest.latestVersionCode)) {
                _showUpdateModal.value = latest
            }
        }
    }

    fun dismissChangelog() {
        _showChangelog.value = null
        viewModelScope.launch {
            userPreferencesRepository?.saveLastSeenVersionCode(BuildConfig.VERSION_CODE)
        }
    }

    fun dismissUpdateModal() {
        _showUpdateModal.value = null
    }

    fun addFriend(receiverUid: String) {
        val sender = currentUser.value
        val profile = _userProfile.value
        if (sender != null && profile != null) {
            viewModelScope.launch {
                firestoreRepository.sendFriendRequest(sender.uid, profile.displayName, profile.scopeId, receiverUid)
            }
        }
    }

    fun acceptFriendRequest(request: FriendRequest) {
        val user = currentUser.value
        if (user != null) {
            viewModelScope.launch {
                firestoreRepository.acceptFriendRequest(user.uid, request)
            }
        }
    }

    fun declineFriendRequest(requestId: String) {
        val user = currentUser.value
        if (user != null) {
            viewModelScope.launch {
                firestoreRepository.declineFriendRequest(user.uid, requestId)
            }
        }
    }

    // Curated Audio Summaries & Flashcards
    private val _audioSummaries = MutableStateFlow<List<AudioSummary>>(
        listOf(
            AudioSummary("a1", "الخوارزميات وهياكل البيانات", "ملخص توازن أشجار AVL و Red-Black Trees", 8, "موازنة الأشجار، الدوران الأيسر والأيمن، تعقيد O(log n)"),
            AudioSummary("a2", "أنظمة قواعد البيانات الموزعة", "مبدأ CAP Theorem واستراتيجيات التوزيع", 12, "الاتساق Consistency، التوافر Availability، تحمل التجزئة Partition Tolerance"),
            AudioSummary("a3", "الذكاء الاصطناعي وتعلم الآلة", "شرح دالي لدالة الخسارة و Backpropagation", 15, "الانحدار التدريجي، حساب الاشتقاقات الجزئية، تجنب Overfitting"),
            AudioSummary("a4", "مهارات القيادة والأكاديميا", "استراتيجيات تخطي التسويف الأكاديمي", 6, "قاعدة الخمس ثواني، تقسيم المهام الضخمة، قوة البدايات الصغيرة")
        )
    )
    val audioSummaries: StateFlow<List<AudioSummary>> = _audioSummaries.asStateFlow()

    private val _flashcards = MutableStateFlow<List<Flashcard>>(
        listOf(
            Flashcard("f1", "الخوارزميات", "ما هو الفرق الرئيسي بين BFS و DFS في استهلاك الذاكرة؟", "تعتمد BFS على Queue وتستهلك O(V) في أسوأ الأحوال، بينما DFS تعتمد على Stack وتستهلك O(h) بحسب عمق الشجرة.", 2),
            Flashcard("f2", "قواعد البيانات", "ما هو الفرق بين ACID و BASE؟", "ACID يضمن الدقة الصارمة في RDBMS، بينما BASE يركز على التوافر العالي والاتساق التدريجي في NoSQL.", 3),
            Flashcard("f3", "الذكاء الاصطناعي", "ما وظيفة Dropout Layer في الشبكات العصبية؟", "تعطيل عشوائي لنسبة من الخلايا أثناء التدريب لمنع فرط التخصيص Overfitting.", 1),
            Flashcard("f4", "الخوارزميات", "ما هو التعقيد الزمني لخوارزمية Dijkstra باستخدام Priority Queue؟", "يساوي O((V + E) log V).", 2)
        )
    )
    val flashcards: StateFlow<List<Flashcard>> = _flashcards.asStateFlow()

    // Sola Vision Engine & Concept Maps
    private val _conceptMaps = MutableStateFlow<List<ConceptMap>>(emptyList())
    val conceptMaps: StateFlow<List<ConceptMap>> = _conceptMaps.asStateFlow()

    fun saveVisionDeck(newFlashcards: List<Flashcard>, conceptMap: ConceptMap, xpReward: Int = 75) {
        _flashcards.value = _flashcards.value + newFlashcards
        _conceptMaps.value = _conceptMaps.value + conceptMap
        val user = currentUser.value
        if (user != null) {
            viewModelScope.launch { firestoreRepository.rewardSparksAndXp(user.uid, 15, xpReward) }
        } else if (_isGuestMode.value) {
            val current = _userProfile.value
            if (current != null) {
                _userProfile.value = current.copy(xp = current.xp + xpReward, sparks = current.sparks + 15)
            }
        }
        triggerGuideEmotion(SolaEmotionState.HAPPY_CELEBRATION, "تم استخراج البطاقات وخرائط المفاهيم! +$xpReward XP ⚡", "أضافت صولا ${newFlashcards.size} بطاقة تفاعلية لمقرر ${conceptMap.subjectName}.", "استعراض البطاقات", "STUDY")
    }

    // Sola AI State
    private val _solaMessages = MutableStateFlow<List<SolaMessage>>(
        listOf(SolaMessage(sender = "sola", text = "أهلاً بك في SCOPE! أنا صولا، رفيقتك الأكاديمية الذكية.", thinkingContent = "تم تهيئة السياق الأكاديمي."))
    )
    val solaMessages: StateFlow<List<SolaMessage>> = _solaMessages.asStateFlow()

    private val _isSolaThinking = MutableStateFlow(false)
    val isSolaThinking: StateFlow<Boolean> = _isSolaThinking.asStateFlow()

    private val _isHighThinkingMode = MutableStateFlow(true)
    val isHighThinkingMode: StateFlow<Boolean> = _isHighThinkingMode.asStateFlow()

    // Focus Timer State
    private val _timerSeconds = MutableStateFlow(25 * 60)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _selectedFocusSubject = MutableStateFlow<Subject?>(null)
    val selectedFocusSubject: StateFlow<Subject?> = _selectedFocusSubject.asStateFlow()

    private val _showReflectionDialog = MutableStateFlow(false)
    val showReflectionDialog: StateFlow<Boolean> = _showReflectionDialog.asStateFlow()

    private var timerJob: Job? = null

    // Sola Guide Engine & Mascot State
    private val _currentGuidePrompt = MutableStateFlow<SolaGuidePrompt?>(null)
    val currentGuidePrompt: StateFlow<SolaGuidePrompt?> = _currentGuidePrompt.asStateFlow()

    // Theme pack
    private val _enable3dBackgrounds = MutableStateFlow(true)
    val enable3dBackgrounds: StateFlow<Boolean> = _enable3dBackgrounds.asStateFlow()
    private val _selectedThemePackId = MutableStateFlow("cyber_academia")
    val selectedThemePackId: StateFlow<String> = _selectedThemePackId.asStateFlow()

    // Sound studio
    private val _isSoundStudioOpen = MutableStateFlow(false)
    val isSoundStudioOpen: StateFlow<Boolean> = _isSoundStudioOpen.asStateFlow()

    fun toggle3dBackgrounds(enabled: Boolean) { _enable3dBackgrounds.value = enabled }
    fun selectThemePack(packId: String) { _selectedThemePackId.value = packId }
    fun setSoundStudioOpen(open: Boolean) { _isSoundStudioOpen.value = open }

    // Circadian Energy
    val circadianBattery: StateFlow<CircadianBatteryStatus> = combine(_userProfile, _studySessions) { profile, sessions ->
        CircadianEnergyEngine.calculateEnergy(preferences = profile?.diagnosticPreferences, sessionsCompletedToday = sessions.size)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CircadianEnergyEngine.calculateEnergy(null))

    fun reorderTasksByEnergy() {
        val energy = circadianBattery.value.percentage
        _tasks.value = CircadianEnergyEngine.reorderTasks(_tasks.value, energy)
        triggerGuideEmotion(if (energy >= 80) SolaEmotionState.HAPPY_CELEBRATION else SolaEmotionState.THINKING_ANALYTICAL, "تمت إعادة ترتيب المهام حسب طاقتك ⚡", circadianBattery.value.state.adviceAr, "استعراض المهام", "PROJECTS")
    }

    fun syncLiveWidget(context: Context) {
        val profile = _userProfile.value
        val battery = circadianBattery.value.percentage
        ScopeWidgetManager.saveMetrics(context, profile?.flameCount ?: 3, profile?.sparks ?: 180, profile?.tokens ?: 25, battery, if (battery >= 80) "happy" else if (battery >= 40) "idle" else "thinking")
    }

    // Global Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _searchCategory = MutableStateFlow(SearchCategory.ALL)
    val searchCategory: StateFlow<SearchCategory> = _searchCategory.asStateFlow()

    val searchResults: StateFlow<List<SearchResultItem>> = combine(_searchQuery, _searchCategory, _subjects, _tasks) { q, cat, subjs, tsks ->
        val query = q.trim().lowercase()
        val allResults = mutableListOf<SearchResultItem>()
        if (cat == SearchCategory.ALL || cat == SearchCategory.SUBJECTS) {
            subjs.filter { query.isEmpty() || it.name.lowercase().contains(query) || it.code.lowercase().contains(query) }.forEach { s ->
                allResults.add(SearchResultItem("subj_${s.id}", "${s.name} (${s.code})", "المحاضر: ${s.instructor}", SearchCategory.SUBJECTS, s.code, s.colorHex, "STUDY"))
            }
        }
        if (cat == SearchCategory.ALL || cat == SearchCategory.TASKS) {
            tsks.filter { query.isEmpty() || it.title.lowercase().contains(query) }.forEach { t ->
                allResults.add(SearchResultItem("task_${t.id}", t.title, "مقرر ${t.subjectName}", SearchCategory.TASKS, if (t.isCompleted) "منجز" else "مطلوب", targetTab = "PROJECTS"))
            }
        }
        allResults
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun updateSearchCategory(category: SearchCategory) { _searchCategory.value = category }

    // Economy & Store
    fun purchaseStoreItem(item: ShopItem, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val user = _userProfile.value ?: return
        val uid = currentUser.value?.uid
        if (user.sparks < item.sparksPrice || user.tokens < item.tokensPrice) { onError("رصيد غير كافٍ!"); return }
        if (uid != null) {
            viewModelScope.launch {
                if (firestoreRepository.purchaseStoreItem(uid, item, user.sparks, user.tokens)) {
                    triggerGuideEmotion(SolaEmotionState.HAPPY_CELEBRATION, "مبروك التخصيص الجديد!", "تم فتح '${item.name}' بنجاح.", "عرض في الاستوديو", "PROFILE")
                    onSuccess()
                } else { onError("فشلت عملية الشراء.") }
            }
        } else if (_isGuestMode.value) {
            _userProfile.value = user.copy(
                sparks = user.sparks - item.sparksPrice,
                tokens = user.tokens - item.tokensPrice,
                unlockedItemIds = user.unlockedItemIds + item.id
            )
            equipItem(item.category, item)
            triggerGuideEmotion(SolaEmotionState.HAPPY_CELEBRATION, "مبروك التخصيص الجديد!", "تم فتح '${item.name}' بنجاح.", "عرض في الاستوديو", "PROFILE")
            onSuccess()
        }
    }

    fun equipItem(category: ShopCategory, item: ShopItem) {
        val uid = currentUser.value?.uid
        if (uid != null) {
            viewModelScope.launch { firestoreRepository.equipItem(uid, category, item.id, item.name) }
        }
        // Optimistically update local profile state immediately
        val p = _userProfile.value
        if (p != null) {
            _userProfile.value = when (category) {
                ShopCategory.FRAME -> p.copy(equippedFrame = item.id)
                ShopCategory.AURA -> p.copy(equippedAura = item.id)
                ShopCategory.TITLE -> p.copy(equippedTitle = item.name)
                ShopCategory.GLASS_FX -> p.copy(equippedGlassFx = item.id)
            }
        }
    }

    fun unequipItem(category: ShopCategory) {
        val uid = currentUser.value?.uid
        if (uid != null) {
            viewModelScope.launch { firestoreRepository.unequipItem(uid, category) }
        }
        val p = _userProfile.value
        if (p != null) {
            _userProfile.value = when (category) {
                ShopCategory.FRAME -> p.copy(equippedFrame = "")
                ShopCategory.AURA -> p.copy(equippedAura = "")
                ShopCategory.TITLE -> p.copy(equippedTitle = "")
                ShopCategory.GLASS_FX -> p.copy(equippedGlassFx = "")
            }
        }
    }

    // Diagnostic Survey
    fun saveDiagnosticSurvey(preferences: DiagnosticPreferences) {
        val current = _userProfile.value
        if (current != null) { _userProfile.value = current.copy(onboardingCompleted = true, diagnosticPreferences = preferences) }
        val uid = currentUser.value?.uid
        if (uid != null) { viewModelScope.launch { firestoreRepository.saveDiagnosticSurvey(uid, preferences) } }
        triggerGuideEmotion(SolaEmotionState.HAPPY_CELEBRATION, "تم ضبط شخصية صولا بنجاح!", "أهلاً بك في عالم التوجيه التحليلي المتوازن.", "ابدأ جلستك الأولى", "STUDY")
    }

    fun skipDiagnosticSurvey() {
        val current = _userProfile.value
        if (current != null) { _userProfile.value = current.copy(onboardingCompleted = true) }
        val uid = currentUser.value?.uid
        if (uid != null) { viewModelScope.launch { firestoreRepository.saveDiagnosticSurvey(uid, DiagnosticPreferences()) } }
    }

    fun reevaluateSolaGuide(justCompletedSession: Boolean = false) {
        _currentGuidePrompt.value = SolaGuideEngine.evaluateAcademicState(_tasks.value, _occurrences.value, _studySessions.value, _exams.value, _isSolaThinking.value, _isTimerRunning.value, justCompletedSession)
    }

    fun dismissGuidePrompt() { _currentGuidePrompt.value = null }
    fun triggerGuideEmotion(emotion: SolaEmotionState, title: String, message: String, actionLabel: String? = null, targetTab: String? = null) {
        _currentGuidePrompt.value = SolaGuidePrompt(emotion = emotion, title = title, message = message, actionLabel = actionLabel, actionTargetTab = targetTab)
    }

    // GPA & Exams
    private val _burnoutRadarStatus = MutableStateFlow(SolaExamAndBurnoutEngine.evaluateBurnoutRadar(emptyList(), emptyList(), emptyList()))
    val burnoutRadarStatus: StateFlow<BurnoutRadarStatus> = _burnoutRadarStatus.asStateFlow()
    private val _activeExamSimulation = MutableStateFlow<ExamSimulationSession?>(null)
    val activeExamSimulation: StateFlow<ExamSimulationSession?> = _activeExamSimulation.asStateFlow()
    private val _showBurnoutShield = MutableStateFlow(false)
    val showBurnoutShield: StateFlow<Boolean> = _showBurnoutShield.asStateFlow()
    private val _showGpaTimeMachine = MutableStateFlow(false)
    val showGpaTimeMachine: StateFlow<Boolean> = _showGpaTimeMachine.asStateFlow()

    fun updateBurnoutRadar() { _burnoutRadarStatus.value = SolaExamAndBurnoutEngine.evaluateBurnoutRadar(_studySessions.value, _exams.value, _tasks.value) }
    fun openGpaTimeMachine() { _showGpaTimeMachine.value = true }
    fun closeGpaTimeMachine() { _showGpaTimeMachine.value = false }
    fun openBurnoutShield() { updateBurnoutRadar(); _showBurnoutShield.value = true }
    fun closeBurnoutShield() { _showBurnoutShield.value = false }
    fun startExamSimulation(exam: Exam? = null) { _activeExamSimulation.value = SolaExamAndBurnoutEngine.generateExamQuiz(exam ?: _exams.value.firstOrNull(), _subjects.value, _flashcards.value) }
    fun closeExamSimulation() { _activeExamSimulation.value = null }

    fun completeExamSimulation(score: Int, total: Int, xpBonus: Int, sparksBonus: Int) {
        val uid = currentUser.value?.uid
        if (uid != null) { viewModelScope.launch { firestoreRepository.rewardSparksAndXp(uid, sparksBonus, xpBonus) } }
        else if (_isGuestMode.value) { _userProfile.value = _userProfile.value?.copy(xp = (_userProfile.value?.xp ?: 0) + xpBonus, sparks = (_userProfile.value?.sparks ?: 0) + sparksBonus) }
        triggerGuideEmotion(SolaEmotionState.HAPPY_CELEBRATION, "أحسنت! أتممت محاكاة الاختبار بنجاح 🎉", "حققت $score من $total أسئلة.", "عرض الجدول", "SCHEDULE")
    }

    fun claimBurnoutRestReward(sparksBonus: Int = 25, xpBonus: Int = 40) {
        val uid = currentUser.value?.uid
        if (uid != null) { viewModelScope.launch { firestoreRepository.rewardSparksAndXp(uid, sparksBonus, xpBonus) } }
        triggerGuideEmotion(SolaEmotionState.HAPPY_CELEBRATION, "تم استعادة النشاط الذهني!", "بطاريتك الآن مستعدة للعودة!", "بدء جلسة خفيفة", "STUDY")
    }

    fun saveGpaTarget(targetGpa: Double) {
        triggerGuideEmotion(SolaEmotionState.HAPPY_CELEBRATION, "تم تثبيت الهدف الأكاديمي: %.2f 🎯".format(targetGpa), "تم تحديث خارطة الطريق الأكاديمية.", "مراجعة المهام", "HOME")
    }

    // Guilds
    private val _activeGuild = MutableStateFlow<StudyGuild?>(StudyGuildsEngine.getSampleGuilds().first())
    val activeGuild: StateFlow<StudyGuild?> = _activeGuild.asStateFlow()
    private val _guildLeaderboard = MutableStateFlow(StudyGuildsEngine.getSampleGuilds())
    val guildLeaderboard: StateFlow<List<StudyGuild>> = _guildLeaderboard.asStateFlow()
    private val _guildActivityFeed = MutableStateFlow(StudyGuildsEngine.getSampleFeed(StudyGuildsEngine.getSampleGuilds().first().id))
    val guildActivityFeed: StateFlow<List<GuildActivityFeedItem>> = _guildActivityFeed.asStateFlow()
    private val _ghostFocusStatus = MutableStateFlow(GhostFocusStatus())
    val ghostFocusStatus: StateFlow<GhostFocusStatus> = _ghostFocusStatus.asStateFlow()
    private val _showStudyGuildsDialog = MutableStateFlow(false)
    val showStudyGuildsDialog: StateFlow<Boolean> = _showStudyGuildsDialog.asStateFlow()

    fun openStudyGuilds() { _showStudyGuildsDialog.value = true }
    fun closeStudyGuilds() { _showStudyGuildsDialog.value = false }
    fun createGuild(name: String, tag: String, description: String, emblem: String) {
        val newGuild = StudyGuildsEngine.createGuild(name, tag, description, emblem, "#2563EB", _userProfile.value)
        _activeGuild.value = newGuild
        _guildLeaderboard.value = listOf(newGuild) + _guildLeaderboard.value
    }

    fun joinGuildByCode(inviteCode: String) {
        StudyGuildsEngine.joinGuildByCode(inviteCode, _guildLeaderboard.value, _userProfile.value)?.let { _activeGuild.value = it }
    }

    fun contributeGuildSparks(sparks: Int = 25) {
        _activeGuild.value?.let { current ->
            val (updated, feed) = StudyGuildsEngine.contributeSparks(current, _userProfile.value?.displayName ?: "طالب", sparks)
            _activeGuild.value = updated
            _guildActivityFeed.value = listOf(feed) + _guildActivityFeed.value
        }
    }

    fun startGhostFocus(subjectName: String, durationMinutes: Int = 25) { _ghostFocusStatus.value = GhostFocusEngine.activateGhostFocus(subjectName, durationMinutes) }
    fun endGhostFocus(sparksEarned: Int = 15) {
        if (_ghostFocusStatus.value.isGhostFocusActive) {
            _guildActivityFeed.value = listOf(GhostFocusEngine.generatePostFocusBroadcast(_userProfile.value?.displayName ?: "طالب", _activeGuild.value?.id ?: "g1", _ghostFocusStatus.value.durationMinutes, sparksEarned)) + _guildActivityFeed.value
            _ghostFocusStatus.value = GhostFocusEngine.deactivateGhostFocus()
        }
    }

    // Projects
    private val _projects = MutableStateFlow(ProjectsHubEngine.getSampleProjects())
    val projects: StateFlow<List<ScopeProject>> = _projects.asStateFlow()
    private val _activeProject = MutableStateFlow<ScopeProject?>(ProjectsHubEngine.getSampleProjects().firstOrNull())
    val activeProject: StateFlow<ScopeProject?> = _activeProject.asStateFlow()
    private val _showProjectDetailModal = MutableStateFlow(false)
    val showProjectDetailModal: StateFlow<Boolean> = _showProjectDetailModal.asStateFlow()

    fun openProjectDetail(project: ScopeProject) { _activeProject.value = project; _showProjectDetailModal.value = true }
    fun closeProjectDetail() { _showProjectDetailModal.value = false }

    fun startProjectElection(title: String, candidateMemberIds: List<String>) {
        _activeProject.value?.let { current ->
            val candidates = current.members.filter { candidateMemberIds.contains(it.id) }
            val updated = ProjectsHubEngine.startElection(current, title, candidates, _userProfile.value?.displayName ?: "أنت")
            updateProjectInList(updated)
        }
    }

    fun castProjectVote(candidateMemberId: String) {
        _activeProject.value?.let { current ->
            val updated = ProjectsHubEngine.castVote(current, _userProfile.value?.uid ?: "m1", candidateMemberId, _userProfile.value?.displayName ?: "أنت")
            updateProjectInList(updated)
        }
    }

    fun addProjectFinancialTransaction(title: String, amountFiat: Double, category: String, isExpense: Boolean) {
        _activeProject.value?.let { current ->
            val updated = ProjectsHubEngine.addFinancialTransaction(current, title, amountFiat, category, _userProfile.value?.displayName ?: "أنت", isExpense)
            updateProjectInList(updated)
        }
    }

    fun poolProjectSparks(sparksAmount: Int, tokensAmount: Int) {
        _activeProject.value?.let { current ->
            val updated = ProjectsHubEngine.poolSparksAndTokens(current, sparksAmount, tokensAmount, _userProfile.value?.displayName ?: "أنت")
            updateProjectInList(updated)
        }
    }

    fun addProjectNote(title: String, content: String) {
        _activeProject.value?.let { current ->
            val updated = ProjectsHubEngine.addNote(current, title, content, _userProfile.value?.displayName ?: "أنت")
            updateProjectInList(updated)
        }
    }

    fun toggleProjectTask(taskId: String, isCompleted: Boolean) {
        _activeProject.value?.let { current ->
            val updated = current.copy(tasks = current.tasks.map { if (it.id == taskId) it.copy(isCompleted = isCompleted) else it })
            updateProjectInList(updated)
        }
    }

    fun addProjectTask(title: String) {
        _activeProject.value?.let { current ->
            val updated = current.copy(tasks = listOf(Task(title = title, isCompleted = false)) + current.tasks)
            updateProjectInList(updated)
        }
    }

    fun deleteProject(projectId: String) {
        _projects.value = _projects.value.filter { it.id != projectId }
        if (_activeProject.value?.id == projectId) {
            _activeProject.value = _projects.value.firstOrNull()
            _showProjectDetailModal.value = false
        }
        currentUser.value?.let { user ->
            viewModelScope.launch {
                firestoreRepository.deleteProject(user.uid, projectId)
            }
        }
    }

    fun deleteSubject(subjectId: String) {
        _subjects.value = _subjects.value.filter { it.id != subjectId }
        _tasks.value = _tasks.value.filter { it.subjectId != subjectId }
        _exams.value = _exams.value.filter { it.subjectId != subjectId }
        _occurrences.value = _occurrences.value.filter { it.subjectId != subjectId }

        currentUser.value?.let { user ->
            viewModelScope.launch {
                firestoreRepository.deleteSubject(user.uid, subjectId)
            }
        }
    }

    fun addRegisteredProjectMember(scopeIdOrEmail: String, customRoleTitle: String) {
        _activeProject.value?.let { current ->
            val updated = ProjectsHubEngine.addRegisteredMember(current, scopeIdOrEmail, customRoleTitle, _userProfile.value?.displayName ?: "أنت")
            updateProjectInList(updated)
        }
    }

    fun addExternalProjectMember(displayName: String, customRoleTitle: String) {
        _activeProject.value?.let { current ->
            val updated = ProjectsHubEngine.addExternalMember(current, displayName, customRoleTitle, _userProfile.value?.displayName ?: "أنت")
            updateProjectInList(updated)
        }
    }

    fun assignProjectMemberRole(memberId: String, newRole: com.example.data.projects.ProjectRole, customRoleTitle: String) {
        _activeProject.value?.let { current ->
            val updated = ProjectsHubEngine.assignMemberRole(current, memberId, newRole, customRoleTitle, _userProfile.value?.displayName ?: "أنت")
            updateProjectInList(updated)
        }
    }

    private fun updateProjectInList(updated: ScopeProject) { _activeProject.value = updated; _projects.value = _projects.value.map { if (it.id == updated.id) updated else it } }

    // Control Center
    private val _solaSpeechStyle = MutableStateFlow("DETAILED")
    val solaSpeechStyle: StateFlow<String> = _solaSpeechStyle.asStateFlow()
    private val _isHapticsEnabled = MutableStateFlow(true)
    val isHapticsEnabled: StateFlow<Boolean> = _isHapticsEnabled.asStateFlow()
    private val _isNotificationsSilenced = MutableStateFlow(false)
    val isNotificationsSilenced: StateFlow<Boolean> = _isNotificationsSilenced.asStateFlow()
    private val _isRestModeActive = MutableStateFlow(false)
    val isRestModeActive: StateFlow<Boolean> = _isRestModeActive.asStateFlow()
    private val _showSolaPersonalityModal = MutableStateFlow(false)
    val showSolaPersonalityModal: StateFlow<Boolean> = _showSolaPersonalityModal.asStateFlow()
    private val _showScopeJourneyModal = MutableStateFlow(false)
    val showScopeJourneyModal: StateFlow<Boolean> = _showScopeJourneyModal.asStateFlow()

    fun setSolaSpeechStyle(style: String) { _solaSpeechStyle.value = style }
    fun toggleHaptics(enabled: Boolean) { _isHapticsEnabled.value = enabled }
    fun toggleNotificationsSilencer(silenced: Boolean) { _isNotificationsSilenced.value = silenced }
    fun toggleRestMode(active: Boolean) { _isRestModeActive.value = active }
    fun openSolaPersonalityModal() { _showSolaPersonalityModal.value = true }
    fun closeSolaPersonalityModal() { _showSolaPersonalityModal.value = false }
    fun openScopeJourneyModal() { _showScopeJourneyModal.value = true }
    fun closeScopeJourneyModal() { _showScopeJourneyModal.value = false }
    fun clearSolaMemoryCache() { _solaMessages.value = listOf(SolaMessage(sender = "sola", text = "تم تنظيف الذاكرة.", thinkingContent = "إعادة ضبط.")) }

    // Feedback
    private val _feedbacks = MutableStateFlow<List<FeedbackItem>>(emptyList())
    val feedbacks: StateFlow<List<FeedbackItem>> = _feedbacks.asStateFlow()
    private val _devCommunityToolsEnabled = MutableStateFlow(true)
    val devCommunityToolsEnabled: StateFlow<Boolean> = _devCommunityToolsEnabled.asStateFlow()
    private val _showUniversalSearchModal = MutableStateFlow(false)
    val showUniversalSearchModal: StateFlow<Boolean> = _showUniversalSearchModal.asStateFlow()
    private val _showNotificationCenter = MutableStateFlow(false)
    val showNotificationCenter: StateFlow<Boolean> = _showNotificationCenter.asStateFlow()
    private val _showScopeListensModal = MutableStateFlow(false)
    val showScopeListensModal: StateFlow<Boolean> = _showScopeListensModal.asStateFlow()
    private val _mutedChannelIds = MutableStateFlow<Set<String>>(emptySet())
    val mutedChannelIds: StateFlow<Set<String>> = _mutedChannelIds.asStateFlow()
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    fun submitFeedback(type: String, content: String, category: String) {
        val user = _userProfile.value
        _feedbacks.value = listOf(FeedbackItem(senderUid = user?.uid ?: "guest", senderName = user?.displayName ?: "طالب", senderScopeId = user?.scopeId ?: "@guest", feedbackType = type, content = content, category = category)) + _feedbacks.value
    }

    fun rewardAndResolveFeedback(feedbackId: String, sparks: Int = 100, tokens: Int = 10) {
        _feedbacks.value = _feedbacks.value.map { if (it.id == feedbackId) it.copy(status = "REWARDED", rewardedSparks = sparks, rewardedTokens = tokens) else it }
        grantDevSparksAndTokens(sparks, tokens, false)
    }

    fun toggleDevCommunityTools(enabled: Boolean) { _devCommunityToolsEnabled.value = enabled }
    fun openUniversalSearchModal() { _showUniversalSearchModal.value = true }
    fun closeUniversalSearchModal() { _showUniversalSearchModal.value = false }
    fun openNotificationCenter() { _showNotificationCenter.value = true }
    fun closeNotificationCenter() { _showNotificationCenter.value = false }
    fun markAllNotificationsAsRead() { _notifications.value = _notifications.value.map { it.copy(isRead = true) } }
    fun toggleNotificationChannelMute(channelId: String) { val current = _mutedChannelIds.value.toMutableSet(); if (current.contains(channelId)) current.remove(channelId) else current.add(channelId); _mutedChannelIds.value = current }
    fun openScopeListensModal() { _showScopeListensModal.value = true }
    fun closeScopeListensModal() { _showScopeListensModal.value = false }
    fun updateDevAnalytics(analytics: DevLiveAnalytics) { }
    fun grantDevSparksAndTokens(sparks: Int, tokens: Int, makePro: Boolean) { currentUser.value?.let { user -> viewModelScope.launch { firestoreRepository.grantDevSparksAndTokens(user.uid, sparks, tokens, makePro) } } }
    fun addCommunityChallenge(challenge: CommunityChallenge) { viewModelScope.launch { firestoreRepository.addCommunityChallenge(challenge) } }

    fun getActionableRecommendation(): Recommendation {
        val now = System.currentTimeMillis()

        // Priority 0: Active Personal Task
        val activePersonalTask = _tasks.value.firstOrNull { !it.isCompleted && it.isPersonal }
        if (activePersonalTask != null) {
            return Recommendation(
                title = "وقت شخصي: ${activePersonalTask.title} ☕",
                explanation = "أنت الآن في موعد أو مهمة شخصية (${activePersonalTask.personalCategory}). تمت تهدئة التنبيهات الدراسية لحين انتهائك.",
                urgency = "شخصي"
            )
        }
        
        // Priority 1: Upcoming Exam within 3 days
        val upcomingExam = _exams.value.firstOrNull { it.examTimestamp - now in 0..(3 * 24 * 3600 * 1000L) }
        if (upcomingExam != null) {
            return Recommendation(
                title = "الاستعداد لاختبار ${upcomingExam.title}",
                explanation = "لديك اختبار قادم خلال أقل من 3 أيام لمقرر ${upcomingExam.subjectName}. استغل وقتك في مراجعة البطاقات التعليمية وحل المحاكاة.",
                targetSubjectId = upcomingExam.subjectId,
                urgency = "عالي جداً 🔥"
            )
        }

        // Priority 2: High Priority Task
        val highTask = _tasks.value.firstOrNull { !it.isCompleted && it.priority == TaskPriority.HIGH }
        if (highTask != null) {
            return Recommendation(
                title = "إنجاز: ${highTask.title}",
                explanation = "هذه المهمة ذات أولوية عالية لمقرر ${highTask.subjectName}. البدء بها الآن سيقلل الضغط الأكاديمي لاحقاً.",
                targetTaskId = highTask.id,
                urgency = "عالي"
            )
        }

        // Priority 3: Low energy battery (Advice)
        val battery = circadianBattery.value
        if (battery.percentage < 40) {
            return Recommendation(
                title = "استراحة قصيرة مجدولة",
                explanation = "طاقتك الذهنية حالياً منخفضة بحسب تحليل صولا. خذ استراحة 15 دقيقة بعيداً عن الشاشات لاستعادة التركيز.",
                urgency = "توجيهي"
            )
        }

        return Recommendation(
            title = "بدء جلسة تركيز ذكية",
            explanation = "أهلاً بك في يومك الدراسي! اختر مادة من موادك وابدأ أول جلسة بومودورو لليوم لزيادة الزخم.",
            urgency = "عادي"
        )
    }

    // Auth
    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()
    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()
    private val _signInError = MutableStateFlow<String?>(null)
    val signInError: StateFlow<String?> = _signInError.asStateFlow()

    fun continueAsGuest() {
        _isGuestMode.value = true
        _userProfile.value = UserProfile(
            uid = "guest_user",
            displayName = "زائر SCOPE",
            scopeId = "@guest",
            academicMajor = "تصفح استكشافي",
            equippedTitle = "طالب مستكشف 🚀"
        )
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _isSigningIn.value = true
            _signInError.value = null
            try {
                val result = withTimeoutOrNull(10_000L) {
                    authRepository.signInWithGoogle(context)
                }
                if (result == null) {
                    _signInError.value = "تعذر الاتصال بالخادم، تحقق من الشبكة وانقر لإعادة المحاولة"
                } else if (result.isFailure) {
                    val ex = result.exceptionOrNull()
                    if (ex !is androidx.credentials.exceptions.GetCredentialCancellationException) {
                        _signInError.value = ex?.message ?: "تعذر الاتصال بالخادم، تحقق من الشبكة"
                    }
                }
            } catch (e: Exception) {
                _signInError.value = e.message ?: "تعذر الاتصال بالخادم، تحقق من الشبكة"
            } finally {
                _isSigningIn.value = false
            }
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _isSigningIn.value = true
            _signInError.value = null
            try {
                val result = withTimeoutOrNull(10_000L) {
                    authRepository.signInWithEmail(email, pass)
                }
                if (result == null) {
                    _signInError.value = "تعذر الاتصال بالخادم، تحقق من الشبكة"
                } else if (result.isFailure) {
                    _signInError.value = result.exceptionOrNull()?.message ?: "فشل تسجيل الدخول، تحقق من البريد وكلمة المرور"
                }
            } catch (e: Exception) {
                _signInError.value = e.message ?: "تعذر الاتصال بالخادم، تحقق من الشبكة"
            } finally {
                _isSigningIn.value = false
            }
        }
    }

    fun signUpWithEmail(
        email: String,
        pass: String,
        name: String,
        scopeId: String,
        uni: String,
        major: String,
        year: String,
        phone: String
    ) {
        viewModelScope.launch {
            _isSigningIn.value = true
            _signInError.value = null
            try {
                val result = withTimeoutOrNull(10_000L) {
                    authRepository.signUpWithEmail(email, pass, name)
                }
                if (result == null) {
                    _signInError.value = "تعذر الاتصال بالخادم، تحقق من الشبكة"
                } else if (result.isFailure) {
                    _signInError.value = result.exceptionOrNull()?.message ?: "فشل إنشاء الحساب، يرجى المحاولة لاحقاً"
                }
            } catch (e: Exception) {
                _signInError.value = e.message ?: "تعذر الاتصال بالخادم، تحقق من الشبكة"
            } finally {
                _isSigningIn.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _userProfile.value = null
            _isGuestMode.value = false
        }
    }

    fun sendPasswordReset(email: String, onResult: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val result = withTimeoutOrNull(10_000L) {
                    authRepository.sendPasswordReset(email)
                }
                if (result != null && result.isSuccess) {
                    onResult?.invoke(true, "تم إرسال رابط استعادة كلمة المرور إلى بريدك الإلكتروني بنجاح 📧")
                } else {
                    val msg = result?.exceptionOrNull()?.message ?: "تعذر الاتصال بالخادم، تحقق من الشبكة"
                    onResult?.invoke(false, msg)
                }
            } catch (e: Exception) {
                onResult?.invoke(false, e.message ?: "تعذر الاتصال بالخادم، تحقق من الشبكة")
            }
        }
    }

    fun updateProfile(
        displayName: String,
        bio: String,
        academicMajor: String,
        academicYear: String,
        avatarId: String,
        photoUrl: String,
        newPassword: String? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val current = _userProfile.value
                val isDev = com.example.data.model.isSuperDeveloperEmail(current?.email)
                val updated = (current ?: UserProfile()).copy(
                    displayName = displayName.ifBlank { current?.displayName ?: "طالب SCOPE" },
                    customStatus = bio.ifBlank { current?.customStatus ?: "طالب SCOPE طموح 🚀" },
                    academicMajor = academicMajor.ifBlank { current?.academicMajor ?: "هندسة البرمجيات والذكاء الاصطناعي" },
                    academicYear = academicYear.ifBlank { current?.academicYear ?: "السنة الثالثة" },
                    avatarId = avatarId.ifBlank { current?.avatarId ?: "avatar_scholar_owl" },
                    photoUrl = photoUrl,
                    equippedTitle = if (isDev) "المطور الأسمى 👑" else (current?.equippedTitle?.ifBlank { "مهندس الخوارزميات" } ?: "مهندس الخوارزميات"),
                    isDeveloper = isDev || (current?.isDeveloper == true)
                )
                _userProfile.value = updated

                val uid = currentUser.value?.uid
                if (uid != null) {
                    firestoreRepository.updateUserProfileFields(
                        userId = uid,
                        displayName = updated.displayName,
                        customStatus = updated.customStatus,
                        academicMajor = updated.academicMajor,
                        academicYear = updated.academicYear,
                        avatarId = updated.avatarId,
                        photoUrl = updated.photoUrl
                    )

                    if (!newPassword.isNullOrBlank()) {
                        val passRes = authRepository.updatePassword(newPassword)
                        if (passRes.isFailure) {
                            onError(passRes.exceptionOrNull()?.message ?: "تعذر تغيير كلمة المرور")
                            return@launch
                        }
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "تعذر حفظ التعديلات، تحقق من الشبكة")
            }
        }
    }

    fun lookupEmailByIdentifier(identifier: String, onResult: (String?) -> Unit) { viewModelScope.launch { onResult(firestoreRepository.lookupEmailByScopeIdOrPhone(identifier)) } }

    // Academic
    fun updateAvatar(avatarId: String, photoUrl: String = "") {
        currentUser.value?.let { user ->
            viewModelScope.launch { firestoreRepository.updateAvatar(user.uid, avatarId, photoUrl) }
        } ?: run {
            if (_isGuestMode.value) {
                _userProfile.value = _userProfile.value?.copy(avatarId = avatarId, photoUrl = photoUrl)
            }
        }
    }

    fun toggleFocusShield(enabled: Boolean) {
        currentUser.value?.let { user ->
            viewModelScope.launch { firestoreRepository.updateFocusShield(user.uid, enabled) }
        } ?: run {
            if (_isGuestMode.value) {
                _userProfile.value = _userProfile.value?.copy(focusShieldEnabled = enabled)
            }
        }
    }

    fun addNewSubject(name: String, code: String, colorHex: String, instructor: String) {
        currentUser.value?.let { user ->
            viewModelScope.launch { firestoreRepository.addSubject(user.uid, Subject(name = name, code = code, colorHex = colorHex, instructor = instructor)) }
        } ?: run {
            if (_isGuestMode.value) {
                _subjects.value = _subjects.value + Subject(id = java.util.UUID.randomUUID().toString(), name = name, code = code, colorHex = colorHex, instructor = instructor)
            }
        }
    }

    fun addNewTask(
        title: String,
        subject: Subject?,
        priority: TaskPriority = TaskPriority.MEDIUM,
        effortMinutes: Int = 45,
        isPersonal: Boolean = false,
        personalCategory: String = "شخصي"
    ) {
        val newTask = Task(
            title = title,
            subjectId = subject?.id ?: "",
            subjectName = if (isPersonal) "موعد/مهمة شخصية 🏠" else (subject?.name ?: "مهمة عامة"),
            priority = priority,
            effortMinutes = effortMinutes,
            isPersonal = isPersonal,
            personalCategory = personalCategory
        )
        currentUser.value?.let { user ->
            viewModelScope.launch { firestoreRepository.addTask(user.uid, newTask) }
        } ?: run {
            if (_isGuestMode.value) {
                _tasks.value = listOf(newTask.copy(id = java.util.UUID.randomUUID().toString())) + _tasks.value
            }
        }
    }

    fun transferGiftingToRecipient(
        recipientIdentifier: String,
        sparks: Int,
        tokens: Int,
        grantPro: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        val user = _userProfile.value
        val uid = currentUser.value?.uid
        if (user == null || uid == null) {
            onResult(false, "يجب تسجيل الدخول لاستخدام نظام الإهداء")
            return
        }

        viewModelScope.launch {
            val res = firestoreRepository.transferGiftingSparksAndTokens(
                senderUid = uid,
                senderProfile = user,
                recipientIdentifier = recipientIdentifier,
                sparks = sparks,
                tokens = tokens,
                grantPro = grantPro
            )
            onResult(res.first, res.second)
        }
    }

    fun toggleTask(taskId: String, currentStatus: Boolean) {
        currentUser.value?.let { user ->
            viewModelScope.launch { firestoreRepository.toggleTaskCompletion(user.uid, taskId, currentStatus) }
        } ?: run {
            if (_isGuestMode.value) {
                _tasks.value = _tasks.value.map { if (it.id == taskId) it.copy(isCompleted = !currentStatus) else it }
            }
        }
    }

    fun addNewClassOccurrence(subject: Subject, dayOfWeek: Int, start: String, end: String, room: String) {
        currentUser.value?.let { user ->
            viewModelScope.launch { firestoreRepository.addClassOccurrence(user.uid, ClassOccurrence(subjectId = subject.id, subjectName = subject.name, dayOfWeek = dayOfWeek, startTime = start, endTime = end, room = room)) }
        } ?: run {
            if (_isGuestMode.value) {
                _occurrences.value = _occurrences.value + ClassOccurrence(id = java.util.UUID.randomUUID().toString(), subjectId = subject.id, subjectName = subject.name, dayOfWeek = dayOfWeek, startTime = start, endTime = end, room = room)
            }
        }
    }

    // Timer
    fun startTimer() { if (_isTimerRunning.value) return; _isTimerRunning.value = true; timerJob = viewModelScope.launch { while (_timerSeconds.value > 0 && _isTimerRunning.value) { delay(1000); _timerSeconds.value -= 1 }; if (_timerSeconds.value == 0) { _isTimerRunning.value = false; _showReflectionDialog.value = true } } }
    fun pauseTimer() { _isTimerRunning.value = false; timerJob?.cancel() }
    fun resetTimer(seconds: Int = 25 * 60) { pauseTimer(); _timerSeconds.value = seconds }
    fun selectFocusSubject(subject: Subject?) { _selectedFocusSubject.value = subject }
    fun saveSessionReflection(signal: LearningSignal, notes: String) {
        val user = currentUser.value
        val session = StudySession(subjectId = _selectedFocusSubject.value?.id ?: "unknown", subjectName = _selectedFocusSubject.value?.name ?: "مقرر عام", durationMinutes = 25, reflectionSignal = signal, reflectionNotes = notes)
        if (user != null) { viewModelScope.launch { firestoreRepository.recordStudySession(user.uid, session) } }
        else if (_isGuestMode.value) { _studySessions.value = listOf(session.copy(id = java.util.UUID.randomUUID().toString())) + _studySessions.value }
        _showReflectionDialog.value = false
    }
    fun dismissReflectionDialog() { _showReflectionDialog.value = false }

    // AI
    fun sendSolaQuery(text: String) {
        if (text.isBlank()) return
        val history = _solaMessages.value.map { it.sender to it.text }
        _solaMessages.value = _solaMessages.value + SolaMessage(sender = "user", text = text)
        viewModelScope.launch {
            _isSolaThinking.value = true
            val response = solaAiService.generateSolaResponse(text, history, _subjects.value, _tasks.value, _exams.value, _isHighThinkingMode.value)
            _solaMessages.value = _solaMessages.value + SolaMessage(sender = "sola", text = response.replyText, thinkingContent = response.thinkingText ?: "")
            _isSolaThinking.value = false
        }
    }
    fun toggleHighThinkingMode() { _isHighThinkingMode.value = !_isHighThinkingMode.value }

    fun calculateAvailableWindows(): List<ScheduleWindow> { return listOf(ScheduleWindow("نافذة تركيز صباحية", "08:00 - 09:30", 90, true), ScheduleWindow("وقت دراسة حر", "14:00 - 15:00", 60, false)) }

    fun addDocumentFile(
        subjectId: String,
        fileName: String,
        alias: String,
        fileType: String,
        category: String,
        isPinned: Boolean = false,
        uriString: String = ""
    ) {
        val targetSubject = if (subjectId.isNotBlank()) {
            _subjects.value.find { it.id == subjectId }
        } else {
            _subjects.value.find { it.code == "GEN" || it.name.contains("عام") }
                ?: _subjects.value.firstOrNull()
        }

        val resolvedSub = if (targetSubject != null) {
            targetSubject
        } else {
            val generalSub = Subject(
                id = java.util.UUID.randomUUID().toString(),
                name = "المستندات العامة 📂",
                code = "GEN",
                colorHex = "#6366F1",
                instructor = "عام"
            )
            currentUser.value?.let { user ->
                viewModelScope.launch { firestoreRepository.addSubject(user.uid, generalSub) }
            } ?: run {
                _subjects.value = _subjects.value + generalSub
            }
            generalSub
        }

        val newFile = SubjectFile(
            id = java.util.UUID.randomUUID().toString(),
            fileName = fileName,
            customAlias = if (alias.isNotBlank()) alias else fileName,
            fileType = fileType,
            category = category,
            isPinned = isPinned,
            subjectId = resolvedSub.id,
            subjectName = resolvedSub.name,
            uriString = uriString,
            timestamp = System.currentTimeMillis()
        )

        val updatedFiles = resolvedSub.files + newFile
        _subjects.value = _subjects.value.map { if (it.id == resolvedSub.id) it.copy(files = updatedFiles) else it }
        currentUser.value?.let { user ->
            viewModelScope.launch { firestoreRepository.updateSubjectFiles(user.uid, resolvedSub.id, updatedFiles) }
        }
    }

    fun addSubjectFile(subjectId: String, fileName: String, alias: String, fileType: String, category: String) {
        addDocumentFile(subjectId, fileName, alias, fileType, category, false, "")
    }

    fun renameSubjectFile(subjectId: String, fileId: String, newAlias: String) {
        val currentSub = _subjects.value.find { it.id == subjectId } ?: return
        val updatedFiles = currentSub.files.map { if (it.id == fileId) it.copy(customAlias = newAlias) else it }
        _subjects.value = _subjects.value.map { if (it.id == subjectId) it.copy(files = updatedFiles) else it }
        currentUser.value?.let { user ->
            viewModelScope.launch { firestoreRepository.updateSubjectFiles(user.uid, subjectId, updatedFiles) }
        }
    }

    fun togglePinFile(subjectId: String, fileId: String) {
        val currentSub = _subjects.value.find { it.id == subjectId } ?: return
        val updatedFiles = currentSub.files.map { if (it.id == fileId) it.copy(isPinned = !it.isPinned) else it }
        _subjects.value = _subjects.value.map { if (it.id == subjectId) it.copy(files = updatedFiles) else it }
        currentUser.value?.let { user ->
            viewModelScope.launch { firestoreRepository.updateSubjectFiles(user.uid, subjectId, updatedFiles) }
        }
    }

    fun deleteSubjectFile(subjectId: String, fileId: String) {
        val currentSub = _subjects.value.find { it.id == subjectId } ?: return
        val updatedFiles = currentSub.files.filter { it.id != fileId }
        _subjects.value = _subjects.value.map { if (it.id == subjectId) it.copy(files = updatedFiles) else it }
        currentUser.value?.let { user ->
            viewModelScope.launch { firestoreRepository.updateSubjectFiles(user.uid, subjectId, updatedFiles) }
        }
    }

    fun moveSubjectFile(fromSubjectId: String, toSubjectId: String, fileId: String) {
        if (fromSubjectId == toSubjectId) return
        val sourceSub = _subjects.value.find { it.id == fromSubjectId } ?: return
        val targetSub = _subjects.value.find { it.id == toSubjectId } ?: return
        val fileToMove = sourceSub.files.find { it.id == fileId } ?: return

        val updatedSourceFiles = sourceSub.files.filter { it.id != fileId }
        val updatedTargetFiles = targetSub.files + fileToMove.copy(subjectId = toSubjectId, subjectName = targetSub.name)

        _subjects.value = _subjects.value.map {
            when (it.id) {
                fromSubjectId -> it.copy(files = updatedSourceFiles)
                toSubjectId -> it.copy(files = updatedTargetFiles)
                else -> it
            }
        }
        currentUser.value?.let { user ->
            viewModelScope.launch {
                firestoreRepository.updateSubjectFiles(user.uid, fromSubjectId, updatedSourceFiles)
                firestoreRepository.updateSubjectFiles(user.uid, toSubjectId, updatedTargetFiles)
            }
        }
    }

    fun wipeData() {
        currentUser.value?.let { user ->
            viewModelScope.launch {
                firestoreRepository.wipeAcademicData(user.uid)
            }
        } ?: run {
            if (_isGuestMode.value) {
                _subjects.value = emptyList()
                _tasks.value = emptyList()
                _exams.value = emptyList()
                _studySessions.value = emptyList()
                _occurrences.value = emptyList()
            }
        }
    }
}
