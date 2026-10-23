package com.example.ui.mascot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CairoFontFamily

@Composable
fun FloatingSolaMascot(
    prompt: SolaGuidePrompt?,
    onActionClick: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var isBubbleExpanded by remember { mutableStateOf(true) }
    val currentPrompt = prompt ?: SolaGuidePrompt(
        emotion = SolaEmotionState.IDLE,
        title = "رفيقتك صولا معك ✨",
        message = "أنا هنا أراقب سياقك الأكاديمي، واقتنص لك أفضل أوقات التركيز. اضغط عليّ في أي وقت!",
        actionLabel = "اسأل صولا",
        actionTargetTab = "SOLA"
    )

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("floating_sola_companion"),
        horizontalAlignment = Alignment.End
    ) {
        // Interactive Floating Speech Bubble
        AnimatedVisibility(
            visible = isBubbleExpanded,
            enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Card(
                modifier = Modifier
                    .widthIn(min = 220.dp, max = 310.dp)
                    .padding(bottom = 6.dp)
                    .shadow(10.dp, RoundedCornerShape(20.dp))
                    .testTag("sola_speech_bubble"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = currentPrompt.emotion.primaryGlow.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header with Emotion Label and Dismiss Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(currentPrompt.emotion.primaryGlow)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentPrompt.emotion.emotionLabelAr,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = currentPrompt.emotion.primaryGlow
                            )
                        }

                        IconButton(
                            onClick = { isBubbleExpanded = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إخفاء الرسالة",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentPrompt.title,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentPrompt.message,
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    if (!currentPrompt.actionLabel.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onActionClick(currentPrompt.actionTargetTab)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentPrompt.emotion.primaryGlow
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("sola_bubble_action_button")
                        ) {
                            Text(
                                text = currentPrompt.actionLabel,
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

        // 3D Transparent Floating Sola Mascot (Border-free, Animated, Responsive)
        SolaMascotCanvas(
            emotionState = currentPrompt.emotion,
            size = 84.dp,
            onClick = {
                isBubbleExpanded = !isBubbleExpanded
            },
            modifier = Modifier.testTag("sola_avatar_clickable")
        )
    }
}
