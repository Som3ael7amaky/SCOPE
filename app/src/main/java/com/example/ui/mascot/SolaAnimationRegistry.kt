package com.example.ui.mascot

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spring physics specs and animation states for Sola Avatar and Onboarding Engine.
 */
object SolaAnimationRegistry {

    val SoftBouncySpring: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val SnappySpring: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val CelebrationSpring: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioHighBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
}

class SolaMascotAnimationState {
    var scaleAnim = Animatable(1.0f)
    var rotationAnim = Animatable(0f)
    var isCelebrating by mutableStateOf(false)

    suspend fun triggerBounce() {
        scaleAnim.animateTo(1.25f, SolaAnimationRegistry.SoftBouncySpring)
        scaleAnim.animateTo(1.0f, SolaAnimationRegistry.SnappySpring)
    }

    suspend fun triggerCelebration() {
        isCelebrating = true
        rotationAnim.animateTo(360f, tween(600, easing = FastOutSlowInEasing))
        rotationAnim.snapTo(0f)
        scaleAnim.animateTo(1.35f, SolaAnimationRegistry.CelebrationSpring)
        scaleAnim.animateTo(1.0f, SolaAnimationRegistry.SoftBouncySpring)
        isCelebrating = false
    }
}

@Composable
fun rememberSolaMascotAnimationState(): SolaMascotAnimationState {
    return remember { SolaMascotAnimationState() }
}

@Composable
fun rememberSolaBreathingOffset(enabled: Boolean = true): State<Float> {
    val transition = rememberInfiniteTransition(label = "SolaBreathing")
    return transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathingY"
    )
}

class SolaOnboardingHighlightState {
    var targetBounds by mutableStateOf<Rect?>(null)
    var currentStepIndex by mutableStateOf(0)
    var isWizardActive by mutableStateOf(false)

    fun highlightTarget(bounds: Rect) {
        targetBounds = bounds
    }

    fun clearHighlight() {
        targetBounds = null
    }
}

@Composable
fun rememberSolaOnboardingHighlightState(): SolaOnboardingHighlightState {
    return remember { SolaOnboardingHighlightState() }
}
