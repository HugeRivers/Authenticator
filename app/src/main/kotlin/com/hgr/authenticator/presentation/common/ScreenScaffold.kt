package com.hgr.authenticator.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * 统一封装的 Scaffold，默认处理状态栏和导航栏的 insets。
 *
 * @param fullScreen 是否全屏显示（内容延伸到状态栏下方，如图片浏览）。默认 false，内容在状态栏下方。
 * @param topBar 顶部栏，默认会自动添加 statusBarsPadding（除非 fullScreen=true）
 * @param bottomBar 底部栏
 * @param snackbarHost Snackbar 宿主
 * @param floatingActionButton FAB
 * @param floatingActionButtonPosition FAB 位置
 * @param containerColor 容器颜色
 * @param contentColor 内容颜色
 * @param contentWindowInsets 内容区域的 window insets，默认只避开导航栏
 * @param content 内容
 */
@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    fullScreen: Boolean = false,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = WindowInsets.navigationBars,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            if (fullScreen) {
                topBar()
            } else {
                Column(modifier = Modifier.statusBarsPadding()) {
                    topBar()
                }
            }
        },
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets
    ) { paddingValues ->
        content(paddingValues)
    }
}
