package com.example.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeAccent
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Random
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * SCOPE Sound Studio: High-performance low-latency UI Audio Feedback (SoundPool)
 * and Layerable Multi-Track Ambient Synthesizer (AudioTrack).
 */
class ScopeSoundStudio private constructor(private val appContext: Context) {

    private var soundPool: SoundPool? = null
    private var tapSoundId: Int = 0
    private var completeSoundId: Int = 0
    private var solaChirpSoundId: Int = 0

    // Master state
    var isMasterMuted: Boolean = false
    var masterVolume: Float = 0.8f

    // Ambient AudioTracks map
    private val ambientTracks = mutableMapOf<AmbientTrack, AudioTrack>()
    private val ambientVolumes = mutableMapOf<AmbientTrack, Float>()
    var isAmbientPlaying: Boolean = false
        private set

    init {
        Thread {
            initSoundPool()
        }.start()
    }

    private fun initSoundPool() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val sp = SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(audioAttributes)
                .build()

            // Synthesize and load short, pristine UI sound files off the UI thread
            val tapFile = createSynthWavFile("scope_tap.wav", ::generateGlassTapWaveform)
            val completeFile = createSynthWavFile("scope_complete.wav", ::generateCompleteChimeWaveform)
            val chirpFile = createSynthWavFile("scope_sola.wav", ::generateSolaChirpWaveform)

            tapSoundId = sp.load(tapFile.absolutePath, 1)
            completeSoundId = sp.load(completeFile.absolutePath, 1)
            solaChirpSoundId = sp.load(chirpFile.absolutePath, 1)
            soundPool = sp
        } catch (e: Exception) {
            Log.e("ScopeSoundStudio", "Failed to init SoundPool", e)
        }
    }

    fun playTap() {
        if (isMasterMuted) return
        val id = tapSoundId
        if (id != 0) {
            soundPool?.play(id, masterVolume * 0.7f, masterVolume * 0.7f, 1, 0, 1.0f)
        }
    }

    fun playComplete() {
        if (isMasterMuted) return
        val id = completeSoundId
        if (id != 0) {
            soundPool?.play(id, masterVolume * 0.9f, masterVolume * 0.9f, 2, 0, 1.0f)
        }
    }

    fun playSolaChirp() {
        if (isMasterMuted) return
        val id = solaChirpSoundId
        if (id != 0) {
            soundPool?.play(id, masterVolume * 0.85f, masterVolume * 0.85f, 2, 0, 1.0f)
        }
    }

    /**
     * Start / resume all active ambient tracks.
     */
    fun startAmbientPlayback() {
        if (isMasterMuted) return
        isAmbientPlaying = true
        Thread {
            AmbientTrack.values().forEach { track ->
                val vol = ambientVolumes[track] ?: 0f
                if (vol > 0.01f) {
                    val audioTrack = getOrCreateAudioTrack(track)
                    audioTrack.setVolume(vol * masterVolume)
                    try {
                        if (audioTrack.playState != AudioTrack.PLAYSTATE_PLAYING) {
                            audioTrack.play()
                        }
                    } catch (e: Exception) {
                        Log.e("ScopeSoundStudio", "Error playing ambient track ${track.name}", e)
                    }
                }
            }
        }.start()
    }

    fun pauseAmbientPlayback() {
        isAmbientPlaying = false
        ambientTracks.values.forEach { track ->
            try {
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.pause()
                }
            } catch (e: Exception) {
                Log.e("ScopeSoundStudio", "Error pausing track", e)
            }
        }
    }

    /**
     * Automatic volume ducking for voice podcast synthesis:
     * Dips ambient sound volume down to 20% while Sola is speaking,
     * and restores back to 100% when Sola pauses or finishes.
     */
    fun duckAmbientVolume(ducked: Boolean) {
        val factor = if (ducked) 0.22f else 1.0f
        ambientTracks.forEach { (track, audioTrack) ->
            val vol = ambientVolumes[track] ?: 0f
            if (vol > 0.01f) {
                val effectiveVol = if (isMasterMuted) 0f else (vol * masterVolume * factor)
                try {
                    audioTrack.setVolume(effectiveVol)
                } catch (e: Exception) {
                    Log.e("ScopeSoundStudio", "Error ducking ambient volume", e)
                }
            }
        }
    }

    fun setTrackVolume(track: AmbientTrack, volume: Float) {
        ambientVolumes[track] = volume.coerceIn(0f, 1f)
        val audioTrack = getOrCreateAudioTrack(track)
        val effectiveVol = if (isMasterMuted) 0f else (volume * masterVolume)
        audioTrack.setVolume(effectiveVol)

        if (isAmbientPlaying && effectiveVol > 0.01f) {
            try {
                if (audioTrack.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    audioTrack.play()
                }
            } catch (e: Exception) {
                Log.e("ScopeSoundStudio", "Error playing track", e)
            }
        } else if (effectiveVol <= 0.01f && audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.pause()
        }
    }

    fun getTrackVolume(track: AmbientTrack): Float {
        return ambientVolumes[track] ?: 0f
    }

    fun setMasterMute(muted: Boolean) {
        isMasterMuted = muted
        if (muted) {
            pauseAmbientPlayback()
        } else {
            // Restore active ambient tracks if any had volume
            val hasActiveTrack = ambientVolumes.values.any { it > 0.05f }
            if (hasActiveTrack) {
                startAmbientPlayback()
            }
        }
    }

    private fun getOrCreateAudioTrack(track: AmbientTrack): AudioTrack {
        return ambientTracks.getOrPut(track) {
            createAmbientAudioTrack(track)
        }
    }

    private fun createAmbientAudioTrack(track: AmbientTrack): AudioTrack {
        val sampleRate = 22050
        val durationSeconds = 3
        val numFrames = sampleRate * durationSeconds
        val pcmData = ShortArray(numFrames * 2) // Stereo (L, R)

        synthesizeAmbientPcm(track, sampleRate, numFrames, pcmData)

        val bufferSize = pcmData.size * 2
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build()

        val audioTrack = AudioTrack(
            audioAttributes,
            audioFormat,
            bufferSize,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )

        audioTrack.write(pcmData, 0, pcmData.size)
        audioTrack.setLoopPoints(0, numFrames, -1) // Loop forever
        return audioTrack
    }

    private fun synthesizeAmbientPcm(track: AmbientTrack, sampleRate: Int, numFrames: Int, outPcm: ShortArray) {
        val random = Random(track.ordinal * 997L + 42)
        when (track) {
            AmbientTrack.RAIN_ON_GLASS -> {
                // Filtered white noise with random droplet impulses
                var lastSample = 0.0
                for (i in 0 until numFrames) {
                    val white = (random.nextDouble() * 2.0 - 1.0)
                    lastSample = 0.85 * lastSample + 0.15 * white
                    var droplet = 0.0
                    if (random.nextDouble() < 0.002) {
                        droplet = (random.nextDouble() * 0.6) * sin(2.0 * PI * 1800.0 * i / sampleRate)
                    }
                    val sampleVal = ((lastSample * 0.4 + droplet) * 32767.0).coerceIn(-32767.0, 32767.0).toInt().toShort()
                    outPcm[i * 2] = sampleVal
                    outPcm[i * 2 + 1] = sampleVal
                }
            }

            AmbientTrack.DEEP_BROWN_NOISE -> {
                // Integrated Brownian random walk (deep low rumble)
                var brown = 0.0
                for (i in 0 until numFrames) {
                    val white = (random.nextDouble() * 2.0 - 1.0)
                    brown = (brown + (0.02 * white)) / 1.02
                    val sampleVal = (brown * 3.5 * 32767.0).coerceIn(-32767.0, 32767.0).toInt().toShort()
                    outPcm[i * 2] = sampleVal
                    outPcm[i * 2 + 1] = sampleVal
                }
            }

            AmbientTrack.ALPHA_WAVES -> {
                // 10Hz Binaural Beat: Left = 200Hz, Right = 210Hz (Frequency difference = 10Hz Alpha)
                for (i in 0 until numFrames) {
                    val t = i.toDouble() / sampleRate
                    val left = sin(2.0 * PI * 200.0 * t) * 0.5
                    val right = sin(2.0 * PI * 210.0 * t) * 0.5
                    outPcm[i * 2] = (left * 32767.0).toInt().toShort()
                    outPcm[i * 2 + 1] = (right * 32767.0).toInt().toShort()
                }
            }

            AmbientTrack.ACADEMIC_CAFE -> {
                // Soft pink murmur + warm low resonance
                var b0 = 0.0
                var b1 = 0.0
                var b2 = 0.0
                for (i in 0 until numFrames) {
                    val white = (random.nextDouble() * 2.0 - 1.0)
                    b0 = 0.99886 * b0 + white * 0.0555179
                    b1 = 0.99332 * b1 + white * 0.0750759
                    b2 = 0.96900 * b2 + white * 0.1538520
                    val pink = b0 + b1 + b2 + white * 0.5362
                    val sampleVal = (pink * 0.12 * 32767.0).coerceIn(-32767.0, 32767.0).toInt().toShort()
                    outPcm[i * 2] = sampleVal
                    outPcm[i * 2 + 1] = sampleVal
                }
            }

            AmbientTrack.MECHANICAL_TYPING -> {
                // Gentle rhythmic keyboard switch clicks
                var clickCountdown = 0
                var clickFreq = 1200.0
                for (i in 0 until numFrames) {
                    if (clickCountdown <= 0 && random.nextDouble() < 0.008) {
                        clickCountdown = (sampleRate * 0.035).toInt()
                        clickFreq = 900.0 + random.nextDouble() * 800.0
                    }
                    var clickSample = 0.0
                    if (clickCountdown > 0) {
                        val progress = clickCountdown.toDouble() / (sampleRate * 0.035)
                        clickSample = sin(2.0 * PI * clickFreq * progress) * progress * 0.6
                        clickCountdown--
                    }
                    val sampleVal = (clickSample * 32767.0).coerceIn(-32767.0, 32767.0).toInt().toShort()
                    outPcm[i * 2] = sampleVal
                    outPcm[i * 2 + 1] = sampleVal
                }
            }
        }
    }

    private fun createSynthWavFile(fileName: String, generator: (Int, ShortArray) -> Unit): File {
        val file = File(appContext.cacheDir, fileName)
        if (file.exists() && file.length() > 100) return file

        val sampleRate = 44100
        val pcmBuffer = ShortArray(sampleRate) // up to 1 second
        generator(sampleRate, pcmBuffer)

        FileOutputStream(file).use { out ->
            val numSamples = pcmBuffer.size
            val byteRate = sampleRate * 2
            val dataSize = numSamples * 2
            val totalSize = 36 + dataSize

            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray())
            header.putInt(totalSize)
            header.put("WAVE".toByteArray())
            header.put("fmt ".toByteArray())
            header.putInt(16) // Subchunk1Size
            header.putShort(1) // AudioFormat (1 = PCM)
            header.putShort(1) // NumChannels (1 = Mono)
            header.putInt(sampleRate)
            header.putInt(byteRate)
            header.putShort(2) // BlockAlign
            header.putShort(16) // BitsPerSample
            header.put("data".toByteArray())
            header.putInt(dataSize)

            out.write(header.array())

            val byteBuf = ByteBuffer.allocate(numSamples * 2).order(ByteOrder.LITTLE_ENDIAN)
            for (sample in pcmBuffer) {
                byteBuf.putShort(sample)
            }
            out.write(byteBuf.array())
        }
        return file
    }

    private fun generateGlassTapWaveform(sampleRate: Int, buffer: ShortArray) {
        val count = (sampleRate * 0.04).toInt().coerceAtMost(buffer.size)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 90.0)
            val v = sin(2.0 * PI * 1850.0 * t) * decay
            buffer[i] = (v * 32000.0).toInt().toShort()
        }
    }

    private fun generateCompleteChimeWaveform(sampleRate: Int, buffer: ShortArray) {
        val totalLen = (sampleRate * 0.45).toInt().coerceAtMost(buffer.size)
        val note1 = 523.25 // C5
        val note2 = 659.25 // E5
        val note3 = 783.99 // G5

        for (i in 0 until totalLen) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 8.0)
            val v = (sin(2.0 * PI * note1 * t) * 0.4 +
                     sin(2.0 * PI * note2 * t) * 0.4 +
                     sin(2.0 * PI * note3 * t) * 0.3) * decay
            buffer[i] = (v * 31000.0).toInt().toShort()
        }
    }

    private fun generateSolaChirpWaveform(sampleRate: Int, buffer: ShortArray) {
        val totalLen = (sampleRate * 0.22).toInt().coerceAtMost(buffer.size)
        for (i in 0 until totalLen) {
            val t = i.toDouble() / sampleRate
            val freq = 800.0 + 800.0 * (t / 0.22)
            val decay = sin(PI * (t / 0.22)) // smooth bell curve
            val v = sin(2.0 * PI * freq * t) * decay
            buffer[i] = (v * 30000.0).toInt().toShort()
        }
    }

    companion object {
        @Volatile
        private var instance: ScopeSoundStudio? = null

        fun getInstance(context: Context): ScopeSoundStudio {
            return instance ?: synchronized(this) {
                instance ?: ScopeSoundStudio(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * Scope Sound Studio Bottom Sheet Modal for mixing layerable study ambiance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScopeSoundStudioBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundStudio = remember { ScopeSoundStudio.getInstance(context) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isPlaying by remember { mutableStateOf(soundStudio.isAmbientPlaying) }
    var isMuted by remember { mutableStateOf(soundStudio.isMasterMuted) }
    var masterVol by remember { mutableFloatStateOf(soundStudio.masterVolume) }

    val trackVolumes = remember {
        mutableStateMapOf<AmbientTrack, Float>().apply {
            AmbientTrack.values().forEach { track ->
                put(track, soundStudio.getTrackVolume(track))
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("sound_studio_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header Row
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
                            .background(ScopePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = ScopePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "استوديو الصوت والأجواء الدراسية",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Scope Multi-Track Sound Studio",
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Master Play / Mute Controls Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ScopePrimary.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (isPlaying) {
                                    soundStudio.pauseAmbientPlayback()
                                    isPlaying = false
                                } else {
                                    soundStudio.playTap()
                                    soundStudio.startAmbientPlayback()
                                    isPlaying = true
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlaying) ScopeFocusGreen else ScopePrimary
                            ),
                            modifier = Modifier.testTag("toggle_ambient_play_btn")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPlaying) "إيقاف الأجواء مؤقتاً" else "تشغيل الأجواء المختارة",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        // Master Mute Switch
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = if (isMuted) ScopeWarningAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = !isMuted,
                                onCheckedChange = { active ->
                                    isMuted = !active
                                    soundStudio.setMasterMute(!active)
                                    isPlaying = soundStudio.isAmbientPlaying
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ScopePrimary,
                                    checkedTrackColor = ScopePrimary.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.testTag("master_audio_mute_switch")
                            )
                        }
                    }

                    // Sound Visualizer Bars Animation when playing
                    AnimatedVisibility(visible = isPlaying && !isMuted) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            AmbientSoundVisualizer()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presets Selector
            Text(
                text = "قوالب التركيز الجاهزة (Presets):",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetChip(label = "مطر هادئ 🌧️") {
                    soundStudio.playTap()
                    trackVolumes[AmbientTrack.RAIN_ON_GLASS] = 0.8f
                    trackVolumes[AmbientTrack.ACADEMIC_CAFE] = 0f
                    trackVolumes[AmbientTrack.DEEP_BROWN_NOISE] = 0.3f
                    trackVolumes[AmbientTrack.ALPHA_WAVES] = 0f
                    trackVolumes[AmbientTrack.MECHANICAL_TYPING] = 0f
                    trackVolumes.forEach { (t, v) -> soundStudio.setTrackVolume(t, v) }
                    soundStudio.startAmbientPlayback()
                    isPlaying = true
                }
                PresetChip(label = "كافيه عميق ☕") {
                    soundStudio.playTap()
                    trackVolumes[AmbientTrack.RAIN_ON_GLASS] = 0.2f
                    trackVolumes[AmbientTrack.ACADEMIC_CAFE] = 0.75f
                    trackVolumes[AmbientTrack.DEEP_BROWN_NOISE] = 0f
                    trackVolumes[AmbientTrack.ALPHA_WAVES] = 0f
                    trackVolumes[AmbientTrack.MECHANICAL_TYPING] = 0.4f
                    trackVolumes.forEach { (t, v) -> soundStudio.setTrackVolume(t, v) }
                    soundStudio.startAmbientPlayback()
                    isPlaying = true
                }
                PresetChip(label = "صفاء ألفا 🌊") {
                    soundStudio.playTap()
                    trackVolumes[AmbientTrack.RAIN_ON_GLASS] = 0f
                    trackVolumes[AmbientTrack.ACADEMIC_CAFE] = 0f
                    trackVolumes[AmbientTrack.DEEP_BROWN_NOISE] = 0.6f
                    trackVolumes[AmbientTrack.ALPHA_WAVES] = 0.85f
                    trackVolumes[AmbientTrack.MECHANICAL_TYPING] = 0f
                    trackVolumes.forEach { (t, v) -> soundStudio.setTrackVolume(t, v) }
                    soundStudio.startAmbientPlayback()
                    isPlaying = true
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Individual Channel Sliders
            Text(
                text = "مكسر القنوات المستقلة (5-Track Ambient Mixer):",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(260.dp)
            ) {
                items(AmbientTrack.values().toList()) { track ->
                    val volume = trackVolumes[track] ?: 0f
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = track.iconEmoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = track.titleAr,
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "${(volume * 100).toInt()}%",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (volume > 0.01f) ScopePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Slider(
                                value = volume,
                                onValueChange = { newVol ->
                                    trackVolumes[track] = newVol
                                    soundStudio.setTrackVolume(track, newVol)
                                    if (newVol > 0.01f && !isPlaying && !isMuted) {
                                        soundStudio.startAmbientPlayback()
                                        isPlaying = true
                                    }
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = ScopePrimary,
                                    activeTrackColor = ScopePrimary,
                                    inactiveTrackColor = ScopePrimary.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("slider_${track.id}")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ScopePrimary.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontFamily = CairoFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = ScopePrimary
        )
    }
}

@Composable
private fun AmbientSoundVisualizer() {
    val infiniteTransition = rememberInfiniteTransition(label = "SoundWaveTransition")
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w1"
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(380, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w2"
    )
    val wave3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(520, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w3"
    )
    val wave4 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w4"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ScopePrimary.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(wave1, wave2, wave3, wave4, wave2, wave1, wave3, wave4, wave1, wave2).forEach { heightRatio ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((22.dp * heightRatio).coerceAtLeast(4.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(ScopePrimary)
            )
        }
    }
}
