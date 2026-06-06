package assistant

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class SpeechActions(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            isInitialized = true
        }
    }

    private fun applyVoiceSettings() {
        val prefs = context.getSharedPreferences("tx_max_settings", Context.MODE_PRIVATE)
        val voiceStyle = prefs.getString("voice_engine", "Default System")

        // Adjust pitch and speed to simulate different voice engines
        when (voiceStyle) {
            "TX-Max Premium Male" -> {
                tts?.setPitch(0.6f)  // Deeper voice
                tts?.setSpeechRate(1.0f)
            }
            "TX-Max Premium Female" -> {
                tts?.setPitch(1.4f)  // Higher voice
                tts?.setSpeechRate(1.05f) // Slightly faster
            }
            "Neural English Echo" -> {
                tts?.setPitch(0.85f)
                tts?.setSpeechRate(0.8f) // Slower, more deliberate
            }
            else -> { // Default System
                tts?.setPitch(1.0f)
                tts?.setSpeechRate(1.0f)
            }
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            applyVoiceSettings() // Check settings right before speaking
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}