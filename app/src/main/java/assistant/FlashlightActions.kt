package assistant

import android.content.Context
import android.hardware.camera2.CameraManager
import android.widget.Toast

class FlashlightActions(
    private val context: Context
) {

    private val cameraManager =
        context.getSystemService(
            Context.CAMERA_SERVICE
        ) as CameraManager

    private var isEnabled = false

    fun turnOn() {

        try {

            val cameraId =
                cameraManager.cameraIdList[0]

            cameraManager.setTorchMode(
                cameraId,
                true
            )

            isEnabled = true

            Toast.makeText(
                context,
                "Flashlight On",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {

            Toast.makeText(
                context,
                e.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun turnOff() {

        try {

            val cameraId =
                cameraManager.cameraIdList[0]

            cameraManager.setTorchMode(
                cameraId,
                false
            )

            isEnabled = false

            Toast.makeText(
                context,
                "Flashlight Off",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {

            Toast.makeText(
                context,
                e.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun toggle() {

        if (isEnabled) {
            turnOff()
        } else {
            turnOn()
        }
    }
}