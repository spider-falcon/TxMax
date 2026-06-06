package assistant

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.weresomeware.tx.txmax.R

class NotificationActions(
    private val context: Context
) {

    private val channelId = "txmax_channel"

    init {
        createChannel()
    }

    private fun createChannel() {

        val channel = NotificationChannel(
            channelId,
            "TX Max",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager =
            context.getSystemService(
                NotificationManager::class.java
            )

        manager.createNotificationChannel(
            channel
        )
    }

    fun showNotification(
        title: String,
        message: String
    ) {

        val notification =
            NotificationCompat.Builder(
                context,
                channelId
            )
                .setSmallIcon(
                    R.mipmap.ic_launcher
                )
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat
            .from(context)
            .notify(
                System.currentTimeMillis()
                    .toInt(),
                notification
            )
    }
}