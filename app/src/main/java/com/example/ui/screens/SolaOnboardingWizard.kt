package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DiagnosticPreferences
import com.example.ui.mascot.SolaEmotionState
import com.example.ui.mascot.SolaMascotCanvas
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber

@Composable
fun SolaOnboardingWizard(
    initialPreferences: DiagnosticPreferences = DiagnosticPreferences(),
    onComplete: (DiagnosticPreferences) -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    var selectedGoal by remember { mutableStateOf(initialPreferences.academicGoal) }
    var selectedPeak by remember { mutableStateOf(initialPreferences.peakWindow) }
    var selectedChallenge by remember { mutableStateOf(initialPreferences.primaryChallenge) }
    var selectedStyle by remember { mutableStateOf(initialPreferences.studyStyle) }
    var selectedTone by remember { mutableStateOf(initialPreferences.solaTone) }

    val currentEmotion = when (step) {
        1 -> SolaEmotionState.THINKING_ANALYTICAL
        2 -> SolaEmotionState.IDLE
        3 -> SolaEmotionState.EMPATHETIC_SAD
        4 -> SolaEmotionState.ASSERTIVE_GUIDE
        5 -> SolaEmotionState.HAPPY_CELEBRATION
        else -> SolaEmotionState.HAPPY_CELEBRATION
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("sola_onboarding_wizard"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Top Navigation Bar (Back, Progress, Skip)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (step > 1) {
                        IconButton(
                            onClick = { step-- },
                            modifier = Modifier.testTag("survey_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "السابق",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    } else {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Progress Counter
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "تشخيص صولا الأكاديمي ($step من 5)",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ScopePrimary
                        )
                    }

                    // Explicit Skip button
                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.testTag("skip_survey_button")
                    ) {
                        Text(
                            text = "تخطي",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { step / 5f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ScopePrimary,
                    trackColor = ScopePrimary.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Question Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 3D Animated Sola Companion Hero
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .testTag("wizard_sola_canvas"),
                        contentAlignment = Alignment.Center
                    ) {
                        SolaMascotCanvas(
                            emotionState = currentEmotion,
                            size = 100.dp,
                            showAura = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic Question Bubble
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(200))
                        },
                        label = "question_content"
                    ) { currentStep ->
                        when (currentStep) {
                            1 -> SurveyQuestionBlock(
                                prompt = "أهلاً بك في SCOPE! أنا صولا، رفيقتك الأكاديمية الذكية. ما هو هدفك الأكاديمي الأول لهذا الفصل؟",
                                subtitle = "الهدف الأكاديمي الرئيسي (Academic Goal)",
                                options = listOf(
                                    "رفع المعدل التراكمي (GPA Improvement)" to "رفع المعدل التراكمي",
                                    "التغلب على التسويف (Overcoming Procrastination)" to "التغلب على التسويف",
                                    "تنظيم وإدارة الجدول (Schedule Organization)" to "تنظيم وإدارة الجدول",
                                    "الاستعداد المكثف للاختبارات (Exam Prep)" to "الاستعداد المكثف للاختبارات"
                                ),
                                selected = selectedGoal,
                                onSelect = { selectedGoal = it }
                            )

                            2 -> SurveyQuestionBlock(
                                prompt = "متى تشعر بأن طاقتك الذهنية وقدرتك على استيعاب المعلومات تكون في ذروتها؟",
                                subtitle = "فترة ذروة الإنتاجية (Peak Productivity Window)",
                                options = listOf(
                                    "الصباح الباكر (06:00 - 11:00) ☀️" to "الصباح الباكر (06:00 - 11:00)",
                                    "فترة الظهيرة (12:00 - 16:00) 🌤️" to "فترة الظهيرة (12:00 - 16:00)",
                                    "المساء المريح (17:00 - 21:00) 🌆" to "المساء (17:00 - 21:00)",
                                    "بومة الليل (22:00 - 03:00) 🌙" to "بومة الليل (22:00 - 03:00)"
                                ),
                                selected = selectedPeak,
                                onSelect = { selectedPeak = it }
                            )

                            3 -> SurveyQuestionBlock(
                                prompt = "كل طالب يواجه تحديات معينة.. ما هو العائق الأكبر الذي يعطلك غالباً؟",
                                subtitle = "التحدي الأكاديمي الأكبر (Primary Study Challenge)",
                                options = listOf(
                                    "صعوبة البدء ومقاومة الانطلاق الأولية" to "صعوبة البدء ومقاومة الانطلاق",
                                    "تشتت التركيز بعد فترات قصيرة" to "تشتت التركيز بعد فترات قصيرة",
                                    "تراكم الجدول وضيق الوقت والمواعيد" to "تراكم الجدول وضيق الوقت",
                                    "استيعاب المفاهيم النظرية المعقدة" to "استيعاب المفاهيم المعقدة"
                                ),
                                selected = selectedChallenge,
                                onSelect = { selectedChallenge = it }
                            )

                            4 -> SurveyQuestionBlock(
                                prompt = "ما هي الطريقة الأنسب لك لحفظ المفاهيم وترسيخ المواد في ذاكرتك؟",
                                subtitle = "أسلوب الدراسة المفضل (Preferred Study Style)",
                                options = listOf(
                                    "بومودورو 25 دقيقة + استراحة 5 دقائق" to "بومودورو 25 دقيقة",
                                    "جلسات تركيز عميقة 50 دقيقة متصلة" to "جلسات تركيز عميقة 50 دقيقة",
                                    "بطاقات الاستذكار السريع والتكرار المتباعد" to "بطاقات الاستذكار السريع",
                                    "الملخصات الصوتية الذكية أثناء التنقل" to "الملخصات الصوتية الذكية"
                                ),
                                selected = selectedStyle,
                                onSelect = { selectedStyle = it }
                            )

                            5 -> SurveyQuestionBlock(
                                prompt = "كيف تفضل أن أتواصل معك وأوجهك أثناء فترات دراستك اليومية؟",
                                subtitle = "نبرة توجيه صولا (Sola Guidance Tone)",
                                options = listOf(
                                    "متعاطفة وداعمة (Empathetic & Gentle) 💜" to "متعاطفة وداعمة",
                                    "متوازنة وتحليلية (Balanced & Analytical) 💙" to "متوازنة وتحليلية",
                                    "حازمة ومحفزة بشدة (Strict & Assertive) 🧡" to "حازمة ومحفزة بشدة"
                                ),
                                selected = selectedTone,
                                onSelect = { selectedTone = it }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Button (Next or Complete)
                Button(
                    onClick = {
                        if (step < 5) {
                            step++
                        } else {
                            val prefs = DiagnosticPreferences(
                                academicGoal = selectedGoal,
                                peakWindow = selectedPeak,
                                primaryChallenge = selectedChallenge,
                                studyStyle = selectedStyle,
                                solaTone = selectedTone,
                                completedAt = System.currentTimeMillis()
                            )
                            onComplete(prefs)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("survey_action_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (step < 5) "التالي والخطوة التالية" else "حفظ التفضيلات وإطلاق صولا ✨ (+10 🪙)",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (step < 5) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SurveyQuestionBlock(
    prompt: String,
    subtitle: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Speech Bubble
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = prompt,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Start
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = subtitle,
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = ScopePrimary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Options
        options.forEach { (label, value) ->
            val isSelected = selected == value || selected.contains(value)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelect(value) }
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) ScopePrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) ScopePrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        fontFamily = CairoFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp,
                        color = if (isSelected) ScopePrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) ScopePrimary else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
