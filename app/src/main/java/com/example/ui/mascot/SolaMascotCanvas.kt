package com.example.ui.mascot

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3D Transparent Floating Sola Mascot Companion.
 * Renders the official 3D emotion assets with procedural aura glows, dynamic ground shadow,
 * smooth breathing/floating physics, and holographic orbit rings.
 */
@Composable
fun SolaMascotCanvas(
    emotionState: SolaEmotionState,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    showAura: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sola_anim")

    // Smooth breathing floating offset (vertical oscillation)
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    // Subtle breathing scale pulse
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )

    // Orbital ring continuous rotation
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    // Reverse ring rotation
    val reverseRingRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reverse_ring_rotation"
    )

    // Pulsing aura glow alpha
    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_pulse"
    )

    // Smooth color animation when emotion changes
    val animatedPrimary by animateColorAsState(
        targetValue = emotionState.primaryGlow,
        animationSpec = tween(400),
        label = "color_prim"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = emotionState.secondaryGlow,
        animationSpec = tween(400),
        label = "color_sec"
    )
    val animatedAccent by animateColorAsState(
        targetValue = emotionState.accentColor,
        animationSpec = tween(400),
        label = "color_acc"
    )

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick() }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(size)
            .then(clickModifier)
            .testTag("sola_mascot_container"),
        contentAlignment = Alignment.Center
    ) {
        // 1. Procedural Glow, Holographic Orbital Rings & Responsive Ground Shadow
        if (showAura) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = this.size.width
                val canvasHeight = this.size.height
                val centerX = canvasWidth / 2f
                val centerY = canvasHeight / 2f + floatOffset
                val center = Offset(centerX, centerY)
                val baseRadius = canvasWidth * 0.34f

                // Dynamic ground shadow at the bottom
                // Contracts and darkens when low; expands and fades when high
                val shadowProgress = (floatOffset + 5f) / 10f // 0f when highest (-5), 1f when lowest (+5)
                val shadowWidth = canvasWidth * (0.42f - shadowProgress * 0.08f)
                val shadowHeight = canvasHeight * 0.08f
                val shadowY = canvasHeight * 0.88f
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.22f + shadowProgress * 0.12f),
                            Color.Black.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, shadowY),
                        radius = shadowWidth
                    ),
                    topLeft = Offset(centerX - shadowWidth, shadowY - shadowHeight / 2f),
                    size = Size(shadowWidth * 2f, shadowHeight)
                )

                // Multi-layer Ambient Bloom Aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedSecondary.copy(alpha = 0.45f * auraPulse),
                            animatedPrimary.copy(alpha = 0.22f * auraPulse),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius * 1.6f
                    ),
                    radius = baseRadius * 1.6f,
                    center = center
                )

                // Holographic Orbiting Ring 1 (Tilted Ellipse)
                rotate(degrees = ringRotation, pivot = center) {
                    drawOval(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                animatedSecondary.copy(alpha = 0.75f),
                                Color.Transparent,
                                animatedAccent.copy(alpha = 0.85f),
                                Color.Transparent,
                                animatedSecondary.copy(alpha = 0.75f)
                            ),
                            center = center
                        ),
                        topLeft = Offset(center.x - baseRadius * 1.45f, center.y - baseRadius * 0.42f),
                        size = Size(baseRadius * 2.9f, baseRadius * 0.84f),
                        style = Stroke(width = 2.2f, cap = StrokeCap.Round)
                    )

                    // Orbiting luminous node
                    val angle1 = ringRotation * (PI / 180f).toFloat()
                    val nodeX1 = center.x + (baseRadius * 1.45f) * cos(angle1)
                    val nodeY1 = center.y + (baseRadius * 0.42f) * sin(angle1)
                    drawCircle(color = animatedAccent, radius = 3f, center = Offset(nodeX1, nodeY1))
                }

                // Holographic Orbiting Ring 2 (Counter-Tilted Inner Ellipse)
                rotate(degrees = reverseRingRotation, pivot = center) {
                    drawOval(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                animatedPrimary.copy(alpha = 0.65f),
                                Color.Transparent,
                                animatedSecondary.copy(alpha = 0.65f)
                            ),
                            center = center
                        ),
                        topLeft = Offset(center.x - baseRadius * 1.25f, center.y - baseRadius * 0.32f),
                        size = Size(baseRadius * 2.5f, baseRadius * 0.64f),
                        style = Stroke(width = 1.6f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // 2. Official High-Resolution 3D Mascot Image with Transparent Cross-Fade
        Crossfade(
            targetState = emotionState,
            animationSpec = tween(350),
            label = "sola_3d_crossfade"
        ) { currentEmotion ->
            Image(
                painter = painterResource(id = currentEmotion.drawableRes),
                contentDescription = "Sola Mascot - ${currentEmotion.emotionLabelAr}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .offset(y = floatOffset.dp)
                    .scale(breathingScale)
                    .testTag("sola_mascot_3d_image")
            )
        }
    }
}
