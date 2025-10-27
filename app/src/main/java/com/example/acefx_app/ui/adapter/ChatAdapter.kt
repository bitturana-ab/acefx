package com.example.acefx_app.ui.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acefx_app.R
import com.example.acefx_app.data.ChatMessage

class ChatAdapter(
    private val messages: List<ChatMessage>,
    private val currentUser: String
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val messageText = view.findViewById<TextView>(R.id.messageText)
        val messageTime = view.findViewById<TextView>(R.id.messageTime)
        val messageContainer = view.findViewById<LinearLayout>(R.id.messageContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msg = messages[position]
        holder.messageText.text = msg.text
        holder.messageTime.text = msg.time

        val params = holder.messageContainer.layoutParams as ViewGroup.MarginLayoutParams

        if (msg.sender == currentUser) {
            holder.messageContainer.setBackgroundResource(R.drawable.message_bg_client)
            params.marginStart = 80
            params.marginEnd = 0
            holder.messageContainer.layoutParams = params
            holder.messageContainer.gravity = Gravity.END
        } else {
            holder.messageContainer.setBackgroundResource(R.drawable.message_bg_admin)
            params.marginStart = 0
            params.marginEnd = 80
            holder.messageContainer.layoutParams = params
            holder.messageContainer.gravity = Gravity.START
        }
    }

    override fun getItemCount() = messages.size
}
