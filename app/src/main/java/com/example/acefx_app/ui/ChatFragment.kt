package com.example.acefx_app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acefx_app.data.ChatMessage
import com.example.acefx_app.databinding.FragmentChatBinding
import com.example.acefx_app.ui.adapter.ChatAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChatAdapter(messages, currentUser = "client")
        binding.chatRecyclerView.adapter = adapter
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Example mock messages
        messages.add(ChatMessage("client", "Hello Admin!", "10:15 AM"))
        messages.add(ChatMessage("admin", "Hi Leo, how can I help?", "10:16 AM"))
        messages.add(ChatMessage("client", "I wanted to ask about my project status.", "10:17 AM"))
        adapter.notifyDataSetChanged()

        // Send message button
        binding.sendButton.setOnClickListener {
            val text = binding.messageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                val msg = ChatMessage("client", text, getCurrentTime())
                messages.add(msg)
                adapter.notifyItemInserted(messages.size - 1)
                binding.chatRecyclerView.scrollToPosition(messages.size - 1)
                binding.messageInput.text?.clear()
            }
        }
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }
}
