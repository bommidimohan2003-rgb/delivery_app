package com.payent.delivery.feature.auth

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Route geometry and kinematics preserved for backwards-compatibility and unit tests.
 */
class RouteGeometry(
    val w: Float,
    val h: Float,
    val density: Float,
    val yStart: Float,
    val yPin: Float
) {
    val totalLength: Float
    val stopDistanceFraction: Float
    val stopY: Float
    val uTurnStart: Float
    val uTurnEnd: Float
    private val sTable: FloatArray
    private val points: Array<Offset>
    private val timeTable: FloatArray

    val guidePath: Path by lazy {
        Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1..NUM_SAMPLES) {
                lineTo(points[i].x, points[i].y)
            }
        }
    }

    val pinTipPath: Path by lazy {
        val xCenter = w * 0.5f
        Path().apply {
            moveTo(pinCenter.x - pinRadius * 0.7f, pinCenter.y + pinRadius * 0.3f)
            lineTo(pinCenter.x + pinRadius * 0.7f, pinCenter.y + pinRadius * 0.3f)
            lineTo(xCenter, yPin)
            close()
        }
    }

    val pinCenter: Offset
    val pinRadius: Float

    companion object {
        const val NUM_SAMPLES = 256
        const val CURVE_AMPLITUDE = 0.10f

        fun getRoutePointX(u: Float, w: Float, turnStart: Float = 0.05f, turnEnd: Float = 0.74f): Float {
            val clamped = u.coerceIn(0f, 1f)
            if (clamped <= turnStart || clamped >= turnEnd) {
                return w * 0.5f
            }
            val tau = (clamped - turnStart) / (turnEnd - turnStart)
            val s1 = sin(2.0 * Math.PI * tau)
            val s2 = sin(4.0 * Math.PI * tau)
            val g = (s1 - 0.5 * s2).toFloat()
            return w * (0.5f - CURVE_AMPLITUDE * g)
        }

        fun getRoutePointY(u: Float, yStart: Float, yPin: Float): Float {
            return yStart + (yPin - yStart) * u.coerceIn(0f, 1f)
        }
    }

    init {
        val numSamples = NUM_SAMPLES
        uTurnStart = 0.05f

        val bikeWheelbaseOffset = 46f * density
        stopY = yPin + bikeWheelbaseOffset
        val uStop = ((yStart - stopY) / (yStart - yPin)).coerceIn(0.70f, 0.94f)
        uTurnEnd = (uStop - 0.08f).coerceAtLeast(0.60f)

        val points = Array(numSamples + 1) { i ->
            val u = i.toFloat() / numSamples
            Offset(getRoutePointX(u, w, uTurnStart, uTurnEnd), getRoutePointY(u, yStart, yPin))
        }

        val dists = FloatArray(numSamples + 1)
        var acc = 0f
        dists[0] = 0f
        for (i in 1..numSamples) {
            val dx = points[i].x - points[i - 1].x
            val dy = points[i].y - points[i - 1].y
            acc += sqrt(dx * dx + dy * dy)
            dists[i] = acc
        }
        totalLength = acc
        sTable = FloatArray(numSamples + 1) { i -> dists[i] / acc }

        stopDistanceFraction = getDistanceFractionFromU(uStop)
        this.points = points
        val xCenter = w * 0.5f
        pinCenter = Offset(xCenter, yPin - 18f * density)
        pinRadius = 11f * density

        val headings = FloatArray(numSamples + 1)
        for (i in 0..numSamples) {
            val prev = points[max(0, i - 1)]
            val next = points[min(numSamples, i + 1)]
            val dx = next.x - prev.x
            val dy = next.y - prev.y
            headings[i] = atan2(dx.toDouble(), -dy.toDouble()).toFloat()
        }

        val rawCurvature = FloatArray(numSamples + 1)
        for (i in 1 until numSamples) {
            val dTheta = abs(headings[i + 1] - headings[i - 1])
            val ds = (sTable[i + 1] - sTable[i - 1]).coerceAtLeast(0.0001f)
            rawCurvature[i] = dTheta / ds
        }
        rawCurvature[0] = rawCurvature[1]
        rawCurvature[numSamples] = rawCurvature[numSamples - 1]

        val lookaheadSamples = 16
        val effCurvature = FloatArray(numSamples + 1)
        for (i in 0..numSamples) {
            var maxK = 0f
            for (k in 0..lookaheadSamples) {
                val idx = min(numSamples, i + k)
                val weight = 1f - (k.toFloat() / (lookaheadSamples + 1)) * 0.30f
                val kVal = rawCurvature[idx] * weight
                if (kVal > maxK) maxK = kVal
            }
            effCurvature[i] = maxK
        }

        val velocities = FloatArray(numSamples + 1)
        for (i in 0..numSamples) {
            val s = sTable[i]
            val sJourney = (s / stopDistanceFraction).coerceIn(0f, 1f)
            val cornerSpeed = 1.0f / (1.0f + 0.10f * effCurvature[i])
            val startRamp = if (sJourney < 0.12f) {
                sin((sJourney / 0.12f) * (PI * 0.5)).toFloat()
            } else 1.0f
            val stopRamp = if (sJourney > 0.80f) {
                sin(((1.0f - sJourney) / 0.20f) * (PI * 0.5)).toFloat()
            } else 1.0f
            velocities[i] = max(0.04f, cornerSpeed * startRamp * stopRamp)
        }

        val cumulativeTime = FloatArray(numSamples + 1)
        cumulativeTime[0] = 0f
        var tAcc = 0f
        val stopIndex = (uStop * numSamples).toInt().coerceIn(1, numSamples)
        for (i in 1..stopIndex) {
            val ds = sTable[i] - sTable[i - 1]
            val vAvg = 0.5f * (velocities[i] + velocities[i - 1])
            tAcc += ds / vAvg
            cumulativeTime[i] = tAcc
        }
        val totalTravelTime = tAcc
        timeTable = FloatArray(numSamples + 1) { i ->
            if (i <= stopIndex) cumulativeTime[i] / totalTravelTime else 1.0f
        }
    }

    fun getPointX(u: Float): Float = getRoutePointX(u, w, uTurnStart, uTurnEnd)
    fun getPointY(u: Float): Float = getRoutePointY(u, yStart, yPin)

    fun getUFromDistanceFraction(s: Float): Float {
        val clampedS = s.coerceIn(0f, 1f)
        val numSamples = sTable.size - 1
        var low = 0
        var high = numSamples
        while (low < high) {
            val mid = (low + high) ushr 1
            if (sTable[mid] < clampedS) {
                low = mid + 1
            } else {
                high = mid
            }
        }
        if (low == 0) return 0f
        val s0 = sTable[low - 1]
        val s1 = sTable[low]
        val frac = if (s1 > s0) (clampedS - s0) / (s1 - s0) else 0f
        val u0 = (low - 1).toFloat() / numSamples
        val u1 = low.toFloat() / numSamples
        return (u0 + frac * (u1 - u0)).coerceIn(0f, 1f)
    }

    private fun getDistanceFractionFromU(u: Float): Float {
        val clampedU = u.coerceIn(0f, 1f)
        val numSamples = sTable.size - 1
        val idx = (clampedU * numSamples).toInt().coerceIn(0, numSamples - 1)
        val frac = (clampedU * numSamples) - idx
        return sTable[idx] + frac * (sTable[idx + 1] - sTable[idx])
    }

    fun getDistanceFractionFromTime(t: Float): Float {
        val clampedT = t.coerceIn(0f, 1f)
        if (clampedT <= 0f) return 0f
        if (clampedT >= 1f) return stopDistanceFraction

        val numSamples = timeTable.size - 1
        var low = 0
        var high = numSamples
        while (low < high) {
            val mid = (low + high) ushr 1
            if (timeTable[mid] < clampedT) {
                low = mid + 1
            } else {
                high = mid
            }
        }
        if (low == 0) return 0f
        val t0 = timeTable[low - 1]
        val t1 = timeTable[low]
        val frac = if (t1 > t0) (clampedT - t0) / (t1 - t0) else 0f
        val s0 = sTable[low - 1]
        val s1 = sTable[low]
        return (s0 + frac * (s1 - s0)).coerceIn(0f, stopDistanceFraction)
    }

    fun getHeadingDegrees(s: Float): Float {
        val ds = 0.022f
        val sRear = (s - ds).coerceAtLeast(0f)
        val sFront = (s + ds).coerceAtMost(1f)

        val uFront = getUFromDistanceFraction(sFront)
        val uRear = getUFromDistanceFraction(sRear)

        val xFront = getPointX(uFront)
        val yFront = getPointY(uFront)
        val xRear = getPointX(uRear)
        val yRear = getPointY(uRear)

        val dx = (xFront - xRear).toDouble()
        val dy = (yFront - yRear).toDouble()
        return Math.toDegrees(atan2(dx, -dy)).toFloat()
    }
}
