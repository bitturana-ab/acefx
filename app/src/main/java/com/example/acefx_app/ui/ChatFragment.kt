package com.example.acefx_app.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acefx_app.data.ChatMessage
import com.example.acefx_app.data.ChatMessageRequest
import com.example.acefx_app.databinding.FragmentChatBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import com.example.acefx_app.ui.adapter.ChatAdapter
import kotlinx.coroutines.launch


class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var chatApi: ApiService
    private var authToken: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Initialize API service
        chatApi = ApiClient.getClient(requireContext()).create(ApiService::class.java)

        // ✅ Get token from SharedPreferences
        val sharedPrefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        authToken = sharedPrefs.getString("authToken", null)

        // ✅ Setup RecyclerView
        adapter = ChatAdapter(messages, currentUser = "client")
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ChatFragment.adapter
        }

        // ✅ Load chat history from DB
        loadChatHistory()

        // ✅ Send message button click
        binding.btnSend.setOnClickListener {
            val messageText = binding.messageInput.text.toString().trim()
            if (messageText.isEmpty()) {
                Toast.makeText(requireContext(), "Message cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = ChatMessageRequest(
                sender = "client",
                message = messageText
            )

            sendMessage(request)
        }
    }

    /** 🚀 Send message to backend */
    private fun sendMessage(request: ChatMessageRequest) {
        lifecycleScope.launch {
            try {
                if (authToken.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Please login first!", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = chatApi.sendMessage("Bearer $authToken", request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val sentChat = response.body()?.data
                    if (sentChat != null) {
                        messages.add(sentChat)
                        adapter.notifyItemInserted(messages.size - 1)
                        binding.recyclerViewChat.scrollToPosition(messages.size - 1)
                        binding.messageInput.text?.clear()
                    }
                    Toast.makeText(requireContext(), "Message sent ✅", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to send ❌", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 🧾 Load chat history */
    private fun loadChatHistory() {
        lifecycleScope.launch {
            try {
                if (authToken.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Login required to view chat!", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = chatApi.getChatHistory("Bearer $authToken")
                if (response.isSuccessful && response.body()?.success == true) {
                    val chatList = response.body()?.data ?: emptyList()
                    messages.clear()
                    messages.addAll(chatList)
                    adapter.notifyDataSetChanged()
                    binding.recyclerViewChat.scrollToPosition(messages.size - 1)
                } else {
                    Toast.makeText(requireContext(), "Failed to load chat ❌ ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
