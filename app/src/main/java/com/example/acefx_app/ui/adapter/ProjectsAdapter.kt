package com.example.acefx_app.ui.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.acefx_app.R
import com.example.acefx_app.data.ProjectData
import com.example.acefx_app.data.ProjectItem

class ProjectsAdapter(
    private var projects: List<ProjectData>,
    private val onProjectClick: (ProjectData) -> Unit
) : RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder>() {

    inner class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val projectName: TextView = itemView.findViewById(R.id.projectTitle)
        val projectStatusBadge: TextView = itemView.findViewById(R.id.projectStatusBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        holder.projectName.text = project.title
        holder.projectStatusBadge.text = project.status

        // Apply badge color based on status
        val context = holder.itemView.context
        val bgDrawable = ContextCompat.getDrawable(context, R.drawable.status_badge_bg)?.mutate() as GradientDrawable

        val color = when (project.status.lowercase()) {
            "approved" -> ContextCompat.getColor(context, R.color.status_active)
            "on hold" -> ContextCompat.getColor(context, R.color.status_pending)
            else -> ContextCompat.getColor(context, android.R.color.darker_gray)
        }
        bgDrawable.setColor(color)
        holder.projectStatusBadge.background = bgDrawable

        holder.itemView.setOnClickListener { onProjectClick(project) }
    }

    override fun getItemCount(): Int = projects.size

    fun updateData(newProjects: List<ProjectData>) {
        projects = newProjects
        notifyDataSetChanged()
    }
}
