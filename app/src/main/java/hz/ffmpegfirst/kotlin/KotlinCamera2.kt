package hz.ffmpegfirst.kotlin

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraDevice.StateCallback
import android.hardware.camera2.CameraManager
import android.util.Log
import android.view.Surface
import java.util.*

class KotlinCamera2 {

    var surface: Surface? = null

    @SuppressLint("MissingPermission")
    fun setupCamera(ctx: Context, surface: Surface) {
        this.surface = surface
        val manager = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        for (camera_id in manager.cameraIdList) {
            val characteristics = manager.getCameraCharacteristics(camera_id)
            if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                manager.openCamera(camera_id, stateCallback, null)
            }
        }
    }

    private var stateCallback = object:StateCallback() {

        override fun onOpened(camera: CameraDevice) {
            Log.d("zhx", "onOpened")
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.d("zhx", "onDisconnected")
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.d("zhx", "onError")
        }

    }
}