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
    private val apps: List<ApplicationInfo>,
    private val isMonitoredList: Boolean,
    private val onAppAction: (ApplicationInfo, Boolean) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        sharedPreferences = parent.context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.bind(app)
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appNameTextView: TextView = itemView.findViewById(R.id.appNameTextView)
        private val appActionButton: Button = itemView.findViewById(R.id.appActionButton)
        private val appIconImageView: ImageView = itemView.findViewById(R.id.appIconImageView)

        fun bind(app: ApplicationInfo) {
            val pm = itemView.context.packageManager
            appNameTextView.text = app.loadLabel(pm)
            appIconImageView.setImageDrawable(app.loadIcon(pm))

            if (isMonitoredList) {
                appActionButton.text = "Remove"
                appActionButton.setOnClickListener {
                    onAppAction(app, false)
                }
            } else {
                val monitoredApps = sharedPreferences.getStringSet("monitored_apps", mutableSetOf())
                if (monitoredApps?.contains(app.packageName) == true) {
                    appActionButton.text = "Added"
                    appActionButton.isEnabled = false
                } else {
                    appActionButton.text = "Add"
                    appActionButton.isEnabled = true
                    appActionButton.setOnClickListener {
                        onAppAction(app, true)
                    }
                }
            }
        }
    }
}
