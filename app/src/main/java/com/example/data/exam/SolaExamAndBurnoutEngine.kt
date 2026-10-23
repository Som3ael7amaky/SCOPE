package com.example.data.exam

import com.example.data.model.Exam
import com.example.data.model.Flashcard
import com.example.data.model.StudySession
import com.example.data.model.Subject
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import kotlin.math.min
import kotlin.math.roundToInt

data class QuizQuestion(
    val id: String = java.util.UUID.randomUUID().toString(),
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val studyTip: String,
    val difficulty: String = "متوسط" // "أساسي", "متوسط", "متقدم"
)

data class ExamSimulationSession(
    val examId: String,
    val examTitle: String,
    val subjectName: String,
    val questions: List<QuizQuestion>,
    val createdAt: Long = System.currentTimeMillis()
)

data class BurnoutRadarStatus(
    val isOverloaded: Boolean,
    val focusHoursToday: Float,
    val upcomingExamsCount48h: Int,
    val pendingHighPriorityTasks: Int,
    val riskScorePercent: Int,
    val riskLevelText: String,
    val diagnosticReason: String,
    val solaAdvice: String
)

object SolaExamAndBurnoutEngine {

    /**
     * Evaluates whether student is entering an intensive burnout state.
     * Thresholds:
     * - > 8 hours intensive focus (or >= 480 minutes)
     * - >= 3 exams within 48 hours
     * - Consecutive cramming with high workload
     */
    fun evaluateBurnoutRadar(
        studySessions: List<StudySession>,
        exams: List<Exam>,
        tasks: List<Task>
    ): BurnoutRadarStatus {
        val now = System.currentTimeMillis()
        val oneDayAgo = now - 86400000L
        val twoDaysAhead = now + (48 * 3600 * 1000L)

        // Calculate focus minutes in last 24h
        val recentSessions = studySessions.filter { it.timestamp in oneDayAgo..now }
        val focusMinutes = recentSessions.sumOf { it.durationMinutes }
        val focusHoursToday = ((focusMinutes / 60.0f) * 10f).roundToInt() / 10f

        // Upcoming exams in 48h
        val upcomingExams48h = exams.count { it.examTimestamp in now..twoDaysAhead }

        // Pending high priority tasks
        val pendingHighTasks = tasks.count { !it.isCompleted && it.priority == TaskPriority.HIGH }

        // Score formula: (focusMinutes / 480) * 50 + (exams48h * 20) + (pendingHighTasks * 10)
        var rawRisk = (focusMinutes / 480.0 * 50.0) + (upcomingExams48h * 22.0) + (pendingHighTasks * 7.0)
        val riskScore = min(100, maxOf(15, rawRisk.roundToInt()))

        val isOverloaded = focusMinutes >= 480 || upcomingExams48h >= 3 || (focusMinutes >= 300 && upcomingExams48h >= 2)

        val riskLevelText = when {
            isOverloaded || riskScore >= 75 -> "تحذير: إجهاد ذهني وشيك ⚠️"
            riskScore >= 50 -> "ضغط أكاديمي مرتفع ⏳"
            else -> "مستوى طاقة متزن ومستقر 🟢"
        }

        val diagnosticReason = when {
            focusMinutes >= 480 -> "سجلت أكثر من 8 ساعات من التركيز المتواصل اليوم ($focusHoursToday ساعة). كفاءة الدماغ في معالجة المعلومات تنخفض بنسبة 40% عند استمرار الجهد بدون فترات نقاهة."
            upcomingExams48h >= 3 -> "لديك $upcomingExams48h اختبارات حاسمة خلال الـ 48 ساعة القادمة مع $pendingHighTasks مهام عاجلة. التركيز المكثف المستمر قد يؤدي إلى شلل الإنجاز."
            else -> "تم رصد تراكم في المهام وجلسات التركيز المتتابعة دون استراحات كافية لتجديد بطارية الدماغ."
        }

        val solaAdvice = if (isOverloaded) {
            "صولا توصي بإيقاف الدراسة فوراً لمدة 20 دقيقة (استراحة إجبارية لحماية الذاكرة طويلة المدى). اغلق الشاشات وتنفس بعمق."
        } else {
            "طاقتك في مسار جيد، ولكن احرص على شرب الماء وأخذ فواصل قصيرة بين كل دورة بومودورو."
        }

        return BurnoutRadarStatus(
            isOverloaded = isOverloaded,
            focusHoursToday = focusHoursToday,
            upcomingExamsCount48h = upcomingExams48h,
            pendingHighPriorityTasks = pendingHighTasks,
            riskScorePercent = riskScore,
            riskLevelText = riskLevelText,
            diagnosticReason = diagnosticReason,
            solaAdvice = solaAdvice
        )
    }

    /**
     * Generates a 5-question interactive quiz from flashcards or academic course questions.
     */
    fun generateExamQuiz(
        exam: Exam?,
        subjects: List<Subject>,
        flashcards: List<Flashcard>
    ): ExamSimulationSession {
        val targetSubjectName = exam?.subjectName
            ?: subjects.firstOrNull()?.name
            ?: "الخوارزميات وهياكل البيانات"
        val examTitle = exam?.title ?: "اختبار المحاكاة الشامل: $targetSubjectName"

        // Filter flashcards matching subject
        val subjectCards = flashcards.filter {
            it.subjectName.contains(targetSubjectName, ignoreCase = true) ||
                    targetSubjectName.contains(it.subjectName, ignoreCase = true)
        }

        val questions = mutableListOf<QuizQuestion>()

        // Convert matching flashcards to questions if available
        for ((idx, card) in subjectCards.take(5).withIndex()) {
            val distractor1 = "تعتمد على تنفيذ متزامن دون تخزين مؤقت للبيانات."
            val distractor2 = "تتطلب استهلاك O(n^2) من المساحة في جميع الحالات."
            val distractor3 = "إلغاء فحص القيود تلقائياً لزيادة سرعة المعالجة."

            val options = listOf(card.back, distractor1, distractor2, distractor3).shuffled()
            val correctIdx = options.indexOf(card.back)

            questions.add(
                QuizQuestion(
                    id = "q_fc_${card.id}_$idx",
                    question = card.front,
                    options = options,
                    correctIndex = correctIdx,
                    explanation = "الإجابة الصحيحة هي: ${card.back}",
                    studyTip = "نصيحة صولا: راجع هذا المفهوم في بطاقات الاستذكار السريع لتثبيته في الذاكرة طويلة المدى.",
                    difficulty = if (idx % 2 == 0) "متوسط" else "متقدم"
                )
            )
        }

        // Fill remaining questions with high-yield academic presets
        if (questions.size < 5) {
            val presets = getCuratedAcademicQuestions(targetSubjectName)
            for (p in presets) {
                if (questions.size >= 5) break
                questions.add(p)
            }
        }

        return ExamSimulationSession(
            examId = exam?.id ?: "sim_exam_${System.currentTimeMillis()}",
            examTitle = examTitle,
            subjectName = targetSubjectName,
            questions = questions.take(5)
        )
    }

    private fun getCuratedAcademicQuestions(subjectName: String): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "ما هو التعقيد الزمني الزمني (Time Complexity) لعملية البحث في شجرة البحث الثنائية المتوازنة (AVL Tree)؟",
                options = listOf("O(log n)", "O(n)", "O(1)", "O(n log n)"),
                correctIndex = 0,
                explanation = "تحافظ أشجار AVL على ارتفاع متوازن h <= 1.44 log n، وبالتالي فإن جميع عمليات البحث والإضافة والحذف تضمن O(log n).",
                studyTip = "تذكر: التوازن الصارم في AVL يمنع تحول الشجرة إلى قائمة مرتبطة (Linked List).",
                difficulty = "أساسي"
            ),
            QuizQuestion(
                question = "في نظرية CAP لنظم قواعد البيانات الموزعة، ما الذي لا يمكن تحقيقه عند حدوث تجزئة للشبكة (Network Partition)؟",
                options = listOf(
                    "الجمع بين الاتساق التام والتوافر بنسبة 100% في نفس اللحظة",
                    "تخزين البيانات في أكثر من عقدة خادم",
                    "استرجاع البيانات المقروءة سابقاً",
                    "تطبيق معاملات ACID محلياً داخل عقدة واحدة"
                ),
                correctIndex = 0,
                explanation = "وفقاً لمبدأ Brewer's CAP Theorem، عند حدوث Partition (انقطاع اتصال الشبكة)، يجب على النظام الاختيار بين إما الاتساق (CP) أو التوافر (AP).",
                studyTip = "نصيحة صولا: أنظمة RDBMS تختار عادة CP، بينما قواعد بيانات NoSQL مثل DynamoDB ترجح AP.",
                difficulty = "متوسط"
            ),
            QuizQuestion(
                question = "ما وظيفة خوارزمية Dijkstra وما هو القيد الرئيسي لتطبيقها؟",
                options = listOf(
                    "إيجاد أقصر مسار بشرط عدم وجود أوزان سالبة للحواف",
                    "إيجاد شجرة الامتداد الأصغري بشرط وجود دورات موجبة فقط",
                    "ترتيب العقد طوبولوجياً للرسوم البيانية غير الموجهة",
                    "البحث المتعمق بدون قيود على الأوزان"
                ),
                correctIndex = 0,
                explanation = "خوارزمية ديكسترا تفترض جشعاً (Greedy) أن المسار الأقصر المكتشف لن يقل وزنه مستقبلاً، لذا تفشل في وجود حواف سالبة ويستخدم بدلها Bellman-Ford.",
                studyTip = "اربط دائماً Dijkstra بالحواف الموجبة، و Bellman-Ford مع الحواف السالبة.",
                difficulty = "متقدم"
            ),
            QuizQuestion(
                question = "ما الفرق الأساسي بين خوارزميات BFS (اتساعاً) و DFS (عمقاً) من حيث بنية البيانات المستخدمة؟",
                options = listOf(
                    "BFS تستخدم طابور Queue بينما DFS تستخدم مكدس Stack",
                    "BFS تستخدم مكدس Stack بينما DFS تستخدم طابور Queue",
                    "كلاهما يستخدم شجرة بحث ثنائية متوازنة",
                    "BFS لا تستهلك أي مساحة إضافية في الذاكرة"
                ),
                correctIndex = 0,
                explanation = "تعتمد BFS على معالجة المستوى تلو الآخر بنظام FIFO عبر Queue، بينما DFS تتعمق في المسارات بنظام LIFO عبر Stack (أو الاستدعاء الذاتي Recursion).",
                studyTip = "تذكر: Queue = BFS للبحث عن أقصر مسار بالخطوات، Stack = DFS لحل المتاهات والدورات.",
                difficulty = "متوسط"
            ),
            QuizQuestion(
                question = "في هندسة البرمجيات، ماذا يعني مبدأ 'Single Responsibility Principle' من مبادئ SOLID؟",
                options = listOf(
                    "يجب أن يكون لكل صنف Class سبب واحد فقط للتغيير ومسؤولية محددة",
                    "يجب دمج جميع دوال الواجهة في دالة تنفيذية واحدة",
                    "منع وراثة الأصناف نهائياً واستبدالها بالواجهات فقط",
                    "تنفيذ البرمجة المتزامنة عبر خيط تنفيذ واحد"
                ),
                correctIndex = 0,
                explanation = "ينص مبدأ المسؤولية الفردية SRP على أن الصنف يجب أن يؤدي وظيفة واحدة متماسكة لتقليل الاعتماديات وتسهيل الصيانة والاختبار.",
                studyTip = "إذا كان كود الصنف يتعامل مع قاعدة البيانات وتنسيق النصوص في آن واحد، فهو ينتهك SRP!",
                difficulty = "أساسي"
            )
        )
    }
}
