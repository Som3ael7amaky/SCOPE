package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.PublicStudentProfile
import com.example.data.model.ScopeAvatarsCatalog
import com.example.data.model.isSuperDeveloperEmail
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber

/**
 * Frosted Glass Interactive Profile Card:
 * Displays student identity, level, streak, showcase shelf, and
 * exclusive holographic Developer Crown & System Badges for Super Devs.
 */
@Composable
fun PublicProfileCardDialog(
    profile: PublicStudentProfile,
    onDismiss: () -> Unit,
    onConnectClick: () -> Unit = {}
) {
    val isSuperDev = profile.isDeveloper || isSuperDeveloperEmail(profile.scopeId)

    val infiniteTransition = rememberInfiniteTransition(label = "HoloCrownSheen")
    val goldenSheen by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sheen"
    )

    val crownOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crownFloat"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("public_profile_card_dialog")
                .shadow(24.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = if (isSuperDev || profile.equippedFrame.isNotBlank()) 2.5.dp else 1.5.dp,
                brush = when {
                    isSuperDev || profile.equippedFrame == "frame_dev_crown_holo" -> Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = goldenSheen),
                            Color(0xFFFF8C00),
                            Color(0xFF38BDF8),
                            Color(0xFFFFD700).copy(alpha = goldenSheen)
                        )
                    )
                    profile.equippedFrame == "frame_cyber_neon" -> Brush.sweepGradient(
                        colors = listOf(Color(0xFF06B6D4).copy(alpha = goldenSheen), Color(0xFF3B82F6), Color(0xFF06B6D4))
                    )
                    profile.equippedFrame == "frame_celestial_gold" -> Brush.sweepGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFF59E0B), Color(0xFFFFD700))
                    )
                    profile.equippedFrame == "frame_holo_prism" -> Brush.sweepGradient(
                        colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF06B6D4), Color(0xFF8B5CF6))
                    )
                    profile.equippedFrame == "frame_emerald_scholar" -> Brush.sweepGradient(
                        colors = listOf(Color(0xFF10B981), Color(0xFF059669), Color(0xFF10B981))
                    )
                    profile.equippedFrame == "frame_obsidian_elite" -> Brush.sweepGradient(
                        colors = listOf(Color(0xFF312E81), Color(0xFF1E1B4B), Color(0xFF4C1D95))
                    )
                    else -> Brush.verticalGradient(
                        colors = listOf(ScopePrimary.copy(alpha = 0.5f), ScopePrimary.copy(alpha = 0.1f))
                    )
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSuperDev) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x22FFD700))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "👑", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SUPER DEVELOPER",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color(0xFFFFD700)
                            )
                        }
                    } else if (profile.isPro) {
                        Text(
                            text = "⭐ SCOPE PRO",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ScopeWarningAmber
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_profile_card_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Avatar / Profile Picture with Floating Crown / Frame
                Box(contentAlignment = Alignment.TopCenter) {
                    if (isSuperDev || profile.equippedFrame == "frame_dev_crown_holo") {
                        Box(
                            modifier = Modifier
                                .offset(y = (-20 + crownOffset).dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFFD700).copy(alpha = 0.6f * goldenSheen),
                                            Color.Transparent
                                        ),
                                        radius = 70f
                                    )
                                )
                                .padding(6.dp)
                        ) {
                            Text(text = "👑", fontSize = 32.sp)
                        }
                    }

                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 3.dp,
                                brush = when (profile.equippedFrame) {
                                    "frame_cyber_neon" -> Brush.sweepGradient(listOf(Color(0xFF06B6D4), Color(0xFF3B82F6)))
                                    "frame_celestial_gold" -> Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFFF8C00)))
                                    "frame_holo_prism" -> Brush.sweepGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))
                                    "frame_emerald_scholar" -> Brush.sweepGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                                    else -> Brush.linearGradient(listOf(ScopePrimary, Color(0xFF38BDF8)))
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profile.photoUrl.isNotBlank()) {
                            AsyncImage(
                                model = profile.photoUrl,
                                contentDescription = profile.displayName,
                                modifier = Modifier.fillMaxWidth().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val avatar = ScopeAvatarsCatalog.getAvatar(profile.avatarId)
                            Text(text = avatar.emoji, fontSize = 46.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Display Name & Verification
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.displayName,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isSuperDev) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "مطور معتمد",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Scope ID & Title
                Text(
                    text = "${profile.scopeId} • ${if (profile.equippedTitle.isNotBlank()) profile.equippedTitle else "طالب SCOPE"}",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (isSuperDev) Color(0xFFFFD700) else ScopePrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Active Focus Status Indicator Box
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ScopeFocusGreen.copy(alpha = 0.12f))
                        .border(1.dp, ScopeFocusGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ScopeFocusGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = profile.activeFocusStatus,
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ScopeFocusGreen
                    )
                }

                // Super Developer Badges
                if (isSuperDev) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DevBadgeChip(title = "[👑 System Creator]", color = Color(0xFFFFD700))
                        DevBadgeChip(title = "[⚡ Lead Architect]", color = Color(0xFF38BDF8))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // University & Academic Major
                Text(
                    text = "${profile.university} • ${profile.academicMajor}",
                    fontFamily = CairoFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Dashboard (Streak, Level, Sparks)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatColumn(label = "الشعلة", value = "${profile.flameCount} 🔥", color = Color(0xFFEF4444))
                    StatColumn(label = "المستوى", value = "Lv.${profile.level} ⭐", color = Color(0xFFF59E0B))
                    StatColumn(label = "الشرارات", value = "${profile.sparks} ⚡", color = ScopePrimary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Level Progress
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "التقدم للمستوى القادم:",
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isSuperDev) "الحد الأقصى (MAX)" else "85%",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ScopePrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { if (isSuperDev) 1f else 0.85f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isSuperDev) Color(0xFFFFD700) else ScopePrimary,
                        trackColor = ScopePrimary.copy(alpha = 0.2f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onConnectClick()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(44.dp).testTag("connect_profile_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSuperDev) Color(0xFFFF8C00) else ScopePrimary
                        )
                    ) {
                        Text(
                            text = if (isSuperDev) "متابعة المطور 👑" else "إضافة كرفيق دراسة 👥",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DevBadgeChip(title: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = title,
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = color
        )
    }
}

@Composable
private fun StatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = color
        )
        Text(
            text = label,
            fontFamily = CairoFontFamily,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
