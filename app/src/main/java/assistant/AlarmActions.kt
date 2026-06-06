package assistant

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.Toast

class AlarmActions(
    private val context: Context
) {

    fun setAlarm(
        hour: Int? = null,
        minute: Int? = null
    ) {
        val intent = if (hour != null && minute != null) {
            // Specific time provided: Set the alarm
            Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            }
        } else {
            // No valid time provided: Open the Clock/Alarms app
            Intent(AlarmClock.ACTION_SHOW_ALARMS)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No clock app found", Toast.LENGTH_SHORT).show()
        }
    }

    fun setTimer(
        seconds: Int? = null
    ) {
        val intent = if (seconds != null && seconds > 0) {
            // Specific duration provided: Set the timer
            Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            }
        } else {
            // No valid time provided: Open the Timers app
            Intent(AlarmClock.ACTION_SHOW_TIMERS)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback: Some older custom ROMs don't support ACTION_SHOW_TIMERS
            try {
                val fallbackIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                Toast.makeText(context, "No clock app found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}