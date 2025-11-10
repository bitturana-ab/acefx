package com.example.acefx_app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acefx_app.data.ChatMessage
import com.example.acefx_app.data.ChatMessageRequest
import com.example.acefx_app.databinding.FragmentChatBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import com.example.acefx_app.ui.adapter.ChatAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var chatApi: ApiService
    private var authToken: String? = null
    private var companyName: String? = null
    private val gson = Gson()

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

        chatApi = ApiClient.getClient(requireContext()).create(ApiService::class.java)

        // Get token & company name from SharedPreferences
        val sharedPrefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        authToken = sharedPrefs.getString("authToken", null)
        companyName = sharedPrefs.getString("companyName", null)

        setupRecyclerView()
        setupUI()
        loadLocalChat()
        loadChatHistory()
    }

    /** Setup RecyclerView and adapter */
    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages, currentUser = "Client")
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ChatFragment.adapter
        }
    }

    /** Setup all UI listeners and animations */
    private fun setupUI() {
        binding.companyNameChat.text = companyName ?: "Company"

        // Smooth scroll when typing
        binding.messageInput.setOnClickListener {
            if (messages.isNotEmpty()) {
                binding.recyclerViewChat.scrollToPosition(messages.size - 1)
            }
        }

        // Back button
        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        // Send button click animation
        binding.btnSend.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            false
        }

        // Send button logic with delay
        binding.btnSend.setOnClickListener {
            val messageText = binding.messageInput.text?.toString()?.trim().orEmpty()

            if (messageText.isEmpty()) {
                Toast.makeText(requireContext(), "Message cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnSend.isEnabled = false
            binding.btnSend.alpha = 0.6f

            // Add short realistic delay (like network)
            binding.btnSend.postDelayed({
                val request = ChatMessageRequest(message = messageText)
                sendMessage(request)
            }, 600)
        }
    }

    /** Send message to backend */
    private fun sendMessage(request: ChatMessageRequest) {
        lifecycleScope.launch {
            try {
                if (authToken.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Please login first!", Toast.LENGTH_SHORT).show()
                    restoreSendButton()
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
                        saveLocalChat(messages)
                    }
                } else {
                    Log.d("FAILED_SEND", response.toString())
                    Toast.makeText(requireContext(), "Failed to send message!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Snackbar.make(binding.root, "Network error, please check your connection", Snackbar.LENGTH_SHORT).show()
                Toast.makeText(requireContext(), "Error sending message!", Toast.LENGTH_SHORT).show()
            } finally {
                restoreSendButton()
            }
        }
    }

    /** Fetch latest chat from backend */
    @SuppressLint("NotifyDataSetChanged")
    private fun loadChatHistory() {
        lifecycleScope.launch {
            try {
                if (authToken.isNullOrEmpty()) return@launch

                val response = chatApi.getChatHistory("Bearer $authToken")

                if (response.isSuccessful && response.body()?.success == true) {
                    val chatList = response.body()?.data.orEmpty()
                    messages.clear()
                    messages.addAll(chatList)
                    adapter.notifyDataSetChanged()

                    if (messages.isNotEmpty()) {
                        binding.recyclerViewChat.scrollToPosition(messages.size - 1)
                    }

                    saveLocalChat(messages)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Save chat to SharedPreferences */
    private fun saveLocalChat(chatList: List<ChatMessage>) {
        try {
            val sharedPrefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val json = gson.toJson(chatList)
            sharedPrefs.edit().putString("chatMessages", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Load locally saved chat */
    @SuppressLint("NotifyDataSetChanged")
    private fun loadLocalChat() {
        try {
            val sharedPrefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val json = sharedPrefs.getString("chatMessages", null)

            if (!json.isNullOrEmpty()) {
                val type = object : TypeToken<List<ChatMessage>>() {}.type
                val savedMessages: List<ChatMessage> = gson.fromJson(json, type)
                messages.clear()
                messages.addAll(savedMessages)
                adapter.notifyDataSetChanged()

                if (messages.isNotEmpty()) {
                    binding.recyclerViewChat.scrollToPosition(messages.size - 1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Re-enable send button safely */
    private fun restoreSendButton() {
        binding.btnSend.isEnabled = true
        binding.btnSend.alpha = 1f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
