package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.data.model.TaskPriority
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber

@Composable
fun TaskCreationDialog(
    subjects: List<Subject>,
    onAddTask: (
        title: String,
        subject: Subject?,
        priority: TaskPriority,
        effortMinutes: Int,
        isPersonal: Boolean,
        personalCategory: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var isPersonal by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()) }
    var personalCategory by remember { mutableStateOf("موعد خارجي") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var effortText by remember { mutableStateOf("30") }

    val personalCategories = listOf(
        "موعد خارجي" to "🏠",
        "عائلة" to "👨‍👩‍👧",
        "صحة" to "🩺",
        "رياضة" to "⚽",
        "شخصي" to "⭐"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("task_creation_dialog"),
        title = {
            Text(
                text = "إضافة مهمة / موعد جديد",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Category Switcher: Academic vs Personal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isPersonal) ScopePrimary else Color.Transparent)
                            .clickable { isPersonal = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = if (!isPersonal) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = "أكاديمية 📚",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (!isPersonal) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isPersonal) ScopeWarningAmber else Color.Transparent)
                            .clickable { isPersonal = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = if (isPersonal) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = "شخصية / موعد 🏠",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isPersonal) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Task Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(
                            text = if (isPersonal) "اسم الموعد الشخصي أو المهمة (مثال: موعد طبيب)" else "عنوان المهمة الأكاديمية",
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input"),
                    singleLine = true
                )

                if (!isPersonal) {
                    // Subject Selector
                    if (subjects.isNotEmpty()) {
                        Text("المقرر المرتبط:", fontFamily = CairoFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(subjects) { subj ->
                                val isSelected = selectedSubject?.id == subj.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) ScopePrimary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { selectedSubject = subj }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = subj.name,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Personal Category Selector
                    Text("تصنيف الموعد الشخصي:", fontFamily = CairoFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(personalCategories) { (catName, emoji) ->
                            val isSelected = personalCategory == catName
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ScopeWarningAmber else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { personalCategory = catName }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$emoji $catName",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Priority Switcher
                Text("درجة الأهمية:", fontFamily = CairoFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        TaskPriority.LOW to ("عادي" to ScopeFocusGreen),
                        TaskPriority.MEDIUM to ("متوسط" to ScopeWarningAmber),
                        TaskPriority.HIGH to ("عاجل" to Color(0xFFEF4444))
                    ).forEach { (prio, pair) ->
                        val (label, col) = pair
                        val isSel = priority == prio
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) col else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { priority = prio }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Effort / Duration
                OutlinedTextField(
                    value = effortText,
                    onValueChange = { effortText = it },
                    label = { Text("المدة التقديرية (بالدقائق)", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val effort = effortText.toIntOrNull() ?: 30
                        onAddTask(
                            title.trim(),
                            if (!isPersonal) selectedSubject else null,
                            priority,
                            effort,
                            isPersonal,
                            personalCategory
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPersonal) ScopeWarningAmber else ScopePrimary
                ),
                modifier = Modifier.testTag("confirm_add_task_button")
            ) {
                Text(
                    text = if (isPersonal) "إضافة الموعد الشخصي 🏠" else "إضافة المهمة الأكاديمية 📚",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = if (isPersonal) Color.Black else Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", fontFamily = CairoFontFamily)
            }
        }
    )
}
