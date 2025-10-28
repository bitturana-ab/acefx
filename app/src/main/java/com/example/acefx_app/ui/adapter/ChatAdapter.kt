package com.example.acefx_app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acefx_app.R
import com.example.acefx_app.data.ChatMessage
import com.example.acefx_app.data.ChatMessageRequest

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
        return if (messages[position].sender == currentUser) VIEW_TYPE_CLIENT else VIEW_TYPE_ADMIN
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        if (holder is UserViewHolder) {
            holder.tvUserName.text = msg.sender
            holder.tvUserMessage.text = msg.message
            holder.tvUserTime.text = msg.time
        } else if (holder is AdminViewHolder) {
            holder.tvAdminName.text = "Admin"
            holder.tvAdminMessage.text = msg.message
            holder.tvAdminTime.text = msg.time
        }
    }

    override fun getItemCount(): Int = messages.size
}
