package com.example.data.repository

import android.util.Log
import com.example.data.model.ClassOccurrence
import com.example.data.model.DiagnosticPreferences
import com.example.data.model.Exam
import com.example.data.model.LearningSignal
import com.example.data.model.OccurrenceStatus
import com.example.data.model.ShopCategory
import com.example.data.model.ShopItem
import com.example.data.model.StudySession
import com.example.data.model.Subject
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val localDatabase: com.example.data.local.ScopeDatabase? = null
) {
    private fun userDoc(userId: String) = firestore.collection("users").document(userId)

    fun getUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val subscription = userDoc(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                trySend(profile)
            } else {
                trySend(null)
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        userDoc(profile.uid).set(profile).await()
    }

    suspend fun updateFocusShield(userId: String, enabled: Boolean) {
        userDoc(userId).update("focusShieldEnabled", enabled).await()
    }

    suspend fun updateLanguage(userId: String, language: String) {
        userDoc(userId).update("language", language).await()
    }

    suspend fun saveDiagnosticSurvey(userId: String, preferences: DiagnosticPreferences) {
        userDoc(userId).update(
            mapOf(
                "onboardingCompleted" to true,
                "diagnosticPreferences" to preferences,
                "tokens" to FieldValue.increment(10), // Reward 10 Sola Tokens for completing onboarding
                "xp" to FieldValue.increment(100)
            )
        ).await()
    }

    suspend fun rewardSparksAndXp(userId: String, sparks: Int, xp: Int) {
        userDoc(userId).update(
            mapOf(
                "sparks" to FieldValue.increment(sparks.toLong()),
                "xp" to FieldValue.increment(xp.toLong())
            )
        ).await()
    }

    suspend fun purchaseStoreItem(userId: String, item: ShopItem, currentSparks: Int, currentTokens: Int): Boolean {
        if (currentSparks < item.sparksPrice || currentTokens < item.tokensPrice) {
            return false
        }
        val updates = mutableMapOf<String, Any>(
            "sparks" to FieldValue.increment(-item.sparksPrice.toLong()),
            "tokens" to FieldValue.increment(-item.tokensPrice.toLong()),
            "unlockedItemIds" to FieldValue.arrayUnion(item.id)
        )
        // Automatically equip purchased item
        when (item.category) {
            ShopCategory.FRAME -> updates["equippedFrame"] = item.id
            ShopCategory.AURA -> updates["equippedAura"] = item.id
            ShopCategory.TITLE -> updates["equippedTitle"] = item.name
            ShopCategory.GLASS_FX -> updates["equippedGlassFx"] = item.id
        }
        userDoc(userId).update(updates).await()
        return true
    }

    suspend fun equipItem(userId: String, category: ShopCategory, itemId: String, itemName: String) {
        val field = when (category) {
            ShopCategory.FRAME -> "equippedFrame" to itemId
            ShopCategory.AURA -> "equippedAura" to itemId
            ShopCategory.TITLE -> "equippedTitle" to itemName
            ShopCategory.GLASS_FX -> "equippedGlassFx" to itemId
        }
        userDoc(userId).update(field.first, field.second).await()
    }

    suspend fun unequipItem(userId: String, category: ShopCategory) {
        val field = when (category) {
            ShopCategory.FRAME -> "equippedFrame" to ""
            ShopCategory.AURA -> "equippedAura" to ""
            ShopCategory.TITLE -> "equippedTitle" to ""
            ShopCategory.GLASS_FX -> "equippedGlassFx" to ""
        }
        userDoc(userId).update(field.first, field.second).await()
    }

    suspend fun updateUserProfileFields(
        userId: String,
        displayName: String,
        customStatus: String,
        academicMajor: String,
        academicYear: String,
        avatarId: String,
        photoUrl: String
    ) {
        userDoc(userId).update(
            mapOf(
                "displayName" to displayName,
                "customStatus" to customStatus,
                "academicMajor" to academicMajor,
                "academicYear" to academicYear,
                "avatarId" to avatarId,
                "photoUrl" to photoUrl
            )
        ).await()
    }

    fun getSubjects(userId: String): Flow<List<Subject>> = callbackFlow {
        val subscription = userDoc(userId).collection("subjects")
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.toObject(Subject::class.java)?.copy(id = it.id) } ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addSubject(userId: String, subject: Subject): String {
        val doc = userDoc(userId).collection("subjects").document()
        doc.set(subject.copy(id = doc.id)).await()
        return doc.id
    }

    suspend fun deleteSubject(userId: String, subjectId: String) {
        try {
            // Delete subject doc
            userDoc(userId).collection("subjects").document(subjectId).delete().await()

            // Purge all tasks linked to this subject
            val tasksSnapshot = userDoc(userId).collection("tasks").whereEqualTo("subjectId", subjectId).get().await()
            for (doc in tasksSnapshot.documents) {
                doc.reference.delete().await()
            }

            // Purge all exams linked to this subject
            val examsSnapshot = userDoc(userId).collection("exams").whereEqualTo("subjectId", subjectId).get().await()
            for (doc in examsSnapshot.documents) {
                doc.reference.delete().await()
            }

            // Purge all occurrences linked to this subject
            val occSnapshot = userDoc(userId).collection("occurrences").whereEqualTo("subjectId", subjectId).get().await()
            for (doc in occSnapshot.documents) {
                doc.reference.delete().await()
            }
        } catch (_: Exception) {}
    }

    fun getProjects(userId: String): Flow<List<com.example.data.projects.ScopeProject>> = callbackFlow {
        val subscription = userDoc(userId).collection("projects")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(com.example.data.projects.ScopeProject::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun saveProject(userId: String, project: com.example.data.projects.ScopeProject) {
        val doc = if (project.id.isNotBlank()) {
            userDoc(userId).collection("projects").document(project.id)
        } else {
            userDoc(userId).collection("projects").document()
        }
        val target = if (project.id.isBlank()) project.copy(id = doc.id) else project
        doc.set(target).await()
    }

    suspend fun deleteProject(userId: String, projectId: String) {
        try {
            userDoc(userId).collection("projects").document(projectId).delete().await()
            firestore.collection("projects").document(projectId).delete().await()
        } catch (_: Exception) {}
    }

    suspend fun updateSubjectFiles(userId: String, subjectId: String, files: List<com.example.data.model.SubjectFile>) {
        userDoc(userId).collection("subjects").document(subjectId).update("files", files).await()
    }

    fun getTasks(userId: String): Flow<List<Task>> = callbackFlow {
        val subscription = userDoc(userId).collection("tasks")
            .orderBy("dueTimestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.toObject(Task::class.java)?.copy(id = it.id) } ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addTask(userId: String, task: Task) {
        val doc = userDoc(userId).collection("tasks").document()
        doc.set(task.copy(id = doc.id)).await()
    }

    suspend fun toggleTaskCompletion(userId: String, taskId: String, currentStatus: Boolean) {
        userDoc(userId).collection("tasks").document(taskId)
            .update("isCompleted", !currentStatus).await()
        if (!currentStatus) {
            // Completed a task: reward 15 Focus Sparks & 30 XP
            rewardSparksAndXp(userId, 15, 30)
        }
    }

    suspend fun deleteTask(userId: String, taskId: String) {
        userDoc(userId).collection("tasks").document(taskId).delete().await()
    }

    fun getExams(userId: String): Flow<List<Exam>> = callbackFlow {
        val subscription = userDoc(userId).collection("exams")
            .orderBy("examTimestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.toObject(Exam::class.java)?.copy(id = it.id) } ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addExam(userId: String, exam: Exam) {
        val doc = userDoc(userId).collection("exams").document()
        doc.set(exam.copy(id = doc.id)).await()
    }

    suspend fun deleteExam(userId: String, examId: String) {
        userDoc(userId).collection("exams").document(examId).delete().await()
    }

    fun getStudySessions(userId: String): Flow<List<StudySession>> = callbackFlow {
        val subscription = userDoc(userId).collection("study_sessions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.toObject(StudySession::class.java)?.copy(id = it.id) } ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun recordStudySession(userId: String, session: StudySession) {
        val doc = userDoc(userId).collection("study_sessions").document()
        doc.set(session.copy(id = doc.id)).await()
        // Focus session completed: reward 25 Focus Sparks & 50 XP
        rewardSparksAndXp(userId, 25, 50)
    }

    fun getClassOccurrences(userId: String): Flow<List<ClassOccurrence>> = callbackFlow {
        val subscription = userDoc(userId).collection("occurrences")
            .orderBy("dayOfWeek", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.toObject(ClassOccurrence::class.java)?.copy(id = it.id) } ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addClassOccurrence(userId: String, occurrence: ClassOccurrence) {
        val doc = userDoc(userId).collection("occurrences").document()
        doc.set(occurrence.copy(id = doc.id)).await()
    }

    suspend fun seedInitialAcademicDataIfEmpty(userId: String, displayName: String, email: String) {
        val subjectsSnap = userDoc(userId).collection("subjects").limit(1).get().await()
        if (subjectsSnap.isEmpty) {
            val isDev = com.example.data.model.isSuperDeveloperEmail(email)
            // Seed Profile with starter economy and diagnostic defaults
            val profile = UserProfile(
                uid = userId,
                email = email,
                displayName = displayName.ifBlank { if (isDev) "المطور إسماعيل 👑" else "طالب SCOPE" },
                scopeId = if (isDev) "@ismail_dev" else "@" + (displayName.filter { it.isLetterOrDigit() }.lowercase().ifBlank { "student" }) + "_" + (1000..9999).random(),
                academicMajor = if (isDev) "هندسة النظم الفائقة والذكاء الاصطناعي (Super Dev)" else "هندسة البرمجيات والذكاء الاصطناعي",
                university = if (isDev) "SCOPE Global Headquarters" else "جامعة الملك سعود",
                academicYear = if (isDev) "Lead System Architect" else "السنة الثالثة",
                language = "ar",
                flameCount = if (isDev) 99 else 3,
                sparks = if (isDev) 9999 else 180,
                tokens = if (isDev) 999 else 25,
                xp = if (isDev) 99990 else 640,
                level = if (isDev) 99 else 4,
                isPro = isDev,
                isDeveloper = isDev,
                customStatus = if (isDev) "👑 مؤسس منصة SCOPE والمهندس الرئيسي للنظام" else "طالب SCOPE طموح 🚀",
                avatarId = if (isDev) "avatar_dev_crown" else "avatar_scholar_owl",
                onboardingCompleted = isDev,
                equippedFrame = "frame_cyber_neon",
                equippedAura = "aura_solar_amber",
                equippedTitle = if (isDev) "المطور الأسمى 👑" else "مهندس الخوارزميات",
                equippedGlassFx = "glass_holographic",
                unlockedItemIds = if (isDev) listOf(
                    "frame_cyber_neon", "frame_default", "aura_solar_amber", "aura_default",
                    "title_default", "glass_holographic", "glass_default", "avatar_dev_crown"
                ) else listOf(
                    "frame_cyber_neon", "frame_default", "aura_solar_amber", "aura_default",
                    "title_default", "glass_holographic", "glass_default"
                ),
                unlockedBadgeIds = if (isDev) listOf(
                    "badge_sys_creator", "badge_lead_architect", "badge_first_spark", "badge_streak_3", "badge_deep_dive"
                ) else listOf("badge_first_spark", "badge_streak_3", "badge_deep_dive")
            )
            userDoc(userId).set(profile).await()

            // Seed 3 realistic starter subjects
            val s1 = Subject(name = "الخوارزميات وهياكل البيانات", code = "CS201", colorHex = "#2563EB", instructor = "د. طارق محمود", creditHours = 3)
            val s2 = Subject(name = "أنظمة قواعد البيانات الموزعة", code = "CS310", colorHex = "#0284C7", instructor = "د. نادية حسن", creditHours = 3)
            val s3 = Subject(name = "الذكاء الاصطناعي وتعلم الآلة", code = "AI401", colorHex = "#7C3AED", instructor = "د. حسام الشريف", creditHours = 4)

            val s1Id = addSubject(userId, s1)
            val s2Id = addSubject(userId, s2)
            val s3Id = addSubject(userId, s3)

            // Seed initial tasks
            addTask(userId, Task(subjectId = s1Id, subjectName = s1.name, title = "حل مسألة شجرة البحث الثنائية (BST Balancing)", priority = TaskPriority.HIGH, effortMinutes = 45, signal = LearningSignal.NEEDS_REVIEW))
            addTask(userId, Task(subjectId = s2Id, subjectName = s2.name, title = "تصميم مخطط قواعد البيانات (Firestore Schema Plan)", priority = TaskPriority.MEDIUM, effortMinutes = 60, signal = LearningSignal.UNDERSTOOD))
            addTask(userId, Task(subjectId = s3Id, subjectName = s3.name, title = "مراجعة خوارزمية Gradient Descent والمصفوفات", priority = TaskPriority.HIGH, effortMinutes = 40, signal = LearningSignal.UNCLEAR))

            // Seed initial upcoming exam
            addExam(userId, Exam(subjectId = s1Id, subjectName = s1.name, title = "اختبار منتصف الفصل في الخوارزميات", weightPercentage = 25, topics = "Big-O, Sorting, Graphs, Heaps"))

            // Seed initial schedule occurrences
            addClassOccurrence(userId, ClassOccurrence(subjectId = s1Id, subjectName = s1.name, dayOfWeek = 1, startTime = "09:00", endTime = "10:30", room = "مدرج 3B", status = OccurrenceStatus.SCHEDULED))
            addClassOccurrence(userId, ClassOccurrence(subjectId = s2Id, subjectName = s2.name, dayOfWeek = 1, startTime = "11:30", endTime = "13:00", room = "معمل البرمجيات 2", status = OccurrenceStatus.SCHEDULED))
            addClassOccurrence(userId, ClassOccurrence(subjectId = s3Id, subjectName = s3.name, dayOfWeek = 2, startTime = "10:00", endTime = "11:30", room = "قاعة الندوات 1", status = OccurrenceStatus.SCHEDULED))
        }
    }

    suspend fun updateAvatar(userId: String, avatarId: String, photoUrl: String = "") {
        val updates = mutableMapOf<String, Any>("avatarId" to avatarId)
        if (photoUrl.isNotBlank()) {
            updates["photoUrl"] = photoUrl
        }
        userDoc(userId).update(updates).await()
    }

    suspend fun updateUserAcademicInfo(
        userId: String,
        displayName: String,
        scopeId: String,
        university: String,
        major: String,
        year: String,
        phone: String
    ) {
        val updates = mapOf<String, Any>(
            "displayName" to displayName,
            "scopeId" to scopeId,
            "university" to university,
            "academicMajor" to major,
            "academicYear" to year,
            "phoneNumber" to phone
        )
        userDoc(userId).update(updates).await()
    }

    suspend fun lookupEmailByScopeIdOrPhone(identifier: String): String? {
        val clean = identifier.trim()
        val formattedScopeId = if (clean.startsWith("@")) clean else "@$clean"
        try {
            val byScopeId = firestore.collection("users")
                .whereEqualTo("scopeId", formattedScopeId)
                .limit(1)
                .get()
                .await()
            if (!byScopeId.isEmpty) {
                return byScopeId.documents[0].getString("email")
            }

            val byPhone = firestore.collection("users")
                .whereEqualTo("phoneNumber", clean)
                .limit(1)
                .get()
                .await()
            if (!byPhone.isEmpty) {
                return byPhone.documents[0].getString("email")
            }
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Lookup failed", e)
        }
        return null
    }

    suspend fun grantDevSparksAndTokens(userId: String, sparks: Int, tokens: Int, makePro: Boolean) {
        val updates = mutableMapOf<String, Any>(
            "sparks" to FieldValue.increment(sparks.toLong()),
            "tokens" to FieldValue.increment(tokens.toLong())
        )
        if (makePro) {
            updates["isPro"] = true
        }
        userDoc(userId).update(updates).await()
    }

    fun getCommunityChallenges(): Flow<List<com.example.data.model.CommunityChallenge>> = callbackFlow {
        val subscription = firestore.collection("community_challenges")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(com.example.data.model.CommunityChallenge::class.java) }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addCommunityChallenge(challenge: com.example.data.model.CommunityChallenge) {
        firestore.collection("community_challenges").document(challenge.id).set(challenge).await()
    }

    suspend fun migrateGuestData(
        newUserId: String,
        guestProfile: UserProfile,
        guestTasks: List<Task>,
        guestSessions: List<StudySession>,
        guestSubjects: List<Subject>
    ) {
        // Merge sparks and tokens earned during guest session
        userDoc(newUserId).update(
            mapOf(
                "sparks" to FieldValue.increment(guestProfile.sparks.toLong()),
                "tokens" to FieldValue.increment(guestProfile.tokens.toLong()),
                "xp" to FieldValue.increment(guestProfile.xp.toLong())
            )
        ).await()

        // Copy guest tasks
        guestTasks.forEach { task ->
            addTask(newUserId, task.copy(id = ""))
        }

        // Copy guest study sessions
        guestSessions.forEach { session ->
            recordStudySession(newUserId, session.copy(id = ""))
        }
    }

    suspend fun sendFriendRequest(senderUid: String, senderName: String, senderScopeId: String, receiverUid: String) {
        val request = com.example.data.model.FriendRequest(
            senderUid = senderUid,
            senderName = senderName,
            senderScopeId = senderScopeId
        )
        firestore.collection("users").document(receiverUid).collection("friend_requests").document(request.id).set(request).await()
    }

    fun getFriendRequests(userId: String): Flow<List<com.example.data.model.FriendRequest>> = callbackFlow {
        val subscription = firestore.collection("users").document(userId).collection("friend_requests")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.toObject(com.example.data.model.FriendRequest::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun acceptFriendRequest(userId: String, request: com.example.data.model.FriendRequest) {
        // Add to friends list, remove request
        val friend = com.example.data.model.Friend(uid = request.senderUid, displayName = request.senderName, scopeId = request.senderScopeId)
        firestore.collection("users").document(userId).collection("friends").document(friend.uid).set(friend).await()
        firestore.collection("users").document(userId).collection("friend_requests").document(request.id).delete().await()
    }

    suspend fun declineFriendRequest(userId: String, requestId: String) {
        firestore.collection("users").document(userId).collection("friend_requests").document(requestId).delete().await()
    }

    fun getFriends(userId: String): Flow<List<com.example.data.model.Friend>> = callbackFlow {
        val subscription = firestore.collection("users").document(userId).collection("friends")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.toObject(com.example.data.model.Friend::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun transferGiftingSparksAndTokens(
        senderUid: String,
        senderProfile: UserProfile,
        recipientIdentifier: String,
        sparks: Int,
        tokens: Int,
        grantPro: Boolean
    ): Pair<Boolean, String> {
        val clean = recipientIdentifier.trim()
        if (clean.isBlank()) return false to "يرجى إدخال كود SCOPE أو البريد الإلكتروني للمستلم"

        // Prevent self-gifting validation
        val formattedScopeId = if (clean.startsWith("@")) clean.lowercase() else "@${clean.lowercase()}"
        if (clean.equals(senderProfile.email, ignoreCase = true) ||
            clean.equals(senderProfile.scopeId, ignoreCase = true) ||
            formattedScopeId.equals(senderProfile.scopeId.lowercase(), ignoreCase = true) ||
            clean.equals(senderUid, ignoreCase = true)
        ) {
            return false to "لا يمكنك إهداء نفسك! اختر زعيماً أو صديقاً آخر 🚫"
        }

        // Validate sender balance (if non-dev)
        if (!senderProfile.isDeveloper && (senderProfile.sparks < sparks || senderProfile.tokens < tokens)) {
            return false to "رصيدك غير كافٍ لإتمام عملية الإهداء"
        }

        try {
            var recipientDoc = firestore.collection("users").whereEqualTo("scopeId", formattedScopeId).limit(1).get().await()
            if (recipientDoc.isEmpty) {
                recipientDoc = firestore.collection("users").whereEqualTo("email", clean).limit(1).get().await()
            }
            if (recipientDoc.isEmpty) {
                recipientDoc = firestore.collection("users").whereEqualTo("scopeId", clean).limit(1).get().await()
            }

            if (recipientDoc.isEmpty) {
                return false to "لم يتم العثور على مستخدم بكود SCOPE أو بريد: $clean"
            }

            val targetDocRef = recipientDoc.documents[0].reference
            val targetUid = targetDocRef.id
            if (targetUid == senderUid) {
                return false to "لا يمكنك إهداء نفسك! اختر زعيماً أو صديقاً آخر 🚫"
            }

            // Deduct from sender if not super dev
            if (!senderProfile.isDeveloper) {
                userDoc(senderUid).update(
                    mapOf(
                        "sparks" to FieldValue.increment(-sparks.toLong()),
                        "tokens" to FieldValue.increment(-tokens.toLong())
                    )
                ).await()
            }

            // Deposit to recipient
            val recipientUpdates = mutableMapOf<String, Any>(
                "sparks" to FieldValue.increment(sparks.toLong()),
                "tokens" to FieldValue.increment(tokens.toLong())
            )
            if (grantPro) {
                recipientUpdates["isPro"] = true
            }
            targetDocRef.update(recipientUpdates).await()

            // Audit log in Firestore
            val logId = java.util.UUID.randomUUID().toString()
            firestore.collection("gift_audit_logs").document(logId).set(
                mapOf(
                    "id" to logId,
                    "senderUid" to senderUid,
                    "senderName" to senderProfile.displayName,
                    "recipientUid" to targetUid,
                    "recipientIdentifier" to clean,
                    "sparks" to sparks,
                    "tokens" to tokens,
                    "grantPro" to grantPro,
                    "timestamp" to System.currentTimeMillis()
                )
            ).await()

            return true to "تم إهداء $sparks ⚡ و $tokens 🪙 للمستلم بنجاح! 🎉"
        } catch (e: Exception) {
            return false to (e.message ?: "فشلت عملية الإهداء، تحقق من اتصالك بالشبكة")
        }
    }

    suspend fun wipeAcademicData(uid: String) {
        try {
            val userSubcoll = userDoc(uid)
            val collections = listOf("subjects", "tasks", "exams", "studySessions", "occurrences")
            for (col in collections) {
                val snapshot = userSubcoll.collection(col).get().await()
                for (doc in snapshot.documents) {
                    doc.reference.delete().await()
                }
            }
        } catch (_: Exception) {}
    }

    fun getStudyNotes(userId: String): Flow<List<com.example.data.model.StudyNote>> = callbackFlow {
        val subscription = userDoc(userId).collection("study_notes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.toObject(com.example.data.model.StudyNote::class.java)?.copy(id = it.id) } ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun saveStudyNote(userId: String, note: com.example.data.model.StudyNote) {
        val docRef = if (note.id.isBlank()) {
            userDoc(userId).collection("study_notes").document()
        } else {
            userDoc(userId).collection("study_notes").document(note.id)
        }
        val target = note.copy(id = docRef.id)
        try {
            localDatabase?.studyNoteDao()?.insertOrUpdate(com.example.data.local.StudyNoteEntity.fromStudyNote(target))
        } catch (_: Exception) {}
        docRef.set(target).await()
    }

    suspend fun deleteStudyNote(userId: String, noteId: String) {
        try {
            localDatabase?.studyNoteDao()?.deleteById(noteId)
        } catch (_: Exception) {}
        userDoc(userId).collection("study_notes").document(noteId).delete().await()
    }
}

