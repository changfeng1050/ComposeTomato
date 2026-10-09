package com.changfeng.tomato.timer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.changfeng.tomato.data.Phase
import com.changfeng.tomato.data.Settings
import com.changfeng.tomato.data.SettingsRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** 阶段内计时状态。完成（Finished）由引擎内部瞬时处理：阶段跑满即自动进入下一阶段。 */
enum class TimerState { NotStarted, Running, Paused }

/** 开始专注前可选关联的便笺事项快照。 */
data class NoteLink(val noteId: Long? = null, val text: String)

/**
 * 番茄计时引擎：持有当前阶段、计时状态与循环计数，独立于 UI 生命周期
 * （在 application 作用域创建，窗口隐藏到托盘时计时继续）。
 *
 * 循环规则（见 CONTEXT.md）：只有跑满的专注推进计数；攒满长休间隔进入长休息；
 * 长休息结束后计数归零。放弃的专注留档但不推进计数。
 */
class TomatoEngine(
    private val settingsProvider: () -> Settings = { SettingsRepo.settings },
    private val clock: () -> Long = System::currentTimeMillis,
    private val listener: Listener? = null,
) {

    interface Listener {
        /** 专注阶段自然跑满。 */
        fun onFocusFinished(startedAtMs: Long, endedAtMs: Long, note: NoteLink?)

        /** 专注阶段被用户主动放弃（含应用退出时仍在专注的情况）。 */
        fun onFocusDiscarded(startedAtMs: Long, endedAtMs: Long, note: NoteLink?)

        /** 任一阶段自然跑满（用于提示音/系统通知）。 */
        fun onPhaseCompleted(phase: Phase)
    }

    var phase by mutableStateOf(Phase.Focus)
        private set
    var phaseState by mutableStateOf(TimerState.NotStarted)
        private set
    var remainingSeconds by mutableStateOf(0)
        private set
    var totalSeconds by mutableStateOf(0)
        private set
    var completedInCycle by mutableStateOf(0)
        private set
    var noteLink by mutableStateOf<NoteLink?>(null)
        private set

    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var ticker: Job? = null
    private var phaseStartedAtMs = 0L
    private var phaseEndsAtMs = 0L

    init {
        refreshRemainingIfIdle()
    }

    /** 设置/切换本次专注要关联的便笺。 */
    fun linkNote(link: NoteLink?) {
        noteLink = link
    }

    fun start() {
        if (phaseState != TimerState.NotStarted) return
        phaseStartedAtMs = clock()
        phaseEndsAtMs = phaseStartedAtMs + durationSeconds(phase) * 1000L
        phaseState = TimerState.Running
        remainingSeconds = durationSeconds(phase)
        startTicker()
    }

    fun pause() {
        if (phase != Phase.Focus || phaseState != TimerState.Running) return
        ticker?.cancel()
        remainingSeconds = remainingMs().toInt().div(1000).coerceAtLeast(0)
        phaseState = TimerState.Paused
    }

    fun resume() {
        if (phase != Phase.Focus || phaseState != TimerState.Paused) return
        phaseEndsAtMs = clock() + remainingSeconds * 1000L
        phaseState = TimerState.Running
        startTicker()
    }

    /** 放弃当前专注（休息阶段不可放弃，只能跳过）。 */
    fun discard() {
        if (phase != Phase.Focus) return
        if (phaseState == TimerState.Running || phaseState == TimerState.Paused) {
            listener?.onFocusDiscarded(phaseStartedAtMs, clock(), noteLink)
        }
        ticker?.cancel()
        phaseState = TimerState.NotStarted
        refreshRemainingIfIdle()
    }

    /** 跳过休息，回到待开始的专注。 */
    fun skipBreak() {
        if (phase == Phase.Focus) return
        if (phaseState == TimerState.Running) ticker?.cancel()
        val wasLongBreak = phase == Phase.LongBreak
        phase = Phase.Focus
        phaseState = TimerState.NotStarted
        if (wasLongBreak) completedInCycle = 0
        refreshRemainingIfIdle()
        if (settingsProvider().autoStartFocus) start()
    }

    /** 时长设置变化后，若处于待开始状态则刷新剩余时间显示。 */
    fun refreshRemainingIfIdle() {
        if (phaseState != TimerState.NotStarted) return
        totalSeconds = durationSeconds(phase)
        remainingSeconds = totalSeconds
    }

    fun durationSeconds(p: Phase): Int = with(settingsProvider()) {
        val minutes = when (p) {
            Phase.Focus -> focusMinutes
            Phase.ShortBreak -> shortBreakMinutes
            Phase.LongBreak -> longBreakMinutes
        }
        minutes.coerceAtLeast(1) * 60
    }

    private fun remainingMs(): Long = (phaseEndsAtMs - clock()).coerceAtLeast(0)

    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                val remaining = remainingMs()
                remainingSeconds = ((remaining + 999) / 1000).toInt()
                if (remaining <= 0L) {
                    onPhaseTimeUp()
                    break
                }
                delay(250)
            }
        }
    }

    private fun onPhaseTimeUp() {
        val endedAt = clock()
        if (phase == Phase.Focus) {
            completedInCycle++
            listener?.onFocusFinished(phaseStartedAtMs, endedAt, noteLink)
        }
        listener?.onPhaseCompleted(phase)
        val finishedPhase = phase
        val interval = settingsProvider().longBreakInterval.coerceAtLeast(1)
        phase = if (finishedPhase == Phase.Focus) {
            if (completedInCycle % interval == 0) Phase.LongBreak else Phase.ShortBreak
        } else {
            Phase.Focus
        }
        if (finishedPhase == Phase.LongBreak) completedInCycle = 0
        phaseState = TimerState.NotStarted
        refreshRemainingIfIdle()
        val autoStart = when (phase) {
            Phase.Focus -> settingsProvider().autoStartFocus
            else -> settingsProvider().autoStartBreak
        }
        if (autoStart) start()
    }
}
