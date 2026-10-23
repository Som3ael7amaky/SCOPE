package com.example.ui.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.PodcastSegment
import com.example.data.model.SolaPodcastEpisode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * SolaPodcastSynthesizer: Condenses notes into 60s conversational podcasts
 * and coordinates Multi-Track Soundscape Layering with automatic volume ducking.
 */
class SolaPodcastSynthesizer(
    private val context: Context,
    private val soundStudio: ScopeSoundStudio
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentProgressSeconds = MutableStateFlow(0)
    val currentProgressSeconds: StateFlow<Int> = _currentProgressSeconds.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _currentEpisode = MutableStateFlow<SolaPodcastEpisode?>(null)
    val currentEpisode: StateFlow<SolaPodcastEpisode?> = _currentEpisode.asStateFlow()

    private var progressJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("SolaPodcastSynthesizer", "TTS Init failed", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val arLocale = Locale.forLanguageTag("ar")
            val result = tts?.setLanguage(arLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            tts?.setSpeechRate(_playbackSpeed.value)
            isTtsReady = true

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    soundStudio.duckAmbientVolume(true)
                }

                override fun onDone(utteranceId: String?) {
                    soundStudio.duckAmbientVolume(false)
                    stopPlayback()
                }

                override fun onError(utteranceId: String?) {
                    soundStudio.duckAmbientVolume(false)
                    stopPlayback()
                }
            })
        }
    }

    /**
     * Synthesizes a structured 60-second podcast script from academic notes.
     */
    fun createEpisodeFromNotes(subjectName: String, title: String, rawNotes: String): SolaPodcastEpisode {
        val s1 = PodcastSegment(
            timeRange = "00:00 - 00:12",
            phaseTitle = "الخطاف الذهني 🎙️",
            text = "أهلاً بك في كبسولة الـ 60 ثانية مع صولا! اليوم سنفكك أهم مفاهيم '$title' في مقرر $subjectName."
        )
        val s2 = PodcastSegment(
            timeRange = "00:12 - 00:35",
            phaseTitle = "التشريح المعرفي والربط العملي 💡",
            text = rawNotes.ifBlank {
                "الفكرة الجوهرية تكمن في ضبط التعقيد الحسابي وتحقيق التوازن الهيكلي. تخيل النظام كميزان دقيق يتحرك لاستيعاب الضغط المتزايد."
            }
        )
        val s3 = PodcastSegment(
            timeRange = "00:35 - 00:50",
            phaseTitle = "سر ورقة الامتحان 📝",
            text = "في الاختبارات، ركز دائماً على حالات الحافة (Edge Cases) والشروط الاستثنائية؛ فهي النقطة الفاصلة لتحقيق العلامة الكاملة."
        )
        val s4 = PodcastSegment(
            timeRange = "00:50 - 01:00",
            phaseTitle = "الختام ودعوة التركيز ⚡",
            text = "حافظ على شعلتك الأكاديمية مشتعلة، واشحن طاقتك الذهنية. أراك في جلسة التركيز القادمة!"
        )

        val fullText = "${s1.text} ${s2.text} ${s3.text} ${s4.text}"

        val episode = SolaPodcastEpisode(
            title = "كبسولة الـ 60 ثانية: $title",
            subjectName = subjectName,
            durationSeconds = 60,
            segments = listOf(s1, s2, s3, s4),
            fullScriptText = fullText
        )
        _currentEpisode.value = episode
        return episode
    }

    fun playEpisode(episode: SolaPodcastEpisode) {
        _currentEpisode.value = episode
        _currentProgressSeconds.value = 0
        _isPlaying.value = true

        // Duck ambient volume
        soundStudio.duckAmbientVolume(true)

        // Start TTS speech
        if (isTtsReady) {
            tts?.setSpeechRate(_playbackSpeed.value)
            tts?.speak(episode.fullScriptText, TextToSpeech.QUEUE_FLUSH, null, "sola_podcast_${episode.id}")
        }

        // Progress counter for UI scrubber and visualizer
        progressJob?.cancel()
        progressJob = coroutineScope.launch {
            while (_currentProgressSeconds.value < episode.durationSeconds && _isPlaying.value) {
                delay((1000 / _playbackSpeed.value).toLong())
                _currentProgressSeconds.value += 1
            }
            if (_currentProgressSeconds.value >= episode.durationSeconds) {
                stopPlayback()
            }
        }
    }

    fun pausePlayback() {
        _isPlaying.value = false
        progressJob?.cancel()
        tts?.stop()
        soundStudio.duckAmbientVolume(false)
    }

    fun resumePlayback() {
        val ep = _currentEpisode.value ?: return
        _isPlaying.value = true
        soundStudio.duckAmbientVolume(true)

        if (isTtsReady) {
            tts?.setSpeechRate(_playbackSpeed.value)
            tts?.speak(ep.fullScriptText, TextToSpeech.QUEUE_FLUSH, null, "sola_podcast_resume")
        }

        progressJob?.cancel()
        progressJob = coroutineScope.launch {
            while (_currentProgressSeconds.value < ep.durationSeconds && _isPlaying.value) {
                delay((1000 / _playbackSpeed.value).toLong())
                _currentProgressSeconds.value += 1
            }
            if (_currentProgressSeconds.value >= ep.durationSeconds) {
                stopPlayback()
            }
        }
    }

    fun stopPlayback() {
        _isPlaying.value = false
        progressJob?.cancel()
        tts?.stop()
        soundStudio.duckAmbientVolume(false)
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        if (isTtsReady) {
            tts?.setSpeechRate(speed)
        }
    }

    fun toggleOfflineSave() {
        val ep = _currentEpisode.value ?: return
        _currentEpisode.value = ep.copy(isSavedOffline = !ep.isSavedOffline)
    }

    fun release() {
        stopPlayback()
        tts?.shutdown()
    }
}
