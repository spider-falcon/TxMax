package assistant.providers

import assistant.models.ScoredIntent
import assistant.models.SessionState

interface IntentProvider {
    /**
     * @param normalized The cleaned string (no noise, synonyms resolved, typos fixed)
     * @param original The raw text (useful for search queries where exact text matters)
     * @param state The current context/memory of the assistant (what is currently playing, open, etc.)
     */
    fun process(normalized: String, original: String, state: SessionState): ScoredIntent?
}