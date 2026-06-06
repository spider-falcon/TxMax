package assistant

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast

class AppActions(
    private val context: Context
) {

    fun openApp(appName: String?) {

        if (appName.isNullOrBlank()) {
            return
        }

        val launcherIntent =
            Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

        val apps =
            context.packageManager.queryIntentActivities(
                launcherIntent,
                PackageManager.MATCH_ALL
            )

        for (app in apps) {

            val label =
                app.loadLabel(
                    context.packageManager
                ).toString()

            if (
                label.contains(appName, true) ||
                appName.contains(label, true)
            ) {

                val launchIntent =
                    context.packageManager
                        .getLaunchIntentForPackage(
                            app.activityInfo.packageName
                        )

                if (launchIntent != null) {

                    launchIntent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )

                    context.startActivity(
                        launchIntent
                    )
                }

                return
            }
        }

        Toast.makeText(
            context,
            "App not found: $appName",
            Toast.LENGTH_SHORT
        ).show()
    }
}