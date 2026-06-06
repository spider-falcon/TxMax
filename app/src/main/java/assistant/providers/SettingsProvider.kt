package assistant.providers

import assistant.models.Actions
import assistant.models.ScoredIntent
import assistant.models.SessionState

class SettingsProvider : IntentProvider {
    override fun process(normalized: String, original: String, state: SessionState): ScoredIntent? {
        if (!normalized.contains("open") && !normalized.contains("show")) return null

        return when {
            normalized.contains("wifi settings") -> ScoredIntent(Actions.OPEN_WIFI_SETTINGS, 90)
            normalized.contains("bluetooth settings") -> ScoredIntent(Actions.OPEN_BLUETOOTH_SETTINGS, 90)
            normalized.contains("battery settings") -> ScoredIntent(Actions.OPEN_BATTERY_SETTINGS, 90)
            normalized.contains("contacts") -> ScoredIntent(Actions.OPEN_CONTACTS, 90)
            normalized.contains("settings") -> ScoredIntent(Actions.OPEN_SETTINGS, 80) // General fallback
            else -> null
        }
    }
}