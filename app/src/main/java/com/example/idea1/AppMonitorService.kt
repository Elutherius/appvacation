package com.example.idea1

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class AppMonitorService : AccessibilityService() {

    private val handler = Handler()
    private val checkInterval = 1000L // Check every second
    private lateinit var sharedPreferences: SharedPreferences
    private var lastOpenTimestamp: Long = 0


    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event != null && event.packageName == "com.instagram.android") {
            val currentTime = System.currentTimeMillis()

            Log.d("AppMonitorService", """
            Event Details:
            EventType: ${event.eventType}
            Class: ${event.className}
            Package: ${event.packageName}
            Time: ${event.eventTime}
            Text: ${event.text}
            ContentDescription: ${event.contentDescription}
            Source: ${event.source}
            WindowId: ${event.windowId}
            EventId: ${event.eventType}
        """.trimIndent())

            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                if (currentTime - lastOpenTimestamp > 2000) { // 2 seconds threshold
                    lastOpenTimestamp = currentTime
                    Log.d("AppMonitorService", "Instagram opened")
                    updateOpenCount()
                    if (!isInstagramAccessAllowed()) {
                        showOverlay()
                    } else {
                        handler.post(checkInstagramAccess)
                    }
                } else {
                    Log.d("AppMonitorService", "Instagram open event ignored due to threshold")
                }
            }
        }
    }




    private val checkInstagramAccess = object : Runnable {
        override fun run() {
            if (!isInstagramAccessAllowed()) {
                closeInstagram()
            } else {
                handler.postDelayed(this, checkInterval)
            }
        }
    }

    private fun isInstagramAccessAllowed(): Boolean {
        val prefs = getSharedPreferences("app_blocks", MODE_PRIVATE)
        val allowedUntil = prefs.getLong("instagram_allowed_until", 0)
        return System.currentTimeMillis() <= allowedUntil
    }

    private fun allowInstagramAccess() {
        val prefs = getSharedPreferences("app_blocks", MODE_PRIVATE)
        prefs.edit().putLong("instagram_allowed_until", System.currentTimeMillis() + 30 * 1000).apply()
    }

    private fun closeInstagram() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun showOverlay() {
        val intent = Intent(this, OverlayService::class.java)
        startService(intent)
    }

    override fun onInterrupt() {
        handler.removeCallbacks(checkInstagramAccess)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.packageNames = arrayOf("com.instagram.android")
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo = info
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkInstagramAccess)
    }

    private fun updateOpenCount() {
        val currentDate = DateUtils.getCurrentDate()
        val lastOpenedDate = sharedPreferences.getString("lastOpenedDate", "")
        var openCount = sharedPreferences.getInt("openCount", 0)

        if (!DateUtils.isSameDay(currentDate, lastOpenedDate ?: "")) {
            openCount = 0
        }

        openCount++
        sharedPreferences.edit()
            .putString("lastOpenedDate", currentDate)
            .putInt("openCount", openCount)
            .apply()

        Log.d("AppMonitorService", "Open count updated: $openCount")
        Toast.makeText(this, "You've opened Instagram $openCount times today", Toast.LENGTH_SHORT).show()
    }
}
