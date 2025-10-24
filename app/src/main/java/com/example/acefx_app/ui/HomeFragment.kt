package com.example.acefx_app.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.acefx_app.ClientProfileActivity
import com.example.acefx_app.R
import com.example.acefx_app.StaffProfileActivity
import com.google.android.material.card.MaterialCardView

class HomeFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_home, container, false)
        sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val role = sharedPreferences.getString("role", "")
            if (role.equals("Client", ignoreCase = true)) {
//                open with same same tab
                view = inflater.inflate(R.layout.activity_client_profile,container,false)
            } else if (role.equals("Employee", ignoreCase = true)) {
                view = inflater.inflate(R.layout.activity_staff_profile,container,false)
            } else {
                Toast.makeText(requireContext(), "No valid role found", Toast.LENGTH_SHORT).show()
            }


        return view
    }
}

