package com.example.acefx_app.ui

import android.annotation.SuppressLint
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
import com.example.acefx_app.data.ProjectsResponse
import com.example.acefx_app.databinding.FragmentProjectsBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import com.example.acefx_app.ui.adapter.ProjectsAdapter
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProjectsFragment : Fragment() {

    private var _binding: FragmentProjectsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProjectsAdapter
    private lateinit var apiService: ApiService
    private lateinit var token: String
    private var allProjects = listOf<ProjectData>()

    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        token = sharedPref.getString("authToken", "") ?: ""
        val companyName = sharedPref.getString("companyName", "Client")
        binding.clientNameText.text = "$companyName's All Projects"

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        setupRecyclerView()
        setupTabLayout()
        setupSwipeRefresh()

        // Load locally saved projects first (offline support)
        loadLocalProjects()

        // Then fetch latest from backend and override
        loadProjects()

        binding.chatNow.setOnClickListener {
            findNavController().navigate(R.id.chatFragment)
        }
    }

    /** Setup RecyclerView */
    private fun setupRecyclerView() {
        adapter = ProjectsAdapter(emptyList()) { project ->
            findNavController().navigate(ProjectsFragmentDirections.actionProjectsFragmentToClientProjectDetailsFragment(project._id))
            Toast.makeText(requireContext(), "Project: ${project.title}", Toast.LENGTH_SHORT).show()
        }
        binding.projectsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.projectsRecyclerView.adapter = adapter
    }

    /** Setup TabLayout filter */
    private fun setupTabLayout() {
        binding.projectTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterProjectsByStatus(tab?.text.toString())
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    /** Setup swipe-to-refresh */
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            // Reset to first tab on refresh
            val firstTab = binding.projectTabLayout.getTabAt(0)
            firstTab?.select()
            loadProjects()
        }
    }

    /** Load projects from backend */
    private fun loadProjects() {
        showLoading(true)
        binding.swipeRefresh.isRefreshing = true
        apiService.getClientProjects("Bearer $token").enqueue(object : Callback<ProjectsResponse> {
            override fun onResponse(call: Call<ProjectsResponse>, response: Response<ProjectsResponse>) {
                if (!isAdded) return
                showLoading(false)
                binding.swipeRefresh.isRefreshing = false

                if (response.isSuccessful) {
                    allProjects = response.body()?.data ?: emptyList()

                    if (allProjects.isEmpty()) {
                        showEmptyState(true)
                    } else {
                        showEmptyState(false)
                        val firstTab = binding.projectTabLayout.getTabAt(0)
                        filterProjectsByStatus(firstTab?.text.toString())
                    }

                    // Save updated projects locally
                    saveLocalProjects(allProjects)
                } else {
                    Toast.makeText(requireContext(), "Failed to load projects!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProjectsResponse>, t: Throwable) {
                if (!isAdded) return
                showLoading(false)
                Log.d("PROJECTS",t.message.toString())
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(requireContext(), "Network error!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /** Filter projects by status (tab name) */
    private fun filterProjectsByStatus(status: String) {
        val filtered = allProjects.filter { it.status.equals(status, ignoreCase = true) }
        adapter.updateData(filtered)

        if (filtered.isEmpty()) {
            showEmptyState(true, "No $status projects found.")
        } else {
            showEmptyState(false)
        }
    }

    /** Save list of projects locally */
    private fun saveLocalProjects(projectList: List<ProjectData>) {
        val sharedPrefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val json = gson.toJson(projectList)
        editor.putString("cachedProjects", json)
        editor.apply()
    }

    /** Load locally saved projects */
    @SuppressLint("NotifyDataSetChanged")
    private fun loadLocalProjects() {
        showLoading(true)
        val sharedPrefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val json = sharedPrefs.getString("cachedProjects", null)
        if (!json.isNullOrEmpty()) {
            showLoading(false)
            val type = object : TypeToken<List<ProjectData>>() {}.type
            val savedProjects: List<ProjectData> = gson.fromJson(json, type)
            allProjects = savedProjects
            adapter.updateData(savedProjects)

            val firstTab = binding.projectTabLayout.getTabAt(0)
            filterProjectsByStatus(firstTab?.text.toString())
        }
        showLoading(false)
    }

    /** Show or hide loading overlay */
    private fun showLoading(isLoading: Boolean) {
        binding.chatNow.isEnabled = !isLoading
        // Loop through all tabs dynamically (in case more are added later)
        for (i in 0 until binding.projectTabLayout.tabCount) {
            val tabView = binding.projectTabLayout.getTabAt(i)?.view
            tabView?.isClickable = !isLoading
            tabView?.alpha = if (isLoading) 0.5f else 1f   // optional fade effect
        }
    }
    /** Show or hide empty message */
    private fun showEmptyState(show: Boolean, message: String = "You don’t have any projects yet. Please add one.") {
        binding.emptyText.visibility = if (show) View.VISIBLE else View.GONE
        binding.emptyText.text = message
    }

    /** Smooth fade-in animation */
    private fun View.fadeIn(duration: Long = 300) {
        this.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(duration).start()
        }
    }

    /** Smooth fade-out animation */
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
