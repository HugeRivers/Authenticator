package com.hgr.authenticator.presentation.base

/**
 * UI 事件基类。
 *
 * 代表用户操作或系统回调，从 UI 层单向流向 ViewModel。
 * 所有事件都应通过 [BaseViewModel.onEvent] 统一处理。
 */
interface UiEvent
