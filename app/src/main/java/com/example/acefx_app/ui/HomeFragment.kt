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

        // chat now navigation handle if login
        binding.btnChatNow.setOnClickListener {
            handleHomeNavigation()
        }
        // add product navigation
        binding.addProjectBtn.setOnClickListener {
            loadLocalUserData() // load local then check
            if (isProfileComplete()) navigateToAddProject() else navigateToProfile()

        }
    }

    // Determine whether to go to profile setup or projects
    private fun handleHomeNavigation() {
        loadLocalUserData()

        when {
            // Profile is complete
            isProfileComplete() -> {
                Toast.makeText(requireContext(), "Welcome back, $companyName!", Toast.LENGTH_SHORT)
                    .show()
                navigateToChat()
            }

            // Token exists but profile incomplete — fetch updated profile
            !token.isNullOrEmpty() -> {
                fetchUpdatedProfile()
            }

            // No token or no profile — go to profile setup
            else -> {
                Toast.makeText(
                    requireContext(), "Please complete your profile.", Toast.LENGTH_SHORT
                ).show()
                navigateToProfile()
            }
        }
    }

    private fun loadLocalUserData() {
        companyName = sharedPref.getString("companyName", null)
        phoneNumber = sharedPref.getString("phoneNumber", null)
        pinCode = sharedPref.getString("pinCode", null)
        token = sharedPref.getString("authToken", null)
    }

    private fun isProfileComplete(): Boolean {
        return !companyName.isNullOrEmpty() && !phoneNumber.isNullOrEmpty() && !pinCode.isNullOrEmpty()
    }

    private fun fetchUpdatedProfile() {
        showLoading(true)

        api.getUserProfile("Bearer $token").enqueue(object : Callback<UserDetailsResponse> {
            override fun onResponse(
                call: Call<UserDetailsResponse>, response: Response<UserDetailsResponse>
            ) {
                showLoading(false)

                if (!isAdded) return

                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        saveUserDataLocally(data)
                        if (isProfileComplete()) {
                            Toast.makeText(
                                requireContext(), "Welcome back, $companyName!", Toast.LENGTH_SHORT
                            ).show()
                            navigateToProjects()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Profile incomplete. Please update your details.",
                                Toast.LENGTH_SHORT
                            ).show()
                            navigateToProfile()
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(), "Failed to load user data.", Toast.LENGTH_SHORT
                    ).show()
                    navigateToProfile()
                }
            }

            override fun onFailure(call: Call<UserDetailsResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                navigateToProfile()
            }
        })
    }

    private fun saveUserDataLocally(data: UserDetailsResponse) {
        companyName = data.companyName
        phoneNumber = data.phoneNumber
        pinCode = data.pinCode

        sharedPref.edit().apply {
            putString("companyName", companyName)
            putString("phoneNumber", phoneNumber)
            putString("pinCode", pinCode)
            apply()
        }
    }

    private fun navigateToProjects() {
        findNavController().navigate(R.id.clientProjectsFragment)
    }

    private fun navigateToChat() {
        findNavController().navigate(R.id.chatFragment)
    }

    private fun navigateToAddProject() {
        findNavController().navigate(R.id.clientAddProjectFragment)
    }

    private fun navigateToProfile() {
        findNavController().navigate(R.id.clientProfileFragment)
    }

    private fun showLoading(isLoading: Boolean) {
//        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
//        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnChatNow.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
