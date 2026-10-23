package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.SolaPodcastEpisode
import com.example.ui.audio.SolaPodcastSynthesizer
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeAccent
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber

/**
 * SolaAudioMiniPlayer: Glassmorphic Audio Player for Sola 60s Synthesized Podcasts.
 * Features speed controls, animated sound wave visualizer, multi-track layering indicator,
 * and offline commute saving.
 */
@Composable
fun SolaAudioMiniPlayer(
    synthesizer: SolaPodcastSynthesizer,
    onDismiss: () -> Unit
) {
    val isPlaying by synthesizer.isPlaying.collectAsState()
    val progressSeconds by synthesizer.currentProgressSeconds.collectAsState()
    val playbackSpeed by synthesizer.playbackSpeed.collectAsState()
    val episode by synthesizer.currentEpisode.collectAsState()

    var showScriptAccordion by remember { mutableStateOf(false) }

    // Sound wave frequency bars animation
    val infiniteTransition = rememberInfiniteTransition(label = "WaveVisualizer")
    val waveScale1 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(animation = tween(400, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "w1"
    )
    val waveScale2 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(animation = tween(550, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "w2"
    )
    val waveScale3 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(animation = tween(350, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "w3"
    )
    val waveScale4 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(animation = tween(480, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "w4"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .clip(RoundedCornerShape(26.dp))
                .border(1.5.dp, ScopePrimary.copy(alpha = 0.6f), RoundedCornerShape(26.dp))
                .testTag("sola_audio_mini_player"),
            color = Color(0xFF090E1A)
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Offline Commute Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .clickable { synthesizer.toggleOfflineSave() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (episode?.isSavedOffline == true) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save Offline",
                            tint = if (episode?.isSavedOffline == true) ScopeFocusGreen else Color(0xFF94A3B8),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (episode?.isSavedOffline == true) "محفوظ للتنقل 📱" else "حفظ أوفلاين",
                            fontFamily = CairoFontFamily,
                            fontSize = 10.sp,
                            color = if (episode?.isSavedOffline == true) ScopeFocusGreen else Color(0xFF94A3B8)
                        )
                    }

                    // Multi-track layering badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ScopePrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Soundscape Layering 🎧",
                            fontFamily = CairoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScopeAccent
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mascot Avatar with Glowing Rings
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(ScopePrimary.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                        .border(2.dp, if (isPlaying) ScopeAccent else ScopePrimary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = if (isPlaying) R.drawable.sola_happy else R.drawable.sola_thinking),
                        contentDescription = "Sola Podcast Host",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(68.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title & Subject
                Text(
                    text = episode?.title ?: "بودكاست صولا الـ 60 ثانية 🎙️",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "مقرر: ${episode?.subjectName ?: "العلوم الهندسية"} • كبسولة صوتية سريعة",
                    fontFamily = CairoFontFamily,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sound Wave Visualizer Bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val scales = listOf(waveScale1, waveScale2, waveScale3, waveScale4, waveScale2, waveScale1, waveScale3, waveScale4)
                    scales.forEach { scale ->
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .fillMaxHeight(if (isPlaying) scale else 0.25f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isPlaying) ScopeAccent else Color(0xFF334155))
                                .padding(horizontal = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Scrubber Bar
                val progressFraction = (progressSeconds / 60f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ScopePrimary,
                    trackColor = Color(0xFF1E293B),
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Time indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "00:${progressSeconds.toString().padStart(2, '0')}",
                        fontFamily = CairoFontFamily,
                        fontSize = 10.sp,
                        color = ScopeAccent
                    )
                    Text(
                        text = "01:00",
                        fontFamily = CairoFontFamily,
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Playback Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Replay 10s
                    IconButton(
                        onClick = {
                            if (episode != null) {
                                synthesizer.playEpisode(episode!!)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = "إعادة", tint = Color.White, modifier = Modifier.size(24.dp))
                    }

                    // Main Play/Pause Button
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(ScopePrimary)
                            .clickable {
                                if (isPlaying) {
                                    synthesizer.pausePlayback()
                                } else {
                                    if (episode != null) {
                                        synthesizer.resumePlayback()
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // Speed Selector Toggle (1x -> 1.25x -> 1.5x -> 2x)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .clickable {
                                val nextSpeed = when (playbackSpeed) {
                                    1.0f -> 1.25f
                                    1.25f -> 1.5f
                                    1.5f -> 2.0f
                                    else -> 1.0f
                                }
                                synthesizer.setSpeed(nextSpeed)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${playbackSpeed}x",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = ScopeAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Script Breakdown Accordion Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                        .clickable { showScriptAccordion = !showScriptAccordion }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "نص الكبسولة ومحاور البودكاست (Script)",
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = if (showScriptAccordion) "إخفاء ▲" else "عرض ▼",
                        fontFamily = CairoFontFamily,
                        fontSize = 10.sp,
                        color = ScopePrimary
                    )
                }

                if (showScriptAccordion && episode != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        items(episode!!.segments) { seg ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = seg.phaseTitle, fontFamily = CairoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ScopeAccent)
                                        Text(text = seg.timeRange, fontFamily = CairoFontFamily, fontSize = 9.sp, color = Color(0xFF64748B))
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = seg.text, fontFamily = CairoFontFamily, fontSize = 10.sp, color = Color(0xFFE2E8F0))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
