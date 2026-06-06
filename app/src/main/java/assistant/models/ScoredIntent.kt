package assistant.models

data class ScoredIntent(
    val action: String,     // <-- FIXED: Changed from 'Actions' to 'String'
    val score: Int,         // 0 to 100 confidence
    val value: String = ""  // Extracted data (app name, time, etc.)
)