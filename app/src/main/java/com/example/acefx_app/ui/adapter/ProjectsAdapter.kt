package com.example.acefx_app.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acefx_app.R

data class ProjectItem(
    val title: String,
    val status: String
)

class ProjectsAdapter(private var projects: List<ProjectItem>) :
    RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder>() {

    class ProjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val projectName: TextView = view.findViewById(R.id.projectTitle)
        val projectStatus: TextView = view.findViewById(R.id.projectStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        holder.projectName.text = project.title
        holder.projectStatus.text = "Status: ${project.status}"
    }

    override fun getItemCount(): Int = projects.size

    // Updates the adapter's data and refreshes the RecyclerView
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newProjects: List<ProjectItem>) {
        projects = newProjects
        notifyDataSetChanged()
    }
}
