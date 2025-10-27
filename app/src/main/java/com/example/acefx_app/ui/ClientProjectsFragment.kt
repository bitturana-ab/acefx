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

        // Back button
        binding.backBtn.setOnClickListener { findNavController().navigateUp() }

        // Setup API client
        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)

        // Load session
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        token = sharedPref.getString("authToken", "") ?: ""
        val companyName = sharedPref.getString("companyName", "Client")
        binding.clientNameText.text = companyName

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        // RecyclerView setup
        adapter = ProjectsAdapter(emptyList()) { project ->
            val action = ClientProjectsFragmentDirections
                .actionClientProjectsFragmentToClientProjectDetailsFragment(projectId = project._id)
            findNavController().navigate(action)
        }
        binding.projectsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.projectsRecyclerView.adapter = adapter

        // Setup Tabs + Refresh
        setupTabs()
        binding.swipeRefresh.setOnRefreshListener { refreshData() }

        // Load projects
        loadProjects()
    }

    /** Tabs setup (only backend statuses) */
    private fun setupTabs() {
        binding.projectTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterProjects()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    /** Pull to refresh */
    private fun refreshData() {
        binding.swipeRefresh.isRefreshing = true
        loadProjects {
            binding.swipeRefresh.isRefreshing = false
        }
    }

    /** Load from backend */
    private fun loadProjects(onComplete: (() -> Unit)? = null) {
        showLoading(true)
        apiService.getClientProjects("Bearer $token")
            .enqueue(object : Callback<ProjectsResponse> {
                override fun onResponse(
                    call: Call<ProjectsResponse>,
                    response: Response<ProjectsResponse>
                ) {
                    if (!isAdded) return
                    showLoading(false)
                    onComplete?.invoke()

                    if (response.isSuccessful) {
                        allProjects = response.body()?.projects ?: emptyList()
                        Toast.makeText(
                            requireContext(),
                            "All projects loaded.",
                            Toast.LENGTH_SHORT
                        ).show()

                        filterProjects()

                    } else {
                        Log.e(
                            "PROJECT_LOAD_FAILED",
                            response.errorBody()?.string() ?: "Unknown error"
                        )
                        Toast.makeText(
                            requireContext(),
                            "Failed to load projects!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ProjectsResponse>, t: Throwable) {
                    if (!isAdded) return
                    showLoading(false)
                    onComplete?.invoke()
                    Toast.makeText(
                        requireContext(),
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    /** Filter projects based on selected tab */
    private fun filterProjects() {
        val selectedTab =
            binding.projectTabLayout.getTabAt(binding.projectTabLayout.selectedTabPosition)
        val statusFilter = selectedTab?.text.toString().lowercase()

        val filtered = allProjects.filter { project ->
            project.status.equals(statusFilter, true)
        }

        adapter.updateData(filtered)
    }

    /** Smooth overlay animation */
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingOverlay.fadeIn()
            binding.progressBar.fadeIn()
        } else {
            binding.progressBar.fadeOut()
            binding.loadingOverlay.fadeOut()
        }
    }

    /** Fade animations */
    private fun View.fadeIn(duration: Long = 300) {
        this.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(duration).start()
        }
    }

    private fun View.fadeOut(duration: Long = 300, endVisibility: Int = View.GONE) {
        this.animate().alpha(0f).setDuration(duration).withEndAction {
            visibility = endVisibility
        }.start()
    }

    override fun onResume() {
        super.onResume()
        filterProjects()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
