package com.example.pomodorotimer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.pomodorotimer.PomodoroViewModel.SessionType
import com.example.pomodorotimer.PomodoroViewModel.TimerState
import com.example.pomodorotimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationHelper: NotificationHelper
    private val viewModel: PomodoroViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied — timer works either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notificationHelper = NotificationHelper(this)

        askNotificationPermission()
        observeViewModel()
        setupButtons()
    }

    private fun observeViewModel() {

        viewModel.timeLeftMs.observe(this) { ms ->
            binding.tvTimer.text = formatTime(ms)
        }

        viewModel.timerState.observe(this) { state ->
            val iconRes = if (state == TimerState.RUNNING) R.drawable.ic_pause
            else                             R.drawable.ic_play
            binding.btnStartPause.setIconResource(iconRes)
        }

        viewModel.sessionType.observe(this) { session ->
            binding.tvSessionLabel.text = when (session) {
                SessionType.WORK        -> getString(R.string.work_session)
                SessionType.SHORT_BREAK -> getString(R.string.short_break)
                SessionType.LONG_BREAK  -> getString(R.string.long_break)
            }
            when (session) {
                SessionType.WORK        -> binding.chipWork.isChecked       = true
                SessionType.SHORT_BREAK -> binding.chipShortBreak.isChecked = true
                SessionType.LONG_BREAK  -> binding.chipLongBreak.isChecked  = true
            }
        }

        viewModel.progress.observe(this) { percent ->
            binding.progressTimer.setProgressCompat(percent, true)
        }

        viewModel.sessionCount.observe(this) { count ->
            binding.tvSessionCount.text = getString(R.string.session_count, count)
            updateDots(count)
        }

        viewModel.sessionEnded.observe(this) { endedSession ->
            if (endedSession != null) {
                notificationHelper.showSessionEndNotification(endedSession)
                viewModel.onSessionEndedHandled()
            }
        }
    }

    private fun setupButtons() {
        binding.btnStartPause.setOnClickListener {
            when (viewModel.timerState.value) {
                TimerState.RUNNING -> viewModel.pauseTimer()
                else               -> viewModel.startTimer()
            }
        }
        binding.btnReset.setOnClickListener { viewModel.resetTimer() }
        binding.btnSkip.setOnClickListener  { viewModel.skipSession() }
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes      = totalSeconds / 60
        val seconds      = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    private fun updateDots(activeIndex: Int) {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4)
        dots.forEachIndexed { index, dot ->
            dot.setBackgroundResource(
                if (index < activeIndex) R.drawable.dot_active
                else                    R.drawable.dot_inactive
            )
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}