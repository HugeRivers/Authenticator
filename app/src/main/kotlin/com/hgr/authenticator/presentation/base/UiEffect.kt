package com.hgr.authenticator.presentation.base

/**
 * UI 副作用基类。
 *
 * 代表一次性副作用，如导航、Toast、SnackBar、弹窗等。
 * 通过 Channel 发送，消费后即消失，不会因重组重复触发。
 */
interface UiEffect
