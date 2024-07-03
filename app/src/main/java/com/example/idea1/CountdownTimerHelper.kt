package com.example.idea1

import android.content.Context
import android.os.Handler
import android.view.View
import android.widget.TextView

class CountdownTimerHelper(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {

    private val handler = Handler()

    fun showCountdownTimer(remainingTime: Long, countdownTextView: TextView) {
        val minutes = remainingTime / 1000 / 60
        val seconds = (remainingTime / 1000) % 60
        countdownTextView.text = "Try again in $minutes minute $seconds second"
        handler.postDelayed(updateTimerRunnable(countdownTextView), 1000)
    }

    private fun updateTimerRunnable(countdownTextView: TextView) = object : Runnable {
        override fun run() {
            val remainingTime = preferencesManager.getRemainingBlockTime("")
            if (remainingTime > 0) {
                showCountdownTimer(remainingTime, countdownTextView)
            } else {
                handler.removeCallbacks(this)
                countdownTextView.visibility = View.GONE
                (context as OverlayUIManager).showAppropriateScreen("")  // Ensure correct type casting
            }
        }
    }
}
