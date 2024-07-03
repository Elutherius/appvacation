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


class MonitoredAppsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var monitoredAppsAdapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitored_apps)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        val monitoredAppsRecyclerView = findViewById<RecyclerView>(R.id.monitoredAppsRecyclerView)
        val addAppsButton = findViewById<Button>(R.id.button_add_apps)

        addAppsButton.setOnClickListener {
            val intent = Intent(this, AddAppsActivity::class.java)
            startActivity(intent)
        }

        val monitoredApps = getMonitoredApps()

        monitoredAppsAdapter = AppAdapter(monitoredApps, isMonitoredList = true) { app, add ->
            handleAppAction(app, add)
        }
        monitoredAppsRecyclerView.layoutManager = LinearLayoutManager(this)
        monitoredAppsRecyclerView.adapter = monitoredAppsAdapter
    }

    private fun getMonitoredApps(): List<ApplicationInfo> {
        val pm = packageManager
        val monitoredAppPackages = sharedPreferences.getStringSet("monitored_apps", setOf())!!
        return pm.getInstalledApplications(PackageManager.GET_META_DATA).filter {
            monitoredAppPackages.contains(it.packageName)
        }
    }

    private fun handleAppAction(app: ApplicationInfo, add: Boolean) {
        val monitoredApps = sharedPreferences.getStringSet("monitored_apps", setOf())!!.toMutableList()
        if (!add) {
            monitoredApps.remove(app.packageName)
        }
        sharedPreferences.edit().putStringSet("monitored_apps", monitoredApps.toSet()).apply()
        monitoredAppsAdapter.apps = getMonitoredApps()
        monitoredAppsAdapter.notifyDataSetChanged()
    }
}
