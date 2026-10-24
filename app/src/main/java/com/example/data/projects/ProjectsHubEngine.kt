package com.example.data.projects

import com.example.data.model.ProjectAttachment
import com.example.data.model.ProjectMilestone
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import com.example.data.model.UserProfile
import java.util.UUID

enum class ProjectRole(val titleAr: String, val badgeEmoji: String) {
    OWNER("المالك", "👑"),
    LEADER("القائد", "⭐️"),
    MEMBER("عضو نشط", "👤")
}

data class ProjectMember(
    val id: String = UUID.randomUUID().toString(),
    val displayName: String = "",
    val scopeId: String = "",
    val role: ProjectRole = ProjectRole.MEMBER,
    val customRoleTitle: String = "عضو",
    val isExternal: Boolean = false,
    val email: String? = null,
    val avatarId: String = "avatar_scholar_owl",
    val focusEfficiency: Int = 92, // %
    val streakDays: Int = 5,
    val votesCount: Int = 0
)

data class FinancialTransaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val amountFiat: Double = 0.0, // positive = income, negative = expense
    val category: String = "", // e.g. "مساهمات الأعضاء", "طباعة ونسخ", "استضافة سحابية"
    val loggedByName: String = "",
    val isExpense: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class ProjectFinancials(
    val balanceFiat: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val fiatCurrency: String = "SAR",
    val pooledSparks: Int = 0,
    val pooledTokens: Int = 0,
    val transactions: List<FinancialTransaction> = emptyList()
)

data class ProjectElection(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val isOpen: Boolean = true,
    val candidates: List<ProjectMember> = emptyList(),
    val votes: Map<String, String> = emptyMap(), // voterMemberId -> candidateMemberId
    val startedTimestamp: Long = System.currentTimeMillis(),
    val closedTimestamp: Long? = null,
    val winnerMemberId: String? = null
)

data class ProjectActivityItem(
    val id: String = UUID.randomUUID().toString(),
    val eventType: String = "", // "ELECTION_STARTED", "ELECTION_CLOSED", "FINANCIAL_TRANSACTION", "MEMBER_JOINED", "TASK_COMPLETED", "NOTE_ADDED", "FILE_UPLOADED", "MILESTONE_COMPLETED"
    val title: String = "",
    val description: String = "",
    val actorName: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun isDeliverable(): Boolean {
        return eventType in listOf("TASK_COMPLETED", "NOTE_ADDED", "FILE_UPLOADED", "MILESTONE_COMPLETED")
    }
}

data class ProjectNote(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val authorName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ScopeProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val code: String = "",
    val description: String = "",
    val ownerProfileId: String = "owner_default",
    val isSoloProject: Boolean = false,
    val members: List<ProjectMember> = emptyList(),
    val financials: ProjectFinancials = ProjectFinancials(),
    val activeElection: ProjectElection? = null,
    val auditLog: List<ProjectActivityItem> = emptyList(),
    val notes: List<ProjectNote> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val milestones: List<ProjectMilestone> = emptyList(),
    val attachments: List<ProjectAttachment> = emptyList(),
    val colorHex: String = "#38BDF8",
    val progressPercentage: Float = 0.0f
)

object ProjectsHubEngine {

    fun getSampleProjects(userProfile: UserProfile? = null): List<ScopeProject> {
        val currentUserName = userProfile?.displayName?.ifBlank { "أنت" } ?: "أنت (صاحب المشروع)"
        val currentScopeId = userProfile?.scopeId?.ifBlank { "@me" } ?: "@me"

        val ownerMember = ProjectMember(
            id = userProfile?.uid ?: "m1",
            displayName = currentUserName,
            scopeId = currentScopeId,
            role = ProjectRole.OWNER,
            avatarId = userProfile?.avatarId ?: "avatar_scholar_owl",
            focusEfficiency = 96,
            streakDays = 12
        )

        val member2 = ProjectMember(
            id = "m2",
            displayName = "سارة القحطاني",
            scopeId = "@sara_se",
            role = ProjectRole.LEADER,
            avatarId = "avatar_cyber_scholar",
            focusEfficiency = 94,
            streakDays = 8
        )

        val member3 = ProjectMember(
            id = "m3",
            displayName = "عبدالرحمن الشمري",
            scopeId = "@abdul_cs",
            role = ProjectRole.MEMBER,
            avatarId = "avatar_quantum_thinker",
            focusEfficiency = 88,
            streakDays = 5
        )

        val member4 = ProjectMember(
            id = "m4",
            displayName = "فيصل العتيبي",
            scopeId = "@faisal_ai",
            role = ProjectRole.MEMBER,
            avatarId = "avatar_solar_prodigy",
            focusEfficiency = 91,
            streakDays = 6
        )

        val membersList = listOf(ownerMember, member2, member3, member4)

        // Initial Transactions
        val sampleTransactions = listOf(
            FinancialTransaction(
                title = "مساهمة رسمية من فريق التخرج",
                amountFiat = 2200.0,
                category = "مساهمات الأعضاء",
                loggedByName = currentUserName,
                isExpense = false,
                timestamp = System.currentTimeMillis() - 86400000L * 4
            ),
            FinancialTransaction(
                title = "رسوم طباعة وتجليد التقارير الفنية",
                amountFiat = 350.0,
                category = "طباعة ونسخ",
                loggedByName = "سارة القحطاني",
                isExpense = true,
                timestamp = System.currentTimeMillis() - 86400000L * 2
            ),
            FinancialTransaction(
                title = "حجز سيرفر سحابي ومستودع نماذج AI",
                amountFiat = 400.0,
                category = "استضافة ودومين",
                loggedByName = currentUserName,
                isExpense = true,
                timestamp = System.currentTimeMillis() - 86400000L * 1
            )
        )

        val financials = ProjectFinancials(
            balanceFiat = 1450.0,
            totalIncome = 2200.0,
            totalExpenses = 750.0,
            fiatCurrency = "ر.س",
            pooledSparks = 840,
            pooledTokens = 60,
            transactions = sampleTransactions
        )

        // Initial Active Election
        val sampleElection = ProjectElection(
            title = "انتخابات قائد الفريق التقني لمشروع التخرج 2026 🗳️",
            isOpen = true,
            candidates = listOf(member2, member3, member4),
            votes = mapOf("m1" to "m2", "m3" to "m2"),
            startedTimestamp = System.currentTimeMillis() - 3600000L * 5
        )

        // Audit Log
        val auditLog = listOf(
            ProjectActivityItem(
                eventType = "ELECTION_STARTED",
                title = "بدء انتخابات قيادة المشروع 🗳️",
                description = "تم فتح باب التصويت لاختيار قائد الفريق التقني بين الأعضاء المرشحين.",
                actorName = currentUserName,
                timestamp = System.currentTimeMillis() - 3600000L * 5
            ),
            ProjectActivityItem(
                eventType = "FINANCIAL_TRANSACTION",
                title = "تسجيل مصروف جديد (-400 ر.س) 💳",
                description = "تم خصم 400 ر.س لحجز خادم استضافة النماذج الذكية.",
                actorName = currentUserName,
                timestamp = System.currentTimeMillis() - 86400000L * 1
            ),
            ProjectActivityItem(
                eventType = "TASK_COMPLETED",
                title = "إنجاز مهمة: إعداد وثيقة معمارية النظام 📋",
                description = "أتمت سارة القحطاني صياغة وتوثيق مخطط الـ System Architecture.",
                actorName = "سارة القحطاني",
                timestamp = System.currentTimeMillis() - 86400000L * 2
            ),
            ProjectActivityItem(
                eventType = "MEMBER_JOINED",
                title = "انضمام عضو جديد للفريق 👥",
                description = "انضم فيصل العتيبي لمشروع التخرج كمطور خوارزميات ذكاء اصطناعي.",
                actorName = "فيصل العتيبي",
                timestamp = System.currentTimeMillis() - 86400000L * 6
            )
        )

        // Notes
        val notes = listOf(
            ProjectNote(
                title = "شروط واعتمادات المعمارية السحابية",
                content = "تم الاتفاق على استخدام Firebase Firestore و Docker في نشر الخوادم لضمان توافقية الأداء العالي.",
                authorName = "سارة القحطاني",
                timestamp = System.currentTimeMillis() - 86400000L * 3
            ),
            ProjectNote(
                title = "أجندة اجتماع المناقشة المبدئية مع المشرف",
                content = "1. استعراض النموذج الأولي للواجهات\n2. تقديم تحليل التكلفة المادية والزمنية\n3. توزيع المهام البرمجية للأسابيع القادمة.",
                authorName = currentUserName,
                timestamp = System.currentTimeMillis() - 86400000L * 1
            )
        )

        val project1 = ScopeProject(
            id = "proj_grad_2026",
            name = "مشروع التخرج: نظام الذكاء الاصطناعي الأكاديمي SCOPE",
            code = "CS499",
            description = "تطوير منصة دراسية متكاملة تعتمد على الذكاء الاصطناعي التكيفي لتحسين الأداء الأكاديمي للطلاب.",
            ownerProfileId = userProfile?.uid ?: "owner_default",
            members = membersList,
            financials = financials,
            activeElection = sampleElection,
            auditLog = auditLog,
            notes = notes,
            milestones = listOf(
                ProjectMilestone(title = "مواصفات المعمارية التقنية (Architecture Spec)", dueDate = "بعد 3 أيام", isCompleted = true),
                ProjectMilestone(title = "تكامل نماذج الذكاء الاصطناعي مع الواجهة", dueDate = "بعد 6 أيام", isCompleted = false),
                ProjectMilestone(title = "العرض التقديمي التجريبي واختبار المستخدمين", dueDate = "بعد 12 يوماً", isCompleted = false)
            ),
            attachments = listOf(
                ProjectAttachment(name = "Scope_System_Design.pdf", sizeLabel = "2.4 MB", fileType = "PDF"),
                ProjectAttachment(name = "Dataset_Analytics_v3.csv", sizeLabel = "850 KB", fileType = "DATA"),
                ProjectAttachment(name = "Sprint_Backlog_Specs.docx", sizeLabel = "1.2 MB", fileType = "DOC")
            ),
            colorHex = "#38BDF8",
            progressPercentage = 0.72f
        )

        val project2 = ScopeProject(
            id = "proj_sensors_305",
            name = "مشروع شبكات الحساسات الموزعة والأنظمة المدمجة",
            code = "CS305",
            description = "تصميم شبكة حساسات ذكية لقياس العوامل البيئية داخل معامل الجامعة ومراقبة جودة الهواء.",
            ownerProfileId = userProfile?.uid ?: "owner_default",
            members = listOf(ownerMember, member3),
            financials = ProjectFinancials(
                balanceFiat = 600.0,
                totalIncome = 800.0,
                totalExpenses = 200.0,
                fiatCurrency = "ر.س",
                pooledSparks = 320,
                pooledTokens = 20
            ),
            activeElection = null,
            auditLog = listOf(
                ProjectActivityItem(
                    eventType = "MEMBER_JOINED",
                    title = "تأسيس مشروع شبكات الحساسات 📡",
                    description = "تمت إضافة المشروع وتحديد الميزانية المبدئية لشراء القطع الإلكترونية.",
                    actorName = currentUserName
                )
            ),
            notes = listOf(
                ProjectNote(
                    title = "قائمة القطع والحساسات المطلوب شراؤها",
                    content = "1. أجهزة ESP32 Microcontroller\n2. حساسات حرارة ورطوبة DHT22\n3. وحدات اتصال LoRa Gateway.",
                    authorName = "عبدالرحمن الشمري"
                )
            ),
            colorHex = "#F59E0B",
            progressPercentage = 0.45f
        )

        return listOf(project1, project2)
    }

    fun startElection(
        project: ScopeProject,
        title: String,
        candidates: List<ProjectMember>,
        actorName: String
    ): ScopeProject {
        val newElection = ProjectElection(
            title = if (title.isBlank()) "انتخابات قيادة المشروع الديمقراطية 🗳️" else title,
            isOpen = true,
            candidates = candidates,
            votes = emptyMap(),
            startedTimestamp = System.currentTimeMillis()
        )

        val logItem = ProjectActivityItem(
            eventType = "ELECTION_STARTED",
            title = "بدء انتخابات قيادية جديدة 🗳️",
            description = "تم إطلاق التصويت الديمقراطي لاختيار قائد المشروع: ${newElection.title}",
            actorName = actorName
        )

        return project.copy(
            activeElection = newElection,
            auditLog = listOf(logItem) + project.auditLog
        )
    }

    fun castVote(
        project: ScopeProject,
        voterMemberId: String,
        candidateMemberId: String,
        voterName: String
    ): ScopeProject {
        val currentElection = project.activeElection ?: return project
        if (!currentElection.isOpen) return project

        val updatedVotes = currentElection.votes.toMutableMap()
        updatedVotes[voterMemberId] = candidateMemberId

        // Count votes per candidate
        val candidateVoteCounts = updatedVotes.values.groupingBy { it }.eachCount()

        // Update vote count in candidates list
        val updatedCandidates = currentElection.candidates.map { candidate ->
            candidate.copy(votesCount = candidateVoteCounts[candidate.id] ?: 0)
        }

        val allVoted = updatedVotes.size >= project.members.size

        var winnerId: String? = currentElection.winnerMemberId
        var isStillOpen = true
        var updatedMembers = project.members
        var closedTime = currentElection.closedTimestamp

        if (allVoted && updatedCandidates.isNotEmpty()) {
            isStillOpen = false
            closedTime = System.currentTimeMillis()

            // Winner score combines direct votes + Focus Efficiency
            val winnerCandidate = updatedCandidates.maxByOrNull {
                (it.votesCount * 10) + (it.focusEfficiency / 2) + it.streakDays
            }

            winnerId = winnerCandidate?.id

            if (winnerId != null) {
                updatedMembers = project.members.map { member ->
                    if (member.id == winnerId) {
                        member.copy(role = ProjectRole.LEADER)
                    } else if (member.role == ProjectRole.LEADER) {
                        member.copy(role = ProjectRole.MEMBER)
                    } else {
                        member
                    }
                }
            }
        }

        val updatedElection = currentElection.copy(
            votes = updatedVotes,
            candidates = updatedCandidates,
            isOpen = isStillOpen,
            closedTimestamp = closedTime,
            winnerMemberId = winnerId
        )

        val logs = project.auditLog.toMutableList()
        logs.add(
            0,
            ProjectActivityItem(
                eventType = if (!isStillOpen) "ELECTION_CLOSED" else "VOTE_CAST",
                title = if (!isStillOpen) "اكتملت الانتخابات وحُسمت القيادة! 🎉" else "تسجيل صوت انتخابي 🗳️",
                description = if (!isStillOpen) {
                    val winnerName = updatedMembers.firstOrNull { it.id == winnerId }?.displayName ?: "القائد الجديد"
                    "فاز العضو ($winnerName) برئاسة وتوجيه الفريق الأكاديمي بناءً على أصوات الأعضاء وكفاءة التركيز."
                } else {
                    "قام العضو ($voterName) بالإدلاء بصوته في الانتخابات الجارية."
                },
                actorName = voterName
            )
        )

        return project.copy(
            members = updatedMembers,
            activeElection = updatedElection,
            auditLog = logs
        )
    }

    fun addFinancialTransaction(
        project: ScopeProject,
        title: String,
        amountFiat: Double,
        category: String,
        loggedByName: String,
        isExpense: Boolean
    ): ScopeProject {
        val newTx = FinancialTransaction(
            title = title,
            amountFiat = amountFiat,
            category = category,
            loggedByName = loggedByName,
            isExpense = isExpense,
            timestamp = System.currentTimeMillis()
        )

        val currentFin = project.financials
        val updatedTransactions = listOf(newTx) + currentFin.transactions
        val newIncome = if (!isExpense) currentFin.totalIncome + amountFiat else currentFin.totalIncome
        val newExpenses = if (isExpense) currentFin.totalExpenses + amountFiat else currentFin.totalExpenses
        val newBalance = newIncome - newExpenses

        val updatedFin = currentFin.copy(
            balanceFiat = newBalance,
            totalIncome = newIncome,
            totalExpenses = newExpenses,
            transactions = updatedTransactions
        )

        val actionEmoji = if (isExpense) "💳" else "💰"
        val signStr = if (isExpense) "-${amountFiat.toInt()}" else "+${amountFiat.toInt()}"

        val logItem = ProjectActivityItem(
            eventType = "FINANCIAL_TRANSACTION",
            title = "معاملة مالية جديدة ($signStr ${currentFin.fiatCurrency}) $actionEmoji",
            description = "تم تسجيل ${if (isExpense) "مصروف" else "مساهمة/دخل"} جديد تحت بند ($category): $title",
            actorName = loggedByName
        )

        return project.copy(
            financials = updatedFin,
            auditLog = listOf(logItem) + project.auditLog
        )
    }

    fun poolSparksAndTokens(
        project: ScopeProject,
        sparksAmount: Int,
        tokensAmount: Int,
        memberName: String
    ): ScopeProject {
        val currentFin = project.financials
        val updatedFin = currentFin.copy(
            pooledSparks = currentFin.pooledSparks + sparksAmount,
            pooledTokens = currentFin.pooledTokens + tokensAmount
        )

        val logItem = ProjectActivityItem(
            eventType = "SPARKS_POOLED",
            title = "مساهمة بالشرارات ورموز صولا ⚡🪙",
            description = "قام العضو ($memberName) بإيداع +$sparksAmount شرارة ⚡ و+$tokensAmount رمز صولا 🪙 لصندوق الفريق.",
            actorName = memberName
        )

        return project.copy(
            financials = updatedFin,
            auditLog = listOf(logItem) + project.auditLog
        )
    }

    fun addNote(
        project: ScopeProject,
        title: String,
        content: String,
        authorName: String
    ): ScopeProject {
        val newNote = ProjectNote(
            title = title,
            content = content,
            authorName = authorName,
            timestamp = System.currentTimeMillis()
        )

        val logItem = ProjectActivityItem(
            eventType = "NOTE_ADDED",
            title = "إضافة ملاحظة جديدة بالمشروع 📝",
            description = "قام العضو ($authorName) بإضافة الملاحظة التوثيقية: $title",
            actorName = authorName
        )

        return project.copy(
            notes = listOf(newNote) + project.notes,
            auditLog = listOf(logItem) + project.auditLog
        )
    }

    fun canEditFinancials(member: ProjectMember?, isSoloProject: Boolean = false): Boolean {
        if (isSoloProject || member == null) return true
        if (member.role == ProjectRole.OWNER || member.role == ProjectRole.LEADER) return true
        val title = member.customRoleTitle.lowercase()
        return title.contains("مالية") || title.contains("financial") || title.contains("محاسب") || title.contains("ميزانية")
    }

    fun addRegisteredMember(
        project: ScopeProject,
        scopeIdOrEmail: String,
        customRoleTitle: String,
        actorName: String
    ): ScopeProject {
        val cleanIdentifier = if (scopeIdOrEmail.startsWith("@")) scopeIdOrEmail else "@$scopeIdOrEmail"
        val newMember = ProjectMember(
            displayName = cleanIdentifier.removePrefix("@").split(".").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: cleanIdentifier,
            scopeId = cleanIdentifier,
            role = ProjectRole.MEMBER,
            customRoleTitle = customRoleTitle.ifBlank { "عضو فريق" },
            isExternal = false,
            focusEfficiency = 90,
            streakDays = 3
        )
        val logItem = ProjectActivityItem(
            eventType = "MEMBER_JOINED",
            title = "انضمام طالب جديد للفريق ⚡",
            description = "انضم $cleanIdentifier لمشروع ${project.code} بصفة (${newMember.customRoleTitle}).",
            actorName = actorName
        )
        return project.copy(
            members = project.members + newMember,
            auditLog = listOf(logItem) + project.auditLog
        )
    }

    fun addExternalMember(
        project: ScopeProject,
        displayName: String,
        customRoleTitle: String,
        actorName: String
    ): ScopeProject {
        val newMember = ProjectMember(
            displayName = displayName,
            scopeId = "@external_member",
            role = ProjectRole.MEMBER,
            customRoleTitle = customRoleTitle.ifBlank { "عضو خارجي" },
            isExternal = true,
            avatarId = "avatar_external_guest",
            focusEfficiency = 85,
            streakDays = 1
        )
        val logItem = ProjectActivityItem(
            eventType = "MEMBER_JOINED",
            title = "إضافة عضو خارجي للفريق 👤",
            description = "تم تسجيل العضو الخارجي ($displayName) كمشارك بصفة (${newMember.customRoleTitle}).",
            actorName = actorName
        )
        return project.copy(
            members = project.members + newMember,
            auditLog = listOf(logItem) + project.auditLog
        )
    }

    fun assignMemberRole(
        project: ScopeProject,
        memberId: String,
        newRole: ProjectRole,
        customRoleTitle: String,
        actorName: String
    ): ScopeProject {
        val targetMember = project.members.find { it.id == memberId } ?: return project
        val updatedMembers = project.members.map { m ->
            if (m.id == memberId) {
                m.copy(role = newRole, customRoleTitle = customRoleTitle.ifBlank { m.customRoleTitle })
            } else if (newRole == ProjectRole.LEADER && m.role == ProjectRole.LEADER) {
                m.copy(role = ProjectRole.MEMBER)
            } else {
                m
            }
        }
        val logItem = ProjectActivityItem(
            eventType = "ROLE_ASSIGNED",
            title = "تعديل أدوار وصلاحيات العضو 🛡️",
            description = "قام $actorName بتحديث دور العضو (${targetMember.displayName}) إلى: $customRoleTitle (${newRole.titleAr}).",
            actorName = actorName
        )
        return project.copy(
            members = updatedMembers,
            auditLog = listOf(logItem) + project.auditLog
        )
    }

    fun generateProjectReportText(project: ScopeProject): String {
        val totalMilestones = project.milestones.size
        val doneMilestones = project.milestones.count { it.isCompleted }
        val totalTasks = project.tasks.size
        val doneTasks = project.tasks.count { it.isCompleted }
        val fin = project.financials

        val sb = StringBuilder()
        sb.appendLine("📊 تقرير إنجاز ومخرجات المشروع الأكاديمي")
        sb.appendLine("==========================================")
        sb.appendLine("اسم المشروع: ${project.name}")
        sb.appendLine("رمز المقرر: ${project.code}")
        sb.appendLine("نسبة الإنجاز الإجمالية: ${(project.progressPercentage * 100).toInt()}%")
        sb.appendLine("نوع المشروع: ${if (project.isSoloProject) "فردي 👤" else "جماعي 👥"}")
        sb.appendLine()
        sb.appendLine("📈 مؤشرات الأداء والمهام:")
        sb.appendLine(" - المراحل الرئيسية المكتملة: $doneMilestones من $totalMilestones")
        sb.appendLine(" - المهام التنفيذية المكتملة: $doneTasks من $totalTasks")
        sb.appendLine(" - الملاحظات والقرارات الموثقة: ${project.notes.size}")
        sb.appendLine()
        sb.appendLine("💰 التقرير المالي والميزانية (${fin.fiatCurrency}):")
        sb.appendLine(" - إجمالي المساهمات والدخل: ${fin.totalIncome.toInt()} ${fin.fiatCurrency}")
        sb.appendLine(" - إجمالي المصروفات: ${fin.totalExpenses.toInt()} ${fin.fiatCurrency}")
        sb.appendLine(" - صافي رصيد الخزينة: ${fin.balanceFiat.toInt()} ${fin.fiatCurrency}")
        sb.appendLine(" - رصيد الشرارات المجمعة: ${fin.pooledSparks} ⚡ (ورموز صولا: ${fin.pooledTokens} 🪙)")
        sb.appendLine()
        sb.appendLine("👥 توزيع المساهمات ونسب الأعضاء:")
        project.members.forEach { m ->
            val externalTag = if (m.isExternal) " [عضو خارجي]" else ""
            val contribScore = (m.focusEfficiency / 2) + (m.streakDays * 2) + 20
            sb.appendLine(" • ${m.displayName} (${m.customRoleTitle} - ${m.role.titleAr})$externalTag: كفاءة التركيز ${m.focusEfficiency}% | الشعلة ${m.streakDays}d | مساهمة مقدرة ~$contribScore%")
        }
        sb.appendLine()
        sb.appendLine("📦 أهم المخرجات والتسليمات المنجزة:")
        project.auditLog.filter { it.isDeliverable() }.take(5).forEach { deliverable ->
            sb.appendLine(" ✓ [${deliverable.actorName}]: ${deliverable.title}")
        }
        sb.appendLine()
        sb.appendLine("صادر تلقائياً عبر منصة SCOPE Student OS • ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}")
        return sb.toString()
    }
}
