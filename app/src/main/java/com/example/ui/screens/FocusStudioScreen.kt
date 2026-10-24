package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AmbientTrack
import com.example.ui.audio.ScopeSoundStudio
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber
import kotlinx.coroutines.delay

enum class FocusMode(val titleAr: String, val workMinutes: Int, val breakMinutes: Int) {
    POMODORO_STANDARD("بومودورو قياسي (25/5)", 25, 5),
    DEEP_WORK("تركيز عميق (50/10)", 50, 10),
    QUICK_SPRINT("سباق سريع (15/3)", 15, 3)
}

enum class TimerState {
    WORK, SHORT_BREAK, PAUSED
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FocusStudioScreen(
    onSessionCompleted: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundStudio = remember { ScopeSoundStudio.getInstance(context) }

    var selectedMode by remember { mutableStateOf(FocusMode.POMODORO_STANDARD) }
    var timerState by remember { mutableStateOf(TimerState.PAUSED) }
    var previousState by remember { mutableStateOf(TimerState.WORK) }

    var totalDurationSeconds by remember { mutableIntStateOf(selectedMode.workMinutes * 60) }
    var remainingSeconds by remember { mutableIntStateOf(selectedMode.workMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }

    var isAmbientPlaying by remember { mutableStateOf(soundStudio.isAmbientPlaying) }
    var isMuted by remember { mutableStateOf(soundStudio.isMasterMuted) }

    val trackVolumes = remember {
        mutableStateMapOf<AmbientTrack, Float>().apply {
            AmbientTrack.values().forEach { track ->
                put(track, soundStudio.getTrackVolume(track))
            }
        }
    }

    // Timer Ticker Loop
    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        } else if (isRunning && remainingSeconds == 0) {
            soundStudio.playComplete()
            if (timerState == TimerState.WORK) {
                onSessionCompleted(totalDurationSeconds / 60)
                timerState = TimerState.SHORT_BREAK
                previousState = TimerState.SHORT_BREAK
                totalDurationSeconds = selectedMode.breakMinutes * 60
                remainingSeconds = selectedMode.breakMinutes * 60
            } else {
                timerState = TimerState.WORK
                previousState = TimerState.WORK
                totalDurationSeconds = selectedMode.workMinutes * 60
                remainingSeconds = selectedMode.workMinutes * 60
            }
            isRunning = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E14))
            .padding(horizontal = 16.dp)
            .testTag("focus_studio_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ScopePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = ScopePrimary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "استوديو التركيز والأجواء الصوتية 🎧",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "ادمج إيقاع بومودورو مع طبقات الصوت الهادئة لحجب المشتتات",
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Mode Selector Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FocusMode.values().forEach { mode ->
                    val isSelected = selectedMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ScopePrimary else Color(0xFF1E293B))
                            .clickable {
                                soundStudio.playTap()
                                selectedMode = mode
                                isRunning = false
                                timerState = TimerState.WORK
                                previousState = TimerState.WORK
                                totalDurationSeconds = mode.workMinutes * 60
                                remainingSeconds = mode.workMinutes * 60
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.titleAr,
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color.LightGray
                        )
                    }
                }
            }
        }

        // Main Circular Timer Display with AnimatedContent
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ScopePrimary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val progress = if (totalDurationSeconds > 0) remainingSeconds.toFloat() / totalDurationSeconds.toFloat() else 0f
                    val minutes = remainingSeconds / 60
                    val seconds = remainingSeconds % 60
                    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

                    AnimatedContent(
                        targetState = timerState,
                        transitionSpec = { fadeIn() with fadeOut() },
                        label = "TimerStateAnimation"
                    ) { state ->
                        Text(
                            text = when (state) {
                                TimerState.WORK -> "جلسة تركيز عمل عميق ⚡"
                                TimerState.SHORT_BREAK -> "استراحة قصيرة للاستعادة ☕"
                                TimerState.PAUSED -> if (previousState == TimerState.SHORT_BREAK) "الاستراحة متوقفة مؤقتاً" else "التركيز متوقف مؤقتاً"
                            },
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = when (state) {
                                TimerState.WORK -> ScopePrimary
                                TimerState.SHORT_BREAK -> ScopeFocusGreen
                                TimerState.PAUSED -> ScopeWarningAmber
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(200.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { 1.0f },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF1E293B),
                            strokeWidth = 12.dp
                        )
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = if (timerState == TimerState.SHORT_BREAK) ScopeFocusGreen else ScopePrimary,
                            strokeWidth = 12.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = timeFormatted,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 42.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (isRunning) "قيد التشغيل ⏱️" else "متوقف",
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Timer Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reset Button
                        IconButton(
                            onClick = {
                                soundStudio.playTap()
                                isRunning = false
                                timerState = TimerState.WORK
                                previousState = TimerState.WORK
                                totalDurationSeconds = selectedMode.workMinutes * 60
                                remainingSeconds = selectedMode.workMinutes * 60
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "إعادة ضبط", tint = Color.White)
                        }

                        // Main Play / Pause Button
                        Button(
                            onClick = {
                                soundStudio.playTap()
                                if (isRunning) {
                                    isRunning = false
                                    previousState = timerState
                                    timerState = TimerState.PAUSED
                                } else {
                                    isRunning = true
                                    timerState = if (previousState == TimerState.SHORT_BREAK) TimerState.SHORT_BREAK else TimerState.WORK
                                    if (!soundStudio.isAmbientPlaying && !isMuted) {
                                        soundStudio.startAmbientPlayback()
                                        isAmbientPlaying = true
                                    }
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) ScopeWarningAmber else ScopePrimary
                            ),
                            modifier = Modifier
                                .size(64.dp)
                                .testTag("focus_main_play_btn")
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = Color.White
                            )
                        }

                        // Skip Break Button
                        IconButton(
                            onClick = {
                                soundStudio.playTap()
                                isRunning = false
                                timerState = TimerState.WORK
                                previousState = TimerState.WORK
                                totalDurationSeconds = selectedMode.workMinutes * 60
                                remainingSeconds = selectedMode.workMinutes * 60
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "تخطي", tint = Color.White)
                        }
                    }
                }
            }
        }

        // Ambient Multi-Track Mixer Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = ScopePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مكسر الأجواء الصوتية (Ambient Mixer)",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = if (isMuted) ScopeWarningAmber else Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = !isMuted,
                                onCheckedChange = { active ->
                                    isMuted = !active
                                    soundStudio.setMasterMute(!active)
                                    isAmbientPlaying = soundStudio.isAmbientPlaying
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ScopePrimary,
                                    checkedTrackColor = ScopePrimary.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AmbientTrack.values().forEach { track ->
                        val volume = trackVolumes[track] ?: 0f
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = track.iconEmoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = track.titleAr,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${(volume * 100).toInt()}%",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 10.sp,
                                        color = ScopePrimary
                                    )
                                }
                                Slider(
                                    value = volume,
                                    onValueChange = { newVol ->
                                        trackVolumes[track] = newVol
                                        soundStudio.setTrackVolume(track, newVol)
                                        if (newVol > 0.01f && !isAmbientPlaying && !isMuted) {
                                            soundStudio.startAmbientPlayback()
                                            isAmbientPlaying = true
                                        }
                                    },
                                    colors = SliderDefaults.colors(
                                        thumbColor = ScopePrimary,
                                        activeTrackColor = ScopePrimary,
                                        inactiveTrackColor = Color(0xFF334155)
                                    ),
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
