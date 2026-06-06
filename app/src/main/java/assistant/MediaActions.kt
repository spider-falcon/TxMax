package assistant

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import android.widget.Toast

class MediaActions(
    private val context: Context
) {

    private val audioManager =
        context.getSystemService(
            Context.AUDIO_SERVICE
        ) as AudioManager

    fun playPause() {

        sendMediaKey(
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        )

        Toast.makeText(
            context,
            "Play / Pause",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun play() {

        sendMediaKey(
            KeyEvent.KEYCODE_MEDIA_PLAY
        )

        Toast.makeText(
            context,
            "Play",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun pause() {

        sendMediaKey(
            KeyEvent.KEYCODE_MEDIA_PAUSE
        )

        Toast.makeText(
            context,
            "Pause",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun next() {

        sendMediaKey(
            KeyEvent.KEYCODE_MEDIA_NEXT
        )

        Toast.makeText(
            context,
            "Next Song",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun previous() {

        sendMediaKey(
            KeyEvent.KEYCODE_MEDIA_PREVIOUS
        )

        Toast.makeText(
            context,
            "Previous Song",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun sendMediaKey(
        keyCode: Int
    ) {

        audioManager.dispatchMediaKeyEvent(
            KeyEvent(
                KeyEvent.ACTION_DOWN,
                keyCode
            )
        )

        audioManager.dispatchMediaKeyEvent(
            KeyEvent(
                KeyEvent.ACTION_UP,
                keyCode
            )
        )
    }
}