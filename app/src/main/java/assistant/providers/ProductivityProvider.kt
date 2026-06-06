package assistant.providers

import assistant.models.Actions
import assistant.models.ScoredIntent
import assistant.models.SessionState

class ProductivityProvider : IntentProvider {
    override fun process(normalized: String, original: String, state: SessionState): ScoredIntent? {

        // 0. Context-Aware Inference (The "Memory" factor)
        // If the user asks to redial, check if we remember the last person we called.
        val genericCall = listOf("redial", "call back", "call them back", "call again")
        if (genericCall.any { normalized == it } && state.activeCallContact != null) {
            return ScoredIntent(Actions.CALL_CONTACT, 95, state.activeCallContact!!)
        }

        // 1. Notifications
        if (normalized == "show notification" || normalized == "test notification") {
            return ScoredIntent(Actions.SHOW_NOTIFICATION, 100)
        }

        // 2. Calendar
        if (normalized.startsWith("create event")) {
            val eventDetails = original.lowercase().removePrefix("create event").trim()
            return ScoredIntent(Actions.ADD_CALENDAR_EVENT, 90, eventDetails)
        }

        // 3. Alarms (Massively upgraded to catch ALL natural language variations)
        val alarmTriggers = listOf("alarm", "wake")
        if (alarmTriggers.any { normalized.contains(it) }) {

            // This Regex brilliantly hunts for a time anywhere in the sentence.
            // It matches: "7", "7:30", "7 am", "7:00 pm", "14:00"
            val timeRegex = Regex("\\b(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?\\b")
            val match = timeRegex.find(normalized)

            if (match != null) {
                // We found a time! Let's format it cleanly as "HH:MM" for the Executor
                var hour = match.groupValues[1].toIntOrNull() ?: 0

                // If the user just said "7", default the minutes to "0"
                val minuteStr = match.groupValues[2]
                val minute = if (minuteStr.isNotEmpty()) minuteStr.toIntOrNull() ?: 0 else 0

                val amPm = match.groupValues[3]

                // Handle AM / PM 24-hour conversion
                if (amPm == "pm" && hour < 12) hour += 12
                if (amPm == "am" && hour == 12) hour = 0

                val formattedTime = "$hour:$minute"
                return ScoredIntent(Actions.SET_ALARM, 95, formattedTime)
            } else {
                // User asked for an alarm but didn't give a time (e.g., "set an alarm")
                // Return an empty string so the Executor safely opens the Clock app
                return ScoredIntent(Actions.SET_ALARM, 85, "")
            }
        }

        // 4. Calls (Smart Routing for Number vs. Contact)
        if (normalized.startsWith("call ") && normalized.length > 5) {
            val target = original.lowercase().removePrefix("call ").trim()

            // Check if the target is entirely digits (ignoring spaces or hyphens)
            val isNumber = target.replace(Regex("[\\s-]"), "").all { it.isDigit() }

            return if (isNumber) {
                val cleanNumber = target.replace(Regex("[\\s-]"), "")
                ScoredIntent(Actions.CALL_NUMBER, 95, cleanNumber)
            } else {
                ScoredIntent(Actions.CALL_CONTACT, 90, target)
            }
        }

        // 5. Timers
        if (normalized.startsWith("set timer") || normalized.startsWith("set a timer")) {
            val timerRegex = Regex("(?:for)?\\s*(\\d+)\\s*(second|minute|hour)s?")
            val match = timerRegex.find(normalized)

            if (match != null) {
                val amount = match.groupValues[1].toIntOrNull() ?: 0
                val unit = match.groupValues[2]

                val seconds = when (unit) {
                    "second" -> amount
                    "minute" -> amount * 60
                    "hour" -> amount * 3600
                    else -> null
                }

                if (seconds != null) {
                    return ScoredIntent(Actions.SET_TIMER, 95, seconds.toString())
                }
            } else {
                return ScoredIntent(Actions.SET_TIMER, 85, "")
            }
        }

        // 6. Texting / SMS
        if (normalized.startsWith("text ") || normalized.startsWith("send message to ") || normalized.startsWith("message ")) {
            val contact = original.lowercase()
                .removePrefix("send message to ")
                .removePrefix("message ")
                .removePrefix("text ")
                .trim()
            return ScoredIntent("SEND_SMS", 90, contact)
        }

        // 7. Note Taking
        if (normalized.startsWith("take a note ") || normalized.startsWith("note to self ")) {
            val noteContent = original
                .replace(Regex("^(take a note|note to self)\\s+", RegexOption.IGNORE_CASE), "")
                .trim()
            return ScoredIntent("TAKE_NOTE", 90, noteContent)
        }

        // 8. Emails
        if (normalized.startsWith("email ") || normalized.startsWith("send email to ")) {
            val contact = original.lowercase()
                .removePrefix("send email to ")
                .removePrefix("email ")
                .trim()
            return ScoredIntent("SEND_EMAIL", 90, contact)
        }

        return null
    }
}