package assistant.models

data class SessionState(
    var isMediaPlaying: Boolean = false,
    var activeCallContact: String? = null,
    var isFlashlightOn: Boolean = false,
    var lastOpenedApp: String? = null,

    // For the future Action Confirmation System
    var pendingDangerousAction: Actions? = null,
    var pendingActionValue: String? = null
)