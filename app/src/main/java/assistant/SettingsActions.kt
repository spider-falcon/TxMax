package assistant

import android.content.Context
import android.content.Intent
import android.provider.Settings

class SettingsActions(
    private val context: Context
) {

    fun openSettings() {

        context.startActivity(
            Intent(
                Settings.ACTION_SETTINGS
            ).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        )
    }

    fun openWifiSettings() {

        context.startActivity(
            Intent(
                Settings.ACTION_WIFI_SETTINGS
            ).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        )
    }

    fun openBluetoothSettings() {

        context.startActivity(
            Intent(
                Settings.ACTION_BLUETOOTH_SETTINGS
            ).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        )
    }

    fun openBatterySettings() {

        context.startActivity(
            Intent(
                Settings.ACTION_BATTERY_SAVER_SETTINGS
            ).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        )
    }
}