package com.example.acefx_app.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acefx_app.R
import com.example.acefx_app.data.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val messages: List<ChatMessage>,
    private val currentUser: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_CLIENT = 1
    private val VIEW_TYPE_ADMIN = 2

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.clientName)
        val tvUserMessage: TextView = view.findViewById(R.id.clientMessage)
        val tvUserTime: TextView = view.findViewById(R.id.clientTime)
    }

    inner class AdminViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAdminName: TextView = view.findViewById(R.id.adminName)
        val tvAdminMessage: TextView = view.findViewById(R.id.adminMessage)
        val tvAdminTime: TextView = view.findViewById(R.id.adminTime)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderRole == currentUser) VIEW_TYPE_CLIENT else VIEW_TYPE_ADMIN
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_CLIENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_admin, parent, false)
            AdminViewHolder(view)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        val formattedTime = formatDateTime(msg.time)

        if (holder is UserViewHolder) {
            holder.tvUserName.text = msg.senderRole
            holder.tvUserMessage.text = msg.message
            holder.tvUserTime.text = formattedTime
        } else if (holder is AdminViewHolder) {
            holder.tvAdminName.text = "Admin"
            holder.tvAdminMessage.text = msg.message
            holder.tvAdminTime.text = formattedTime
        }
    }

    override fun getItemCount(): Int = messages.size

    /** Format ISO date/time from server into readable string */
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
}
