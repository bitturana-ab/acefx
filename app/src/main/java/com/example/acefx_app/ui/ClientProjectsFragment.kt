package com.example.acefx_app.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acefx_app.R
import com.example.acefx_app.data.ProjectData
import com.example.acefx_app.data.ProjectItem
import com.example.acefx_app.data.ProjectsResponse
import com.example.acefx_app.databinding.FragmentClientProjectsBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import com.example.acefx_app.ui.adapter.ProjectsAdapter
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientProjectsFragment : Fragment() {

    private var _binding: FragmentClientProjectsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProjectsAdapter
    private lateinit var apiService: ApiService
    private lateinit var token: String
    private var allProjects = listOf<ProjectItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize API service
        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)

        // Load token and company name from SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        token = sharedPref.getString("authToken", "") ?: ""
        val companyName = sharedPref.getString("companyName", "Client")
        binding.clientNameText.text = companyName

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        // Setup RecyclerView
        adapter = ProjectsAdapter(emptyList()) { project ->
            // Navigate to details fragment using Safe Args
            val action = ClientProjectsFragmentDirections
                .actionClientProjectsFragmentToClientProjectDetailsFragment(projectId = project._id) // pass the id
            findNavController().navigate(action)
        }
        binding.projectsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.projectsRecyclerView.adapter = adapter

        // Load projects from backend
        loadProjects()

        // Tab listener
        binding.projectTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                TODO("update when app loaded then show")
                filterProjectsByStatus(tab?.text.toString())
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Add Project button
        binding.addProjectBtn.setOnClickListener {
            Toast.makeText(requireContext(), "Open Add Project screen", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.clientAddProjectFragment)
        }
    }

    /** Load projects from backend */
    private fun loadProjects() {
        showLoading(true)

        apiService.getClientProjects("Bearer $token")
            .enqueue(object : Callback<ProjectsResponse> {
                override fun onResponse(
                    call: Call<ProjectsResponse>,
                    response: Response<ProjectsResponse>
                ) {
                    Log.d("FAILDE_TO_LOAD",  response.toString())
                    if (!isAdded) return
                    showLoading(false)

                    if (response.isSuccessful) {
                        allProjects = response.body()?.projects ?: emptyList()

                    } else {
                        Log.d("FAILDE_TO_LOAD", response.errorBody()?.string() ?: response.toString())
                        Toast.makeText(requireContext(), "Failed to load projects!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ProjectsResponse>, t: Throwable) {
                    if (!isAdded) return
                    showLoading(false)
                    Toast.makeText(requireContext(), "Network error!", Toast.LENGTH_SHORT).show()
                }
            })
    }


    /** Filter projects based on tab selection */
    private fun filterProjectsByStatus(status: String) {
        showLoading(true)
        val filtered = allProjects.filter { it.status.equals(status, ignoreCase = true) }
        adapter.updateData(filtered)
        showLoading(false)
    }

    /** Smooth fade-in/fade-out loading overlay */
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingOverlay.fadeIn()
            binding.progressBar.fadeIn()
        } else {
            binding.progressBar.fadeOut()
            binding.loadingOverlay.fadeOut()
        }
    }

    /** Fade-in extension */
    private fun View.fadeIn(duration: Long = 300) {
        this.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(duration).start()
        }
    }

    /** Fade-out extension */
    private fun View.fadeOut(duration: Long = 300, endVisibility: Int = View.GONE) {
        this.animate().alpha(0f).setDuration(duration).withEndAction {
            visibility = endVisibility
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
