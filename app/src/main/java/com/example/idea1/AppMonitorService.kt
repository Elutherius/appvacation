package com.example.idea1

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.view.accessibility.AccessibilityEvent

class AppMonitorService : AccessibilityService() {

    private val handler = Handler()
    private val checkInterval = 1000L // Check every second

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.packageName == "com.instagram.android") {
                if (!isInstagramAccessAllowed()) {
                    showOverlay()
                } else {
                    handler.post(checkInstagramAccess)
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
}
