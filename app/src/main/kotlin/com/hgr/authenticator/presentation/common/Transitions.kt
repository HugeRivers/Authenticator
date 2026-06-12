package com.hgr.authenticator.presentation.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset

object Transitions {
    val standardTween = tween<Float>(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )

    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val slideInFromRight = slideInHorizontally(
        animationSpec = tween(400),
        initialOffsetX = { it }
    ) + fadeIn(tween(300))

    val slideOutToRight = slideOutHorizontally(
        animationSpec = tween(400),
        targetOffsetX = { it }
    ) + fadeOut(tween(200))

    val slideInFromLeft = slideInHorizontally(
        animationSpec = tween(400),
        initialOffsetX = { -it }
    ) + fadeIn(tween(300))

    val slideOutToLeft = slideOutHorizontally(
        animationSpec = tween(400),
        targetOffsetX = { -it }
    ) + fadeOut(tween(200))

    val slideInFromBottom = slideInVertically(
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        initialOffsetY = { it }
    ) + fadeIn(tween(250))

    val slideOutToBottom = slideOutVertically(
        animationSpec = tween(300),
        targetOffsetY = { it }
    ) + fadeOut(tween(200))

    val scaleIn = fadeIn(tween(300))

    val scaleOut = fadeOut(tween(200))
}
