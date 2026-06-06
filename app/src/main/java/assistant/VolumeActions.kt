package assistant

import android.content.Context
import android.media.AudioManager
import android.widget.Toast

class VolumeActions(
    private val context: Context
) {

    private val audioManager =
        context.getSystemService(
            Context.AUDIO_SERVICE
        ) as AudioManager

    fun volumeUp() {

        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_RAISE,
            AudioManager.FLAG_SHOW_UI
        )

        Toast.makeText(
            context,
            "Volume Up",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun volumeDown() {

        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_SHOW_UI
        )

        Toast.makeText(
            context,
            "Volume Down",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun mute() {

        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_MUTE,
            AudioManager.FLAG_SHOW_UI
        )

        Toast.makeText(
            context,
            "Muted",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun unmute() {

        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_UNMUTE,
            AudioManager.FLAG_SHOW_UI
        )

        Toast.makeText(
            context,
            "Unmuted",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun maxVolume() {

        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(
                AudioManager.STREAM_MUSIC
            ),
            AudioManager.FLAG_SHOW_UI
        )

        Toast.makeText(
            context,
            "Max Volume",
            Toast.LENGTH_SHORT
        ).show()
    }
}