package com.example.idea1

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val OVERLAY_PERMISSION_REQUEST_CODE = 1
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        val enableAccessibilityButton = findViewById<Button>(R.id.enableAccessibilityButton)
        enableAccessibilityButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        val enableOverlayButton = findViewById<Button>(R.id.enableOverlayButton)
        enableOverlayButton.setOnClickListener {
            requestOverlayPermission()
        }

        // Start the MonitorService
        val serviceIntent = Intent(this, MonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
            } else {
                Toast.makeText(this, "Overlay permission already granted", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Overlay permission not required for this Android version", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Overlay permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Overlay permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateOpenCountTextView(openCountTextView: TextView) {
        val currentDate = DateUtils.getCurrentDate()
        val lastOpenedDate = sharedPreferences.getString("lastOpenedDate", "")
        var openCount = sharedPreferences.getInt("openCount", 0)

        if (!DateUtils.isSameDay(currentDate, lastOpenedDate ?: "")) {
            openCount = 0
        }

        openCountTextView.text = "You've already opened Instagram $openCount times today"
    }
}
