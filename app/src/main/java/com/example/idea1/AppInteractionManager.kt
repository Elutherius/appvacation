package com.example.idea1

import android.content.Context
import android.content.Intent

class AppInteractionManager(private val context: Context) {

    fun closeApp() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun allowAppAccess(packageName: String, duration: Long) {
        val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("${packageName}_allowed_until", System.currentTimeMillis() + duration).apply()
    }

    fun isAppAccessAllowed(packageName: String): Boolean {
        val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val allowedUntil = prefs.getLong("${packageName}_allowed_until", 0)
        return System.currentTimeMillis() <= allowedUntil
    }
}
