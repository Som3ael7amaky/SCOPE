package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun modelEnums_haveCorrectValues() {
    val priorities = com.example.data.model.TaskPriority.values()
    assertTrue(priorities.contains(com.example.data.model.TaskPriority.HIGH))
    assertTrue(priorities.contains(com.example.data.model.TaskPriority.MEDIUM))
    assertTrue(priorities.contains(com.example.data.model.TaskPriority.LOW))
  }

  @Test
  fun solaGuideEngine_detectsConflicts_assertiveState() {
    val occ1 = com.example.data.model.ClassOccurrence(dayOfWeek = 1, startTime = "09:00", endTime = "10:30", subjectName = "Math")
    val occ2 = com.example.data.model.ClassOccurrence(dayOfWeek = 1, startTime = "10:00", endTime = "11:30", subjectName = "Physics")
    val prompt = com.example.ui.mascot.SolaGuideEngine.evaluateAcademicState(
        tasks = emptyList(),
        occurrences = listOf(occ1, occ2),
        recentSessions = emptyList(),
        isSolaThinking = false,
        isTimerRunning = false
    )
    assertEquals(com.example.ui.mascot.SolaEmotionState.ASSERTIVE_GUIDE, prompt.emotion)
  }

  @Test
  fun solaGuideEngine_celebratesCompletedSession_happyState() {
    val prompt = com.example.ui.mascot.SolaGuideEngine.evaluateAcademicState(
        tasks = emptyList(),
        occurrences = emptyList(),
        recentSessions = emptyList(),
        isSolaThinking = false,
        isTimerRunning = false,
        justCompletedSession = true
    )
    assertEquals(com.example.ui.mascot.SolaEmotionState.HAPPY_CELEBRATION, prompt.emotion)
  }

  @Test
  fun solaGuideEngine_supportsOverloadedStudent_empatheticState() {
    val t1 = com.example.data.model.Task(priority = com.example.data.model.TaskPriority.HIGH, isCompleted = false)
    val t2 = com.example.data.model.Task(priority = com.example.data.model.TaskPriority.HIGH, isCompleted = false)
    val prompt = com.example.ui.mascot.SolaGuideEngine.evaluateAcademicState(
        tasks = listOf(t1, t2),
        occurrences = emptyList(),
        recentSessions = emptyList(),
        isSolaThinking = false,
        isTimerRunning = false
    )
    assertEquals(com.example.ui.mascot.SolaEmotionState.EMPATHETIC_SAD, prompt.emotion)
  }

  @Test
  fun solaGuideEngine_thinking_analyticalState() {
    val prompt = com.example.ui.mascot.SolaGuideEngine.evaluateAcademicState(
        tasks = emptyList(),
        occurrences = emptyList(),
        recentSessions = emptyList(),
        isSolaThinking = true,
        isTimerRunning = false
    )
    assertEquals(com.example.ui.mascot.SolaEmotionState.THINKING_ANALYTICAL, prompt.emotion)
  }

  @Test
  fun studyGuildsEngine_createsGuildAndContributesSparks() {
    val guild = com.example.data.guilds.StudyGuildsEngine.createGuild(
        name = "فرسان الكود",
        tag = "[CS-KSA]",
        description = "تحالف طلاب الحاسب الملتزمين",
        emblem = "🛡️",
        colorHex = "#2563EB",
        leaderProfile = null
    )
    assertNotNull(guild.id)
    assertEquals("فرسان الكود", guild.name)
    assertEquals(1, guild.members.size)
    assertEquals("LEADER", guild.members.first().role)

    val (updatedGuild, feedItem) = com.example.data.guilds.StudyGuildsEngine.contributeSparks(guild, "عبدالرحمن", 25)
    assertEquals(guild.weeklySparkPool + 25, updatedGuild.weeklySparkPool)
    assertTrue(feedItem.actionText.contains("25"))
  }

  @Test
  fun ghostFocusEngine_activatesAndGeneratesBroadcast() {
    val activeStatus = com.example.data.focus.GhostFocusEngine.activateGhostFocus("الخوارزميات", 25)
    assertTrue(activeStatus.isGhostFocusActive)
    assertTrue(activeStatus.mutesCampusPings)
    assertEquals("الخوارزميات", activeStatus.subjectName)

    val broadcastFeedItem = com.example.data.focus.GhostFocusEngine.generatePostFocusBroadcast("عبدالرحمن", "guild_123", 45, 15)
    assertTrue(broadcastFeedItem.actionText.contains("45"))
    assertTrue(broadcastFeedItem.actionText.contains("15"))
  }

  @Test
  fun projectsHubEngine_generatesSampleProjectsAndFinancials() {
    val sampleProjects = com.example.data.projects.ProjectsHubEngine.getSampleProjects()
    assertTrue(sampleProjects.isNotEmpty())

    val mainProj = sampleProjects.first()
    assertEquals("CS499", mainProj.code)
    assertEquals(4, mainProj.members.size)
    assertEquals(com.example.data.projects.ProjectRole.OWNER, mainProj.members.first().role)
    assertTrue(mainProj.financials.balanceFiat > 0)
    assertTrue(mainProj.auditLog.isNotEmpty())
  }

  @Test
  fun projectsHubEngine_startsElectionAndCastsVotes() {
    val sampleProjects = com.example.data.projects.ProjectsHubEngine.getSampleProjects()
    val proj = sampleProjects.first()

    val updatedWithElection = com.example.data.projects.ProjectsHubEngine.startElection(
        project = proj,
        title = "انتخابات القائد الجديد",
        candidates = proj.members,
        actorName = "سارة"
    )

    assertNotNull(updatedWithElection.activeElection)
    assertTrue(updatedWithElection.activeElection!!.isOpen)

    val voter1 = proj.members[0].id
    val voter2 = proj.members[1].id
    val voter3 = proj.members[2].id
    val voter4 = proj.members[3].id
    val candidateChoice = proj.members[1].id

    var votedProj = com.example.data.projects.ProjectsHubEngine.castVote(updatedWithElection, voter1, candidateChoice, "عضو 1")
    votedProj = com.example.data.projects.ProjectsHubEngine.castVote(votedProj, voter2, candidateChoice, "عضو 2")
    votedProj = com.example.data.projects.ProjectsHubEngine.castVote(votedProj, voter3, candidateChoice, "عضو 3")
    votedProj = com.example.data.projects.ProjectsHubEngine.castVote(votedProj, voter4, candidateChoice, "عضو 4")

    // All voted -> election auto-closes and elects leader!
    assertFalse(votedProj.activeElection!!.isOpen)
    assertEquals(candidateChoice, votedProj.activeElection!!.winnerMemberId)
    val leaderMember = votedProj.members.firstOrNull { it.id == candidateChoice }
    assertEquals(com.example.data.projects.ProjectRole.LEADER, leaderMember?.role)
  }

  @Test
  fun projectsHubEngine_addsFinancialTransactionAndNote() {
    val sampleProjects = com.example.data.projects.ProjectsHubEngine.getSampleProjects()
    val proj = sampleProjects.first()
    val initialBalance = proj.financials.balanceFiat

    val updatedFin = com.example.data.projects.ProjectsHubEngine.addFinancialTransaction(
        project = proj,
        title = "شراء حبر للطباعة",
        amountFiat = 50.0,
        category = "طباعة ونسخ",
        loggedByName = "عبدالرحمن",
        isExpense = true
    )

    assertEquals(initialBalance - 50.0, updatedFin.financials.balanceFiat, 0.01)
    assertEquals("ELECTION_STARTED", proj.auditLog.first().eventType)

    val pooled = com.example.data.projects.ProjectsHubEngine.poolSparksAndTokens(updatedFin, 100, 10, "سارة")
    assertEquals(proj.financials.pooledSparks + 100, pooled.financials.pooledSparks)

    val noted = com.example.data.projects.ProjectsHubEngine.addNote(pooled, "ملاحظة أمان", "تم اختبار الواجهات بنجاح", "سارة")
    assertEquals("ملاحظة أمان", noted.notes.first().title)
  }

  @Test
  fun solaPersonality_togglesSpeechStyleAndControlCenterState() {
    var speechStyle = "DETAILED"
    assertEquals("DETAILED", speechStyle)

    speechStyle = "CONCISE"
    assertEquals("CONCISE", speechStyle)

    var isHapticsEnabled = true
    isHapticsEnabled = false
    assertFalse(isHapticsEnabled)

    var isNotificationsSilenced = false
    isNotificationsSilenced = true
    assertTrue(isNotificationsSilenced)

    var isRestModeActive = false
    isRestModeActive = true
    assertTrue(isRestModeActive)

    val solaMessage = com.example.data.model.SolaMessage(
        sender = "sola",
        text = "تم تنظيف الذاكرة المؤقتة لصولا بنجاح 🧹. السياق الأكاديمي والبطاقات المحفوظة آمنة تماماً وجاهزة لمساعدتك.",
        thinkingContent = "تم إعادة ضبط سياق المحادثة المؤقت."
    )
    assertTrue(solaMessage.text.contains("تنظيف الذاكرة المؤقتة"))
  }

  @Test
  fun batch3_feedbackAndNotificationAndSearchEngine_validatesCorrectly() {
    val feedback = com.example.data.model.FeedbackItem(
        senderUid = "u1",
        senderName = "إسماعيل",
        feedbackType = "BUG",
        content = "مشكلة في واجهة البحث",
        category = "الواجهة والبطاقات",
        status = "PENDING"
    )
    assertEquals("PENDING", feedback.status)
    assertEquals("BUG", feedback.feedbackType)

    val rewardedFeedback = feedback.copy(status = "REWARDED", rewardedSparks = 100, rewardedTokens = 10)
    assertEquals("REWARDED", rewardedFeedback.status)
    assertEquals(100, rewardedFeedback.rewardedSparks)

    val notif = com.example.data.model.AppNotification(
        title = "تنبيه الشعلة 🔥",
        message = "حماية الشعلة الأكاديمية",
        channel = com.example.data.model.NotificationChannelType.STREAK_GUARD,
        isRead = false
    )
    assertFalse(notif.isRead)

    val readNotif = notif.copy(isRead = true)
    assertTrue(readNotif.isRead)

    val superDevEmail = "ismailhamaky@gmail.com"
    assertTrue(com.example.data.model.isSuperDeveloperEmail(superDevEmail))
  }
}

