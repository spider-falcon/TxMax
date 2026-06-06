package assistant

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeActions(
    private val context: Context
) {

    fun getCurrentTime() {

        val time =
            SimpleDateFormat(
                "h:mm a",
                Locale.getDefault()
            ).format(
                Date()
            )

        Toast.makeText(
            context,
            "Current time is $time",
            Toast.LENGTH_LONG
        ).show()
    }

    fun getCurrentDate() {

        val date =
            SimpleDateFormat(
                "EEEE, d MMMM yyyy",
                Locale.getDefault()
            ).format(
                Date()
            )

        Toast.makeText(
            context,
            "Today is $date",
            Toast.LENGTH_LONG
        ).show()
    }

    fun getCurrentDay() {

        val day =
            SimpleDateFormat(
                "EEEE",
                Locale.getDefault()
            ).format(
                Date()
            )

        Toast.makeText(
            context,
            "Today is $day",
            Toast.LENGTH_LONG
        ).show()
    }
}