package assistant.providers

import assistant.models.Actions
import assistant.models.ScoredIntent
import assistant.models.SessionState

class MediaProvider : IntentProvider {
    override fun process(normalized: String, original: String, state: SessionState): ScoredIntent? {

        // 1. Context-Aware Inference (The "Memory" factor)
        // If the user uses generic commands, we check the current state to infer their intent.

        val genericPause = listOf("stop it", "pause it", "halt", "stop")
        if (genericPause.any { normalized == it } && state.isMediaPlaying) {
            return ScoredIntent(Actions.PAUSE_MEDIA, 90)
        }

        val genericPlay = listOf("play it", "resume it", "continue", "play")
        if (genericPlay.any { normalized == it } && !state.isMediaPlaying) {
            return ScoredIntent(Actions.PLAY_MEDIA, 90)
        }

        val genericNext = listOf("next", "skip it", "skip")
        if (genericNext.any { normalized == it } && state.isMediaPlaying) {
            return ScoredIntent(Actions.NEXT_MEDIA, 90)
        }

        // 2. Explicit Media Commands
        return when {
            // High confidence (explicitly mentions music)
            normalized.contains("play music") -> ScoredIntent(Actions.PLAY_MEDIA, 100)
            normalized.contains("pause music") || normalized.contains("stop music") -> ScoredIntent(Actions.PAUSE_MEDIA, 100)
            normalized.contains("toggle music") || normalized == "play pause" -> ScoredIntent(Actions.PLAY_PAUSE_MEDIA, 100)

            // Medium confidence (general media commands as fallback)
            normalized.matches(Regex(".*\\b(play|resume)\\b.*")) -> ScoredIntent(Actions.PLAY_MEDIA, 70)
            normalized.matches(Regex(".*\\b(pause|stop|halt)\\b.*")) -> ScoredIntent(Actions.PAUSE_MEDIA, 70)
            normalized.matches(Regex(".*\\b(next song|next track|skip)\\b.*")) -> ScoredIntent(Actions.NEXT_MEDIA, 85)
            normalized.matches(Regex(".*\\b(previous song|previous track|go back)\\b.*")) -> ScoredIntent(Actions.PREVIOUS_MEDIA, 85)

            else -> null
        }
    }
}