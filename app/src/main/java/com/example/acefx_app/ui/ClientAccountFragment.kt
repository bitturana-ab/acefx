package com.example.acefx_app.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.acefx_app.R
import com.example.acefx_app.data.UserDetailsResponse
import com.example.acefx_app.databinding.FragmentClientAccountBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientAccountFragment : Fragment() {

    private var _binding: FragmentClientAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private lateinit var sharedPref: android.content.SharedPreferences
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        token = sharedPref.getString("authToken", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Session expired. Please log in again!", Toast.LENGTH_SHORT).show()
            return
        }

        // Load from SharedPreferences first
        loadUserFromSharedPref()

        // Then try loading fresh from server
        loadUserFromServer()

        // Navigation buttons
        binding.viewProjectsBtn.setOnClickListener {
            safeNavigate {
                findNavController().navigate(AccountFragmentDirections.actionAccountFragmentToClientProjectsFragment())
            }
        }

        binding.updateProfileBtn.setOnClickListener {
            safeNavigate {
                findNavController().navigate(AccountFragmentDirections.actionAccountFragmentToClientProfileFragment())
            }
        }
    }

    /** Load user details instantly from SharedPreferences */
    private fun loadUserFromSharedPref() {
        val name = sharedPref.getString("name", "N/A")
        val email = sharedPref.getString("email", "N/A")
        val company = sharedPref.getString("companyName", "N/A")
        val phone = sharedPref.getString("phoneNumber", "N/A")
        val pin = sharedPref.getString("pinCode", "N/A")

        binding.apply {
            nameText.text = "Name: $name"
            emailText.text = "Email: $email"
            companyText.text = "Company: $company"
            phoneText.text = "Phone: $phone"
            pinText.text = "Pincode: $pin"
        }
    }

    /** Load user details from API and save to SharedPreferences */
    private fun loadUserFromServer() {
        apiService.getUserProfile("Bearer $token").enqueue(object : Callback<UserDetailsResponse> {
            override fun onResponse(
                call: Call<UserDetailsResponse>,
                response: Response<UserDetailsResponse>
            ) {
                if (!isAdded || _binding == null) return

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    updateUI(user)
                    saveUserToSharedPref(user)
                } else {
                    Toast.makeText(requireContext(), "Failed to refresh user details!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserDetailsResponse>, t: Throwable) {
                if (!isAdded || _binding == null) return
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /** Save latest user details locally */
    private fun saveUserToSharedPref(user: UserDetailsResponse) {
        sharedPref.edit().apply {
            putString("name", user.name)
            putString("email", user.email)
            putString("companyName", user.companyName)
            putString("phoneNumber", user.phoneNumber)
            putString("pinCode", user.pinCode)
            apply()
        }
    }

    /** Update screen with user details */
    private fun updateUI(user: UserDetailsResponse) {
        binding.apply {
            nameText.text = "Name: ${user.name ?: "N/A"}"
            emailText.text = "Email: ${user.email ?: "N/A"}"
            companyText.text = "Company: ${user.companyName ?: "N/A"}"
            phoneText.text = "Phone: ${user.phoneNumber ?: "N/A"}"
            pinText.text = "Pincode: ${user.pinCode ?: "N/A"}"
        }
    }

    private fun safeNavigate(action: () -> Unit) {
        if (isAdded && findNavController().currentDestination?.id == R.id.accountFragment) {
            try {
                action()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
