package com.example.acefx_app.ui

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

    private val apiService = ApiClient.getClient().create(ApiService::class.java)
    private var _binding: FragmentClientProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextBtn.setOnClickListener {
            val name = binding.companyNameInput.text.toString().trim()
            val phone = binding.phoneInput.text.toString().trim()
            val pin = binding.pinCodeInput.text.toString().trim()
            val userId = "USER_ID_HERE" // replace with actual logged-in user ID

            if (name.isEmpty() || phone.isEmpty() || pin.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT)
                    .show()
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
            override fun onResponse(
                call: Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Profile saved successfully!", Toast.LENGTH_SHORT)
                        .show()
                    // TODO: Navigate to next screen if needed
                } else {
                    Toast.makeText(requireContext(), "Error: ${response.code()}", Toast.LENGTH_SHORT)
                        .show()
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
