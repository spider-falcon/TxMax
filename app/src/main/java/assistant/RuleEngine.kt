package assistant

import assistant.models.Actions
import assistant.models.AssistantIntent
import assistant.models.ScoredIntent
import assistant.models.SessionState
import assistant.providers.*
import assistant.utils.TextPreprocessor

class RuleEngine {

    // 1. The Assistant's Brain/Memory (Lives as long as the engine does)
    val sessionState = SessionState()

    // Register all modular providers
    private val providers = listOf(
        GreetingProvider(),
        TimeWeatherProvider(),
        MediaProvider(),
        HardwareProvider(),
        SettingsProvider(),
        ProductivityProvider(),
        AppAndSearchProvider()
    )

    /**
     * Processes raw user speech input and converts it into a high-confidence system intent.
     * @param text The raw input string from Speech-To-Text or text input.
     * @return An AssistantIntent mapped to an actionable system command.
     */
    fun process(text: String): AssistantIntent {
        // 1. Foundation Cleanup (strips punctuation, filters noise words, normalizes aliases)
        val normalized = TextPreprocessor.clean(text)

        // If the cleanup completely strips the text, immediately exit with UNKNOWN
        if (normalized.isBlank()) {
            return AssistantIntent(action = Actions.UNKNOWN, value = text)
        }

        // 2. Query all independent domain providers for context matching and scores
        // PASS THE SESSION STATE into the providers so they know what's currently happening
        val possibleIntents = providers.mapNotNull { provider ->
            try {
                provider.process(normalized, text, sessionState)
            } catch (e: Exception) {
                // Fail-safe protection: ensure a crash in one provider doesn't take down the entire engine
                null
            }
        }

        // 3. Score-based selection arbitration
        val bestIntent = possibleIntents.maxByOrNull { it.score }

        // 4. Threshold validation (Requires > 50% confidence score to execute)
        return if (bestIntent != null && bestIntent.score > 50) {
            AssistantIntent(
                action = bestIntent.action,
                value = bestIntent.value.ifBlank { null }
            )
        } else {
            AssistantIntent(
                action = Actions.UNKNOWN,
                value = text
            )
        }
    }
}