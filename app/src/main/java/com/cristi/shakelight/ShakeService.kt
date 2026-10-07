package com.cristi.shakelight

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator

/** Foreground service that keeps listening to the accelerometer, even with the screen off. */
class ShakeService : Service(), SensorEventListener {
    private lateinit var sensors: SensorManager
    private lateinit var torch: Torch
    private val detector = ShakeDetector()
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        torch = Torch(this)
        sensors = getSystemService(SensorManager::class.java)
        // A wake-up accelerometer wakes the CPU by itself; otherwise hold a partial wake lock
        // so samples keep arriving when the screen is off.
        val accel = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER, true)
            ?: sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).also {
                wakeLock = getSystemService(PowerManager::class.java)
                    .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ShakeLight:sensor")
                    .apply { acquire() }
            }
        sensors.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            setEnabled(this, false)
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.action == ACTION_TOGGLE) torch.toggle()
        setEnabled(this, true)
        val type = if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        if (Build.VERSION.SDK_INT >= 29) startForeground(NOTIF_ID, notification(), type)
        else startForeground(NOTIF_ID, notification())
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent) {
        val v = event.values
        if (detector.onSample(SystemClock.elapsedRealtime(), v[0], v[1], v[2])) {
            torch.toggle()
            buzz()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun buzz() {
        getSystemService(Vibrator::class.java)
            ?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun notification(): Notification {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, getString(R.string.channel_name), NotificationManager.IMPORTANCE_MIN)
        )
        fun action(act: String, req: Int) = PendingIntent.getService(
            this, req, Intent(this, ShakeService::class.java).setAction(act),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(getString(R.string.notif_text))
            .setOngoing(true)
            .setContentIntent(action(ACTION_TOGGLE, 1))
            .addAction(Notification.Action.Builder(null, getString(R.string.notif_stop), action(ACTION_STOP, 2)).build())
            .build()
    }

    override fun onDestroy() {
        sensors.unregisterListener(this)
        wakeLock?.release()
        torch.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL = "shake"
        private const val NOTIF_ID = 1
        private const val ACTION_STOP = "stop"
        private const val ACTION_TOGGLE = "toggle"
        private const val PREFS = "shakelight"

        fun start(context: Context) {
            context.startForegroundService(Intent(context, ShakeService::class.java))
        }

        fun isEnabled(context: Context) =
            context.getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean("enabled", false)

        private fun setEnabled(context: Context, enabled: Boolean) {
            context.getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean("enabled", enabled).apply()
        }
    }
}
