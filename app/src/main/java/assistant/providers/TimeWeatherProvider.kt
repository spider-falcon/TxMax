package assistant.providers

import assistant.models.Actions
import assistant.models.ScoredIntent
import assistant.models.SessionState

class TimeWeatherProvider : IntentProvider {
    override fun process(normalized: String, original: String, state: SessionState): ScoredIntent? {
        return when {
            normalized.matches(Regex(".*\\b(time is it|current time|what time)\\b.*")) || normalized == "time" ->
                ScoredIntent(Actions.GET_TIME, score = 90)

            normalized.matches(Regex(".*\\b(date is today|today's date|what date)\\b.*")) || normalized == "date" ->
                ScoredIntent(Actions.GET_DATE, score = 90)

            normalized.matches(Regex(".*\\b(day is today|what day is it)\\b.*")) || normalized == "today" ->
                ScoredIntent(Actions.GET_DAY, score = 90)

            normalized.contains("weather") || normalized.contains("temperature") || normalized.contains("forecast") ->
                ScoredIntent(Actions.GET_WEATHER, score = 90)

            else -> null
        }
    }
}