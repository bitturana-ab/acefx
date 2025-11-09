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
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ProjectsAdapter(
    private var projects: List<ProjectData>,
    private val onProjectClick: (ProjectData) -> Unit
) : RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder>() {

    inner class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val projectName: TextView = itemView.findViewById(R.id.projectTitle)
        val projectStatusBadge: TextView = itemView.findViewById(R.id.projectStatusBadge)
        val deadlineText: TextView = itemView.findViewById(R.id.deadlineText)
        val paymentStatus: TextView = itemView.findViewById(R.id.paymentStatus)
        val remainingAmount: TextView = itemView.findViewById(R.id.remainingAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        val context = holder.itemView.context

        // --- Basic info ---
        holder.projectName.text = project.title
        holder.projectStatusBadge.text = project.status
        holder.deadlineText.text = formatDateTime(project.deadline)

        // --- Payment details ---
        val payment = project.paymentId
        val paymentStatusText: String
        val remainingText: String

        val totalAmount = project.actualAmount?.takeIf { it > 0 } ?: project.expectedAmount ?: 0.0

        when (payment?.paidType?.lowercase()) {
            "half" -> {
                paymentStatusText = "Half Paid"
                remainingText = "₹${(totalAmount / 2).toInt()} remaining"
                holder.remainingAmount.setTextColor(ContextCompat.getColor(context, R.color.status_pending))
            }
            "full" -> {
                paymentStatusText = "Paid in Full"
                remainingText = "₹0 remaining"
                holder.remainingAmount.setTextColor(ContextCompat.getColor(context, R.color.status_active))
            }
            else -> {
                paymentStatusText = "Unpaid"
                remainingText = "₹${totalAmount.toInt()} remaining"
                holder.remainingAmount.setTextColor(ContextCompat.getColor(context, R.color.status_pending))
            }
        }

        holder.paymentStatus.text = paymentStatusText
        holder.remainingAmount.text = remainingText

        // --- Badge Color ---
        val bgDrawable = (ContextCompat.getDrawable(context, R.drawable.status_badge_bg)?.mutate() as GradientDrawable)
        val color = when (project.status.lowercase()) {
            "approved" -> ContextCompat.getColor(context, R.color.status_active)
            "on hold" -> ContextCompat.getColor(context, R.color.status_pending)
            "success" -> ContextCompat.getColor(context, R.color.status_completed)
            else -> ContextCompat.getColor(context, android.R.color.darker_gray)
        }
        bgDrawable.setColor(color)
        holder.projectStatusBadge.background = bgDrawable

        // Click listener
        holder.itemView.setOnClickListener { onProjectClick(project) }
    }

    private fun formatDateTime(isoDate: String?): String {
        if (isoDate.isNullOrEmpty()) return ""
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(isoDate)
            val formatter = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault())
            formatter.format(date!!)
        } catch (e: Exception) {
            e.printStackTrace()
            isoDate
        }
    }

    override fun getItemCount(): Int = projects.size

    fun updateData(newProjects: List<ProjectData>) {
        projects = newProjects
        notifyDataSetChanged()
    }
}
