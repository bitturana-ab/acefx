package com.example.acefx_app.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.acefx_app.R
import com.example.acefx_app.databinding.FragmentClientProfileBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientProfileFragment : Fragment() {

    private var _binding: FragmentClientProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private lateinit var token: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)

        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        token = sharedPref.getString("authToken", "") ?: ""

        binding.updateProfileBtn.setOnClickListener {
            val clientNameInput = binding.clientNameInput.text.toString().trim()
            val companyName = binding.companyNameInput.text.toString().trim()
            val phoneNumber = binding.phoneInput.text.toString().trim()
            val pinCode = binding.pinCodeInput.text.toString().trim()

            when {
                clientNameInput.isEmpty() || companyName.isEmpty() || phoneNumber.isEmpty() || pinCode.isEmpty() -> {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT)
                        .show()
                }

                token.isEmpty() -> {
                    Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {
                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle("Update Profile Confirmation")
                        .setMessage("Are you sure you want to update your profile?")
                        .setPositiveButton("Update Profile") { _, _ ->
                            saveClientProfile(clientNameInput, companyName, phoneNumber, pinCode)
                        }.setNegativeButton("Cancel", null)
                        .create()

                    dialog.show()
                    // Make Logout button text red
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                android.R.color.holo_red_light
                            )
                        )

                    // Optional: Make cancel button gray
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }
        }
        binding.backBtn.setOnClickListener { findNavController().popBackStack() }
    }

    private fun saveClientProfile(
        clientNameInput: String,
        companyName: String,
        phoneNumber: String,
        pinCode: String
    ) {
        val request = mapOf(
            "name" to clientNameInput,
            "companyName" to companyName,
            "phoneNumber" to phoneNumber,
            "pinCode" to pinCode
        )

        apiService.updateUser(request, "Bearer $token")
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    if (response.isSuccessful) {
                        // Save companyName for use in other fragments
                        val sharedPref = requireContext().getSharedPreferences(
                            "UserSession",
                            Context.MODE_PRIVATE
                        )
                        sharedPref.edit().apply {
                            putString("name", clientNameInput)
                            putString("companyName", companyName)
                            putString("phoneNumber", phoneNumber)
                            putString("pinCode", pinCode)
                            apply()
                        }

                        Toast.makeText(
                            requireContext(),
                            "Profile saved successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to ClientProjectsFragment using Navigation Component
                        viewLifecycleOwner.lifecycleScope.launch {
                            delay(100) // Ensure NavController is ready
                            val action = ClientProfileFragmentDirections
                                .actionClientProfileFragmentToClientProjectsFragment()
                            findNavController().navigate(action)
                            // Remove ClientProfileFragment so user cannot come back by back button
                            findNavController().popBackStack(R.id.homeFragment, true)

                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Network Error! Check Internet Connection",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
