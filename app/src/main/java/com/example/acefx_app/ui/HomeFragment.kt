package com.example.acefx_app.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.acefx_app.R

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences =
            requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val role = sharedPreferences.getString("role", "")

        // Safe navigation after view is fully attached
        view.post {
            when (role?.lowercase()) {
                "client" -> {
                    val action =
                        HomeFragmentDirections.actionHomeFragmentToClientProfileActivity()
                    findNavController().navigate(action)
                }
                "employee" -> {
                    val action =
                        HomeFragmentDirections.actionHomeFragmentToStaffProfileActivity()
                    findNavController().navigate(action)
                }
                else -> {
                    Toast.makeText(requireContext(), "No valid role found", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}
