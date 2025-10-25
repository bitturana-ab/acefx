package com.example.acefx_app.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acefx_app.databinding.FragmentClientProjectsBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import com.example.acefx_app.ui.adapter.ProjectItem
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
    private var allProjects = listOf<ProjectItem>()
    private lateinit var token: String

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
        adapter = ProjectsAdapter(emptyList())
        binding.projectsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.projectsRecyclerView.adapter = adapter

        // Load projects from backend
        loadProjects()

        // Tabs listener
        binding.projectTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val status = tab?.text.toString()
                filterProjectsByStatus(status)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Add Project button
        binding.addProjectBtn.setOnClickListener {
            Toast.makeText(requireContext(), "Open Add Project screen", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to AddProjectFragment
        }
    }

    private fun loadProjects() {
        apiService.getClientProjects("Bearer $token").enqueue(object : Callback<List<Map<String, Any>>> {
            override fun onResponse(
                call: Call<List<Map<String, Any>>>,
                response: Response<List<Map<String, Any>>>
            ) {
                if (response.isSuccessful) {
                    allProjects = response.body()?.map {
                        ProjectItem(
                            title = it["title"].toString(),
                            status = it["status"].toString()
                        )
                    } ?: emptyList()

                    // Show first tab by default
                    val selectedTab =
                        binding.projectTabLayout.getTabAt(binding.projectTabLayout.selectedTabPosition)
                    val status = selectedTab?.text.toString()
                    filterProjectsByStatus(status)
                } else {
                    Toast.makeText(requireContext(), "Failed to load projects!", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<List<Map<String, Any>>>, t: Throwable) {
                Toast.makeText(requireContext(), "Network error!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterProjectsByStatus(status: String) {
        val filtered = allProjects.filter { it.status.equals(status, ignoreCase = true) }
        adapter.updateData(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
