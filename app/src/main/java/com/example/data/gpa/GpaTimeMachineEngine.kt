package com.example.data.gpa

import com.example.data.model.Subject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class GpaScale(val scaleName: String, val maxGpa: Double) {
    FIVE_POINT("مقياس 5.0 (الجامعات السعودية)", 5.0),
    FOUR_POINT("مقياس 4.0 (النظام الأمريكي)", 4.0)
}

data class GradeOption(
    val letter: String,
    val points5: Double,
    val points4: Double,
    val labelAr: String
)

data class CourseSimulation(
    val subjectId: String,
    val subjectName: String,
    val subjectCode: String,
    val creditHours: Int = 3,
    val selectedGrade: String = "A",
    val colorHex: String = "#2563EB"
)

data class TargetRoadmapResult(
    val isAchievableThisSemester: Boolean,
    val targetGpa: Double,
    val projectedGpa: Double,
    val requiredSemesterGpa: Double,
    val recommendedGrades: Map<String, String>, // subjectId -> Grade Letter
    val summaryAdvice: String
)

object GpaTimeMachineEngine {

    val GRADE_OPTIONS_5: List<GradeOption> = listOf(
        GradeOption("A+", 5.0, 4.0, "ممتاز مرتفع (A+)"),
        GradeOption("A", 4.75, 3.75, "ممتاز (A)"),
        GradeOption("B+", 4.5, 3.5, "جيد جداً مرتفع (B+)"),
        GradeOption("B", 4.0, 3.0, "جيد جداً (B)"),
        GradeOption("C+", 3.5, 2.5, "جيد مرتفع (C+)"),
        GradeOption("C", 3.0, 2.0, "جيد (C)"),
        GradeOption("D+", 2.5, 1.5, "مقبول مرتفع (D+)"),
        GradeOption("D", 2.0, 1.0, "مقبول (D)"),
        GradeOption("F", 1.0, 0.0, "راسب (F)")
    )

    fun getGradePoints(gradeLetter: String, scale: GpaScale): Double {
        val grade = GRADE_OPTIONS_5.firstOrNull { it.letter.equals(gradeLetter, ignoreCase = true) }
            ?: return if (scale == GpaScale.FIVE_POINT) 4.75 else 3.75
        return if (scale == GpaScale.FIVE_POINT) grade.points5 else grade.points4
    }

    fun calculateProjectedGpa(
        completedCredits: Int,
        currentCumulativeGpa: Double,
        simulations: List<CourseSimulation>,
        scale: GpaScale
    ): Double {
        val validPrevCredits = max(0, completedCredits)
        val currentSemesterCredits = simulations.sumOf { max(1, it.creditHours) }
        val totalCredits = validPrevCredits + currentSemesterCredits

        if (totalCredits <= 0) return currentCumulativeGpa

        val previousPoints = validPrevCredits * currentCumulativeGpa
        var currentPoints = 0.0
        for (sim in simulations) {
            val pts = getGradePoints(sim.selectedGrade, scale)
            currentPoints += pts * max(1, sim.creditHours)
        }

        val totalPoints = previousPoints + currentPoints
        val projected = totalPoints / totalCredits
        return (projected * 100.0).roundToInt() / 100.0
    }

    fun calculateSemesterGpa(
        simulations: List<CourseSimulation>,
        scale: GpaScale
    ): Double {
        val totalCredits = simulations.sumOf { max(1, it.creditHours) }
        if (totalCredits == 0) return 0.0
        var currentPoints = 0.0
        for (sim in simulations) {
            val pts = getGradePoints(sim.selectedGrade, scale)
            currentPoints += pts * max(1, sim.creditHours)
        }
        val semGpa = currentPoints / totalCredits
        return (semGpa * 100.0).roundToInt() / 100.0
    }

    fun calculateRecoveryRoadmap(
        completedCredits: Int,
        currentCumulativeGpa: Double,
        simulations: List<CourseSimulation>,
        targetCumulativeGpa: Double,
        scale: GpaScale
    ): TargetRoadmapResult {
        val validPrevCredits = max(0, completedCredits)
        val currentSemesterCredits = simulations.sumOf { max(1, it.creditHours) }
        val totalCredits = validPrevCredits + currentSemesterCredits

        if (currentSemesterCredits == 0) {
            return TargetRoadmapResult(
                isAchievableThisSemester = false,
                targetGpa = targetCumulativeGpa,
                projectedGpa = currentCumulativeGpa,
                requiredSemesterGpa = targetCumulativeGpa,
                recommendedGrades = emptyMap(),
                summaryAdvice = "يرجى إضافة مقررات مسجلة لهذا الفصل لحساب خارطة الاستعادة."
            )
        }

        // targetTotalPoints = targetCumulativeGpa * totalCredits
        // requiredCurrentPoints = targetTotalPoints - (validPrevCredits * currentCumulativeGpa)
        // requiredSemesterGpa = requiredCurrentPoints / currentSemesterCredits
        val targetTotalPoints = targetCumulativeGpa * totalCredits
        val previousPoints = validPrevCredits * currentCumulativeGpa
        val requiredSemesterPoints = targetTotalPoints - previousPoints
        val requiredSemesterGpaRaw = requiredSemesterPoints / currentSemesterCredits
        val requiredSemesterGpa = (requiredSemesterGpaRaw * 100.0).roundToInt() / 100.0

        val maxScale = scale.maxGpa
        val isAchievable = requiredSemesterGpa <= maxScale

        val recommendedMap = mutableMapOf<String, String>()

        if (isAchievable) {
            // Find appropriate letter grade
            val targetLetter = when {
                requiredSemesterGpa >= (if (scale == GpaScale.FIVE_POINT) 4.85 else 3.85) -> "A+"
                requiredSemesterGpa >= (if (scale == GpaScale.FIVE_POINT) 4.60 else 3.65) -> "A"
                requiredSemesterGpa >= (if (scale == GpaScale.FIVE_POINT) 4.25 else 3.35) -> "B+"
                requiredSemesterGpa >= (if (scale == GpaScale.FIVE_POINT) 3.75 else 2.85) -> "B"
                requiredSemesterGpa >= (if (scale == GpaScale.FIVE_POINT) 3.25 else 2.35) -> "C+"
                else -> "C"
            }
            simulations.forEach {
                recommendedMap[it.subjectId] = targetLetter
            }
            val advice = "للوصول لمعدل $targetCumulativeGpa، يلزمك معدل فصلي لا يقل عن $requiredSemesterGpa ($targetLetter كمتوسط في جميع مقرراتك)."
            return TargetRoadmapResult(
                isAchievableThisSemester = true,
                targetGpa = targetCumulativeGpa,
                projectedGpa = (calculateProjectedGpa(completedCredits, currentCumulativeGpa, simulations, scale)),
                requiredSemesterGpa = max(0.0, requiredSemesterGpa),
                recommendedGrades = recommendedMap,
                summaryAdvice = advice
            )
        } else {
            // Max achievable this semester
            val allAplusSims = simulations.map { it.copy(selectedGrade = "A+") }
            val maxPossibleGpa = calculateProjectedGpa(completedCredits, currentCumulativeGpa, allAplusSims, scale)
            allAplusSims.forEach {
                recommendedMap[it.subjectId] = "A+"
            }
            val advice = "المعدل المستهدف يتطلب فصولاً إضافية. أقصى معدل تراكمي يمكنك بلوغه هذا الفصل هو $maxPossibleGpa (عند تحقيق A+ في كل المقررات)."
            return TargetRoadmapResult(
                isAchievableThisSemester = false,
                targetGpa = targetCumulativeGpa,
                projectedGpa = maxPossibleGpa,
                requiredSemesterGpa = requiredSemesterGpa,
                recommendedGrades = recommendedMap,
                summaryAdvice = advice
            )
        }
    }

    fun getHonorsClassification(gpa: Double, scale: GpaScale): String {
        return if (scale == GpaScale.FIVE_POINT) {
            when {
                gpa >= 4.75 -> "مرتبة الشرف الأولى 🌟"
                gpa >= 4.25 -> "مرتبة الشرف الثانية 🎖️"
                gpa >= 3.75 -> "تقدير جيد جداً مرتفع 📈"
                gpa >= 2.75 -> "تقدير جيد جداً 📘"
                else -> "في نطاق التحسين المستمر 🚀"
            }
        } else {
            when {
                gpa >= 3.75 -> "مرتبة الشرف الأولى (Summa Cum Laude) 🌟"
                gpa >= 3.50 -> "مرتبة الشرف الثانية (Magna Cum Laude) 🎖️"
                gpa >= 3.00 -> "تقدير جيد جداً 📈"
                else -> "في نطاق التحسين المستمر 🚀"
            }
        }
    }
}
