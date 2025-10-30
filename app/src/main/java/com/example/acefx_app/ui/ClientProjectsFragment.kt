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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        token = sharedPref.getString("authToken", "") ?: ""
        val companyName = sharedPref.getString("companyName", "Client")
        binding.clientNameText.text = companyName

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        adapter = ProjectsAdapter(emptyList()) { project ->
            findNavController().navigate(ClientProjectsFragmentDirections.actionClientProjectsFragmentToClientProjectDetailsFragment(project._id))
            Toast.makeText(requireContext(), "Project: ${project.title}", Toast.LENGTH_SHORT).show()
        }

        binding.projectsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.projectsRecyclerView.adapter = adapter

        setupTabLayout()
        setupActions()

        // Load cached projects first
        loadCachedProjects()

        // Fetch updated projects
        loadProjects()
    }

    private fun setupTabLayout() {
        binding.projectTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterProjectsByStatus(tab?.text.toString())
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupActions() {
        binding.swipeRefresh.setOnRefreshListener {
            // Reset to first tab on refresh
            val firstTab = binding.projectTabLayout.getTabAt(0)
            firstTab?.select()
            loadProjects()
        }

        binding.addProjectBtn.setOnClickListener {
            findNavController().navigate(R.id.clientAddProjectFragment)
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    /** Load projects from local cache */
    private fun loadCachedProjects() {
        val prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val cachedJson = prefs.getString("cachedProjects", null)
        if (cachedJson != null) {
            val type = object : TypeToken<List<ProjectItem>>() {}.type
            val cachedList: List<ProjectItem> = gson.fromJson(cachedJson, type)
            if (cachedList.isNotEmpty()) {
                allProjects = cachedList
                adapter.updateData(allProjects)
                showEmptyState(false)
                Log.d("CACHE", "Loaded ${cachedList.size} cached projects")
            }
        }
    }

    /** Save projects to SharedPreferences */
    private fun saveProjectsToCache(projects: List<ProjectItem>) {
        val prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val json = gson.toJson(projects)
        prefs.edit().putString("cachedProjects", json).apply()
        Log.d("CACHE", "Saved ${projects.size} projects to cache")
    }

    /** Fetch projects from backend */
    private fun loadProjects() {
        apiService.getClientProjects("Bearer $token").enqueue(object : Callback<ProjectsResponse> {
            override fun onResponse(
                call: Call<ProjectsResponse>, response: Response<ProjectsResponse>
            ) {
                if (!isAdded) return
                showLoading(false)
                binding.swipeRefresh.isRefreshing = false

                if (response.isSuccessful) {
                    allProjects = response.body()?.data ?: emptyList()
                    Log.d("LOAD_PROJECTS", "Loaded ${allProjects.size} projects")

                    if (allProjects.isEmpty()) {
                        showEmptyState(true)
                    } else {
                        showEmptyState(false)
                        saveProjectsToCache(allProjects) // Save to cache
                        val selectedTab = binding.projectTabLayout.getTabAt(
                            binding.projectTabLayout.selectedTabPosition
                        )
                        val tabText = selectedTab?.text?.toString() ?: "approved"
                        filterProjectsByStatus(tabText)
                    }
                } else {
                    Toast.makeText(
                        requireContext(), "Failed to load projects!", Toast.LENGTH_SHORT
                    ).show()
                    showEmptyState(true)
                }
            }

            override fun onFailure(call: Call<ProjectsResponse>, t: Throwable) {
                if (!isAdded) return
                showLoading(false)
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(
                    requireContext(), "Check internet connection", Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    /** Filter projects by selected status */
    private fun filterProjectsByStatus(statusText: String) {
        val filtered = when (statusText.lowercase()) {
            "approved" -> allProjects.filter { it.status.equals("approved", true) }
            "on hold" -> allProjects.filter { it.status.equals("on hold", true) }
            else -> allProjects
        }

        adapter.updateData(filtered)
        showEmptyState(filtered.isEmpty())
    }

    /** Show or hide loading */
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    /** Show or hide empty message */
    private fun showEmptyState(show: Boolean) {
        binding.emptyText.visibility = if (show) View.VISIBLE else View.GONE
        binding.projectsRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
