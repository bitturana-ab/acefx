package com.example.acefx_app.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.acefx_app.R
import com.google.android.material.card.MaterialCardView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val welcomeText = view.findViewById<TextView>(R.id.welcomeText)
        val projectList = view.findViewById<LinearLayout>(R.id.projectList)

        // Load user name from SharedPreferences
        val prefs = requireContext().getSharedPreferences("AceFXPrefs", Context.MODE_PRIVATE)
        val userName = prefs.getString("userName", "User")

        welcomeText.text = "Welcome, $userName 👋"

        // Dummy recent projects
        val projects = listOf("AI Assistant", "Finance Tracker", "Chat Sync", "AceFX Android")

        // Dynamically create project cards
        for (proj in projects) {
            val card = MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 12, 0, 0)
                }
                radius = 20f
                cardElevation = 6f
                setCardBackgroundColor(resources.getColor(R.color.dark_gray, null))

                val text = TextView(requireContext()).apply {
                    text = "• $proj"
                    textSize = 18f
                    setTextColor(resources.getColor(R.color.white, null))
                    setPadding(20, 20, 20, 20)
                }
                addView(text)
            }
            projectList.addView(card)
        }

        return view
    }
}
