package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.UserProfile
import com.example.ui.audio.ScopeSoundStudio
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeAccent
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber

@Composable
fun ControlCenterDialog(
    userProfile: UserProfile?,
    solaSpeechStyle: String = "DETAILED",
    isHapticsEnabled: Boolean = true,
    isNotificationsSilenced: Boolean = false,
    isRestModeActive: Boolean = false,
    onDismiss: () -> Unit,
    onToggleFocusShield: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit = {},
    onToggleNotificationsSilencer: (Boolean) -> Unit = {},
    onToggleRestMode: (Boolean) -> Unit = {},
    onNavigateToProfileStudio: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onRetakeDiagnostic: () -> Unit,
    onOpenSolaPersonality: () -> Unit = {},
    onOpenScopeJourney: () -> Unit = {},
    onSignOut: () -> Unit,
    onOpenSoundStudio: () -> Unit = {},
    onOpenDevPanel: () -> Unit = {}
) {
    val context = LocalContext.current
    val soundStudio = remember { ScopeSoundStudio.getInstance(context) }

    var soundEnabled by remember { mutableStateOf(!soundStudio.isMasterMuted) }
    var showQrModal by remember { mutableStateOf(false) }

    val rawCode = userProfile?.scopeId?.replace("@", "")?.uppercase()?.take(6) ?: "ZXVC37"
    val scopeHoloCode = "SC-$rawCode"

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        modifier = Modifier.testTag("control_center_dialog"),
        containerColor = Color(0xFF0F172A),
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مركز التحكم والخيارات السريعة",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    IconButton(
                        onClick = {
                            soundStudio.playTap()
                            onDismiss()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 1. Holographic Identity Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.scope_logo),
                                    contentDescription = "Profile Logo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, Color(0xFF38BDF8), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = userProfile?.displayName ?: "طالب SCOPE",
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "المستوى ${userProfile?.level ?: 1} • ${userProfile?.equippedTitle ?: "طالب متميز"}",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 10.sp,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }

                            IconButton(onClick = {
                                soundStudio.playTap()
                                showQrModal = true
                            }) {
                                Icon(Icons.Default.QrCode2, contentDescription = "QR Code", tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Holographic Code Copy Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("كود SCOPE الهولوجرام الفريد:", fontFamily = CairoFontFamily, fontSize = 9.sp, color = Color.Gray)
                                Text(scopeHoloCode, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ScopeFocusGreen)
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                                    .clickable {
                                        soundStudio.playTap()
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("SCOPE Code", scopeHoloCode)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "تم نسخ كود SCOPE: $scopeHoloCode", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("نسخ 📋", fontFamily = CairoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Quick Control Glass Pills Grid (6 Frosted Controls)
                Text("التحكمات السريعة (Quick Glass Controls):", fontFamily = CairoFontFamily, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GlassPillControl(
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            label = "الصوت 🔊",
                            isActive = soundEnabled,
                            activeColor = Color(0xFF38BDF8),
                            onClick = {
                                soundStudio.playTap()
                                soundEnabled = !soundEnabled
                                soundStudio.setMasterMute(!soundEnabled)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        GlassPillControl(
                            icon = Icons.Default.Vibration,
                            label = "الاهتزاز 📳",
                            isActive = isHapticsEnabled,
                            activeColor = ScopeAccent,
                            onClick = {
                                soundStudio.playTap()
                                onToggleHaptics(!isHapticsEnabled)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        GlassPillControl(
                            icon = Icons.Default.Shield,
                            label = "الدرع 🛡️",
                            isActive = userProfile?.focusShieldEnabled ?: false,
                            activeColor = ScopeFocusGreen,
                            onClick = {
                                soundStudio.playTap()
                                onToggleFocusShield(!(userProfile?.focusShieldEnabled ?: false))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GlassPillControl(
                            icon = Icons.Default.Coffee,
                            label = "استراحة ☕",
                            isActive = isRestModeActive,
                            activeColor = ScopeWarningAmber,
                            onClick = {
                                soundStudio.playTap()
                                onToggleRestMode(!isRestModeActive)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        GlassPillControl(
                            icon = Icons.Default.DarkMode,
                            label = "داكن 🌙",
                            isActive = true,
                            activeColor = Color(0xFF8B5CF6),
                            onClick = {
                                soundStudio.playTap()
                            },
                            modifier = Modifier.weight(1f)
                        )

                        GlassPillControl(
                            icon = Icons.Default.NotificationsOff,
                            label = "صامت 🔕",
                            isActive = isNotificationsSilenced,
                            activeColor = MaterialTheme.colorScheme.error,
                            onClick = {
                                soundStudio.playTap()
                                onToggleNotificationsSilencer(!isNotificationsSilenced)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Feature Action Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sola Personality
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onDismiss()
                                onOpenSolaPersonality()
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = ScopeAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("شخصية صولا 🤖", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                Text("أسلوب الإجابة والسياق", fontFamily = CairoFontFamily, fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }

                    // 3D Scope Journey
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onDismiss()
                                onOpenScopeJourney()
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("رحلة سكوب 3D 🗺️", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                Text("شجرة نموك الأكاديمي", fontFamily = CairoFontFamily, fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Developer & Studio Hub Actions
                val isSuperDev = userProfile != null && (com.example.data.model.isSuperDeveloperEmail(userProfile.email) || userProfile.isDeveloper)
                if (isSuperDev) {
                    Button(
                        onClick = {
                            onDismiss()
                            onOpenDevPanel()
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("open_dev_panel_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                    ) {
                        Text("👑 لوحة تحكم المطور الأسمى (Super Dev)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onDismiss()
                            onNavigateToProfileStudio()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("المتجر والاستوديو", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onNavigateToSettings()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("الإعدادات", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sign Out Button
                Button(
                    onClick = {
                        onDismiss()
                        onSignOut()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تسجيل الخروج من الحساب", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    )

    // Interactive 3D QR Code Modal
    if (showQrModal) {
        Dialog(onDismissRequest = { showQrModal = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(0.88f).padding(16.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("رمز التعرف السريع (3D SCOPE QR Code)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("امسح الرمز للانضمام الفوري للتحالفات وغرف المذاكرة", fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, tint = Color.Black, modifier = Modifier.fillMaxSize())
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(scopeHoloCode, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ScopeFocusGreen)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showQrModal = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                    ) {
                        Text("إغلاق", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassPillControl(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isActive) activeColor.copy(alpha = 0.22f) else Color(0xFF1E293B))
            .border(width = 1.dp, color = if (isActive) activeColor else Color.Transparent, shape = RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, tint = if (isActive) activeColor else Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontFamily = CairoFontFamily, fontSize = 9.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium, color = if (isActive) Color.White else Color.Gray)
        }
    }
}
