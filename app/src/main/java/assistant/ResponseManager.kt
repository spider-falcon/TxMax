package assistant

import assistant.models.Actions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ResponseManager {

    // Define multiple phrase variations for different actions
    private val greetings = listOf(
        "Hello, how can I help you today?",
        "Hi there! What can I do for you?",
        "Hey! How can I assist you right now?",
        "Hello! What's on your mind?"
    )

    private val timeFormats = listOf(
        "Current time is %s",
        "Right now it's %s",
        "It is exactly %s"
    )

    private val dateFormats = listOf(
        "Today is %s",
        "The calendar says today is %s",
        "It's %s"
    )

    private val weatherPhrases = listOf(
        "Getting the weather forecast info.",
        "Checking the latest weather details for you now.",
        "Let me look up the weather forecast."
    )

    private val openAppPhrases = listOf(
        "Opening %s right away.",
        "Launching %s.",
        "Starting up %s."
    )

    private val navigatePhrases = listOf(
        "Navigating to %s.",
        "Starting navigation route to %s.",
        "Let's head over to %s."
    )

    private val settingsPhrases = listOf("Opening settings.", "Launching system settings.")
    private val wifiPhrases = listOf("Opening wifi settings.", "Bringing up your Wi-Fi dashboard.")
    private val bluetoothPhrases = listOf("Opening bluetooth settings.", "Accessing bluetooth configurations.")
    private val batteryPhrases = listOf("Opening battery settings.", "Checking power and battery status.")
    private val contactsPhrases = listOf("Opening contacts.", "Accessing your address book.")

    private val alarmPhrasesWithVal = listOf("Setting alarm for %s.", "Alarm configured for %s.")
    private val alarmPhrasesNoVal = listOf("Opening alarms.", "Showing your alarm clock overview.")
    private val timerPhrasesWithVal = listOf("Setting a timer.", "Timer sequence initialized.")
    private val timerPhrasesNoVal = listOf("Opening timers.", "Bringing up your stopwatch and timers.")

    private val callContactPhrases = listOf("Calling %s.", "Dialing up %s now.")
    private val callNumberPhrases = listOf("Dialing %s.", "Connecting you to %s.")
    private val calendarPhrases = listOf("Creating event.", "Adding appointment to your calendar.")
    private val smsPhrases = listOf("Preparing to text %s.", "Opening compose window for %s.")
    private val notePhrases = listOf("Note saved.", "Got it, note secured safely.")
    private val emailPhrases = listOf("Opening email to %s.", "Drafting a new message to %s.")
    private val notificationPhrases = listOf("Sending test notification.", "Push warning dispatched.")

    private val playMediaPhrases = listOf("Playing music.", "Starting audio playback.", "Turning on your tracks.")
    private val pauseMediaPhrases = listOf("Pausing music.", "Audio playback suspended.", "Halting audio streams.")
    private val nextMediaPhrases = listOf("Next song.", "Skipping forward.", "Playing the next track.")
    private val prevMediaPhrases = listOf("Previous song.", "Going back a track.", "Restarting previous track.")

    private val flashlightOnPhrases = listOf("Turning on the flashlight.", "Let there be light! Flashlight activated.")
    private val flashlightOffPhrases = listOf("Turning off the flashlight.", "Switching off flashlight beams.")
    private val volumeUpPhrases = listOf("Increasing volume.", "Turning the audio up.", "Making it louder.")
    private val volumeDownPhrases = listOf("Decreasing volume.", "Lowering the audio footprint.")
    private val mutePhrases = listOf("Muting the device.", "Silencing your outputs.")
    private val unmutePhrases = listOf("Unmuting the device.", "Restoring audio volume streams.")
    private val maxVolumePhrases = listOf("Setting volume to maximum.", "Cranking the audio output to max!")

    private val fallbackPhrases = listOf(
        "Sorry, I did not catch that.",
        "I'm not sure I understood that command.",
        "Could you rephrase that? I didn't understand."
    )

    /**
     * Resolves an intent string action to a randomized text variation instance.
     */
    fun generateResponse(action: String?, value: String?, isMediaPlaying: Boolean, isFlashlightOn: Boolean): SearchResult {
        val cleanValue = value ?: ""

        val textResult = when (action) {
            Actions.GREETING -> greetings.random()

            Actions.GET_TIME -> {
                val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                timeFormats.random().format(timeStr)
            }

            Actions.GET_DATE -> {
                val dateStr = SimpleDateFormat("EEEE d MMMM yyyy", Locale.getDefault()).format(Date())
                dateFormats.random().format(dateStr)
            }

            Actions.GET_DAY -> {
                val dayStr = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
                dateFormats.random().format(dayStr)
            }

            Actions.GET_WEATHER -> weatherPhrases.random()
            Actions.OPEN_APP -> openAppPhrases.random().format(cleanValue)
            Actions.NAVIGATE -> navigatePhrases.random().format(cleanValue)

            Actions.OPEN_SETTINGS -> settingsPhrases.random()
            Actions.OPEN_WIFI_SETTINGS -> wifiPhrases.random()
            Actions.OPEN_BLUETOOTH_SETTINGS -> bluetoothPhrases.random()
            Actions.OPEN_BATTERY_SETTINGS -> batteryPhrases.random()
            Actions.OPEN_CONTACTS -> contactsPhrases.random()

            Actions.SET_ALARM -> if (cleanValue.isBlank()) alarmPhrasesNoVal.random() else alarmPhrasesWithVal.random().format(cleanValue)
            Actions.SET_TIMER -> if (cleanValue.isBlank()) timerPhrasesNoVal.random() else timerPhrasesWithVal.random()

            Actions.CALL_CONTACT -> callContactPhrases.random().format(cleanValue)
            Actions.CALL_NUMBER -> callNumberPhrases.random().format(cleanValue)
            Actions.ADD_CALENDAR_EVENT -> calendarPhrases.random()

            "SEND_SMS" -> smsPhrases.random().format(cleanValue)
            "TAKE_NOTE" -> notePhrases.random()
            "SEND_EMAIL" -> emailPhrases.random().format(cleanValue)
            Actions.SHOW_NOTIFICATION -> notificationPhrases.random()

            Actions.PLAY_MEDIA -> playMediaPhrases.random()
            Actions.PAUSE_MEDIA -> pauseMediaPhrases.random()
            Actions.PLAY_PAUSE_MEDIA -> if (isMediaPlaying) pauseMediaPhrases.random() else playMediaPhrases.random()
            Actions.NEXT_MEDIA -> nextMediaPhrases.random()
            Actions.PREVIOUS_MEDIA -> prevMediaPhrases.random()

            Actions.FLASHLIGHT_ON -> flashlightOnPhrases.random()
            Actions.FLASHLIGHT_OFF -> flashlightOffPhrases.random()
            Actions.FLASHLIGHT_TOGGLE -> if (isFlashlightOn) flashlightOffPhrases.random() else flashlightOnPhrases.random()

            Actions.VOLUME_UP -> volumeUpPhrases.random()
            Actions.VOLUME_DOWN -> volumeDownPhrases.random()
            Actions.MUTE_VOLUME -> mutePhrases.random()
            Actions.UNMUTE_VOLUME -> unmutePhrases.random()
            Actions.MAX_VOLUME -> maxVolumePhrases.random()

            else -> fallbackPhrases.random()
        }

        return SearchResult(text = textResult)
    }
}