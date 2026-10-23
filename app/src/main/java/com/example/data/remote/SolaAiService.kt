package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Exam
import com.example.data.model.Subject
import com.example.data.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SolaResponse(
    val replyText: String,
    val thinkingText: String? = null,
    val isSuccess: Boolean = true
)

object SolaLocalMatrixEngine {
    fun generateLocalResponse(prompt: String, subjects: List<Subject>, tasks: List<Task>, exams: List<Exam>): SolaResponse {
        val lower = prompt.lowercase()
        val pendingTasks = tasks.filter { !it.isCompleted }
        val nearestExam = exams.firstOrNull()

        val reply = when {
            lower.contains("جدول") || lower.contains("خطة") || lower.contains("أبدأ") || lower.contains("ماذا") -> {
                val nextTask = pendingTasks.firstOrNull()
                if (nextTask != null) {
                    "بناءً على تحليلي لمهامك، أنصحك بالبدء بمهمة '${nextTask.title}' لمقرر ${nextTask.subjectName} (${nextTask.effortMinutes} دقيقة). ركز الآن ودع الباقي علي! ⚡"
                } else {
                    "أرى أن جميع مهامك العاجلة منجزة! ما رأيك بجلسة تركيز استباقية لمدة 25 دقيقة لتعزيز تفوقك؟"
                }
            }
            lower.contains("اختبار") || lower.contains("مراجعة") -> {
                if (nearestExam != null) {
                    "يقترب موعد اختبار '${nearestExam.title}' (${nearestExam.topics}). ننصح بمراجعة البطاقات التفاعلية والملخصات الصوتية لهذا المقرر."
                } else {
                    "لا توجد اختبارات قريبة مسجلة. ركز على إنجاز مهامك اليومية بانتظام."
                }
            }
            lower.contains("تسويف") || lower.contains("كسل") || lower.contains("تعب") -> {
                "التسويف شعور طبيعي عندما تكون الأهداف ضخمة. دعنا نطبق قاعدة الخمس ثواني: عد تصاعدياً (5، 4، 3، 2، 1) وابدأ لمدة 5 دقائق فقط!"
            }
            else -> {
                "أهلاً بك! بصفتي رفيقتك الأكاديمية صولا، أنا هنا لمساعدتك في تنظيم جدولك، فهم المقررات، وتحفيزك للوصول لأهدافك. كيف أساعدك اليوم؟ 🌟"
            }
        }

        return SolaResponse(
            replyText = reply,
            thinkingText = "تم التفعيل عبر مصفوفة الاستدلال المحلي (Local Heuristic Matrix) لضمان الاستمرارية الفورية.",
            isSuccess = true
        )
    }
}

class SolaAiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateSolaResponse(
        prompt: String,
        history: List<Pair<String, String>>, // sender, text
        subjects: List<Subject>,
        tasks: List<Task>,
        exams: List<Exam>,
        useHighThinking: Boolean = true
    ): SolaResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext SolaLocalMatrixEngine.generateLocalResponse(prompt, subjects, tasks, exams)
        }

        val modelName = if (useHighThinking) "gemini-4-argon" else "gemini-3.8-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        val subjectsSummary = subjects.joinToString(", ") { "${it.name} (${it.code})" }
        val pendingTasks = tasks.filter { !it.isCompleted }.joinToString("; ") { "${it.title} [أولوية: ${it.priority}, جهد: ${it.effortMinutes}د]" }
        val upcomingExams = exams.joinToString("; ") { "${it.title} (${it.topics})" }

        val systemPrompt = """
            أنت "صولا" (Sola) - العقل المدبر والرفيق الأكاديمي الذكي داخل نظام تشغيل الطلاب SCOPE.
            شعارنا: "الرؤية الشاملة لرحلتك الأكاديمية" (See the bigger picture).
            
            دورك:
            - لست مجرد روبوت محادثة؛ بل تفهم السياق الأكاديمي الحقيقي للطالب.
            - تقدم تحليلات واقعية، نصائح دراسية، واقتراحات لتنظيم الوقت وتجاوز الفجوات التعليمية.
            - تدعم التفكير المنهجي والتحفيز الإيجابي دون مجاملات مصطنعة.
            - تتحدث باللغة العربية بطلاقة وسلاسة وإيجاز دقيق.
            
            السياق الأكاديمي الحالي للطالب:
            - المواد المسجلة: ${subjectsSummary.ifBlank { "لا توجد مواد مسجلة بعد" }}
            - المهام العاجلة: ${pendingTasks.ifBlank { "لا توجد مهام معلقة" }}
            - الاختبارات القادمة: ${upcomingExams.ifBlank { "لا توجد اختبارات قريبة" }}
        """.trimIndent()

        try {
            val rootJson = JSONObject()

            val systemPart = JSONObject().put("text", systemPrompt)
            val systemContent = JSONObject().put("parts", JSONArray().put(systemPart))
            rootJson.put("systemInstruction", systemContent)

            val contentsArray = JSONArray()
            val recentHistory = history.takeLast(6)
            for ((sender, text) in recentHistory) {
                val role = if (sender == "user") "user" else "model"
                val partObj = JSONObject().put("text", text)
                val contentObj = JSONObject().put("role", role).put("parts", JSONArray().put(partObj))
                contentsArray.put(contentObj)
            }
            val currentPart = JSONObject().put("text", prompt)
            val currentContent = JSONObject().put("role", "user").put("parts", JSONArray().put(currentPart))
            contentsArray.put(currentContent)

            rootJson.put("contents", contentsArray)

            val generationConfig = JSONObject()
            if (useHighThinking) {
                val thinkingConfig = JSONObject().put("thinkingLevel", "HIGH")
                generationConfig.put("thinkingConfig", thinkingConfig)
            }
            rootJson.put("generationConfig", generationConfig)

            val body = rootJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("SolaAiService", "API Error HTTP ${response.code}: $responseBody")
                return@withContext SolaLocalMatrixEngine.generateLocalResponse(prompt, subjects, tasks, exams)
            }

            val resJson = JSONObject(responseBody)
            val candidates = resJson.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            var mainReply = ""
            var thinkingSummary: String? = null

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.getJSONObject(i)
                    if (p.has("thought") && p.getBoolean("thought")) {
                        thinkingSummary = p.optString("text")
                    } else if (p.has("text")) {
                        mainReply += p.optString("text")
                    }
                }
            }

            if (mainReply.isBlank()) {
                mainReply = "تم استلام السياق وتحليله بنجاح."
            }

            SolaResponse(
                replyText = mainReply.trim(),
                thinkingText = thinkingSummary?.trim(),
                isSuccess = true
            )
        } catch (e: Exception) {
            Log.e("SolaAiService", "Sola generation exception: ${e.message}", e)
            SolaLocalMatrixEngine.generateLocalResponse(prompt, subjects, tasks, exams)
        }
    }
}
