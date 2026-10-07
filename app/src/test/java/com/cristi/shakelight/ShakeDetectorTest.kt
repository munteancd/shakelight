package com.cristi.shakelight

import org.junit.Assert.assertEquals
import org.junit.Test

class ShakeDetectorTest {
    private val g = 9.81f

    /** Feeds a 50 Hz signal; [accelG] gives the total acceleration (in g) at time t. Returns trigger times. */
    private fun run(d: ShakeDetector, fromMs: Long, toMs: Long, accelG: (Long) -> Float): List<Long> {
        val hits = mutableListOf<Long>()
        var t = fromMs
        while (t <= toMs) {
            if (d.onSample(t, 0f, 0f, accelG(t) * g)) hits += t
            t += 20
        }
        return hits
    }

    /** Short spikes of [peakG] every [periodMs], 1g otherwise. */
    private fun spikes(periodMs: Long, peakG: Float) = { t: Long -> if (t % periodMs < 40) peakG else 1f }

    @Test fun vigorousShakeTriggersOnce() {
        val hits = run(ShakeDetector(), 0, 800, spikes(160, 3.5f))
        assertEquals(1, hits.size)
    }

    @Test fun stillPhoneNeverTriggers() {
        assertEquals(0, run(ShakeDetector(), 0, 10_000) { 1f }.size)
    }

    @Test fun walkingNeverTriggers() {
        assertEquals(0, run(ShakeDetector(), 0, 20_000, spikes(550, 1.6f)).size)
    }

    @Test fun runningNeverTriggers() {
        // ~170 steps/min, hard heel strikes
        assertEquals(0, run(ShakeDetector(), 0, 20_000, spikes(360, 3.0f)).size)
    }

    @Test fun singleBumpNeverTriggers() {
        assertEquals(0, run(ShakeDetector(), 0, 3_000) { t -> if (t in 1000..1040) 6f else 1f }.size)
    }

    @Test fun twoSeparateShakesAfterCooldownTriggerTwice() {
        val d = ShakeDetector()
        val first = run(d, 0, 800, spikes(160, 3.5f))
        run(d, 820, 2_980) { 1f }
        val second = run(d, 3_000, 3_800, spikes(160, 3.5f))
        assertEquals(1, first.size)
        assertEquals(1, second.size)
    }

    @Test fun continuedShakingWithinCooldownDoesNotRetrigger() {
        assertEquals(1, run(ShakeDetector(), 0, 1_400, spikes(160, 3.5f)).size)
    }
}
