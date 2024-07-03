package com.example.idea1

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlin.random.Random

class OverlayUIManager(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val mathProblemGenerator: MathProblemGenerator,
    private val countdownTimerHelper: CountdownTimerHelper
) {

    private val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val overlayView: View
    private var correctAnswer: Int = 0
    private val handler = Handler()

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
        }
    }

    fun showAppropriateScreen(packageName: String) {
        val instructionTextView = overlayView.findViewById<TextView>(R.id.instructionTextView)
        val mathProblemTextView = overlayView.findViewById<TextView>(R.id.mathProblemTextView)
        val answerEditText = overlayView.findViewById<EditText>(R.id.answerEditText)
        val submitButton = overlayView.findViewById<Button>(R.id.submitButton)
        val countdownTextView = overlayView.findViewById<TextView>(R.id.countdownTextView)
        val incorrectAnswerTextView = overlayView.findViewById<TextView>(R.id.incorrectAnswerTextView)
        val openCountTextView = overlayView.findViewById<TextView>(R.id.openCountTextView)

        val remainingTime = preferencesManager.getRemainingBlockTime(packageName)
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
        preferencesManager.updateOpenCountTextView(openCountTextView, packageName)

        submitButton.setOnClickListener {
            val answer = answerEditText.text.toString()
            submitAnswer(answer, packageName, instructionTextView, mathProblemTextView, answerEditText, submitButton, countdownTextView, incorrectAnswerTextView)
        }
    }

    private fun showCountdownTimer(remainingTime: Long, countdownTextView: TextView) {
        val minutes = remainingTime / 1000 / 60
        val seconds = (remainingTime / 1000) % 60
        countdownTextView.text = "Try again in $minutes minute $seconds second"
        handler.postDelayed(updateTimerRunnable(remainingTime, countdownTextView), 1000)
    }

    private fun updateTimerRunnable(remainingTime: Long, countdownTextView: TextView) = object : Runnable {
        override fun run() {
            if (remainingTime > 0) {
                showCountdownTimer(remainingTime - 1000, countdownTextView)
            } else {
                handler.removeCallbacks(this)
                countdownTextView.visibility = View.GONE
                showAppropriateScreen("")
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
        packageName: String,
        instructionTextView: TextView,
        mathProblemTextView: TextView,
        answerEditText: EditText,
        submitButton: Button,
        countdownTextView: TextView,
        incorrectAnswerTextView: TextView
    ) {
        if (checkAnswer(answer)) {
            preferencesManager.saveLastSolvedTime(packageName)
            preferencesManager.allowAppAccess(packageName)
            closeApp()
        } else {
            preferencesManager.saveLastWrongAnswerTime(packageName)
            preferencesManager.setBlockStatus(packageName, true)
            instructionTextView.visibility = View.GONE
            mathProblemTextView.visibility = View.GONE
            answerEditText.visibility = View.GONE
            submitButton.visibility = View.GONE
            incorrectAnswerTextView.visibility = View.VISIBLE
            countdownTextView.visibility = View.VISIBLE
            val remainingTime = preferencesManager.getRemainingBlockTime(packageName)
            showCountdownTimer(remainingTime, countdownTextView)
            closeApp()
        }
    }

    private fun checkAnswer(answer: String): Boolean {
        return answer == correctAnswer.toString()
    }

    fun closeApp() {
        windowManager.removeView(overlayView)
    }

    fun showOverlayScreen() {
        // Logic to show the appropriate overlay screen after the countdown finishes
    }
}
