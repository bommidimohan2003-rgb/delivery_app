package com.payent.delivery.feature.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payent.delivery.R
import com.payent.delivery.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Data model for individual cartoon exhaust puffs emitted by the motorcycle.
 * Coordinates are fixed in world space at the moment of emission so each puff drifts naturally.
 */
private class ActiveExhaustPuff(
    val spawnTimeMs: Long,
    val startXDp: Float,
    val startYDp: Float,
    val baseRadiusDp: Float
)

/**
 * PayentDelivery Cartoon Bike Splash Animation:
 *
 * Automatically adapts styling to both Light and Dark Themes:
 * - Light Mode: Pure White background (#FFFFFF), Solid Black wordmark "payent" (#000000).
 * - Dark Mode: Sleek Dark background (#080808), Solid White wordmark "payent" (#FFFFFF).
 * - Both Themes: Signature Royal Blue "DELIVERY" label and flanking horizontal divider lines (#2563EB).
 *
 * Sequence:
 * Stage 1: Empty theme screen (White in Light / Dark in Dark) for 0.4s.
 * Stage 2: Cartoon delivery motorcycle with rider enters from completely outside the LEFT edge,
 *          travelling horizontally LEFT to RIGHT with natural deceleration and thin grey motion streaks (~0.95s).
 * Stage 3: Realistic cartoon exhaust puffs emerge from the rear silencer, drift backward, expand, become lighter grey, and fade.
 * Stage 4: Bike stops smoothly around the horizontal center with a subtle suspension settling bounce (~0.48s).
 *          Exhaust puffs continue emerging naturally as the engine idles at center.
 * Stage 5: Bike glides in ultra-smooth, floaty slow-motion upward into upper-middle position (~0.90s,
 *          custom soft-onset CubicBezierEasing with zero jerk).
 *          Exhaust puffs continue emerging from the ascending silencer tip throughout the climb.
 * Stage 6: Wordmark "payent" (Black in Light / White in Dark) revealed below the bike with smooth fade & slight upward motion (~0.5s).
 * Stage 7: Blue "DELIVERY" label and two flanking horizontal blue lines reveal outward from center (~0.5s).
 * Stage 8: Final composition holds briefly (~0.8s) then continues to existing next screen flow.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val backgroundColor = if (isDark) Color(0xFF080808) else Color(0xFFFFFFFF)
    val payentTextColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)
    val brandBlueColor = Color(0xFF2563EB)

    // Single-trigger navigation guard
    var isNavigated by remember { mutableStateOf(false) }
    val currentOnFinish = rememberUpdatedState(onSplashFinished)
    val finishSplash: () -> Unit = {
        if (!isNavigated) {
            isNavigated = true
            currentOnFinish.value()
        }
    }

    val bikeBodyPainter = painterResource(id = R.drawable.cartoon_delivery_bike_body)
    val bikeWheelPainter = painterResource(id = R.drawable.cartoon_delivery_bike_wheel)

    // Animation progress controllers
    var currentStage by remember { mutableIntStateOf(1) }
    val bikeProgress = remember { Animatable(0f) } // 0 = offscreen left, 1 = centered
    val suspensionOffset = remember { Animatable(0f) } // subtle vertical bounce dip & settle (dp)
    val bikeOffsetY = remember { Animatable(0f) } // upward translation in stage 5 (dp)
    val payentAlpha = remember { Animatable(0f) }
    val payentOffsetY = remember { Animatable(12f) }
    val deliveryAlpha = remember { Animatable(0f) }
    val deliveryLineWidth = remember { Animatable(0f) }

    // Active cartoon exhaust puffs
    val puffs = remember { mutableStateListOf<ActiveExhaustPuff>() }

    // Display frame ticker to drive 60/120 FPS continuous redraw during animation
    var animClockMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            withInfiniteAnimationFrameMillis { frameTime ->
                animClockMs = frameTime
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = finishSplash
            )
    ) {
        val screenWidthDp = maxWidth
        val screenHeightDp = maxHeight

        // Proportional bike dimensions (aspect ratio 950 : 806)
        val bikeWidthDp = if (screenWidthDp < 380.dp) screenWidthDp * 0.52f else 196.dp
        val bikeHeightDp = bikeWidthDp * (806f / 950f)

        // Target centered position
        val targetBikeXDp = (screenWidthDp - bikeWidthDp) / 2
        val baseBikeYDp = (screenHeightDp - bikeHeightDp) / 2 - 20.dp
        val startBikeXDp = -bikeWidthDp - 60.dp

        // Coordinated animation sequence
        LaunchedEffect(Unit) {
            // Stage 1: Empty theme screen (0.4s)
            currentStage = 1
            delay(400)

            // Stage 2 & 3: Cartoon Bike Enters From Left to Center (~0.95s)
            currentStage = 2

            // Continuous cartoon exhaust puff spawner:
            // Runs continuously while traveling, stopping at center, gliding upward, and idling at top!
            val puffSpawner = launch {
                while (!isNavigated) {
                    if (bikeProgress.value > 0.08f) {
                        val currentX = startBikeXDp + (targetBikeXDp - startBikeXDp) * bikeProgress.value
                        val currentY = baseBikeYDp + suspensionOffset.value.dp + bikeOffsetY.value.dp

                        // Silencer tip location: 8% from left, 75% from top of bike
                        val tipXDp = currentX + bikeWidthDp * 0.08f
                        val tipYDp = currentY + bikeHeightDp * 0.75f

                        puffs.add(
                            ActiveExhaustPuff(
                                spawnTimeMs = System.currentTimeMillis(),
                                startXDp = tipXDp.value,
                                startYDp = tipYDp.value,
                                baseRadiusDp = 6.5f
                            )
                        )
                    }

                    // Dynamic emission rhythm: rapid during horizontal travel, gentle rhythmic pulse when stopped/climbing
                    val interval = if (bikeProgress.value < 0.95f) 120L else 200L
                    delay(interval)
                }
            }

            bikeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 950,
                    easing = CubicBezierEasing(0.22f, 0.0f, 0.18f, 1.0f)
                )
            )

            // Stage 4: Bike Stops at Center with subtle suspension settling bounce (~0.48s)
            // Exhaust smoke continues emerging as engine idles at center
            currentStage = 4
            suspensionOffset.animateTo(2.0f, tween(160, easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)))
            suspensionOffset.animateTo(-0.8f, tween(140, easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)))
            suspensionOffset.animateTo(0f, tween(120, easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)))
            delay(180) // Clean, natural settle pause before upward movement

            // Stage 5: Bike Glides In Ultra-Smooth Float Upward (~0.90s)
            // Ultra-gentle S-curve bezier: 0 initial acceleration spike, velvety float upwards, soft landing
            currentStage = 5
            bikeOffsetY.animateTo(
                targetValue = -46f,
                animationSpec = tween(
                    durationMillis = 900,
                    easing = CubicBezierEasing(0.35f, 0.0f, 0.15f, 1.0f)
                )
            )

            // Stage 6: Reveal the Payent Brand Name (~0.5s)
            currentStage = 6
            launch {
                payentOffsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                )
            }
            payentAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )

            // Stage 7: Reveal the DELIVERY Label & flanking blue lines (~0.5s)
            currentStage = 7
            launch {
                deliveryLineWidth.animateTo(
                    targetValue = 28f, // 28dp thin blue divider lines
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                )
            }
            deliveryAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )

            // Stage 8: Final Splash Screen Hold (~0.8s)
            currentStage = 8
            delay(800)

            puffSpawner.cancel()

            // Seamless navigation to next screen
            finishSplash()
        }

        // Canvas for Bike Artwork, Ground Contact Shadow, Motion Streaks & Exhaust Puffs
        // All animated values are read directly inside DrawScope for guaranteed 120 FPS frame rate
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Read clock to guarantee continuous refresh across frames
            @Suppress("UNUSED_VARIABLE")
            val tick = animClockMs

            val density = this.density
            val bikeWidthPx = bikeWidthDp.toPx()
            val bikeHeightPx = bikeHeightDp.toPx()

            val startBikeXPx = startBikeXDp.toPx()
            val targetBikeXPx = targetBikeXDp.toPx()
            val baseBikeYPx = baseBikeYDp.toPx()

            // Read animatable values directly in DrawScope to eliminate any recomposition lag
            val progress = bikeProgress.value
            val suspensionPx = suspensionOffset.value * density
            val climbOffsetPx = bikeOffsetY.value * density

            val bikeXPx = startBikeXPx + (targetBikeXPx - startBikeXPx) * progress
            val bikeYPx = baseBikeYPx + suspensionPx + climbOffsetPx

            // 1. Ground contact shadow / depth beneath wheels (adapted to Light and Dark mode)
            if (progress > 0.05f) {
                val shadowCenter = Offset(bikeXPx + bikeWidthPx * 0.50f, bikeYPx + bikeHeightPx - 1f * density)
                val shadowWidth = bikeWidthPx * 0.74f
                val shadowHeight = 9f * density

                val shadowColors = if (isDark) {
                    listOf(
                        Color(0x28FFFFFF),
                        Color(0x0CFFFFFF),
                        Color(0x00000000)
                    )
                } else {
                    listOf(
                        Color(0x38000000),
                        Color(0x18000000),
                        Color(0x00000000)
                    )
                }

                drawOval(
                    brush = Brush.radialGradient(
                        colors = shadowColors,
                        center = shadowCenter,
                        radius = shadowWidth * 0.5f
                    ),
                    topLeft = Offset(shadowCenter.x - shadowWidth / 2, shadowCenter.y - shadowHeight / 2),
                    size = Size(shadowWidth, shadowHeight)
                )
            }

            // 2. Motion streaks behind the bike while travelling horizontally at speed
            if (currentStage == 2 && progress in 0.12f..0.90f) {
                val speedFactor = sin(progress * PI.toFloat()).coerceIn(0f, 1f)
                val streakAlpha = (speedFactor * 0.65f).coerceIn(0f, 0.65f)

                if (streakAlpha > 0.01f) {
                    val streakBaseColor = if (isDark) Color(0xFFD4D4D4) else Color(0xFFC0C0C0)

                    // Streak 1 (upper delivery box)
                    val y1 = bikeYPx + bikeHeightPx * 0.38f
                    val endX1 = bikeXPx - 4f * density
                    val startX1 = endX1 - (36f * speedFactor) * density
                    drawLine(
                        color = streakBaseColor.copy(alpha = streakAlpha * 0.85f),
                        start = Offset(startX1, y1),
                        end = Offset(endX1, y1),
                        strokeWidth = 1.8f * density,
                        cap = StrokeCap.Round
                    )

                    // Streak 2 (lower delivery box)
                    val y2 = bikeYPx + bikeHeightPx * 0.49f
                    val endX2 = bikeXPx - 8f * density
                    val startX2 = endX2 - (44f * speedFactor) * density
                    drawLine(
                        color = (if (isDark) Color(0xFFE2E2E2) else Color(0xFFB8B8B8)).copy(alpha = streakAlpha),
                        start = Offset(startX2, y2),
                        end = Offset(endX2, y2),
                        strokeWidth = 2.0f * density,
                        cap = StrokeCap.Round
                    )

                    // Streak 3 (rear wheel level)
                    val y3 = bikeYPx + bikeHeightPx * 0.76f
                    val endX3 = bikeXPx - 6f * density
                    val startX3 = endX3 - (30f * speedFactor) * density
                    drawLine(
                        color = streakBaseColor.copy(alpha = streakAlpha * 0.75f),
                        start = Offset(startX3, y3),
                        end = Offset(endX3, y3),
                        strokeWidth = 1.8f * density,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 3. Cartoon exhaust puffs from rear silencer tip
            // Each puff drifts from its fixed emission coordinates in world space
            val nowMs = System.currentTimeMillis()

            puffs.forEach { puff ->
                val startXPx = puff.startXDp * density
                val startYPx = puff.startYDp * density

                val ageMs = nowMs - puff.spawnTimeMs
                val lifespanMs = 700f
                val puffAgeRatio = (ageMs / lifespanMs).coerceIn(0f, 1f)

                if (puffAgeRatio < 1.0f) {
                    val driftX = startXPx - (22f * puffAgeRatio) * density
                    val driftY = startYPx - (6f * puffAgeRatio) * density
                    val radius = (puff.baseRadiusDp * (1f + 0.80f * puffAgeRatio)) * density

                    // Become lighter grey and fade out as it drifts (adapted for both themes)
                    val baseGrey = if (isDark) 0.78f else 0.74f
                    val greyValue = (baseGrey + 0.16f * puffAgeRatio).coerceIn(0f, 1f)
                    val alpha = ((1.0f - puffAgeRatio) * 0.75f).coerceIn(0f, 0.75f)
                    val puffColor = Color(greyValue, greyValue, greyValue, alpha)

                    // Cartoon cloud cluster (overlapping circles)
                    drawCircle(puffColor, radius, Offset(driftX, driftY))
                    drawCircle(puffColor, radius * 0.65f, Offset(driftX - radius * 0.45f, driftY - radius * 0.22f))
                    drawCircle(puffColor, radius * 0.60f, Offset(driftX + radius * 0.40f, driftY - radius * 0.26f))
                    drawCircle(puffColor, radius * 0.55f, Offset(driftX - radius * 0.35f, driftY + radius * 0.25f))
                }
            }

            // Clean up fully faded puffs
            puffs.removeAll { (nowMs - it.spawnTimeMs) > 800 }

            // 4. Render Cartoon Delivery Bike with Rotating Wheels
            if (progress > 0f) {
                translate(left = bikeXPx, top = bikeYPx) {
                    // Physical rolling calculation:
                    // Wheel diameter is 260px in 950px wide artwork
                    val wheelDiameterPx = bikeWidthPx * (260f / 950f)
                    val wheelRadiusPx = wheelDiameterPx / 2f
                    val wheelCircumferencePx = (PI.toFloat() * wheelDiameterPx).coerceAtLeast(1f)

                    // Forward distance traveled from starting position off-screen left
                    val distanceTraveledPx = (targetBikeXPx - startBikeXPx) * progress

                    // Wheel rotation angle in degrees: spins clockwise as bike drives forward
                    val wheelRotation = (distanceTraveledPx / wheelCircumferencePx) * 360f

                    // Axle center coordinates in local bike frame (aspect ratio 950 : 806)
                    val rearWheelCenterXPx = bikeWidthPx * (203.5f / 950f)
                    val rearWheelCenterYPx = bikeHeightPx * (658.6f / 806f)

                    val frontWheelCenterXPx = bikeWidthPx * (799.5f / 950f)
                    val frontWheelCenterYPx = bikeHeightPx * (654.5f / 806f)

                    // 4a. Rotating Rear Wheel
                    rotate(
                        degrees = wheelRotation,
                        pivot = Offset(rearWheelCenterXPx, rearWheelCenterYPx)
                    ) {
                        translate(
                            left = rearWheelCenterXPx - wheelRadiusPx,
                            top = rearWheelCenterYPx - wheelRadiusPx
                        ) {
                            with(bikeWheelPainter) {
                                draw(size = Size(wheelDiameterPx, wheelDiameterPx))
                            }
                        }
                    }

                    // 4b. Rotating Front Wheel
                    rotate(
                        degrees = wheelRotation,
                        pivot = Offset(frontWheelCenterXPx, frontWheelCenterYPx)
                    ) {
                        translate(
                            left = frontWheelCenterXPx - wheelRadiusPx,
                            top = frontWheelCenterYPx - wheelRadiusPx
                        ) {
                            with(bikeWheelPainter) {
                                draw(size = Size(wheelDiameterPx, wheelDiameterPx))
                            }
                        }
                    }

                    // 4c. Bike Body (with fork, exhaust, and fenders overlapping the wheels)
                    with(bikeBodyPainter) {
                        draw(size = Size(bikeWidthPx, bikeHeightPx))
                    }
                }
            }
        }

        // Brand Typography: Revealed below the bike in Stages 6, 7 & 8
        if (payentAlpha.value > 0.01f) {
            val brandingTopYDp = baseBikeYDp - 46.dp + bikeHeightDp + 18.dp

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = brandingTopYDp + payentOffsetY.value.dp)
                    .graphicsLayer {
                        alpha = payentAlpha.value
                    }
            ) {
                // Wordmark: Black in Light Mode, White in Dark Mode
                Text(
                    text = "payent",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = payentTextColor,
                        letterSpacing = (-0.5).sp
                    )
                )

                Spacer(modifier = Modifier.height(7.dp))

                // DELIVERY label: Signature Blue (#2563EB) in both themes flanked by two horizontal blue divider lines
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Left blue line
                    Box(
                        modifier = Modifier
                            .width(deliveryLineWidth.value.dp)
                            .height(1.8.dp)
                            .background(brandBlueColor)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "DELIVERY",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = brandBlueColor,
                            letterSpacing = 3.5.sp
                        ),
                        modifier = Modifier.graphicsLayer {
                            alpha = deliveryAlpha.value
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right blue line
                    Box(
                        modifier = Modifier
                            .width(deliveryLineWidth.value.dp)
                            .height(1.8.dp)
                            .background(brandBlueColor)
                    )
                }
            }
        }
    }
}
