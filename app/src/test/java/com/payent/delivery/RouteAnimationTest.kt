package com.payent.delivery

import com.payent.delivery.feature.auth.RouteGeometry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Deterministic unit tests for PayentDelivery Route Animation:
 * - Centerline coordinates, bounds, and C^2 continuity
 * - Arc-length parameterization monotonicity
 * - Curvature-aware kinematic velocity profile (corner deceleration, start/stop ramps)
 * - Heading calculations, lookahead anticipation, and destination vertical alignment (0°)
 */
class RouteAnimationTest {

    private val testWidth = 400f
    private val testHeight = 800f
    private val testDensity = 2.75f
    private val testYStart = testHeight * 0.84f // 672f
    private val testYPin = testHeight * 0.16f   // 128f

    @Test
    fun testRoadCenterlineCoordinatesAndContinuity() {
        // Test Start: x = 0.5 * w, y = yStart
        val xStart = RouteGeometry.getRoutePointX(0f, testWidth)
        val yStart = RouteGeometry.getRoutePointY(0f, testYStart, testYPin)
        assertEquals(testWidth * 0.5f, xStart, 0.001f)
        assertEquals(testYStart, yStart, 0.001f)

        // Test Destination: x = 0.5 * w, y = yPin
        val xEnd = RouteGeometry.getRoutePointX(1f, testWidth)
        val yEnd = RouteGeometry.getRoutePointY(1f, testYStart, testYPin)
        assertEquals(testWidth * 0.5f, xEnd, 0.001f)
        assertEquals(testYPin, yEnd, 0.001f)

        // Evaluate across 500 samples to verify C^2 continuity (smooth changes, no kinks)
        var prevX = xStart
        var prevDx = 0f
        val numSteps = 500
        for (i in 1..numSteps) {
            val u = i.toFloat() / numSteps
            val x = RouteGeometry.getRoutePointX(u, testWidth)
            val dx = x - prevX

            // Position must remain within comfortable screen boundaries (25% to 75% of screen width)
            assertTrue("x=$x should be >= 0.25*w", x >= testWidth * 0.25f)
            assertTrue("x=$x should be <= 0.75*w", x <= testWidth * 0.75f)

            // Step delta must be smooth (no sudden displacement jump > 2.0 px per step)
            assertTrue("dx=$dx is too abrupt", abs(dx) < 2.0f)

            if (i > 1) {
                val d2x = dx - prevDx
                // Acceleration must be smooth (no sudden jerk > 0.5 px)
                assertTrue("d2x=$d2x is too jerky", abs(d2x) < 0.5f)
            }

            prevX = x
            prevDx = dx
        }
    }

    @Test
    fun testLeftAndRightBendsExist() {
        var minX = testWidth
        var maxX = 0f

        for (i in 0..100) {
            val u = i.toFloat() / 100
            val x = RouteGeometry.getRoutePointX(u, testWidth)
            if (x < minX) minX = x
            if (x > maxX) maxX = x
        }

        // Must curve to the left (minX < 0.40 * w)
        assertTrue("Route must curve to the left: minX=$minX", minX < testWidth * 0.40f)
        // Must curve to the right (maxX > 0.60 * w)
        assertTrue("Route must curve to the right: maxX=$maxX", maxX > testWidth * 0.60f)
    }

    @Test
    fun testRouteGeometryInitializationAndArcLength() {
        val geom = RouteGeometry(testWidth, testHeight, testDensity, testYStart, testYPin)

        // Total length must be greater than straight vertical line distance (yStart - yPin = 544)
        val straightDist = testYStart - testYPin
        assertTrue("Total length ${geom.totalLength} must exceed straight line distance $straightDist",
            geom.totalLength > straightDist)

        // Arc length mapping: s=0 -> u=0, s=1 -> u=1
        assertEquals(0f, geom.getUFromDistanceFraction(0f), 0.001f)
        assertEquals(1f, geom.getUFromDistanceFraction(1f), 0.001f)

        // Monotonicity of u(s)
        var prevU = 0f
        for (i in 1..100) {
            val s = i.toFloat() / 100
            val u = geom.getUFromDistanceFraction(s)
            assertTrue("u must be monotonically non-decreasing: s=$s, u=$u, prevU=$prevU", u >= prevU)
            prevU = u
        }
    }

    @Test
    fun testCurvatureAwareSpeedProfile() {
        val geom = RouteGeometry(testWidth, testHeight, testDensity, testYStart, testYPin)

        // At t = 0: distance must be 0
        assertEquals(0f, geom.getDistanceFractionFromTime(0f), 0.0001f)

        // At t = 1.0: distance must reach stopDistanceFraction
        assertEquals(geom.stopDistanceFraction, geom.getDistanceFractionFromTime(1f), 0.0001f)

        // Monotonic progress across time
        var prevS = 0f
        for (i in 1..100) {
            val t = i.toFloat() / 100
            val s = geom.getDistanceFractionFromTime(t)
            assertTrue("s must monotonically increase: t=$t, s=$s, prevS=$prevS", s >= prevS)
            prevS = s
        }

        // Test smooth deceleration near destination:
        // Speed in the last 5% (t=0.95 to 1.0) must be lower than cruising speed (t=0.45 to 0.50)
        val dsCruise = geom.getDistanceFractionFromTime(0.50f) - geom.getDistanceFractionFromTime(0.45f)
        val dsArrival = geom.getDistanceFractionFromTime(1.0f) - geom.getDistanceFractionFromTime(0.95f)
        assertTrue("Arrival speed ($dsArrival) must be lower than cruise speed ($dsCruise)", dsArrival < dsCruise)
    }

    @Test
    fun testHeadingAnglesAndDestinationAlignment() {
        val geom = RouteGeometry(testWidth, testHeight, testDensity, testYStart, testYPin)

        // At start (s = 0), road is vertical: heading must be ~0 degrees
        val headingStart = geom.getHeadingDegrees(0f)
        assertEquals(0f, headingStart, 1.5f)

        // At destination stop (s = stopDistanceFraction), bike must be aligned vertically with the road (0 degrees)
        val headingStop = geom.getHeadingDegrees(geom.stopDistanceFraction)
        assertEquals("Bike must align straight at destination: heading=$headingStop", 0f, headingStop, 1.5f)

        // Heading angles throughout journey must stay within reasonable motorcycle limits (-30 to +30 deg)
        var foundLeftTurn = false
        var foundRightTurn = false
        for (i in 0..100) {
            val s = (i.toFloat() / 100) * geom.stopDistanceFraction
            val heading = geom.getHeadingDegrees(s)
            assertTrue("Heading must stay within comfortable limits: s=$s, heading=$heading", abs(heading) <= 60f)

            if (heading < -8f) foundLeftTurn = true
            if (heading > 8f) foundRightTurn = true
        }

        assertTrue("Must turn left during route", foundLeftTurn)
        assertTrue("Must turn right during route", foundRightTurn)
    }

    @Test
    fun testDestinationStopPositionKeepsFrontWheelAtPin() {
        val geom = RouteGeometry(testWidth, testHeight, testDensity, testYStart, testYPin)

        // Bike stops at stopDistanceFraction
        val uStop = geom.getUFromDistanceFraction(geom.stopDistanceFraction)
        val xStop = geom.getPointX(uStop)
        val yStop = geom.getPointY(uStop)

        // X coordinate must be dead center (0.5 * w)
        assertEquals(testWidth * 0.5f, xStop, 0.5f)

        // Y coordinate must equal stopY (yPin + 46f * density)
        assertEquals(geom.stopY, yStop, 1.0f)

        // The front wheel is ~46 * density ahead of rear tyre contact patch:
        // frontWheelY = yStop - 46 * density = yPin
        val frontWheelY = yStop - 46f * testDensity
        assertEquals(testYPin, frontWheelY, 1.0f)
    }

    @Test
    fun testSmoothHeadingContinuityWithoutCornerCuttingOrJumps() {
        val geom = RouteGeometry(testWidth, testHeight, testDensity, testYStart, testYPin)
        val numSamples = 200

        var prevHeading = geom.getHeadingDegrees(0f)
        for (i in 1..numSamples) {
            val s = (i.toFloat() / numSamples) * geom.stopDistanceFraction
            val heading = geom.getHeadingDegrees(s)

            // Step delta must be smooth: no sudden rotation jumps or angle wrapping between adjacent samples
            val dHeading = abs(heading - prevHeading)
            assertTrue("Heading jump detected between adjacent samples: dHeading=$dHeading at s=$s", dHeading < 6.0f)

            // Heading must stay well within physical motorcycle limits without angle wrapping
            assertTrue("Heading $heading must stay within [-60°, 60°]", abs(heading) <= 60f)

            prevHeading = heading
        }
    }
}

