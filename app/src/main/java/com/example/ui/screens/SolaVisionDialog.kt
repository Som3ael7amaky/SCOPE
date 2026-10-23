package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ConceptMap
import com.example.data.model.ConceptMapNode
import com.example.data.model.Flashcard
import com.example.data.model.Subject
import com.example.data.vision.SolaVisionEngine
import com.example.data.vision.VisionScanResult
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeAccent
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SolaVisionDialog: Snap to 3D Flashcards & Node-based Concept Maps Studio.
 */
@Composable
fun SolaVisionDialog(
    subjects: List<Subject>,
    onSaveDeck: (List<Flashcard>, ConceptMap, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()?.name ?: "الخوارزميات وهياكل البيانات") }

    // Input Mode: 0: Instant Presets / Camera, 1: Text & Paste
    var inputMode by remember { mutableIntStateOf(0) }

    var selectedPresetIndex by remember { mutableIntStateOf(0) }
    var customText by remember { mutableStateOf("") }
    var deckTitle by remember { mutableStateOf("") }

    var isScanning by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<VisionScanResult?>(null) }
    var activeTab by remember { mutableIntStateOf(0) } // 0: 3D Flashcards, 1: Concept Map

    // 3D Card Flip Animation State
    var currentCardIndex by remember { mutableIntStateOf(0) }
    var isCardFlipped by remember { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isCardFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "card_flip"
    )

    // Laser Scanner Line Animation
    val infiniteTransition = rememberInfiniteTransition(label = "LaserScan")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "laser"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .clip(RoundedCornerShape(26.dp))
                .border(1.5.dp, ScopePrimary.copy(alpha = 0.5f), RoundedCornerShape(26.dp)),
            color = Color(0xFF090E17)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ScopePrimary.copy(alpha = 0.18f))
                                .border(1.dp, ScopePrimary.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = ScopePrimary, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "صولا فيجن (Sola Vision)",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "3D Scanner 📷",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 10.sp,
                                    color = ScopeAccent
                                )
                            }
                            Text(
                                text = "تحويل السلايدات والكتب إلى بطاقات 3D وخرائط مفاهيم ذكية",
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (scanResult == null) {
                    // --- INPUT & SCANNING VIEW ---
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Subject Selector
                        item {
                            Text(
                                text = "اختر المقرر التابع للوثيقة:",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(subjects) { subj ->
                                    val isSelected = subj.name == selectedSubject
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) ScopePrimary else Color(0xFF1E293B))
                                            .border(1.dp, if (isSelected) ScopeAccent else Color(0xFF334155), RoundedCornerShape(10.dp))
                                            .clickable { selectedSubject = subj.name }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = subj.name,
                                            fontFamily = CairoFontFamily,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp,
                                            color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }

                        // Mode Selector
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (inputMode == 0) ScopePrimary else Color.Transparent)
                                        .clickable { inputMode = 0 }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "سلايدات أكاديمية جاهزة",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (inputMode == 1) ScopePrimary else Color.Transparent)
                                        .clickable { inputMode = 1 }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "إدخال / لصق نص السلايد",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        if (inputMode == 0) {
                            // Instant Presets
                            item {
                                Text(
                                    text = "اختر نموذج وثيقة للفحص والاستخراج الفوري:",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFCBD5E1)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                SolaVisionEngine.SAMPLE_ACADEMIC_SLIDES.forEachIndexed { idx, slide ->
                                    val isSelected = selectedPresetIndex == idx
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                selectedPresetIndex = idx
                                                selectedSubject = slide.subjectName
                                                deckTitle = slide.title
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) ScopePrimary.copy(alpha = 0.2f) else Color(0xFF1E293B).copy(alpha = 0.6f)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) ScopePrimary else Color(0xFF334155)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = slide.title,
                                                    fontFamily = CairoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color.White
                                                )
                                                if (isSelected) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ScopeFocusGreen, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = slide.content.lines().firstOrNull() ?: "",
                                                fontFamily = CairoFontFamily,
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Text Input Mode
                            item {
                                OutlinedTextField(
                                    value = deckTitle,
                                    onValueChange = { deckTitle = it },
                                    label = { Text("عنوان المجموعة (مثال: مفاهيم التعلم العميق)", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ScopePrimary,
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = customText,
                                    onValueChange = { customText = it },
                                    label = { Text("نص السلايد، أو الملاحظات المصورة (استخراج OCR)", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                                    minLines = 5,
                                    maxLines = 8,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ScopePrimary,
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scan Trigger Button
                    Button(
                        onClick = {
                            isScanning = true
                            coroutineScope.launch {
                                delay(1200) // Realistic OCR scanning latency
                                val (title, content) = if (inputMode == 0) {
                                    val sample = SolaVisionEngine.SAMPLE_ACADEMIC_SLIDES[selectedPresetIndex]
                                    sample.title to sample.content
                                } else {
                                    (deckTitle.ifBlank { "مجموعة مفاهيم جديدة" }) to (customText.ifBlank { "مفهوم رئيسي: تفاصيل الشرح والتطبيق العملي." })
                                }

                                val result = SolaVisionEngine.processDocument(
                                    rawText = content,
                                    subjectName = selectedSubject,
                                    deckTitle = title
                                )
                                scanResult = result
                                isScanning = false
                            }
                        },
                        enabled = !isScanning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("sola_vision_scan_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جاري استخراج وتحليل المفاهيم ثلاثية الأبعاد...", fontFamily = CairoFontFamily, fontSize = 12.sp, color = Color.White)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("مسح واستخراج البطاقات والخرائط المفاهيمية ⚡", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                    }
                } else {
                    // --- RESULTS DISPLAY (3D FLASHCARDS & CONCEPT MAP) ---
                    val result = scanResult!!

                    // Results Sub-tab switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (activeTab == 0) ScopePrimary else Color.Transparent)
                                .clickable { activeTab = 0 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "بطاقات 3D التفاعلية (${result.flashcards.size}) 🎴",
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (activeTab == 1) ScopePrimary else Color.Transparent)
                                .clickable { activeTab = 1 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "خريطة المفاهيم الشبكية 🕸️",
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (activeTab == 0) {
                        // --- 3D FLASHCARD DISPLAY WITH 3D FLIP ---
                        val currentCard = result.flashcards.getOrNull(currentCardIndex) ?: result.flashcards.first()

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "بطاقة ${currentCardIndex + 1} من ${result.flashcards.size}",
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3D Flip Card Container
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(230.dp)
                                    .graphicsLayer {
                                        rotationY = rotationAngle
                                        cameraDistance = 14f * density
                                    }
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable { isCardFlipped = !isCardFlipped }
                                    .background(
                                        if (rotationAngle <= 90f) {
                                            Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                                        } else {
                                            Brush.verticalGradient(listOf(Color(0xFF064E3B), Color(0xFF065F46)))
                                        }
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        color = if (rotationAngle <= 90f) ScopePrimary else ScopeFocusGreen,
                                        shape = RoundedCornerShape(22.dp)
                                    )
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (rotationAngle <= 90f) {
                                    // FRONT OF CARD: Question
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(ScopePrimary.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(text = "سؤال / مفهوم ❓", fontFamily = CairoFontFamily, fontSize = 10.sp, color = ScopeAccent)
                                        }
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = currentCard.front,
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = "المس لقلب البطاقة 3D 🔄",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                } else {
                                    // BACK OF CARD: Answer (Flip content horizontally to counter rotation)
                                    Column(
                                        modifier = Modifier.graphicsLayer { rotationY = 180f },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(ScopeFocusGreen.copy(alpha = 0.25f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(text = "الإجابة النموذجية صولا 💡", fontFamily = CairoFontFamily, fontSize = 10.sp, color = ScopeFocusGreen)
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = currentCard.back,
                                            fontFamily = CairoFontFamily,
                                            fontSize = 13.sp,
                                            color = Color(0xFFF1F5F9),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Card navigation buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        if (currentCardIndex > 0) {
                                            isCardFlipped = false
                                            currentCardIndex--
                                        }
                                    },
                                    enabled = currentCardIndex > 0,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                                ) {
                                    Text("السابق", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.White)
                                }

                                TextButton(onClick = { isCardFlipped = !isCardFlipped }) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = ScopePrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("اقلب 3D", fontFamily = CairoFontFamily, fontSize = 12.sp, color = ScopePrimary)
                                }

                                Button(
                                    onClick = {
                                        if (currentCardIndex < result.flashcards.size - 1) {
                                            isCardFlipped = false
                                            currentCardIndex++
                                        }
                                    },
                                    enabled = currentCardIndex < result.flashcards.size - 1,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                                ) {
                                    Text("التالي", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    } else {
                        // --- NODE-BASED CONCEPT MAP DISPLAY ---
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(result.conceptMap.nodes) { node ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.75f)),
                                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(android.graphics.Color.parseColor(node.colorHex)).copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(android.graphics.Color.parseColor(node.colorHex)).copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = when (node.category) {
                                                    "CORE" -> "👑"
                                                    "APPLICATION" -> "⚡"
                                                    else -> "📌"
                                                },
                                                fontSize = 16.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = node.title,
                                                    fontFamily = CairoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFF0F172A))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = when (node.category) {
                                                            "CORE" -> "المفهوم المحوري"
                                                            "APPLICATION" -> "تطبيق عملي"
                                                            else -> "تفرع معرفي"
                                                        },
                                                        fontFamily = CairoFontFamily,
                                                        fontSize = 9.sp,
                                                        color = Color(android.graphics.Color.parseColor(node.colorHex))
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = node.summary,
                                                fontFamily = CairoFontFamily,
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Save to Study Hub Action Button
                    Button(
                        onClick = {
                            onSaveDeck(result.flashcards, result.conceptMap, 75)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_vision_deck_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ScopeFocusGreen)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "حفظ في قسم المذاكرة واستلام +75 XP ⚡",
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
}
