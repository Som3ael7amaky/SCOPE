package com.example.data.vision

import com.example.data.model.ConceptMap
import com.example.data.model.ConceptMapNode
import com.example.data.model.Flashcard

data class VisionScanResult(
    val title: String,
    val subjectName: String,
    val extractedRawText: String,
    val flashcards: List<Flashcard>,
    val conceptMap: ConceptMap,
    val keyTermsCount: Int
)

object SolaVisionEngine {

    /**
     * OCR & Document Text Extraction Pipeline:
     * Analyzes raw academic lecture text / slides and extracts 3D Flashcards
     * and a connected Concept Map.
     */
    fun processDocument(
        rawText: String,
        subjectName: String,
        deckTitle: String
    ): VisionScanResult {
        val cleanText = rawText.trim()

        // Extract key terms and concept blocks
        val lines = cleanText.split("\n").map { it.trim() }.filter { it.isNotBlank() }

        val flashcards = mutableListOf<Flashcard>()
        val nodes = mutableListOf<ConceptMapNode>()

        val safeTitle = deckTitle.ifBlank { "مفاهيم ملخصة من الوثيقة" }
        val rootNodeId = "node_root"

        // Root Core Node
        nodes.add(
            ConceptMapNode(
                id = rootNodeId,
                title = safeTitle,
                summary = "المفهوم المحوري المستخرج من الوثيقة الأكاديمية",
                category = "CORE",
                colorHex = "#2563EB",
                connections = mutableListOf()
            )
        )

        // Parse questions, definitions or bullet points
        var cardIndex = 1
        lines.forEachIndexed { idx, line ->
            if (line.contains(":") || line.contains("؟") || line.contains("-") || line.contains("=")) {
                val delimiter = when {
                    line.contains("؟") -> "؟"
                    line.contains(":") -> ":"
                    line.contains("=") -> "="
                    else -> "-"
                }
                val parts = line.split(delimiter, limit = 2)
                if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                    val question = parts[0].trim() + if (delimiter == "؟") "؟" else ""
                    val answer = parts[1].trim()

                    flashcards.add(
                        Flashcard(
                            id = "fc_vis_${System.currentTimeMillis()}_$cardIndex",
                            subjectName = subjectName,
                            front = question,
                            back = answer,
                            masteryLevel = 1
                        )
                    )

                    val nodeId = "node_sub_$cardIndex"
                    nodes.add(
                        ConceptMapNode(
                            id = nodeId,
                            title = question.take(28),
                            summary = answer.take(70) + if (answer.length > 70) "..." else "",
                            category = if (cardIndex % 2 == 0) "APPLICATION" else "SUB_TOPIC",
                            colorHex = if (cardIndex % 2 == 0) "#10B981" else "#8B5CF6",
                            connections = emptyList()
                        )
                    )
                    cardIndex++
                }
            }
        }

        // Fallback default cards if raw text had no explicit delimiters
        if (flashcards.isEmpty()) {
            val sampleQuestions = listOf(
                "ما هي الفكرة الجوهرية للوثيقة؟" to cleanText.take(120),
                "ما أهم الشروط والمتطلبات؟" to "التركيز على التطبيق العملي، ومراجعة العلاقات بين المتغيرات المذكورة.",
                "كيف يتم تطبيق هذا المفهوم في الامتحان؟" to "يتم اختبار الطلاب عادة عبر مسائل التحليل والمقارنة المباشرة."
            )
            sampleQuestions.forEachIndexed { i, pair ->
                flashcards.add(
                    Flashcard(
                        id = "fc_vis_${System.currentTimeMillis()}_$i",
                        subjectName = subjectName,
                        front = pair.first,
                        back = pair.second,
                        masteryLevel = 1
                    )
                )
                nodes.add(
                    ConceptMapNode(
                        id = "node_sub_$i",
                        title = pair.first.take(25),
                        summary = pair.second.take(60),
                        category = "SUB_TOPIC",
                        colorHex = "#0284C7"
                    )
                )
            }
        }

        // Link root node to all children
        val childIds = nodes.filter { it.id != rootNodeId }.map { it.id }
        val updatedNodes = nodes.map { node ->
            if (node.id == rootNodeId) {
                node.copy(connections = childIds)
            } else {
                node
            }
        }

        val conceptMap = ConceptMap(
            title = safeTitle,
            subjectName = subjectName,
            nodes = updatedNodes
        )

        return VisionScanResult(
            title = safeTitle,
            subjectName = subjectName,
            extractedRawText = cleanText,
            flashcards = flashcards,
            conceptMap = conceptMap,
            keyTermsCount = flashcards.size + updatedNodes.size
        )
    }

    /**
     * Curated Slide Presets for instant live demonstration scanning.
     */
    val SAMPLE_ACADEMIC_SLIDES = listOf(
        SampleSlide(
            title = "خوارزميات الأشجار المتوازنة (AVL & Red-Black)",
            subjectName = "الخوارزميات وهياكل البيانات",
            content = """
                شجرة AVL: شجرة بحث ثنائية ذاتية التوازن حيث لا يتجاوز فرق الارتفاع بين الشجرتين الفرعيتين 1.
                معامل التوازن Balance Factor: يساوي ارتفاع الشجرة اليسرى ناقص ارتفاع الشجرة اليمنى (-1، 0، +1).
                الدوران الأحادي Single Rotation: يستخدم في حالات Left-Left أو Right-Right لاستعادة التوازن.
                الدوران المزدوج Double Rotation: يستخدم في حالات Left-Right أو Right-Left.
                التعقيد الزمني Time Complexity: البحث والإدراج والحذف يستغرق O(log n) دائماً.
            """.trimIndent()
        ),
        SampleSlide(
            title = "نظرية CAP ونظم قواعد البيانات الموزعة",
            subjectName = "أنظمة قواعد البيانات الموزعة",
            content = """
                مبدأ الاتساق Consistency: جميع العقد ترى نفس البيانات في نفس اللحظة.
                التوافر Availability: كل طلب غير معطل يتلقى استجابة دون ضمان أنها أحدث نسخة.
                تحمل التجزئة Partition Tolerance: استمرار النظام في العمل رغم فقدان الرسائل أو انقطاع الشبكة.
                قاعدة بروير Brewer's Law: يستحيل على نظام موزع ضمان الخصائص الثلاث معاً في نفس الوقت.
                أنظمة CP: تضحي بالتوافر من أجل دقة البيانات مثل BigTable و HBase.
                أنظمة AP: تضحي بالاتساق الصارم لصالح استمرار الخدمة مثل Cassandra و DynamoDB.
            """.trimIndent()
        ),
        SampleSlide(
            title = "التعلم العميق وشبكات Transformer الانتباهية",
            subjectName = "الذكاء الاصطناعي وتعلم الآلة",
            content = """
                آلية الانتباه الذاتي Self-Attention: تتيح للنموذج وزن أهمية كل كلمة بالنسبة للكلمات الأخرى.
                متجهات Query و Key و Value: يتم حساب التوافق بين الاستعلام والمفتاح لتحديد أوزان القيمة.
                المعالجة المتوازية Parallelization: تجاوز مشكلة التكرار الزمني في RNNs مما سرع تدريب النماذج الضخمة.
                التشفير الموضعي Positional Encoding: يزود النموذج بترتيب الكلمات لغياب التكرار التسلسلي.
            """.trimIndent()
        )
    )
}

data class SampleSlide(
    val title: String,
    val subjectName: String,
    val content: String
)
