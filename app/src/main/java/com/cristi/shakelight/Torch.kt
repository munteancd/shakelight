package com.cristi.shakelight

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import android.util.Log

/** Back-camera flashlight. Tracks the real state, so it stays in sync with the Quick Settings tile. */
class Torch(context: Context) {
    private val cameras = context.getSystemService(CameraManager::class.java)
    private val cameraId: String? = cameras.cameraIdList.firstOrNull { id ->
        val c = cameras.getCameraCharacteristics(id)
        c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
            c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
    }
    var isOn = false
        private set

    private val callback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(id: String, enabled: Boolean) {
            if (id == cameraId) isOn = enabled
        }
    }

    init {
        cameras.registerTorchCallback(callback, Handler(Looper.getMainLooper()))
    }

    fun toggle() {
        val id = cameraId ?: return
        try {
            cameras.setTorchMode(id, !isOn)
        } catch (e: Exception) {
            // Camera busy (another app is using it) — nothing we can do.
            Log.w("ShakeLight", "setTorchMode failed", e)
        }
    }

    fun release() {
        cameras.unregisterTorchCallback(callback)
    }
}
