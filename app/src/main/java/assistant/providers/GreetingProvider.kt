package assistant.providers

import assistant.models.Actions
import assistant.models.ScoredIntent
import assistant.models.SessionState

class GreetingProvider : IntentProvider {
    override fun process(normalized: String, original: String, state: SessionState): ScoredIntent? {
        val greetings = listOf("hello", "hi", "hey", "wake up", "sup", "greetings")

        return if (greetings.any { normalized == it }) {
            ScoredIntent(action = Actions.GREETING, score = 100)
        } else if (greetings.any { normalized.contains(it) }) {
            ScoredIntent(action = Actions.GREETING, score = 50)
        } else {
            null
        }
    }
}