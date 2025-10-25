package com.example.acefx_app.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.acefx_app.databinding.FragmentClientProfileBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientProfileFragment : Fragment() {

    private var _binding: FragmentClientProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize API service with context
        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)

        // Get stored userId from SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userId", null)

        binding.nextBtn.setOnClickListener {
            val name = binding.companyNameInput.text.toString().trim()
            val phone = binding.phoneInput.text.toString().trim()
            val pin = binding.pinCodeInput.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || pin.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (userId.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            } else {
                saveClientProfile(userId, name, phone, pin)
            }
        }
    }

    private fun saveClientProfile(userId: String, companyName: String, phoneNumber: String, pinCode: String) {
        val request = mapOf(
            "userId" to userId,
            "companyName" to companyName,
            "phoneNumber" to phoneNumber,
            "pinCode" to pinCode
        )

        apiService.updateUser(request).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Profile saved successfully!", Toast.LENGTH_SHORT).show()
                    // TODO: Navigate to next screen
                } else {
                    Toast.makeText(requireContext(), "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
