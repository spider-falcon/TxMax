package assistant.providers

import assistant.models.Actions
import assistant.models.ScoredIntent
import assistant.models.SessionState

class HardwareProvider : IntentProvider {
    override fun process(normalized: String, original: String, state: SessionState): ScoredIntent? {

        // 1. Context-Aware Inference (The "It" factor)
        // If the user uses generic pronouns, we check the current state to infer their intent.
        val genericOff = listOf("turn it off", "stop it", "disable it", "shut it off")
        if (genericOff.any { normalized == it }) {
            if (state.isFlashlightOn) {
                return ScoredIntent(Actions.FLASHLIGHT_OFF, 90)
            }
        }

        // 2. Flashlight Logic
        if (normalized.contains("flashlight") || normalized.contains("torch")) {
            return when {
                normalized.matches(Regex(".*\\b(on|enable|activate)\\b.*")) -> ScoredIntent(Actions.FLASHLIGHT_ON, 95)
                normalized.matches(Regex(".*\\b(off|disable|deactivate)\\b.*")) -> ScoredIntent(Actions.FLASHLIGHT_OFF, 95)
                else -> ScoredIntent(Actions.FLASHLIGHT_TOGGLE, 80)
            }
        }

        // 3. Volume Logic
        if (normalized.contains("volume")) {
            return when {
                normalized.matches(Regex(".*\\b(up|increase|higher)\\b.*")) -> ScoredIntent(Actions.VOLUME_UP, 95)
                normalized.matches(Regex(".*\\b(down|decrease|lower)\\b.*")) -> ScoredIntent(Actions.VOLUME_DOWN, 95)
                normalized.contains("mute") -> ScoredIntent(Actions.MUTE_VOLUME, 95)
                normalized.contains("unmute") -> ScoredIntent(Actions.UNMUTE_VOLUME, 95)
                normalized.contains("max") -> ScoredIntent(Actions.MAX_VOLUME, 95)
                else -> null
            }
        }

        // 4. Catch standalone mute/unmute without the word "volume"
        if (normalized == "mute" || normalized == "mute phone") return ScoredIntent(Actions.MUTE_VOLUME, 80)
        if (normalized == "unmute" || normalized == "unmute phone") return ScoredIntent(Actions.UNMUTE_VOLUME, 80)

        return null
    }
}