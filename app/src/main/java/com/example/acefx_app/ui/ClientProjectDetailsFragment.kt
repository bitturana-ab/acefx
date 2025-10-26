package com.example.acefx_app.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.acefx_app.R

class ClientProjectDetailsFragment : Fragment() {

    private var projectId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectId = arguments?.getString("projectId")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (projectId != null) {
            fetchProjectDetails(projectId!!)
        }
    }

    private fun fetchProjectDetails(id: String) {
        // call your API: /projects/{id} and populate the UI
    }
}
