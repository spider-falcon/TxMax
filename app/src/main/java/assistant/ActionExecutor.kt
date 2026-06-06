package assistant

import android.content.Context
import android.widget.Toast
import assistant.models.Actions
import assistant.models.AssistantIntent
import assistant.models.SessionState

class ActionExecutor(
    private val context: Context
) {
    private val appActions = AppActions(context)
    private val settingsActions = SettingsActions(context)
    private val timeActions = TimeActions(context)
    private val alarmActions = AlarmActions(context)
    private val flashlightActions = FlashlightActions(context)
    private val volumeActions = VolumeActions(context)
    private val contactsActions = ContactsActions(context)
    private val mediaActions = MediaActions(context)
    private val notificationActions = NotificationActions(context)
    private val calendarActions = CalendarActions(context)
    private val speechActions = SpeechActions(context)

    // NEW ACTION HANDLERS
    private val webActions = WebActions(context)
    private val navigationActions = NavigationActions(context)

    // Pass the SessionState into the execute function so we can update memory!
    fun execute(intent: AssistantIntent, state: SessionState) {
        when (intent.action) {
            Actions.GREETING -> Toast.makeText(context, "Hello!", Toast.LENGTH_SHORT).show()

            Actions.OPEN_APP -> {
                appActions.openApp(intent.value)
                state.lastOpenedApp = intent.value // <-- MEMORY UPDATE
            }

            Actions.OPEN_SETTINGS -> settingsActions.openSettings()
            Actions.OPEN_WIFI_SETTINGS -> settingsActions.openWifiSettings()
            Actions.OPEN_BLUETOOTH_SETTINGS -> settingsActions.openBluetoothSettings()
            Actions.OPEN_BATTERY_SETTINGS -> settingsActions.openBatterySettings()
            Actions.GET_TIME -> timeActions.getCurrentTime()
            Actions.GET_DATE -> timeActions.getCurrentDate()
            Actions.GET_DAY -> timeActions.getCurrentDay()

            Actions.SET_ALARM -> {
                val parts = intent.value?.split(":")
                val hour = parts?.getOrNull(0)?.trim()?.toIntOrNull()
                val minute = parts?.getOrNull(1)?.trim()?.toIntOrNull()
                alarmActions.setAlarm(hour, minute)
            }

            Actions.SET_TIMER -> {
                val seconds = intent.value?.trim()?.toIntOrNull()
                alarmActions.setTimer(seconds)
            }

            Actions.FLASHLIGHT_ON -> {
                flashlightActions.turnOn()
                state.isFlashlightOn = true // <-- MEMORY UPDATE
            }
            Actions.FLASHLIGHT_OFF -> {
                flashlightActions.turnOff()
                state.isFlashlightOn = false // <-- MEMORY UPDATE
            }
            Actions.FLASHLIGHT_TOGGLE -> {
                flashlightActions.toggle()
                state.isFlashlightOn = !state.isFlashlightOn // <-- MEMORY UPDATE
            }

            Actions.VOLUME_UP -> volumeActions.volumeUp()
            Actions.VOLUME_DOWN -> volumeActions.volumeDown()
            Actions.MUTE_VOLUME -> volumeActions.mute()
            Actions.UNMUTE_VOLUME -> volumeActions.unmute()
            Actions.MAX_VOLUME -> volumeActions.maxVolume()

            Actions.OPEN_CONTACTS -> contactsActions.openContacts()
            Actions.CALL_NUMBER -> {
                val number = intent.value ?: return
                contactsActions.callNumber(number)
            }
            Actions.CALL_CONTACT -> {
                val contactName = intent.value ?: return
                contactsActions.callContact(contactName)
                state.activeCallContact = contactName // <-- MEMORY UPDATE
            }

            Actions.PLAY_MEDIA -> {
                mediaActions.play()
                state.isMediaPlaying = true // <-- MEMORY UPDATE
            }
            Actions.PAUSE_MEDIA -> {
                mediaActions.pause()
                state.isMediaPlaying = false // <-- MEMORY UPDATE
            }
            Actions.PLAY_PAUSE_MEDIA -> {
                mediaActions.playPause()
                state.isMediaPlaying = !state.isMediaPlaying // <-- MEMORY UPDATE
            }
            Actions.NEXT_MEDIA -> mediaActions.next()
            Actions.PREVIOUS_MEDIA -> mediaActions.previous()

            Actions.SHOW_NOTIFICATION -> {
                notificationActions.showNotification("TX Max", "Notification test successful")
            }
            Actions.ADD_CALENDAR_EVENT -> {
                val details = intent.value ?: return
                calendarActions.createEvent(details, details)
            }

            // --- NEW FEATURES EXECUTED HERE ---
            Actions.GET_WEATHER -> webActions.searchWeather()
            Actions.SEARCH_WEB -> {
                val query = intent.value ?: return
                webActions.searchWeb(query)
            }
            Actions.NAVIGATE -> {
                val destination = intent.value ?: return
                navigationActions.navigateTo(destination)
            }

            // --- PRODUCTIVITY FEATURES ---
            "SEND_SMS" -> {
                // Placeholder: call smsActions.sendSms(intent.value) here when ready!
                Toast.makeText(context, "Preparing to text ${intent.value}", Toast.LENGTH_SHORT).show()
            }
            "TAKE_NOTE" -> {
                // Placeholder: save note locally or launch Notes app
                Toast.makeText(context, "Note saved: ${intent.value}", Toast.LENGTH_SHORT).show()
            }
            "SEND_EMAIL" -> {
                // Placeholder: trigger email intent
                Toast.makeText(context, "Opening email to ${intent.value}", Toast.LENGTH_SHORT).show()
            }

            Actions.UNKNOWN -> Toast.makeText(context, "Unknown action", Toast.LENGTH_SHORT).show()
        }
    }
}