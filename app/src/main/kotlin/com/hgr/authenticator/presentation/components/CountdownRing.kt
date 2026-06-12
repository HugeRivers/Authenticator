package com.hgr.authenticator.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.hgr.authenticator.presentation.theme.LightAccent
import com.hgr.authenticator.presentation.theme.LightDanger

@Composable
fun CountdownRing(
    progress: Float,
    warningThreshold: Float = 0.17f,
    modifier: Modifier = Modifier,
    size: Int = 24,
    strokeWidth: Float = 5f
) {
    val isWarning = progress <= warningThreshold
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000),
        label = "progress"
    )

    val color by animateColorAsState(
        targetValue = if (isWarning) LightDanger else LightAccent,
        animationSpec = tween(300),
        label = "color"
    )

    val bgColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Box(modifier = modifier.size(size.dp)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val radius = (size - strokeWidth) / 2
            val center = this.center
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

            drawCircle(
                color = bgColor,
                radius = radius,
                center = center,
                style = stroke
            )

            val sweepAngle = animatedProgress * 360f
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = stroke
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CountdownRingPreview() {
    CountdownRing(progress = 0.5f, size = 48, strokeWidth = 5f)
}

@Preview(showBackground = true)
@Composable
private fun CountdownRingWarningPreview() {
    CountdownRing(progress = 0.1f, size = 48, strokeWidth = 5f)
}
