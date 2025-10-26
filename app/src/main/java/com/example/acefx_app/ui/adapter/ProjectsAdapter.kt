package com.example.acefx_app.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acefx_app.R

data class ProjectItem(
    val title: String,
    val status: String
)

class ProjectsAdapter(
    private var projects: List<ProjectItem>,
    private val onItemClick: (ProjectItem) -> Unit   // Pass click lambda
) : RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder>() {

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

        // Item click listener
        holder.itemView.setOnClickListener {
            onItemClick(project)
        }

        // Fade-in animation for each item
        setFadeAnimation(holder.itemView, position)
    }

    override fun getItemCount(): Int = projects.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newProjects: List<ProjectItem>) {
        projects = newProjects
        notifyDataSetChanged()
    }

    /** Fade-in animation for RecyclerView items */
    private var lastPosition = -1
    private fun setFadeAnimation(view: View, position: Int) {
        if (position > lastPosition) {
            val anim = AlphaAnimation(0.0f, 1.0f)
            anim.duration = 300
            view.startAnimation(anim)
            lastPosition = position
        }
    }
}
