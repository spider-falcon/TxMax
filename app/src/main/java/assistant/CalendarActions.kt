package assistant

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract

class CalendarActions(
    private val context: Context
) {

    fun createEvent(
        title: String,
        description: String
    ) {

        val intent = Intent(
            Intent.ACTION_INSERT
        ).apply {

            data =
                CalendarContract.Events.CONTENT_URI

            putExtra(
                CalendarContract.Events.TITLE,
                title
            )

            putExtra(
                CalendarContract.Events.DESCRIPTION,
                description
            )

            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        }

        context.startActivity(
            intent
        )
    }
}