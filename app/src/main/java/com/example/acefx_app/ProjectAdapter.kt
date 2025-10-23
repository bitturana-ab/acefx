package com.example.acefx_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProjectAdapter(private val projects: List<Map<String, Any>>) :
    RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    class ProjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val projectName: TextView = view.findViewById(R.id.projectName)
        val projectStatus: TextView = view.findViewById(R.id.projectStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        holder.projectName.text = project["title"]?.toString() ?: "Untitled"
        holder.projectStatus.text = "Status: ${project["status"] ?: "Unknown"}"
    }

    override fun getItemCount(): Int = projects.size
}
