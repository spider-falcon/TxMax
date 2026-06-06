package assistant

import android.content.Context
import android.content.Intent
import android.net.Uri

class NavigationActions(private val context: Context) {

    fun navigateTo(destination: String) {
        // The "google.navigation:q=" URI specifically triggers turn-by-turn navigation
        val uri = Uri.parse("google.navigation:q=${Uri.encode(destination)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps") // Forces Google Maps if installed
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Fallback just in case Google Maps isn't installed
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
        }
    }
}