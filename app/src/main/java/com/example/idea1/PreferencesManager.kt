package com.example.idea1

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.widget.TextView

class PreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    fun getRemainingBlockTime(packageName: String): Long {
        val lastWrongAnswerTime = sharedPreferences.getLong("lastWrongAnswerTime_$packageName", 0)
        val blockDuration = 10 * 60 * 1000 // 10 minutes (example)
        val elapsedTime = System.currentTimeMillis() - lastWrongAnswerTime
        return blockDuration - elapsedTime
    }

    fun saveLastWrongAnswerTime(packageName: String) {
        sharedPreferences.edit().putLong("lastWrongAnswerTime_$packageName", System.currentTimeMillis()).apply()
    }

    fun saveLastSolvedTime(packageName: String) {
        sharedPreferences.edit().putLong("lastSolvedTime_$packageName", System.currentTimeMillis()).apply()
    }

    fun allowAppAccess(packageName: String) {
        sharedPreferences.edit().putLong("${packageName}_allowed_until", System.currentTimeMillis() + 30 * 1000).apply()
    }

    fun getOpenCount(packageName: String): Int {
        return sharedPreferences.getInt("openCount_$packageName", 0)
    }

    fun incrementOpenCount(packageName: String) {
        val currentCount = getOpenCount(packageName)
        sharedPreferences.edit().putInt("openCount_$packageName", currentCount + 1).apply()
    }

    fun setBlockStatus(packageName: String, isBlocked: Boolean) {
        sharedPreferences.edit().putBoolean("isBlocked_$packageName", isBlocked).apply()
    }

    fun isAppBlocked(packageName: String): Boolean {
        return sharedPreferences.getBoolean("isBlocked_$packageName", false)
    }

    fun updateOpenCountTextView(openCountTextView: TextView, packageName: String) {
        val openCount = getOpenCount(packageName)
        val pm = openCountTextView.context.packageManager
        val appName = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString()
        openCountTextView.text = "You've already opened $appName $openCount times today"
    }
}
