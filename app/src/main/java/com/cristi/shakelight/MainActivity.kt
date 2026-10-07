package com.cristi.shakelight

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast

/**
 * No screen of its own: walks through the permission prompts, starts the service and closes.
 * Opening the app again just re-checks and restarts it.
 */
class MainActivity : Activity() {
    private var askedNotifications = false
    private var askedBattery = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
    }

    private fun next() {
        if (!askedNotifications && Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            askedNotifications = true
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQ_NOTIF)
            return
        }
        val power = getSystemService(PowerManager::class.java)
        if (!askedBattery && !power.isIgnoringBatteryOptimizations(packageName)) {
            askedBattery = true
            // System dialog "Let app always run in background?" — without it the phone kills the service.
            startActivityForResult(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName")),
                REQ_BATTERY
            )
            return
        }
        ShakeService.start(this)
        Toast.makeText(this, R.string.started, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        next()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        next()
    }

    companion object {
        private const val REQ_NOTIF = 1
        private const val REQ_BATTERY = 2
    }
}
