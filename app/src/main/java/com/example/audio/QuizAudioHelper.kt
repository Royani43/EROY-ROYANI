package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class QuizAudioHelper(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private var musicPlayer: MediaPlayer? = null
    private var isMusicEnabled = true
    private var isAppInForeground = true
    private var isDuckedForTts = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val indonesian = Locale.forLanguageTag("id-ID")
                    val result = tts?.setLanguage(indonesian)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.getDefault())
                    }
                    tts?.setSpeechRate(0.92f)
                    tts?.setPitch(1.08f)
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                            setMusicDucking(true)
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                            setMusicDucking(false)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                            setMusicDucking(false)
                        }
                    })
                    isTtsReady = true
                }
            }
        } catch (_: Throwable) {
            isTtsReady = false
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        updateMusicPlaybackState()
    }

    fun onAppForegroundChanged(inForeground: Boolean) {
        isAppInForeground = inForeground
        updateMusicPlaybackState()
    }

    private fun updateMusicPlaybackState() {
        try {
            if (isMusicEnabled && isAppInForeground) {
                val player = ensureMusicPlayer() ?: return
                val vol = if (isDuckedForTts) DUCKED_MUSIC_VOLUME else NORMAL_MUSIC_VOLUME
                player.setVolume(vol, vol)
                if (!player.isPlaying) {
                    player.start()
                }
            } else {
                musicPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.pause()
                    }
                }
            }
        } catch (_: Throwable) {
            // Gracefully ignore in test/headless environments without audio codecs
        }
    }

    private fun setMusicDucking(ducked: Boolean) {
        isDuckedForTts = ducked
        try {
            val vol = if (ducked) DUCKED_MUSIC_VOLUME else NORMAL_MUSIC_VOLUME
            musicPlayer?.setVolume(vol, vol)
        } catch (_: Throwable) {
        }
    }

    private fun ensureMusicPlayer(): MediaPlayer? {
        if (musicPlayer != null) return musicPlayer
        return try {
            MediaPlayer.create(appContext, R.raw.kids_theme)?.apply {
                isLooping = true
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setVolume(NORMAL_MUSIC_VOLUME, NORMAL_MUSIC_VOLUME)
                musicPlayer = this
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun speak(text: String) {
        if (!isTtsReady) return
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kuis_ceria_utterance")
        } catch (_: Throwable) {
            _isSpeaking.value = false
            setMusicDucking(false)
        }
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
        } catch (_: Throwable) {
            // Ignore in environments without TTS engine
        }
        _isSpeaking.value = false
        setMusicDucking(false)
    }

    fun playCorrectChime() {
        scope.launch {
            playToneSequence(
                listOf(
                    523.25 to 95,  // C5
                    659.25 to 95,  // E5
                    783.99 to 170  // G5
                )
            )
        }
    }

    fun playWrongChime() {
        scope.launch {
            playToneSequence(
                listOf(
                    329.63 to 110, // E4
                    261.63 to 170  // C4
                )
            )
        }
    }

    fun playCelebrationFanfare() {
        scope.launch {
            playToneSequence(
                listOf(
                    523.25 to 90,  // C5
                    659.25 to 90,  // E5
                    783.99 to 90,  // G5
                    1046.50 to 260 // C6
                )
            )
        }
    }

    private fun playToneSequence(notes: List<Pair<Double, Int>>) {
        try {
            val sampleRate = 22050
            val totalSamples = notes.sumOf { (sampleRate * it.second) / 1000 }
            val buffer = ShortArray(totalSamples)
            var offset = 0

            for ((freqHz, durationMs) in notes) {
                val count = (sampleRate * durationMs) / 1000
                for (i in 0 until count) {
                    val t = i.toDouble() / sampleRate
                    val envelope = exp(-4.5 * i / count.toDouble())
                    val sample = sin(2.0 * PI * freqHz * t) * envelope * 0.28
                    buffer[offset + i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }
                offset += count
            }

            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val format = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val minBufBytes = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(buffer.size * 2)

            val track = AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(minBufBytes)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            val totalMs = notes.sumOf { it.second }.toLong() + 60L
            Thread.sleep(totalMs)
            track.stop()
            track.release()
        } catch (_: Throwable) {
            // Gracefully no-op on devices/tests without audio hardware
        }
    }

    fun shutdown() {
        stopSpeaking()
        try {
            musicPlayer?.stop()
            musicPlayer?.release()
        } catch (_: Throwable) {
        }
        musicPlayer = null
        try {
            tts?.shutdown()
        } catch (_: Throwable) {
        }
        scope.cancel()
    }

    companion object {
        private const val NORMAL_MUSIC_VOLUME = 0.28f
        private const val DUCKED_MUSIC_VOLUME = 0.06f
    }
}
