package com.hgr.authenticator.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.hgr.authenticator.presentation.theme.LightAccent

@Preview(showBackground = true, widthDp = 100, heightDp = 60)
@Composable
private fun ToggleSwitchOnPreview() {
    ToggleSwitch(checked = true, onCheckedChange = {})
}

@Preview(showBackground = true, widthDp = 100, heightDp = 60)
@Composable
private fun ToggleSwitchOffPreview() {
    ToggleSwitch(checked = false, onCheckedChange = {})
}

@Composable
fun ToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (checked) LightAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "trackColor"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 2.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "thumbOffset"
    )

    Box(
        modifier = modifier
            .width(50.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .width(26.dp)
                .height(26.dp)
                .offset(x = thumbOffset, y = 2.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
