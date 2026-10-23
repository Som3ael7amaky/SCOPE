package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.gpa.CourseSimulation
import com.example.data.gpa.GpaScale
import com.example.data.gpa.GpaTimeMachineEngine
import com.example.data.model.Subject
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeAccent
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber
import kotlin.math.roundToInt

@Composable
fun GpaTimeMachineDialog(
    subjects: List<Subject>,
    initialCumulativeGpa: Double = 4.15,
    initialCompletedCredits: Int = 54,
    onSaveTarget: (Double) -> Unit = {},
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(GpaScale.FIVE_POINT) }
    var completedCredits by remember { mutableIntStateOf(initialCompletedCredits) }
    var currentGpa by remember { mutableDoubleStateOf(initialCumulativeGpa) }

    // Target GPA slider
    var targetGpa by remember {
        mutableDoubleStateOf(
            if (scale == GpaScale.FIVE_POINT) 4.50 else 3.60
        )
    }

    // Active Courses for What-If scenario
    val courseSimulations = remember(subjects) {
        mutableStateListOf<CourseSimulation>().apply {
            if (subjects.isNotEmpty()) {
                addAll(
                    subjects.map { subj ->
                        CourseSimulation(
                            subjectId = subj.id,
                            subjectName = subj.name,
                            subjectCode = subj.code.ifBlank { "CS" },
                            creditHours = if (subj.creditHours > 0) subj.creditHours else 3,
                            selectedGrade = "A",
                            colorHex = subj.colorHex
                        )
                    }
                )
            } else {
                addAll(
                    listOf(
                        CourseSimulation("s1", "الخوارزميات وهياكل البيانات", "CS201", 3, "A", "#2563EB"),
                        CourseSimulation("s2", "أنظمة قواعد البيانات الموزعة", "CS310", 3, "B+", "#0284C7"),
                        CourseSimulation("s3", "الذكاء الاصطناعي وتعلم الآلة", "CS340", 4, "A+", "#8B5CF6"),
                        CourseSimulation("s4", "هندسة البرمجيات وتطوير النظم", "CS380", 3, "A", "#10B981")
                    )
                )
            }
        }
    }

    // Real-time calculations
    val projectedCumulativeGpa by remember {
        derivedStateOf {
            GpaTimeMachineEngine.calculateProjectedGpa(
                completedCredits = completedCredits,
                currentCumulativeGpa = currentGpa,
                simulations = courseSimulations,
                scale = scale
            )
        }
    }

    val semesterGpa by remember {
        derivedStateOf {
            GpaTimeMachineEngine.calculateSemesterGpa(courseSimulations, scale)
        }
    }

    val deltaGpa by remember {
        derivedStateOf {
            ((projectedCumulativeGpa - currentGpa) * 100.0).roundToInt() / 100.0
        }
    }

    val roadmapResult by remember {
        derivedStateOf {
            GpaTimeMachineEngine.calculateRecoveryRoadmap(
                completedCredits = completedCredits,
                currentCumulativeGpa = currentGpa,
                simulations = courseSimulations,
                targetCumulativeGpa = targetGpa,
                scale = scale
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("gpa_time_machine_dialog"),
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF030712).copy(alpha = 0.96f),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                Brush.verticalGradient(listOf(ScopePrimary, ScopeAccent.copy(alpha = 0.4f)))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ScopePrimary.copy(alpha = 0.2f))
                                .border(1.dp, ScopePrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = ScopePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "آلة زمن المعدل GPA 🚀",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Text(
                                text = "محاكي السيناريوهات وخارطة استعادة الهدف الأكاديمي",
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_gpa_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scale Toggle (5.0 vs 4.0)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (scale == GpaScale.FIVE_POINT) ScopePrimary else Color.Transparent)
                            .clickable {
                                scale = GpaScale.FIVE_POINT
                                if (currentGpa <= 4.0) currentGpa = 4.15
                                targetGpa = 4.50
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "مقياس 5.0 (نظام الجامعات)",
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = if (scale == GpaScale.FIVE_POINT) FontWeight.Bold else FontWeight.Medium,
                            color = if (scale == GpaScale.FIVE_POINT) Color.White else Color(0xFF94A3B8)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (scale == GpaScale.FOUR_POINT) ScopePrimary else Color.Transparent)
                            .clickable {
                                scale = GpaScale.FOUR_POINT
                                if (currentGpa > 4.0) currentGpa = 3.45
                                targetGpa = 3.75
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "مقياس 4.0 (النظام العالمي)",
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = if (scale == GpaScale.FOUR_POINT) FontWeight.Bold else FontWeight.Medium,
                            color = if (scale == GpaScale.FOUR_POINT) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Projected GPA Holographic Hero Meter
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1329)),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (deltaGpa >= 0) ScopeFocusGreen.copy(alpha = 0.5f) else ScopeWarningAmber.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Current GPA
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "المعدل التراكمي الحالي",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = "%.2f".format(currentGpa),
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp,
                                            color = Color(0xFFE2E8F0)
                                        )
                                        Text(
                                            text = "($completedCredits ساعة منجزة)",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    // Delta Indicator
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (deltaGpa >= 0) ScopeFocusGreen.copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = if (deltaGpa >= 0) "+%.2f ▲".format(deltaGpa) else "%.2f ▼".format(deltaGpa),
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (deltaGpa >= 0) ScopeFocusGreen else Color(0xFFEF4444)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "المعدل الفصلي: %.2f".format(semesterGpa),
                                            fontFamily = CairoFontFamily,
                                            fontSize = 10.sp,
                                            color = ScopeAccent
                                        )
                                    }

                                    // Projected Cumulative GPA
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "المعدل المتوقع (Projected)",
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = ScopePrimary
                                        )
                                        Text(
                                            text = "%.2f".format(projectedCumulativeGpa),
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 28.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = GpaTimeMachineEngine.getHonorsClassification(projectedCumulativeGpa, scale),
                                            fontFamily = CairoFontFamily,
                                            fontSize = 10.sp,
                                            color = ScopeFocusGreen
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // What-If Simulator Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "محاكاة درجات المقررات النشطة (What-If):",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = "${courseSimulations.size} مواد مسجلة",
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Active Course simulation cards
                    items(courseSimulations.indices.toList()) { index ->
                        val course = courseSimulations[index]
                        CourseSimulationCard(
                            course = course,
                            scale = scale,
                            onGradeChange = { newGrade ->
                                courseSimulations[index] = course.copy(selectedGrade = newGrade)
                            }
                        )
                    }

                    // Target Recovery Roadmap Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.85f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ScopeAccent.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Psychology, contentDescription = null, tint = ScopeAccent, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "خارطة استعادة الهدف الأكاديمي 🎯",
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                    }

                                    Text(
                                        text = "المستهدف: %.2f".format(targetGpa),
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = ScopeAccent
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Target Slider
                                Slider(
                                    value = targetGpa.toFloat(),
                                    onValueChange = { targetGpa = (it * 100.0).roundToInt() / 100.0 },
                                    valueRange = if (scale == GpaScale.FIVE_POINT) 3.5f..5.0f else 2.5f..4.0f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = ScopeAccent,
                                        activeTrackColor = ScopeAccent,
                                        inactiveTrackColor = Color(0xFF334155)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Preset Target Chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val presets = if (scale == GpaScale.FIVE_POINT) {
                                        listOf("4.75 (شرف 1)", "4.50 (تفوق)", "4.25 (شرف 2)")
                                    } else {
                                        listOf("3.85 (شرف 1)", "3.60 (تفوق)", "3.35 (شرف 2)")
                                    }

                                    presets.forEach { preset ->
                                        val pVal = preset.substringBefore(" ").toDoubleOrNull() ?: 4.5
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF1E293B))
                                                .clickable { targetGpa = pVal }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = preset,
                                                fontFamily = CairoFontFamily,
                                                fontSize = 10.sp,
                                                color = Color(0xFFCBD5E1)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Roadmap Advice Banner
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (roadmapResult.isAchievableThisSemester) ScopeFocusGreen.copy(alpha = 0.12f)
                                            else ScopeWarningAmber.copy(alpha = 0.15f)
                                        )
                                        .border(
                                            1.dp,
                                            if (roadmapResult.isAchievableThisSemester) ScopeFocusGreen.copy(alpha = 0.3f)
                                            else ScopeWarningAmber.copy(alpha = 0.4f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(
                                            imageVector = if (roadmapResult.isAchievableThisSemester) Icons.Default.Check else Icons.Default.Info,
                                            contentDescription = null,
                                            tint = if (roadmapResult.isAchievableThisSemester) ScopeFocusGreen else ScopeWarningAmber,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = roadmapResult.summaryAdvice,
                                            fontFamily = CairoFontFamily,
                                            fontSize = 11.sp,
                                            color = Color.White,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Button
                Button(
                    onClick = {
                        onSaveTarget(targetGpa)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_gpa_target_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                ) {
                    Text(
                        text = "اعتماد المعدل المستهدف (%.2f) كهدف فصلي 🎯".format(targetGpa),
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseSimulationCard(
    course: CourseSimulation,
    scale: GpaScale,
    onGradeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.7f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.subjectName,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Text(
                        text = "${course.subjectCode} • ${course.creditHours} ساعات معتمدة",
                        fontFamily = CairoFontFamily,
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ScopePrimary.copy(alpha = 0.2f))
                        .border(1.dp, ScopePrimary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${course.selectedGrade} (${GpaTimeMachineEngine.getGradePoints(course.selectedGrade, scale)} نق)",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = ScopePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grade Chips selector
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val grades = listOf("A+", "A", "B+", "B", "C+", "C", "D", "F")
                items(grades) { g ->
                    val isSelected = course.selectedGrade.equals(g, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ScopePrimary else Color(0xFF1E293B))
                            .clickable { onGradeChange(g) }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = g,
                            fontFamily = CairoFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}
