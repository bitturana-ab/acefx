package com.example.acefx_app.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.acefx_app.R

class ClientProjectDetailsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_client_project_details, container, false)
        val projectTitle = arguments?.getString("projectTitle")
        view.findViewById<TextView>(R.id.projectName).text = projectTitle.toString()
        return view
    }
}