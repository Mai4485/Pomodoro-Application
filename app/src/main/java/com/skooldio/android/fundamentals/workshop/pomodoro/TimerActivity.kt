package com.skooldio.android.fundamentals.workshop.pomodoro

import android.app.Notification
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.skooldio.android.fundamentals.workshop.pomodoro.config.NotificationConfig
import com.skooldio.android.fundamentals.workshop.pomodoro.data.PomodoroCounter
import com.skooldio.android.fundamentals.workshop.pomodoro.databinding.ActivityTimerBinding

class TimerActivity : AppCompatActivity() {
    companion object {
//        private const val EXTRA_WORK_DURATION = "work_duration"
//        private const val EXTRA_SHORT_BREAK_DURATION = "short_break_duration"
//        private const val EXTRA_LONG_BREAK_DURATION = "long_break_duration"
        private const val EXTRA_CONFIG = "config"
        fun newIntent(
            context: Context,
//            workDuration: Int,
//            shortBreakDuration: Int,
//            longBreakDuration: Int
            config: Config
        ) : Intent{
            return Intent(context, TimerActivity::class.java).apply {
//                putExtra(EXTRA_WORK_DURATION, workDuration)
//                putExtra(EXTRA_SHORT_BREAK_DURATION, shortBreakDuration)
//                putExtra(EXTRA_LONG_BREAK_DURATION, longBreakDuration)
                putExtra(EXTRA_CONFIG, config)
            }
        }
    }

//    private var workDuration: Int = 0
//    private var shortBreakDuration: Int = 0
//    private var longBreakDuration: Int = 0
    private var config: Config? = null

    private val binding: ActivityTimerBinding by lazy {
        ActivityTimerBinding.inflate(layoutInflater)
    }

    private val pomodoroCounter = PomodoroCounter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Additional code to support edge-to-edge in Android 15
        // Leave this code above the setContentView method
        enableEdgeToEdge()
        setContentView(binding.root)
        restoreBundle()
        setupView()
        setupPomodoroCounter()

        binding.buttonPlay.setOnClickListener {
            showNotification(
                "science",
                "Hello from android development course"
            )
        }

        // Additional code to support edge-to-edge in Android 15
        // Leave this code below the setContentView method
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupView() {
        binding.buttonPlay.setOnClickListener {
            binding.buttonPlay.visibility = View.GONE
            binding.buttonPause.visibility = View.VISIBLE
            pomodoroCounter.play()
        }
        binding.buttonPause.setOnClickListener {
            binding.buttonPlay.visibility = View.VISIBLE
            binding.buttonPause.visibility = View.GONE
            pomodoroCounter.pause()
        }
        binding.buttonFastForward.setOnClickListener {
            binding.buttonPlay.visibility = View.VISIBLE
            binding.buttonPause.visibility = View.GONE
            pomodoroCounter.fastForward()
        }
    }
    private fun setupPomodoroCounter() {
        config?.let {
            pomodoroCounter.apply {
                config(
                    workDuration = it.workDuration,
                    shortBreakDuration = it.shortBreakDuration,
                    longBreakDuration = it.longBreakDuration
                )
                setListener(
                    onReady = { minute, second ->
                        updateCounterStatus(State.Ready, minute, second)
                    },
                    onWork = { minute, second ->
                        updateCounterStatus(State.Work, minute, second)
                    },
                    onShortBreak = { minute, second ->
                        updateCounterStatus(State.ShortBreak, minute, second)
                    },
                    onLongBreak = { minute, second ->
                        updateCounterStatus(State.LongBreak, minute, second)
                    }
                )
            }
        }
    }
    private fun restoreBundle() {
//        workDuration = intent.getIntExtra(EXTRA_WORK_DURATION, 0)
//        shortBreakDuration = intent.getIntExtra(EXTRA_SHORT_BREAK_DURATION,0)
//        longBreakDuration = intent.getIntExtra(EXTRA_LONG_BREAK_DURATION, 0)
        config = intent.getParcelableExtra(EXTRA_CONFIG)
//        Log.d("Comsci", "Work Duration = $workDuration")
    }
    @SuppressLint("MissingPermission")
    private fun showNotification(title: String, text: String) {
        Log.d("TimerActivity", "showNotification called with title: $title")
        val notification: Notification = NotificationCompat.Builder(
            this,
            NotificationConfig.CHANNEL_ID
        ).apply {
            setContentTitle(title)
            setContentText(text)
            setSmallIcon(R.drawable.ic_notification)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setDefaults(NotificationCompat.DEFAULT_ALL)
        }.build()

        val manager = NotificationManagerCompat.from(this)
        manager.notify(1, notification)
    }

    sealed class State {
        object Ready: State()
        object Work: State()
        object ShortBreak: State()
        object LongBreak: State()
    }

    private fun updateCounterStatus(state: State, minute: Int, second: Int) {
        binding.textViewState.setText(getStateTextResourceId(state))
        binding.textViewMinute.text = getString(R.string.time_value, minute)
        binding.textViewSecond.text = getString(R.string.time_value, second)

        if (minute == 0 && second == 0){
            onPomodoroCounterFinished(state)
        }
    }

    private fun onPomodoroCounterFinished(state: State) {
        when(state){
            State.Ready -> {}
            State.Work -> showNotification(
                title = getString(R.string.notification_time_up_title),
                text = getString(R.string.notification_time_up_text_work)
            )
            State.ShortBreak -> showNotification(
                title = getString(R.string.notification_time_up_title),
                text = getString(R.string.notification_time_up_text_short_break)
            )
            State.LongBreak -> showNotification(
                title = getString(R.string.notification_time_up_title),
                text = getString(R.string.notification_time_up_text_long_break)
            )
        }
    }

    private fun getStateTextResourceId(state: State): Int {
        return when(state) {
            State.Ready -> R.string.ready
            State.Work -> R.string.state_work
            State.ShortBreak -> R.string.state_short_break
            State.LongBreak -> R.string.state_long_break
        }
    }
}
