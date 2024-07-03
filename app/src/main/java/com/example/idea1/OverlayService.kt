package com.example.idea1

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlin.random.Random

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var correctAnswer: Int = 0
    private val handler = Handler()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_layout, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER

        windowManager.addView(overlayView, params)

        val exitButton = overlayView.findViewById<Button>(R.id.exitButton)
        exitButton.setOnClickListener {
            closeApp()
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val packageName = intent?.getStringExtra("packageName") ?: return START_STICKY
        val instructionTextView = overlayView.findViewById<TextView>(R.id.instructionTextView)
        val mathProblemTextView = overlayView.findViewById<TextView>(R.id.mathProblemTextView)
        val answerEditText = overlayView.findViewById<EditText>(R.id.answerEditText)
        val submitButton = overlayView.findViewById<Button>(R.id.submitButton)
        val countdownTextView = overlayView.findViewById<TextView>(R.id.countdownTextView)
        val incorrectAnswerTextView = overlayView.findViewById<TextView>(R.id.incorrectAnswerTextView)
        val openCountTextView = overlayView.findViewById<TextView>(R.id.openCountTextView)

        displayAppropriateScreen(instructionTextView, mathProblemTextView, answerEditText, submitButton, countdownTextView, incorrectAnswerTextView, openCountTextView, packageName)

        submitButton.setOnClickListener {
            val answer = answerEditText.text.toString()
            submitAnswer(answer, instructionTextView, mathProblemTextView, answerEditText, submitButton, countdownTextView, incorrectAnswerTextView, packageName)
        }

        return START_STICKY
    }

    private fun displayAppropriateScreen(
        instructionTextView: TextView,
        mathProblemTextView: TextView,
        answerEditText: EditText,
        submitButton: Button,
        countdownTextView: TextView,
        incorrectAnswerTextView: TextView,
        openCountTextView: TextView,
        packageName: String
    ) {
        val remainingTime = getRemainingBlockTime(packageName)
        if (remainingTime > 0) {
            instructionTextView.visibility = View.GONE
            mathProblemTextView.visibility = View.GONE
            answerEditText.visibility = View.GONE
            submitButton.visibility = View.GONE
            incorrectAnswerTextView.visibility = View.VISIBLE
            countdownTextView.visibility = View.VISIBLE
            showCountdownTimer(remainingTime, countdownTextView)
        } else {
            instructionTextView.visibility = View.VISIBLE
            mathProblemTextView.visibility = View.VISIBLE
            answerEditText.visibility = View.VISIBLE
            submitButton.visibility = View.VISIBLE
            incorrectAnswerTextView.visibility = View.GONE
            countdownTextView.visibility = View.GONE
            generateRandomMathProblem(mathProblemTextView)
        }
        updateOpenCountTextView(openCountTextView, packageName)
    }

    private fun showCountdownTimer(remainingTime: Long, countdownTextView: TextView) {
        val minutes = remainingTime / 1000 / 60
        val seconds = (remainingTime / 1000) % 60
        countdownTextView.text = "Try again in $minutes minute $seconds second"
        handler.postDelayed(updateTimerRunnable(countdownTextView), 1000)
    }

    private fun updateTimerRunnable(countdownTextView: TextView) = object : Runnable {
        override fun run() {
            val remainingTime = getRemainingBlockTime("")
            if (remainingTime > 0) {
                showCountdownTimer(remainingTime, countdownTextView)
            } else {
                handler.removeCallbacks(this)
                countdownTextView.visibility = View.GONE
                val instructionTextView = overlayView.findViewById<TextView>(R.id.instructionTextView)
                val mathProblemTextView = overlayView.findViewById<TextView>(R.id.mathProblemTextView)
                val answerEditText = overlayView.findViewById<EditText>(R.id.answerEditText)
                val submitButton = overlayView.findViewById<Button>(R.id.submitButton)
                instructionTextView.visibility = View.VISIBLE
                mathProblemTextView.visibility = View.VISIBLE
                answerEditText.visibility = View.VISIBLE
                submitButton.visibility = View.VISIBLE
                generateRandomMathProblem(mathProblemTextView)
            }
        }
    }

    private fun generateRandomMathProblem(mathProblemTextView: TextView) {
        val num1 = Random.nextInt(1, 10)
        val num2 = Random.nextInt(1, 10)
        correctAnswer = num1 * num2
        mathProblemTextView.text = "$num1 x $num2 = ?"
    }

    private fun submitAnswer(
        answer: String,
        instructionTextView: TextView,
        mathProblemTextView: TextView,
        answerEditText: EditText,
        submitButton: Button,
        countdownTextView: TextView,
        incorrectAnswerTextView: TextView,
        packageName: String
    ) {
        if (checkAnswer(answer)) {
            saveLastSolvedTime(packageName)
            allowAppAccess(packageName)
            stopSelf()
        } else {
            saveLastWrongAnswerTime(packageName)
            setBlockStatus(packageName, true)
            instructionTextView.visibility = View.GONE
            mathProblemTextView.visibility = View.GONE
            answerEditText.visibility = View.GONE
            submitButton.visibility = View.GONE
            incorrectAnswerTextView.visibility = View.VISIBLE
            countdownTextView.visibility = View.VISIBLE
            showCountdownTimer(packageName, countdownTextView)
            closeApp()
            stopSelf()
        }
    }

    private fun saveLastWrongAnswerTime(packageName: String) {
        val prefs = getSharedPreferences("app_blocks", MODE_PRIVATE)
        prefs.edit().putLong("lastWrongAnswerTime_$packageName", System.currentTimeMillis()).apply()
    }

    private fun showCountdownTimer(packageName: String, countdownTextView: TextView) {
        val remainingTime = getRemainingBlockTime(packageName)
        val minutes = remainingTime / 1000 / 60
        val seconds = (remainingTime / 1000) % 60
        countdownTextView.text = "Try again in $minutes minute $seconds second"
        handler.postDelayed(updateTimerRunnable(packageName, countdownTextView), 1000)
    }

    private fun updateTimerRunnable(packageName: String, countdownTextView: TextView) = object : Runnable {
        override fun run() {
            val remainingTime = getRemainingBlockTime(packageName)
            if (remainingTime > 0) {
                showCountdownTimer(packageName, countdownTextView)
            } else {
                handler.removeCallbacks(this)
                countdownTextView.visibility = View.GONE
                setBlockStatus(packageName, false)
                val instructionTextView = overlayView.findViewById<TextView>(R.id.instructionTextView)
                val mathProblemTextView = overlayView.findViewById<TextView>(R.id.mathProblemTextView)
                val answerEditText = overlayView.findViewById<EditText>(R.id.answerEditText)
                val submitButton = overlayView.findViewById<Button>(R.id.submitButton)
                instructionTextView.visibility = View.VISIBLE
                mathProblemTextView.visibility = View.VISIBLE
                answerEditText.visibility = View.VISIBLE
                submitButton.visibility = View.VISIBLE
                generateRandomMathProblem(mathProblemTextView)
            }
        }
    }

    private fun getRemainingBlockTime(packageName: String): Long {
        val prefs = getSharedPreferences("app_blocks", MODE_PRIVATE)
        val lastWrongAnswerTime = prefs.getLong("lastWrongAnswerTime_$packageName", 0)
        val blockDuration = 10 * 60 * 1000 // 10 minutes (example)
        val elapsedTime = System.currentTimeMillis() - lastWrongAnswerTime
        return blockDuration - elapsedTime
    }

    private fun checkAnswer(answer: String): Boolean {
        return answer == correctAnswer.toString()
    }

    private fun closeApp() {
        stopSelf()
    }


    private fun saveLastSolvedTime(packageName: String) {
        val prefs = getSharedPreferences("app_blocks", MODE_PRIVATE)
        prefs.edit().putLong("lastSolvedTime_$packageName", System.currentTimeMillis()).apply()
    }

    private fun allowAppAccess(packageName: String) {
        val prefs = getSharedPreferences("app_blocks", MODE_PRIVATE)
        prefs.edit().putLong("${packageName}_allowed_until", System.currentTimeMillis() + 30 * 1000).apply()
    }

    private fun updateOpenCountTextView(openCountTextView: TextView, packageName: String) {
        val openCount = sharedPreferences.getInt("openCount_$packageName", 0)
        val pm = packageManager
        val appName = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString()
        openCountTextView.text = "You've already opened $appName $openCount times today"
    }

    private fun setBlockStatus(packageName: String, isBlocked: Boolean) {
        val prefs = getSharedPreferences("app_blocks", MODE_PRIVATE)
        prefs.edit().putBoolean("isBlocked_$packageName", isBlocked).apply()
    }

    private fun isAppBlocked(packageName: String): Boolean {
        val prefs = getSharedPreferences("app_blocks", MODE_PRIVATE)
        return prefs.getBoolean("isBlocked_$packageName", false)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::overlayView.isInitialized) windowManager.removeView(overlayView)
        handler.removeCallbacksAndMessages(null)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
