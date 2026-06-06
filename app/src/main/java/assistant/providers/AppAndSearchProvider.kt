package assistant.providers

import assistant.models.Actions
import assistant.models.ScoredIntent
import assistant.models.SessionState
import assistant.utils.TextPreprocessor

class AppAndSearchProvider : IntentProvider {
    override fun process(normalized: String, original: String, state: SessionState): ScoredIntent? {

        // 1. Context-Aware Inference (The "Memory" factor)
        // If the user says "open it", check if we remember the last opened app.
        val genericOpen = listOf("open it", "launch it", "reopen it", "start it")
        if (genericOpen.any { normalized == it } && state.lastOpenedApp != null) {
            return ScoredIntent(Actions.OPEN_APP, 95, state.lastOpenedApp!!)
        }

        // 2. Intent Hints (Single-token inference)
        // If the user just says "youtube" or "spotify", automatically infer they want to open it.
        if (TextPreprocessor.installedApps.contains(normalized)) {
            return ScoredIntent(Actions.OPEN_APP, 95, normalized)
        }

        // 3. App Launching (Explicit)
        val appRegex = Regex("^(open|launch|start|run)\\s+(.*)")
        val appMatch = appRegex.find(normalized)
        if (appMatch != null && !normalized.contains("settings")) {
            // Avoid conflict with SettingsProvider
            val appName = appMatch.groupValues[2].trim()
            return ScoredIntent(Actions.OPEN_APP, 85, appName)
        }

        // 4. Navigation
        if (normalized.startsWith("navigate to ") || normalized.startsWith("directions to ")) {
            val dest = original.lowercase().removePrefix("navigate to ").removePrefix("directions to ").trim()
            return ScoredIntent(Actions.NAVIGATE, 95, dest)
        }

        // 5. Explicit Browser Search
        if (normalized.startsWith("browser ") || normalized.startsWith("search in browser ")) {
            val query = original.lowercase().replace(Regex("^(browser search|browser|search in browser)\\s+"), "").trim()
            return ScoredIntent(Actions.SEARCH_WEB, 95, query)
        }

        // 6. Natural Language Web Search
        if (normalized.matches(Regex("^(search for|search|google|who is|what is|how to)\\s+.*"))) {
            val query = if (normalized.startsWith("search") || normalized.startsWith("google")) {
                original.lowercase().replace(Regex("^(search for|search|google)\\s+"), "").trim()
            } else {
                original // Keep "who is" / "what is" intact for better search engine results
            }
            return ScoredIntent(Actions.SEARCH_WEB, 80, query)
        }

        return null
    }
}