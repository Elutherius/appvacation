package com.example.idea1

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppSelectionActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var monitoredApps: MutableList<String>
    private lateinit var allAppsAdapter: AppAdapter
    private lateinit var monitoredAppsAdapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_selection)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        monitoredApps = sharedPreferences.getStringSet("monitored_apps", setOf())!!.toMutableList()

        val monitoredAppsRecyclerView = findViewById<RecyclerView>(R.id.monitoredAppsRecyclerView)
        val allAppsRecyclerView = findViewById<RecyclerView>(R.id.allAppsRecyclerView)

        val installedApps = getInstalledApps()
        val monitoredAppsList = installedApps.filter { monitoredApps.contains(it.packageName) }
        val allAppsList = installedApps

        monitoredAppsAdapter = AppAdapter(monitoredAppsList, isMonitoredList = true) { app, add ->
            handleAppAction(app, add)
        }
        monitoredAppsRecyclerView.layoutManager = LinearLayoutManager(this)
        monitoredAppsRecyclerView.adapter = monitoredAppsAdapter

        allAppsAdapter = AppAdapter(allAppsList, isMonitoredList = false) { app, add ->
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
        if (add) {
            monitoredApps.add(app.packageName)
        } else {
            monitoredApps.remove(app.packageName)
        }
        sharedPreferences.edit().putStringSet("monitored_apps", monitoredApps.toSet()).apply()
        refreshMonitoredApps()
    }

    private fun refreshMonitoredApps() {
        val newMonitoredApps = getMonitoredApps()
        monitoredAppsAdapter.apps = newMonitoredApps
        monitoredAppsAdapter.notifyDataSetChanged()

        val newInstalledApps = getInstalledApps()
        allAppsAdapter.apps = newInstalledApps
        allAppsAdapter.notifyDataSetChanged()
    }

    private fun getMonitoredApps(): List<ApplicationInfo> {
        val pm = packageManager
        val monitoredAppPackages = sharedPreferences.getStringSet("monitored_apps", setOf())!!
        return pm.getInstalledApplications(PackageManager.GET_META_DATA).filter {
            monitoredAppPackages.contains(it.packageName)
        }
    }
}
