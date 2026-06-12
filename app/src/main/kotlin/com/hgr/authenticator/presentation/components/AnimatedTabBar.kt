package com.hgr.authenticator.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.hgr.authenticator.presentation.theme.LightAccent
import com.hgr.authenticator.presentation.theme.LightAccentOn

@Preview(showBackground = true, widthDp = 300)
@Composable
private fun AnimatedTabBarPreview() {
    AnimatedTabBar(
        selectedTab = 0,
        onTabSelected = {},
        tabs = listOf("Scan", "Manual")
    )
}

@Composable
fun AnimatedTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<String>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val containerWidthPx = remember { mutableStateOf(0f) }
    val indicatorWidth = remember(tabs.size) { 1f / tabs.size }
    val indicatorOffset by animateDpAsState(
        targetValue = with(density) { (containerWidthPx.value / tabs.size * selectedTab).toDp() },
        animationSpec = tween(300),
        label = "indicator"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp)
            .onSizeChanged { containerWidthPx.value = it.width.toFloat() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(indicatorWidth)
                .height(40.dp)
                .offset(x = indicatorOffset)
                .clip(RoundedCornerShape(8.dp))
                .background(LightAccent)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        color = if (index == selectedTab) LightAccentOn else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
