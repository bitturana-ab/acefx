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
import com.example.acefx_app.databinding.FragmentHomeBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var api: ApiService
    private lateinit var sharedPref: android.content.SharedPreferences

    private var companyName: String? = null
    private var phoneNumber: String? = null
    private var pinCode: String? = null
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        api = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)

        binding.goToHomeBtn.setOnClickListener {
            goToProfileOrProjects()
        }
    }

    private fun goToProfileOrProjects() {
        companyName = sharedPref.getString("companyName", null)
        phoneNumber = sharedPref.getString("phoneNumber", null)
        pinCode = sharedPref.getString("pinCode", null)
        token = sharedPref.getString("authToken", null)

        when {
            // All profile info exists — go to projects
            !companyName.isNullOrEmpty() && !phoneNumber.isNullOrEmpty() && !pinCode.isNullOrEmpty() -> {
                Toast.makeText(requireContext(), "Welcome back, $companyName!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.clientProjectsFragment)
            }

            // Token exists — fetch from backend
            !token.isNullOrEmpty() -> {
                getUserUpdatedProfile()
            }

            // No token or incomplete profile — go to profile setup
            else -> {
                Toast.makeText(requireContext(), "Please complete your profile.", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.clientProfileFragment)
            }
        }
    }

    // Fetch user's updated profile from backend and store locally
    private fun getUserUpdatedProfile() {
        // Show loading spinner before fetching
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
        binding.goToHomeBtn.isEnabled = false

        api.getUserProfile("Bearer $token").enqueue(object : Callback<UserDetailsResponse> {
            override fun onResponse(
                call: Call<UserDetailsResponse>,
                response: Response<UserDetailsResponse>
            ) {
                // Hide spinner
                binding.progressBar.visibility = View.GONE
                binding.loadingOverlay.visibility = View.GONE
                binding.goToHomeBtn.isEnabled = true

                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        companyName = data.companyName
                        phoneNumber = data.phoneNumber
                        pinCode = data.pinCode

                        // Save fetched data to SharedPreferences
                        with(sharedPref.edit()) {
                            putString("companyName", companyName)
                            putString("phoneNumber", phoneNumber)
                            putString("pinCode", pinCode)
                            apply()
                        }

                        if (!companyName.isNullOrEmpty() && !phoneNumber.isNullOrEmpty() && !pinCode.isNullOrEmpty()) {
                            Toast.makeText(requireContext(), "Welcome back, $companyName!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.clientProjectsFragment)
                        } else {
                            Toast.makeText(requireContext(), "Profile incomplete. Please update your details.", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.clientProfileFragment)
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.clientProfileFragment)
                }
            }

            override fun onFailure(call: Call<UserDetailsResponse>, t: Throwable) {
                // Hide spinner
                binding.progressBar.visibility = View.GONE
                binding.goToHomeBtn.isEnabled = true

                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.clientProfileFragment)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
