package com.example.idea1

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class AppMonitorService : AccessibilityService() {

    private val handler = Handler()
    private val checkInterval = 1000L // Check every second
    private lateinit var sharedPreferences: SharedPreferences
    private var lastOpenedTime: Long = 0

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event != null && event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val monitoredApps = sharedPreferences.getStringSet("monitored_apps", setOf())!!
            if (monitoredApps.contains(event.packageName)) {
                Log.d("AppMonitorService", "${event.packageName} opened")
                if (System.currentTimeMillis() - lastOpenedTime > 2000) {
                    updateOpenCount(event.packageName.toString())
                    lastOpenedTime = System.currentTimeMillis()
                }
                if (!isAppAccessAllowed(event.packageName.toString())) {
                    showOverlay(event.packageName.toString())
                } else {
                    handler.post(checkAppAccess(event.packageName.toString()))
                }
            }
        }
    }

    private val checkAppAccess = { packageName: String ->
        object : Runnable {
            override fun run() {
                if (!isAppAccessAllowed(packageName)) {
                    closeApp(packageName)
                } else {
                    handler.postDelayed(this, checkInterval)
                }
            }
        }
    }

    private fun getMonitoredApps(): Set<String> {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        return prefs.getStringSet("monitored_apps", setOf()) ?: setOf()
    }

    private fun isAppAccessAllowed(packageName: String): Boolean {
        val prefs = getSharedPreferences("app_blocks", MODE_PRIVATE)
        val allowedUntil = prefs.getLong("${packageName}_allowed_until", 0)
        return System.currentTimeMillis() <= allowedUntil
    }

    private fun allowAppAccess(packageName: String) {
        val prefs = getSharedPreferences("app_blocks", MODE_PRIVATE)
        val allowedTime = prefs.getLong("${packageName}_allowed_time", 30 * 1000)
        prefs.edit().putLong("${packageName}_allowed_until", System.currentTimeMillis() + allowedTime).apply()
    }

    private fun closeApp(packageName: String) {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun showOverlay(packageName: String) {
        val intent = Intent(this, OverlayService::class.java)
        intent.putExtra("packageName", packageName)
        startService(intent)
    }

    private fun updateOpenCount(packageName: String) {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val currentDate = DateUtils.getCurrentDate()
        val lastOpenedDate = prefs.getString("lastOpenedDate_$packageName", "")
        var openCount = prefs.getInt("openCount_$packageName", 0)

        if (!DateUtils.isSameDay(currentDate, lastOpenedDate ?: "")) {
            openCount = 0
        }

        openCount += 1
        prefs.edit().putInt("openCount_$packageName", openCount).apply()
        prefs.edit().putString("lastOpenedDate_$packageName", currentDate).apply()

        Log.d("AppMonitorService", "Open count for $packageName updated: $openCount")
    }

    override fun onInterrupt() {
        handler.removeCallbacksAndMessages(null)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.packageNames = null // Monitor all apps
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo = info
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
