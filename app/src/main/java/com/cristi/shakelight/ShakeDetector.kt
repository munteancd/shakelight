package com.cristi.shakelight

import kotlin.math.sqrt

/**
 * Recognises a deliberate back-and-forth shake from raw accelerometer samples.
 *
 * A "peak" is the moment total acceleration rises above [thresholdG]. A shake is [peaksNeeded]
 * peaks in a row with each one less than [maxGapMs] after the previous. Walking/running produce
 * peaks too, but spaced ~350-600 ms apart, so they never chain. After a shake, peaks are ignored
 * until the phone has been calm for [cooldownMs].
 */
class ShakeDetector(
    private val thresholdG: Float = 2.5f,
    private val rearmG: Float = 1.8f,
    private val peaksNeeded: Int = 4,
    private val maxGapMs: Long = 300,
    private val cooldownMs: Long = 1500,
) {
    private var above = false
    private var peaks = 0
    private var lastPeakMs = -1_000_000L
    private var cooldownUntilMs = Long.MIN_VALUE

    /** Returns true exactly once per recognised shake. Values are in m/s². */
    fun onSample(timeMs: Long, x: Float, y: Float, z: Float): Boolean {
        val gForce = sqrt(x * x + y * y + z * z) / 9.81f

        if (above) {
            if (gForce < rearmG) above = false
            return false
        }
        if (gForce < thresholdG) return false
        above = true

        if (timeMs < cooldownUntilMs) {
            // Still shaking after a toggle: keep waiting, so one long shake = one toggle.
            cooldownUntilMs = timeMs + cooldownMs
            return false
        }
        peaks = if (timeMs - lastPeakMs <= maxGapMs) peaks + 1 else 1
        lastPeakMs = timeMs
        if (peaks < peaksNeeded) return false

        peaks = 0
        cooldownUntilMs = timeMs + cooldownMs
        return true
    }
}
