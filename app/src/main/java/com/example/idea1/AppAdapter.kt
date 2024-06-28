package com.example.idea1

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    var apps: List<ApplicationInfo>,
    private val isMonitoredList: Boolean,
    private val onAppAction: (app: ApplicationInfo, add: Boolean) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        sharedPreferences = parent.context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.appNameTextView.text = app.loadLabel(holder.itemView.context.packageManager)
        holder.appIconImageView.setImageDrawable(app.loadIcon(holder.itemView.context.packageManager))

        val monitoredApps = sharedPreferences.getStringSet("monitored_apps", setOf())!!.toMutableSet()

        if (isMonitoredList) {
            holder.appActionButton.text = "Remove"
            holder.appActionButton.setOnClickListener {
                monitoredApps.remove(app.packageName)
                sharedPreferences.edit().putStringSet("monitored_apps", monitoredApps).apply()
                apps = apps.toMutableList().apply { remove(app) }
                notifyDataSetChanged()
                onAppAction(app, false)
            }
        } else {
            if (monitoredApps.contains(app.packageName)) {
                holder.appActionButton.text = "Added"
                holder.appActionButton.isEnabled = false
            } else {
                holder.appActionButton.text = "Add"
                holder.appActionButton.isEnabled = true
                holder.appActionButton.setOnClickListener {
                    monitoredApps.add(app.packageName)
                    sharedPreferences.edit().putStringSet("monitored_apps", monitoredApps).apply()
                    holder.appActionButton.text = "Added"
                    holder.appActionButton.isEnabled = false
                    onAppAction(app, true)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIconImageView: ImageView = view.findViewById(R.id.appIconImageView)
        val appNameTextView: TextView = view.findViewById(R.id.appNameTextView)
        val appActionButton: Button = view.findViewById(R.id.appActionButton)
    }
}
