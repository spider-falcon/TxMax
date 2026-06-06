package assistant.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

object AppRegistryManager {

    /**
     * Scans the device for all launchable applications and dynamically
     * updates the TextPreprocessor's vocabulary.
     */
    fun syncInstalledApps(context: Context) {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = context.packageManager.queryIntentActivities(
            launcherIntent,
            PackageManager.MATCH_ALL
        )

        val appNames = apps.map {
            it.loadLabel(context.packageManager).toString().lowercase().trim()
        }.toSet()

        // Feed the real apps directly into the NLP layer
        TextPreprocessor.installedApps = appNames
    }
}