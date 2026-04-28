package com.example.pomodorotimer

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PomodoroViewModel : ViewModel() {

    enum class SessionType { WORK, SHORT_BREAK, LONG_BREAK }
    enum class TimerState  { IDLE, RUNNING, PAUSED }

    companion object {
        const val WORK_MS        = 25 * 60 * 1000L
        const val SHORT_BREAK_MS =  5 * 60 * 1000L
        const val LONG_BREAK_MS  = 15 * 60 * 1000L
        const val INTERVAL_MS    = 1000L
    }

    private val _timeLeftMs   = MutableLiveData(WORK_MS)
    private val _timerState   = MutableLiveData(TimerState.IDLE)
    private val _sessionType  = MutableLiveData(SessionType.WORK)
    private val _sessionCount = MutableLiveData(1)
    private val _progress     = MutableLiveData(100)
    private val _sessionEnded = MutableLiveData<SessionType?>()

    val timeLeftMs:   LiveData<Long>         = _timeLeftMs
    val timerState:   LiveData<TimerState>   = _timerState
    val sessionType:  LiveData<SessionType>  = _sessionType
    val sessionCount: LiveData<Int>          = _sessionCount
    val progress:     LiveData<Int>          = _progress
    val sessionEnded: LiveData<SessionType?> = _sessionEnded

    private var countDownTimer: CountDownTimer? = null
    private var currentDurationMs = WORK_MS

    fun startTimer() {
        if (_timerState.value == TimerState.RUNNING) return
        val timeLeft = _timeLeftMs.value ?: WORK_MS
        countDownTimer = object : CountDownTimer(timeLeft, INTERVAL_MS) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeftMs.value = millisUntilFinished
                _progress.value   = (millisUntilFinished * 100 / currentDurationMs).toInt()
            }
            override fun onFinish() {
                _timeLeftMs.value   = 0
                _progress.value     = 0
                _timerState.value   = TimerState.IDLE
                _sessionEnded.value = _sessionType.value
                advanceToNextSession()
            }
        }.start()
        _timerState.value = TimerState.RUNNING
    }

    fun pauseTimer() {
        if (_timerState.value != TimerState.RUNNING) return
        countDownTimer?.cancel()
        _timerState.value = TimerState.PAUSED
    }

    fun resetTimer() {
        countDownTimer?.cancel()
        currentDurationMs = durationFor(_sessionType.value ?: SessionType.WORK)
        _timeLeftMs.value = currentDurationMs
        _progress.value   = 100
        _timerState.value = TimerState.IDLE
    }

    fun skipSession() {
        countDownTimer?.cancel()
        _timerState.value = TimerState.IDLE
        advanceToNextSession()
    }

    fun onSessionEndedHandled() {
        _sessionEnded.value = null
    }

    private fun advanceToNextSession() {
        val current = _sessionType.value ?: SessionType.WORK
        val count   = _sessionCount.value ?: 1

        val nextSession = when (current) {
            SessionType.WORK -> {
                if (count >= 4) SessionType.LONG_BREAK
                else            SessionType.SHORT_BREAK
            }
            SessionType.SHORT_BREAK -> SessionType.WORK
            SessionType.LONG_BREAK  -> {
                _sessionCount.value = 1
                SessionType.WORK
            }
        }

        if (current == SessionType.SHORT_BREAK) {
            _sessionCount.value = count + 1
        }

        _sessionType.value    = nextSession
        currentDurationMs     = durationFor(nextSession)
        _timeLeftMs.value     = currentDurationMs
        _progress.value       = 100
    }

    private fun durationFor(session: SessionType) = when (session) {
        SessionType.WORK        -> WORK_MS
        SessionType.SHORT_BREAK -> SHORT_BREAK_MS
        SessionType.LONG_BREAK  -> LONG_BREAK_MS
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
}