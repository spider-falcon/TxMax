package assistant.utils

object TextPreprocessor {

    // Dynamic app registry (Can be updated later by ActionExecutor pulling real installed apps)
    var installedApps = setOf("youtube", "spotify", "whatsapp", "chrome", "instagram", "gallery", "camera")

    // 1. HUGE phrase expansion
    private val phraseMap = mapOf(
        // Assistant wake
        "hey max" to "", "okay max" to "", "ok max" to "", "hello max" to "",
        // Polite
        "can you please" to "", "could you please" to "", "would you please" to "", "please can you" to "",
        // Navigation
        "take me to" to "navigate to", "bring me to" to "navigate to",
        "show me directions to" to "navigate to", "route me to" to "navigate to",
        // Hardware
        "wi fi" to "wifi", "wireless internet" to "wifi",
        "blu tooth" to "bluetooth", "blue tooth" to "bluetooth",
        "torch light" to "flashlight", "phone light" to "flashlight",
        // Timers
        "half an hour" to "30 minutes", "half hour" to "30 minutes", "quarter hour" to "15 minutes",
        // Media & Volume
        "turn the music on" to "play music", "turn music off" to "pause music",
        "make it louder" to "volume up", "make it quieter" to "volume down",
        // Search & Browser
        "look up" to "search", "search for" to "search",
        "internet browser" to "browser", "web browser" to "browser"
    )

    // 2. MUCH larger synonym map
    private val synonymMap = mapOf(
        "torch" to "flashlight", "lamp" to "flashlight", "light" to "flashlight",
        "song" to "music", "songs" to "music", "audio" to "music",
        "track" to "music", "tracks" to "music", "playlist" to "music", "spotify" to "music",
        "resume" to "play", "continue" to "play", "halt" to "pause",
        "map" to "navigate", "maps" to "navigate",
        "google" to "search", "bing" to "search",
        "louder" to "up", "higher" to "up", "increase" to "up",
        "quieter" to "down", "lower" to "down", "decrease" to "down", "silence" to "mute",
        "execute" to "open", "launch" to "open", "load" to "open", "run" to "open", "show" to "open",
        "dial" to "call", "phone" to "call",
        "config" to "settings", "configuration" to "settings",
        // Numbers & STT Error handling (won -> 1 handled in context later to avoid breaking "I won")
        "zero" to "0", "one" to "1", "two" to "2", "three" to "3", "four" to "4",
        "five" to "5", "six" to "6", "seven" to "7", "eight" to "8", "nine" to "9", "ten" to "10",
        "sec" to "seconds", "secs" to "seconds", "min" to "minutes", "mins" to "minutes",
        "hr" to "hours", "hrs" to "hours"
    )

    // 3. MUCH better core vocabulary (Safe targets for fuzzy matching)
    private val coreVocabulary = listOf(
        // apps handled dynamically via installedApps
        // commands
        "open", "close", "play", "pause", "search", "navigate", "call", "message",
        // hardware
        "flashlight", "bluetooth", "wifi", "volume",
        // settings
        "settings", "battery",
        // media
        "music", "video",
        // time
        "alarm", "timer",
        // utility
        "weather", "browser"
    )

    // 8. Stopword filtering
    private val noiseWords = setOf(
        "the", "a", "an", "please", "kindly", "actually", "just", "um", "uh"
    )

    // 12. PROTECTED ENTITY TRIGGERS
    // If we see these words, we STOP dropping noise words and STOP fuzzy matching
    // so we don't accidentally ruin contact names or search queries.
    private val protectedTriggers = setOf(
        "search", "browser", "google", "call", "dial", "message", "text", "email", "navigate"
    )

    fun clean(input: String): String {
        var text = input.lowercase().trim()

        // 5. Repeated letter cleanup (e.g., "pleaaase" -> "please", "heyyyy" -> "heyy")
        text = text.replace(Regex("(.)\\1{2,}"), "$1$1")

        // 1. Multi-word phrases replacement (Sorted by length so long phrases get matched first!)
        val sortedPhrases = phraseMap.entries.sortedByDescending { it.key.length }
        for ((phrase, replacement) in sortedPhrases) {
            text = text.replace(Regex("\\b$phrase\\b"), replacement)
        }

        // Remove punctuation
        text = text.replace(Regex("[^a-z0-9\\s]"), "")

        // Tokenize
        val tokens = text.split(Regex("\\s+")).filter { it.isNotBlank() }

        val processedTokens = mutableListOf<String>()
        var isProtectedContext = false
        var previousToken = ""

        for (token in tokens) {
            // 7. Command deduplication (e.g., "open open youtube" -> "open youtube")
            if (token == previousToken) continue

            // 4. & 12. Context Protection Check
            if (protectedTriggers.contains(token)) {
                isProtectedContext = true
            }

            when {
                // Drop noise words ONLY if we are not in a protected context
                noiseWords.contains(token) && !isProtectedContext -> continue

                // Exact Synonyms ONLY if not in a protected context
                // (prevents "call map" from turning into "call navigate")
                synonymMap.containsKey(token) && !isProtectedContext -> {
                    val mapped = synonymMap[token]!!
                    processedTokens.add(mapped)
                    previousToken = mapped
                }

                // 11. & Fuzzy Matching (ONLY if not protected and length > 3)
                !isProtectedContext && token.length > 3 -> {
                    val maxAllowedTypos = if (token.length > 5) 2 else 1

                    // Prioritize core vocabulary first
                    var fuzzyMatch = coreVocabulary.firstOrNull {
                        StringUtils.isFuzzyMatch(token, it, maxAllowedTypos)
                    }

                    // Fallback to installed apps registry
                    if (fuzzyMatch == null) {
                        fuzzyMatch = installedApps.firstOrNull {
                            StringUtils.isFuzzyMatch(token, it, maxAllowedTypos)
                        }
                    }

                    val finalToken = fuzzyMatch ?: token
                    processedTokens.add(finalToken)
                    previousToken = finalToken
                }

                // Default: keep token exactly as it is (protects entities, names, and queries)
                else -> {
                    processedTokens.add(token)
                    previousToken = token
                }
            }
        }

        return processedTokens.joinToString(" ").trim()
    }
}