package com.example.acefx_app.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acefx_app.R
import com.example.acefx_app.data.ProjectItem
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
                .actionClientProjectsFragmentToClientProjectDetailsFragment(projectTitle = project.title)
            findNavController().navigate(action)
        }
        binding.projectsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.projectsRecyclerView.adapter = adapter

        // Load projects from backend
        loadProjects()

        // Tab listener
        binding.projectTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
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
            .enqueue(object : Callback<List<Map<String, Any>>> {
                override fun onResponse(
                    call: Call<List<Map<String, Any>>>,
                    response: Response<List<Map<String, Any>>>
                ) {
                    if (!isAdded) return
                    showLoading(false)

                    if (response.isSuccessful) {
                        allProjects = response.body()?.map {
                            ProjectItem(
                                id = it["_id"].toString(),
                                title = it["title"].toString(),
                                status = it["status"].toString()
                            )
                        } ?: emptyList()

                        if (allProjects.isEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                "You don’t have any projects.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        // Display projects for selected tab by default
                        val selectedTab =
                            binding.projectTabLayout.getTabAt(binding.projectTabLayout.selectedTabPosition)
                        filterProjectsByStatus(selectedTab?.text.toString())
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to load projects!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<Map<String, Any>>>, t: Throwable) {
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
