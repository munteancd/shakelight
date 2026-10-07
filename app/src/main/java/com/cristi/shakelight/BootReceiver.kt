package com.cristi.shakelight

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Restarts listening after a reboot or an app update, unless the user stopped it. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ShakeService.isEnabled(context)) ShakeService.start(context)
    }
}
