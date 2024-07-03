package com.example.idea1

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button


class AddAppsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var allAppsAdapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_apps)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        val allAppsRecyclerView = findViewById<RecyclerView>(R.id.allAppsRecyclerView)
        val viewMonitoredAppsButton = findViewById<Button>(R.id.button_view_monitored_apps)

        viewMonitoredAppsButton.setOnClickListener {
            val intent = Intent(this, MonitoredAppsActivity::class.java)
            startActivity(intent)
        }

        val installedApps = getInstalledApps()

        allAppsAdapter = AppAdapter(installedApps, isMonitoredList = false) { app, add ->
            handleAppAction(app, add)
        }
        allAppsRecyclerView.layoutManager = LinearLayoutManager(this)
        allAppsRecyclerView.adapter = allAppsAdapter
    }

    private fun getInstalledApps(): List<ApplicationInfo> {
        val pm = packageManager
        val allApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val userApps = allApps.filterNot { isSystemPackage(it) }
        return userApps.sortedBy { it.loadLabel(pm).toString() }
    }

    private fun isSystemPackage(app: ApplicationInfo): Boolean {
        return (app.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0
    }

    private fun handleAppAction(app: ApplicationInfo, add: Boolean) {
        val monitoredApps = sharedPreferences.getStringSet("monitored_apps", setOf())!!.toMutableList()
        if (add) {
            monitoredApps.add(app.packageName)
        } else {
            monitoredApps.remove(app.packageName)
        }
        sharedPreferences.edit().putStringSet("monitored_apps", monitoredApps.toSet()).apply()
    }
}
