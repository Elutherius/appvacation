package com.example.idea1

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log

class OverlayService : Service() {

    private lateinit var uiManager: OverlayUIManager
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var mathProblemGenerator: MathProblemGenerator
    private lateinit var countdownTimerHelper: CountdownTimerHelper

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(this)
        mathProblemGenerator = MathProblemGenerator()
        countdownTimerHelper = CountdownTimerHelper(this, preferencesManager)
        uiManager = OverlayUIManager(this, preferencesManager, mathProblemGenerator, countdownTimerHelper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val packageName = intent?.getStringExtra("packageName") ?: return START_STICKY
        Log.d("OverlayService", "Package name received: $packageName")
        uiManager.showAppropriateScreen(packageName)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        uiManager.closeApp()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
