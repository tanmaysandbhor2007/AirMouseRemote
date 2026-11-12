package com.airmouse.remote.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.airmouse.remote.model.AirPointerSettings
import com.airmouse.remote.transport.InputDispatcher
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

class GyroMotionEngine(
    context: Context,
    private val onDeltaCalculated: (dx: Int, dy: Int) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val gyroSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    var settings: AirPointerSettings = AirPointerSettings()

    // Control States
    var isAirPointerEnabled: Boolean = false
    var isClutchEngaged: Boolean = false // e.g. Thumb down on touchpad/trigger

    // Smoothing filter states
    private var smoothedDx: Float = 0f
    private var smoothedDy: Float = 0f
    private val smoothingFactor: Float = 0.75f // 0.75 new, 0.25 old

    fun start() {
        gyroSensor?.let { sensor ->
            sensorManager?.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        resetSmoothing()
    }

    fun resetSmoothing() {
        smoothedDx = 0f
        smoothedDy = 0f
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_GYROSCOPE) return
        if (!isAirPointerEnabled) return

        // If "hold-to-aim" clutch is enabled, only process motion while clutch is held
        if (settings.holdToAim && !isClutchEngaged) {
            resetSmoothing()
            return
        }

        // Raw angular velocity in rad/s:
        // event.values[0] = Pitch (X axis - tilting up/down)
        // event.values[1] = Roll (Y axis - tilting left/right)
        // event.values[2] = Yaw (Z axis - swiveling left/right)
        val rawPitch = event.values[0]
        val rawYaw = event.values[2]

        // 1. Deadzone filtering (removes natural micro-tremor)
        val deadzone = settings.deadzone
        val filteredYaw = applyDeadzone(rawYaw, deadzone)
        val filteredPitch = applyDeadzone(rawPitch, deadzone)

        if (filteredYaw == 0f && filteredPitch == 0f) {
            resetSmoothing()
            return
        }

        // 2. Axis direction mapping (swiveling left -> cursor left, tilting up -> cursor up)
        var dxRate = -filteredYaw
        var dyRate = -filteredPitch

        if (settings.invertX) dxRate = -dxRate
        if (settings.invertY) dyRate = -dyRate

        // 3. Dynamic ballistic acceleration curve:
        // Slow wrist motion = precision pointing. Fast flick = swift traversal across screen.
        val baseSensitivity = settings.sensitivity * 50f
        val accelMultiplierX = 1f + (settings.accelFactor * abs(dxRate))
        val accelMultiplierY = 1f + (settings.accelFactor * abs(dyRate))

        val targetDx = dxRate * baseSensitivity * accelMultiplierX
        val targetDy = dyRate * baseSensitivity * accelMultiplierY

        // 4. Exponential Smoothing Filter (EMA)
        smoothedDx = (smoothingFactor * targetDx) + ((1f - smoothingFactor) * smoothedDx)
        smoothedDy = (smoothingFactor * targetDy) + ((1f - smoothingFactor) * smoothedDy)

        val intDx = smoothedDx.roundToInt()
        val intDy = smoothedDy.roundToInt()

        if (intDx != 0 || intDy != 0) {
            onDeltaCalculated(intDx, intDy)
        }
    }

    private fun applyDeadzone(value: Float, threshold: Float): Float {
        val magnitude = abs(value)
        return if (magnitude > threshold) {
            sign(value) * (magnitude - threshold)
        } else {
            0f
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
