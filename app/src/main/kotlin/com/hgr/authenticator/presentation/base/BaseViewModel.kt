package com.hgr.authenticator.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI 架构 ViewModel 基类。
 *
 * 提供统一的状态管理和副作用发送能力，子类只需：
 * 1. 通过 [setState] 更新不可变状态
 * 2. 通过 [setEffect] 发送一次性副作用
 * 3. 实现 [onEvent] 处理所有 UI 事件
 *
 * @param State 不可变 UI 状态
 * @param Event 用户操作或系统事件
 * @param Effect 一次性副作用
 */
abstract class BaseViewModel<
    State : UiState,
    Event : UiEvent,
    Effect : UiEffect
>(initialState: State) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    /**
     * 当前状态快捷访问。
     */
    protected val currentState: State get() = _state.value

    /**
     * 设置新状态。
     *
     * 所有状态变更都应通过此方法完成，确保状态不可变且可追踪。
     */
    protected fun setState(reduce: State.() -> State) {
        _state.update { it.reduce() }
    }

    /**
     * 发送一次性副作用。
     *
     * 用于导航、Toast、SnackBar、弹窗等只需执行一次的操作。
     */
    protected fun setEffect(effect: Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    /**
     * 处理事件入口。
     *
     * 子类必须实现此方法，统一处理所有来自 UI 层的事件。
     */
    abstract fun onEvent(event: Event)
}
