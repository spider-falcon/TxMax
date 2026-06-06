package assistant

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

class WebActions(private val context: Context) {

    fun searchWeb(query: String) {
        // Construct the DuckDuckGo search URL
        val url = "https://duckduckgo.com/?q=${Uri.encode(query)}"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            // Directly start the activity instead of using resolveActivity
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Fallback if the user somehow has absolutely no web browser installed
            Toast.makeText(context, "No web browser found to open the link.", Toast.LENGTH_SHORT).show()
        }
    }

    fun searchWeather() {
        searchWeb("weather near me")
    }
}